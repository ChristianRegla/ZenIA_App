package com.zenia.app.util

import com.zenia.app.model.DiarioEntrada

object AnalysisUtils {

    data class Insight(
        val activityName: String,
        val score: Float,
        val count: Int,
        val type: InsightType
    )

    enum class InsightType { POSITIVE, NEGATIVE, NEUTRAL }

    fun analyzePatterns(entries: List<DiarioEntrada>): Pair<Insight?, Insight?> {

        val activityMoods = mutableMapOf<String, MutableList<Float>>()

        entries.forEach { entry ->
            val moodValue = ChartUtils.mapMoodToValue(entry.estadoAnimo)
            if (moodValue > 0) {
                entry.actividades.forEach { actividad ->
                    if (activityMoods[actividad] == null) {
                        activityMoods[actividad] = mutableListOf()
                    }
                    activityMoods[actividad]?.add(moodValue)
                }
            }
        }

        val insights = activityMoods.map { (name, values) ->
            val average = values.average().toFloat()
            val count = values.size

            val type = when {
                average >= 4.0 -> InsightType.POSITIVE
                average <= 2.5 -> InsightType.NEGATIVE
                else -> InsightType.NEUTRAL
            }
            Insight(name, average, count, type)
        }.filter { it.count >= 3 }

        val topBooster = insights
            .filter { it.type == InsightType.POSITIVE }
            .maxByOrNull { it.score }

        val topDrainer = insights
            .filter { it.type == InsightType.NEGATIVE }
            .maxByOrNull { it.score }

        return Pair(topBooster, topDrainer)
    }
}