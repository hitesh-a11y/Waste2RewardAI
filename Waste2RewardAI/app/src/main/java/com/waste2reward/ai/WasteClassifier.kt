package com.waste2reward.ai

import android.content.Context
import android.graphics.Bitmap
import java.io.File

interface WasteClassifier {
    suspend fun classify(bitmap: Bitmap): List<WasteItem>
}

/**
 * Local demo classifier. It is deliberately deterministic and does not claim
 * real AI accuracy. Replace this implementation with TFLiteWasteClassifier
 * when a trained model is placed in app/src/main/assets/waste_model.tflite.
 */
class DemoWasteClassifier(private val context: Context) : WasteClassifier {
    override suspend fun classify(bitmap: Bitmap): List<WasteItem> {
        // The hackathon demo uses a known mixed-waste scenario so the full
        // scan/rescan story can be demonstrated without a trained model.
        return listOf(
            WasteItem("Plastic bottle", WasteCategory.PLASTIC, .947f, 1),
            WasteItem("Newspaper", WasteCategory.PAPER, .923f, 1),
            WasteItem("Banana peel", WasteCategory.ORGANIC, .901f, 1),
            WasteItem("Aluminium can", WasteCategory.METAL, .938f, 1),
            WasteItem("Battery", WasteCategory.EWASTE, .884f, 1)
        )
    }
}

class TFLiteWasteClassifier(private val context: Context) : WasteClassifier {
    override suspend fun classify(bitmap: Bitmap): List<WasteItem> {
        val model = File(context.filesDir, "waste_model.tflite")
        if (!model.exists()) throw IllegalStateException(
            "Waste model is not installed. Add waste_model.tflite and connect its input/output labels."
        )
        throw NotImplementedError("Model adapter interface is ready; connect your trained model's tensor contract here.")
    }
}
