package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.DoubtDao
import com.example.data.local.DoubtEntity
import com.example.data.remote.Content
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.GenerationConfig
import com.example.data.remote.Part
import com.example.data.remote.ResponseSchema
import com.example.data.remote.ResponseSchemaProperty
import com.example.data.remote.RetrofitClient
import com.example.data.remote.TeacherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DoubtRepository(private val doubtDao: DoubtDao) {

    val allDoubts: Flow<List<DoubtEntity>> = doubtDao.getAllDoubts()
    val bookmarkedDoubts: Flow<List<DoubtEntity>> = doubtDao.getBookmarkedDoubts()

    suspend fun saveDoubt(doubt: DoubtEntity): Long = withContext(Dispatchers.IO) {
        doubtDao.insertDoubt(doubt)
    }

    suspend fun deleteDoubtById(id: Int) = withContext(Dispatchers.IO) {
        doubtDao.deleteDoubtById(id)
    }

    suspend fun toggleBookmark(doubt: DoubtEntity) = withContext(Dispatchers.IO) {
        doubtDao.updateDoubt(doubt.copy(isBookmarked = !doubt.isBookmarked))
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        doubtDao.clearAll()
    }

    suspend fun askGuruJi(
        studentClass: String,
        board: String,
        subject: String,
        question: String,
        isHinglish: Boolean
    ): TeacherResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing or default! Please set it in the Secrets Panel in AI Studio Dashboard.")
        }

        // 1. Construct prompt
        val languagePreference = if (isHinglish) {
            "Hinglish (a mix of Hindi and English. Write Hindi words using the English/Latin script, e.g., 'Aapko yeh dhyan rakhna hai' or 'Yeh cell structure bohot important hai'). Use a naturally spoken teacher-like tone of an Indian school teacher conversing with their student."
        } else {
            "Simple, clean English. Use a warm, encouraging teacher-like tone of an experienced Indian CBSE/ICSE school teacher."
        }

        val prompt = """
            You are a highly experienced Indian school teacher with 15+ years of teaching experience. Your goal is to help Indian students excel in their board and school exams by making learning simple, intuitive, and fun.
            
            STUDENT DETAILS:
            - Class/Grade: $studentClass
            - Board or Curriculum: $board
            - Subject: $subject
            
            QUESTION FROM THE STUDENT:
            $question
            
            LANGUAGE OF EXPLANATION PREFERRED:
            $languagePreference
            
            Follow these pedagogical guidelines strictly:
            1. Keep your tone encouraging and warm (like a caring Guru/Teacher, using warm Indian educational greetings or words like "Beta" or "Dear Student" naturally).
            2. Break down the concept step-by-step so a student of $studentClass can easily score full marks.
            3. Highlight key high-scoring terms/keywords that the examiner looks for in CBSE, ICSE, or State Board exams.
            4. State frequent mistakes or misconceptions that students make during this topic in exams to warn them.
            5. If the question involves Math or Science, clearly show calculations, equations, formulas, and steps.
            6. If the question is from Arts, Humanities, History, Geography, Civics, or English, provide the final answer structured exactly in the premium format required for board exams (bullet points, clear headings, definitions).
            7. Avoid long paragraphs and make it highly scannable and readable.
            
            Return your response in structured JSON format matching the schema properties:
            - `conceptExplanation`: Explanation of the fundamental core concept.
            - `stepByStepAnswer`: Detailed step-by-step solved response, with clear formula application or bulleted context.
            - `finalAnswer`: Concise absolute final answer or concluding summary of the solution (useful for quick reference / review).
            - `examTips`: Highlighted key vocabulary, score-boosting terms, and diagram instructions (if applicable) that secure maximum points.
            - `commonMistakes`: Typical mistakes students commit in this exact problem or topic, and how to avoid them.
        """.trimIndent()

        // 2. Define schema properties conforming to the response structure
        val schemaProperties = mapOf(
            "conceptExplanation" to ResponseSchemaProperty(type = "STRING", description = "Explains the underlying scientific/mathematical/conceptual base in a student-friendly way."),
            "stepByStepAnswer" to ResponseSchemaProperty(type = "STRING", description = "The core derivation, step-by-step math solver steps, or descriptive point-to-point response layout as expected in a board exam sheet."),
            "finalAnswer" to ResponseSchemaProperty(type = "STRING", description = "The direct short answer, final calculation value, or high-level conclusion."),
            "examTips" to ResponseSchemaProperty(type = "STRING", description = "Keywords, exam writing styles, key terms checker list to get solid marks from external examiners."),
            "commonMistakes" to ResponseSchemaProperty(type = "STRING", description = "A checklist of silly mistakes, wrong assumptions, sign errors, or formatting blunders to steer clear of.")
        )

        val responseSchema = ResponseSchema(
            type = "OBJECT",
            properties = schemaProperties,
            required = listOf("conceptExplanation", "stepByStepAnswer", "finalAnswer", "examTips", "commonMistakes"),
            description = "Structured Indian school tutor exam response format."
        )

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = responseSchema,
                temperature = 0.4f
            ),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "You are a warm, wise, 15+ years experienced Indian CBSE, ICSE, and State Board secondary/senior school teacher tutor assistant. You always output valid, clean JSON according to the specified format.")
                )
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.flatMap { it.content?.parts ?: emptyList() }
                ?.firstOrNull()?.text
                ?: throw IllegalStateException("Nothing returned from GuruJi's servers. Please try again.")

            Log.d("DoubtRepository", "Raw JSON from Gemini: $jsonText")

            val teacherResponse = RetrofitClient.teacherAdapter.fromJson(jsonText)
                ?: throw IllegalStateException("Failed to parse GuruJi's final structured response.")

            // 3. Save to database for auto-history tracking!
            val newDoubt = DoubtEntity(
                studentClass = studentClass,
                board = board,
                subject = subject,
                question = question,
                isHinglish = isHinglish,
                conceptExplanation = teacherResponse.conceptExplanation,
                stepByStepAnswer = teacherResponse.stepByStepAnswer,
                finalAnswer = teacherResponse.finalAnswer,
                examTips = teacherResponse.examTips,
                commonMistakes = teacherResponse.commonMistakes,
                isBookmarked = false
            )
            saveDoubt(newDoubt)

            teacherResponse
        } catch (e: Exception) {
            Log.e("DoubtRepository", "API Error: ${e.message}", e)
            throw e
        }
    }
}
