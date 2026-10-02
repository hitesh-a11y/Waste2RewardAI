package com.waste2reward.ai

enum class ScanMode { SINGLE, MIXED, RESCAN }

enum class WasteCategory(val label: String, val disposal: String, val points: Int) {
    PLASTIC("Plastic", "Recycle", 10),
    PAPER("Paper", "Recycle", 10),
    CARDBOARD("Cardboard", "Recycle", 10),
    GLASS("Glass", "Recycle carefully", 15),
    METAL("Metal", "Recycle", 15),
    ORGANIC("Organic", "Compost / organic waste", 10),
    EWASTE("E-waste", "Use an authorized e-waste collection point", 25),
    HAZARDOUS("Hazardous", "Use an authorized hazardous-waste facility", 25),
    TEXTILE("Textile", "Reuse / textile recycling", 10),
    OTHER("Other", "Check local waste-authority instructions", 0)
}

data class WasteItem(val name: String, val category: WasteCategory, val confidence: Float, val quantity: Int = 1)
data class ScanResult(val items: List<WasteItem>, val score: Int, val properlySeparated: Boolean, val points: Int)
data class Reward(val name: String, val points: Int, val icon: String)
