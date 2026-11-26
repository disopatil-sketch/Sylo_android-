package com.example.data.repository

import android.util.Log
import com.example.data.model.AiModerationResult
import com.example.data.model.AiTranslationResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "SyloAiRepo"

class SyloAiRepository {

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "detectedLanguage" to Schema.string(description = "Detected source language name"),
            "translatedText" to Schema.string(description = "Natural translation into the target language script"),
            "targetLanguage" to Schema.string(description = "Target language name")
        )
    )

    private val moderationSchema = Schema.obj(
        properties = mapOf(
            "safe" to Schema.boolean(description = "True if content follows community guidelines"),
            "riskLevel" to Schema.enumeration(
                values = listOf("LOW", "MEDIUM", "HIGH"),
                description = "Moderation risk tier"
            ),
            "summary" to Schema.string(description = "Concise one-sentence moderation summary")
        )
    )

    private val translationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    private val moderationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = moderationSchema
            }
        )
    }

    suspend fun translateAndDetect(
        text: String,
        targetLanguageName: String
    ): Result<AiTranslationResult> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Nothing to translate."))
        }
        return try {
            val prompt = buildString {
                append("Detect the language of the following South Asian social media text and ")
                append("translate it naturally into $targetLanguageName using its proper Unicode script. ")
                append("Preserve hashtags and cultural tone.\n\nText:\n$text")
            }
            val response = withTimeout(60_000L) {
                translationModel.generateContent(prompt)
            }
            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("AI translation returned empty output."))
            val obj = JSONObject(rawJson)
            Result.success(
                AiTranslationResult(
                    detectedLanguage = obj.optString("detectedLanguage", "Unknown"),
                    translatedText = obj.optString("translatedText", text),
                    targetLanguage = obj.optString("targetLanguage", targetLanguageName)
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Translation timed out", e)
            Result.failure(IllegalStateException("Translation timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI translation failed", e)
            Result.failure(e)
        }
    }

    suspend fun moderateContent(
        title: String,
        body: String
    ): Result<AiModerationResult> {
        return try {
            val prompt = buildString {
                append("Review the following social media post for hate speech, harassment, spam, ")
                append("explicit harm, or copyright scam red flags. Keep summary concise.\n\n")
                append("Title: $title\nBody: $body")
            }
            val response = withTimeout(60_000L) {
                moderationModel.generateContent(prompt)
            }
            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("AI moderation returned empty output."))
            val obj = JSONObject(rawJson)
            Result.success(
                AiModerationResult(
                    safe = obj.optBoolean("safe", true),
                    riskLevel = obj.optString("riskLevel", "LOW"),
                    summary = obj.optString("summary", "Content appears compliant with community guidelines.")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Moderation timed out", e)
            Result.failure(IllegalStateException("Safety check timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI moderation failed", e)
            Result.failure(e)
        }
    }
}
