package com.cpnsjuara.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private fun requestJson(
        url: String,
        method: String = "GET",
        authToken: String? = null,
        body: JSONObject? = null
    ): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 12000
        connection.readTimeout = 20000
        connection.setRequestProperty("Accept", "application/json")
        if (!authToken.isNullOrBlank()) {
            connection.setRequestProperty("Authorization", "Bearer $authToken")
        }
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val response = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code: $response")
            }
            JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }

    fun fetchPackages(baseUrl: String): List<PackageInfo> {
        val array = requestJson("$baseUrl/api/packages/").getJSONArray("packages")
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    PackageInfo(
                        id = item.getInt("id"),
                        name = item.getString("name"),
                        description = item.optString("description"),
                        version = item.getInt("version"),
                        questionCount = item.getInt("question_count"),
                        isPremium = item.optBoolean("is_premium", false)
                    )
                )
            }
        }
    }

    fun fetchPackage(
        baseUrl: String,
        packageId: Int,
        authToken: String? = null
    ): Pair<Int, List<QuizQuestion>> {
        val json = requestJson(
            "$baseUrl/api/packages/$packageId/",
            authToken = authToken
        )
        val array = json.getJSONArray("questions")

        val questions = buildList {
            for (i in 0 until array.length()) {
                val q = array.getJSONObject(i)
                val optionsArray = q.getJSONArray("options")
                val options = buildList {
                    for (k in 0 until optionsArray.length()) {
                        val o = optionsArray.getJSONObject(k)
                        add(
                            QuizOption(
                                code = o.getString("code"),
                                contentHtml = o.optString("rendered_html", o.optString("content_html", "")),
                                score = o.getInt("score"),
                                isCorrect = o.getBoolean("is_correct")
                            )
                        )
                    }
                }

                val blueprint = q.optJSONObject("blueprint")
                val stimulus = q.optJSONObject("stimulus")

                add(
                    QuizQuestion(
                        id = q.getInt("id"),
                        code = q.optString("code"),
                        category = q.getString("category"),
                        subcategory = q.optString("subcategory"),
                        blueprintCode = blueprint?.optString("code") ?: "",
                        material = blueprint?.optString("material") ?: "",
                        submaterial = blueprint?.optString("submaterial") ?: "",
                        indicator = blueprint?.optString("indicator") ?: "",
                        stimulusType = stimulus?.optString("type", "NONE") ?: "NONE",
                        stimulusText = stimulus?.optString("rendered_html", stimulus.optString("text", "")) ?: "",
                        stimulusImageUrl = stimulus?.optString("image_url") ?: "",
                        stimulusImageLocalPath = "",
                        stimulusSource = stimulus?.optString("source") ?: "",
                        prompt = q.optString("prompt_rendered_html", q.optString("prompt", "")),
                        explanation = q.optString("explanation_rendered_html", q.optString("explanation", "")),
                        scoringMode = q.getString("scoring_mode"),
                        options = options
                    )
                )
            }
        }
        return json.getInt("version") to questions
    }

    fun syncFirebaseUser(baseUrl: String, idToken: String) =
        requestJson("$baseUrl/api/auth/firebase/", "POST", idToken, JSONObject())

    fun billingStatus(baseUrl: String, idToken: String) =
        requestJson("$baseUrl/api/billing/status/", "GET", idToken)

    fun verifySubscription(
        baseUrl: String,
        idToken: String,
        productId: String,
        purchaseToken: String
    ) = requestJson(
        "$baseUrl/api/billing/verify/",
        "POST",
        idToken,
        JSONObject()
            .put("product_id", productId)
            .put("purchase_token", purchaseToken)
    )

    fun syncProgress(baseUrl: String, idToken: String, payload: JSONArray) =
        requestJson(
            "$baseUrl/api/sync/progress/",
            "POST",
            idToken,
            JSONObject().put("results", payload)
        )

    fun ranking(baseUrl: String, idToken: String) =
        requestJson("$baseUrl/api/ranking/", "GET", idToken)
}
