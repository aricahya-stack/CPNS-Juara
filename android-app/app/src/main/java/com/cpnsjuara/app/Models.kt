package com.cpnsjuara.app

data class PackageInfo(
    val id: Int,
    val name: String,
    val description: String,
    val version: Int,
    val questionCount: Int,
    val isPremium: Boolean = false
)

data class QuizOption(
    val code: String,
    val contentHtml: String,
    val score: Int,
    val isCorrect: Boolean
)

data class QuizQuestion(
    val id: Int,
    val code: String,
    val category: String,
    val subcategory: String,
    val blueprintCode: String,
    val material: String,
    val submaterial: String,
    val indicator: String,
    val stimulusType: String,
    val stimulusText: String,
    val stimulusImageUrl: String,
    val stimulusImageLocalPath: String,
    val stimulusSource: String,
    val prompt: String,
    val explanation: String,
    val scoringMode: String,
    val options: List<QuizOption>
) {
    fun hasStimulus(): Boolean =
        stimulusType != "NONE" &&
            (stimulusText.isNotBlank() ||
                stimulusImageUrl.isNotBlank() ||
                stimulusImageLocalPath.isNotBlank())

    fun hasVisualStimulus(): Boolean =
        stimulusType in setOf("IMAGE", "GRAPHIC", "MIXED") &&
            (stimulusImageUrl.isNotBlank() || stimulusImageLocalPath.isNotBlank())
}
