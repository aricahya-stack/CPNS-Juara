package com.cpnsjuara.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "cpnsjuara.db", null, 5) {

    private val appContext = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE questions(
                server_id INTEGER PRIMARY KEY,
                package_id INTEGER NOT NULL,
                code TEXT,
                category TEXT NOT NULL,
                subcategory TEXT,
                blueprint_code TEXT,
                material TEXT,
                submaterial TEXT,
                indicator TEXT,
                stimulus_type TEXT NOT NULL DEFAULT 'NONE',
                stimulus_text TEXT,
                stimulus_image_url TEXT,
                stimulus_image_local_path TEXT,
                stimulus_source TEXT,
                prompt TEXT NOT NULL,
                explanation TEXT,
                scoring_mode TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE TABLE options(id INTEGER PRIMARY KEY AUTOINCREMENT, question_id INTEGER NOT NULL, code TEXT NOT NULL, text TEXT, content_html TEXT NOT NULL, score INTEGER NOT NULL DEFAULT 0, is_correct INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE packages(package_id INTEGER PRIMARY KEY, version INTEGER NOT NULL, downloaded_at INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE results(id INTEGER PRIMARY KEY AUTOINCREMENT, question_id INTEGER NOT NULL, category TEXT NOT NULL, selected_code TEXT NOT NULL, score INTEGER NOT NULL, is_correct INTEGER NOT NULL, answered_at INTEGER NOT NULL, synced INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE bookmarks(question_id INTEGER PRIMARY KEY, created_at INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL(
                    "ALTER TABLE results ADD COLUMN synced INTEGER NOT NULL DEFAULT 0"
                )
            } catch (_: Exception) {
            }
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS bookmarks(question_id INTEGER PRIMARY KEY, created_at INTEGER NOT NULL)"
            )
        }

        if (oldVersion < 3) {
            listOf(
                "code TEXT",
                "blueprint_code TEXT",
                "material TEXT",
                "submaterial TEXT",
                "indicator TEXT",
                "stimulus_title TEXT",
                "stimulus_text TEXT"
            ).forEach { column ->
                try {
                    db.execSQL("ALTER TABLE questions ADD COLUMN $column")
                } catch (_: Exception) {
                }
            }
        }

        if (oldVersion < 4) {
            listOf(
                "stimulus_type TEXT NOT NULL DEFAULT 'NONE'",
                "stimulus_image_url TEXT",
                "stimulus_image_local_path TEXT",
                "stimulus_source TEXT"
            ).forEach { column ->
                try {
                    db.execSQL("ALTER TABLE questions ADD COLUMN $column")
                } catch (_: Exception) {
                }
            }

            // Data v3 yang sudah memiliki stimulus_text tetap dianggap stimulus teks.
            try {
                db.execSQL(
                    "UPDATE questions SET stimulus_type='TEXT' WHERE COALESCE(stimulus_text,'') != '' AND (stimulus_type IS NULL OR stimulus_type='NONE')"
                )
            } catch (_: Exception) {
            }
        }

        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE options ADD COLUMN content_html TEXT")
                db.execSQL("UPDATE options SET content_html=text WHERE content_html IS NULL OR content_html='' ")
            } catch (_: Exception) {
            }
        }
    }

    fun replacePackage(
        packageId: Int,
        version: Int,
        questions: List<QuizQuestion>
    ) {
        val prepared = questions.map { q ->
            val localPath = if (q.hasVisualStimulus()) downloadStimulusForOffline(q.stimulusImageUrl) else ""
            q.copy(
                stimulusImageLocalPath = localPath,
                stimulusText = cacheRichImagesForOffline(q.stimulusText),
                prompt = cacheRichImagesForOffline(q.prompt),
                explanation = cacheRichImagesForOffline(q.explanation),
                options = q.options.map { it.copy(contentHtml = cacheRichImagesForOffline(it.contentHtml)) }
            )
        }

        val db = writableDatabase
        db.beginTransaction()
        try {
            val ids = mutableListOf<Int>()
            db.rawQuery(
                "SELECT server_id FROM questions WHERE package_id=?",
                arrayOf(packageId.toString())
            ).use {
                while (it.moveToNext()) ids.add(it.getInt(0))
            }
            ids.forEach {
                db.delete("options", "question_id=?", arrayOf(it.toString()))
            }
            db.delete("questions", "package_id=?", arrayOf(packageId.toString()))

            prepared.forEach { q ->
                db.insertOrThrow(
                    "questions",
                    null,
                    ContentValues().apply {
                        put("server_id", q.id)
                        put("package_id", packageId)
                        put("code", q.code)
                        put("category", q.category)
                        put("subcategory", q.subcategory)
                        put("blueprint_code", q.blueprintCode)
                        put("material", q.material)
                        put("submaterial", q.submaterial)
                        put("indicator", q.indicator)
                        put("stimulus_type", q.stimulusType)
                        put("stimulus_text", q.stimulusText)
                        put("stimulus_image_url", q.stimulusImageUrl)
                        put("stimulus_image_local_path", q.stimulusImageLocalPath)
                        put("stimulus_source", q.stimulusSource)
                        put("prompt", q.prompt)
                        put("explanation", q.explanation)
                        put("scoring_mode", q.scoringMode)
                    }
                )

                q.options.forEach { o ->
                    db.insertOrThrow(
                        "options",
                        null,
                        ContentValues().apply {
                            put("question_id", q.id)
                            put("code", o.code)
                            put("content_html", o.contentHtml)
                            put("score", o.score)
                            put("is_correct", if (o.isCorrect) 1 else 0)
                        }
                    )
                }
            }

            db.insertWithOnConflict(
                "packages",
                null,
                ContentValues().apply {
                    put("package_id", packageId)
                    put("version", version)
                    put("downloaded_at", System.currentTimeMillis())
                },
                SQLiteDatabase.CONFLICT_REPLACE
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun downloadStimulusForOffline(remoteUrl: String): String {
        if (remoteUrl.isBlank()) return ""

        val directory = File(appContext.filesDir, "stimuli")
        if (!directory.exists()) directory.mkdirs()

        val key = sha256(remoteUrl)
        val target = File(directory, "$key.bin")
        if (target.exists() && target.length() > 0) {
            return target.absolutePath
        }

        var connection: HttpURLConnection? = null
        return try {
            connection = URL(remoteUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 12000
            connection.readTimeout = 20000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "CPNS-JUARA-Android")
            connection.connect()

            if (connection.responseCode !in 200..299) return ""
            val contentLength = connection.contentLengthLong
            if (contentLength > MAX_STIMULUS_BYTES) return ""

            var total = 0L
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        total += read
                        if (total > MAX_STIMULUS_BYTES) {
                            throw IllegalStateException("Stimulus visual terlalu besar")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            if (target.length() > 0) target.absolutePath else ""
        } catch (_: Exception) {
            target.delete()
            ""
        } finally {
            connection?.disconnect()
        }
    }


    private fun cacheRichImagesForOffline(html: String): String {
        if (html.isBlank()) return ""
        val regex = Regex("(<img\\b[^>]*?\\bsrc\\s*=\\s*[\\\"'])([^\\\"']+)([\\\"'])", RegexOption.IGNORE_CASE)
        return regex.replace(html) { match ->
            val url = match.groupValues[2]
            if (url.startsWith("data:") || url.startsWith("file:")) return@replace match.value
            val local = downloadRichMedia(url)
            if (local.isBlank()) match.value
            else match.groupValues[1] + File(local).toURI().toString() + match.groupValues[3]
        }
    }

    private fun downloadRichMedia(remoteUrl: String): String {
        if (remoteUrl.isBlank()) return ""
        val directory = File(appContext.filesDir, "rich_media")
        if (!directory.exists()) directory.mkdirs()
        val key = sha256(remoteUrl)
        val target = File(directory, "$key.bin")
        if (target.exists() && target.length() > 0) return target.absolutePath
        var connection: HttpURLConnection? = null
        return try {
            connection = URL(remoteUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 12000
            connection.readTimeout = 20000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "CPNS-JUARA-Android")
            connection.connect()
            if (connection.responseCode !in 200..299) return ""
            if (connection.contentLengthLong > MAX_RICH_MEDIA_BYTES) return ""
            var total = 0L
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        total += read
                        if (total > MAX_RICH_MEDIA_BYTES) throw IllegalStateException("Rich media terlalu besar")
                        output.write(buffer, 0, read)
                    }
                }
            }
            if (target.length() > 0) target.absolutePath else ""
        } catch (_: Exception) {
            target.delete(); ""
        } finally { connection?.disconnect() }
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun countQuestions(): Int = readableDatabase.rawQuery(
        "SELECT COUNT(*) FROM questions",
        null
    ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

    fun getQuestions(category: String, limit: Int = 10) =
        queryQuestions(
            "WHERE category=? ORDER BY RANDOM() LIMIT ?",
            arrayOf(category, limit.toString())
        )

    fun getMixedQuestions(limit: Int = 30) =
        queryQuestions("ORDER BY RANDOM() LIMIT ?", arrayOf(limit.toString()))

    fun getBookmarkedQuestions(limit: Int = 50) =
        queryQuestions(
            "WHERE server_id IN (SELECT question_id FROM bookmarks) ORDER BY RANDOM() LIMIT ?",
            arrayOf(limit.toString())
        )

    fun getWrongQuestions(limit: Int = 20) =
        queryQuestions(
            "WHERE scoring_mode != 'weighted' AND server_id IN (SELECT DISTINCT question_id FROM results WHERE is_correct=0) ORDER BY RANDOM() LIMIT ?",
            arrayOf(limit.toString())
        )

    private fun queryQuestions(
        suffix: String,
        args: Array<String>
    ): List<QuizQuestion> {
        val out = mutableListOf<QuizQuestion>()
        val db = readableDatabase
        db.rawQuery(
            """
            SELECT server_id,code,category,subcategory,blueprint_code,
                   material,submaterial,indicator,
                   stimulus_type,stimulus_text,stimulus_image_url,
                   stimulus_image_local_path,stimulus_source,
                   prompt,explanation,scoring_mode
            FROM questions $suffix
            """.trimIndent(),
            args
        ).use { c ->
            while (c.moveToNext()) {
                val id = c.getInt(0)
                out.add(
                    QuizQuestion(
                        id = id,
                        code = c.getString(1) ?: "",
                        category = c.getString(2),
                        subcategory = c.getString(3) ?: "",
                        blueprintCode = c.getString(4) ?: "",
                        material = c.getString(5) ?: "",
                        submaterial = c.getString(6) ?: "",
                        indicator = c.getString(7) ?: "",
                        stimulusType = c.getString(8) ?: "NONE",
                        stimulusText = c.getString(9) ?: "",
                        stimulusImageUrl = c.getString(10) ?: "",
                        stimulusImageLocalPath = c.getString(11) ?: "",
                        stimulusSource = c.getString(12) ?: "",
                        prompt = c.getString(13),
                        explanation = c.getString(14) ?: "",
                        scoringMode = c.getString(15),
                        options = getOptions(db, id)
                    )
                )
            }
        }
        return out
    }

    private fun getOptions(db: SQLiteDatabase, qid: Int): List<QuizOption> {
        val out = mutableListOf<QuizOption>()
        db.rawQuery(
            "SELECT code,COALESCE(NULLIF(content_html,''),text,''),score,is_correct FROM options WHERE question_id=? ORDER BY code",
            arrayOf(qid.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out.add(
                    QuizOption(
                        code = c.getString(0),
                        contentHtml = c.getString(1),
                        score = c.getInt(2),
                        isCorrect = c.getInt(3) == 1
                    )
                )
            }
        }
        return out
    }

    fun saveResult(q: QuizQuestion, o: QuizOption) {
        writableDatabase.insert(
            "results",
            null,
            ContentValues().apply {
                put("question_id", q.id)
                put("category", q.category)
                put("selected_code", o.code)
                put("score", o.score)
                put("is_correct", if (o.isCorrect) 1 else 0)
                put("answered_at", System.currentTimeMillis())
                put("synced", 0)
            }
        )
    }

    fun pendingResultsJson(): JSONArray {
        val array = JSONArray()
        readableDatabase.rawQuery(
            "SELECT id,question_id,category,selected_code,score,is_correct,answered_at FROM results WHERE synced=0 ORDER BY id",
            null
        ).use { c ->
            while (c.moveToNext()) {
                array.put(
                    JSONObject()
                        .put("local_id", c.getInt(0))
                        .put("question_id", c.getInt(1))
                        .put("category", c.getString(2))
                        .put("selected_code", c.getString(3))
                        .put("score", c.getInt(4))
                        .put("is_correct", c.getInt(5) == 1)
                        .put("answered_at_ms", c.getLong(6))
                )
            }
        }
        return array
    }

    fun markResultsSynced(ids: List<Int>) {
        ids.forEach {
            writableDatabase.execSQL(
                "UPDATE results SET synced=1 WHERE id=?",
                arrayOf(it)
            )
        }
    }

    fun toggleBookmark(qid: Int): Boolean {
        if (isBookmarked(qid)) {
            writableDatabase.delete(
                "bookmarks",
                "question_id=?",
                arrayOf(qid.toString())
            )
            return false
        }
        writableDatabase.insert(
            "bookmarks",
            null,
            ContentValues().apply {
                put("question_id", qid)
                put("created_at", System.currentTimeMillis())
            }
        )
        return true
    }

    fun isBookmarked(qid: Int): Boolean = readableDatabase.rawQuery(
        "SELECT 1 FROM bookmarks WHERE question_id=? LIMIT 1",
        arrayOf(qid.toString())
    ).use { it.moveToFirst() }

    fun stats(): Triple<Int, Int, Int> = readableDatabase.rawQuery(
        "SELECT COUNT(*),COALESCE(SUM(is_correct),0),COALESCE(SUM(score),0) FROM results",
        null
    ).use {
        if (it.moveToFirst()) Triple(it.getInt(0), it.getInt(1), it.getInt(2))
        else Triple(0, 0, 0)
    }

    fun singleAnswerStats(): Pair<Int, Int> = readableDatabase.rawQuery(
        "SELECT COUNT(*),COALESCE(SUM(r.is_correct),0) FROM results r JOIN questions q ON q.server_id=r.question_id WHERE q.scoring_mode='single'",
        null
    ).use {
        if (it.moveToFirst()) Pair(it.getInt(0), it.getInt(1))
        else Pair(0, 0)
    }

    companion object {
        private const val MAX_STIMULUS_BYTES = 8L * 1024L * 1024L
        private const val MAX_RICH_MEDIA_BYTES = 8L * 1024L * 1024L
    }
}
