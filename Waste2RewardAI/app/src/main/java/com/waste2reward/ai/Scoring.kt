package com.waste2reward.ai

object SegregationEngine {
    fun score(items: List<WasteItem>, rescan: Boolean): Int {
        if (items.isEmpty()) return 0
        if (rescan) return 100
        val categories = items.map { it.category }.distinct()
        val mixedPenalty = (categories.size - 1) * 10
        val hazardousMixed = if (items.any { it.category == WasteCategory.EWASTE || it.category == WasteCategory.HAZARDOUS }) 10 else 0
        return (100 - mixedPenalty - hazardousMixed).coerceIn(0, 100)
    }

    fun points(items: List<WasteItem>, verified: Boolean): Int {
        if (!verified) return 0
        return items.sumOf { it.category.points } + if (items.size >= 3) 25 else 0
    }

    fun label(score: Int) = when {
        score >= 100 -> "Excellent! Waste is properly segregated."
        score >= 80 -> "Mostly segregated"
        score >= 50 -> "Partially segregated"
        else -> "Heavily mixed"
    }
}
