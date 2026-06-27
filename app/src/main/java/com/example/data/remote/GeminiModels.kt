package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String,
    val properties: Map<String, ResponseSchemaProperty>? = null,
    val required: List<String>? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchemaProperty(
    val type: String,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class TeacherResponse(
    val conceptExplanation: String,
    val stepByStepAnswer: String,
    val finalAnswer: String,
    val examTips: String,
    val commonMistakes: String
)
