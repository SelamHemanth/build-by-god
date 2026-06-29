package com.buildbygod.domain.model

import java.time.LocalDate

/** Broad weather buckets the animated background reacts to. */
enum class WeatherCondition { CLEAR, CLOUDY, RAIN, SNOW, FOG, STORM }

/** Meteorological season, used to tint the backdrop even when there's no live weather. */
enum class Season { SPRING, SUMMER, AUTUMN, WINTER }

data class WeatherInfo(
    val condition: WeatherCondition = WeatherCondition.CLEAR,
    val season: Season = Season.SUMMER,
    val temperatureC: Float? = null,
    val isDay: Boolean = true,
    val resolved: Boolean = false
) {
    companion object {
        /**
         * Season from month + hemisphere. Negative latitude flips the seasons.
         * Falls back to northern hemisphere when latitude is unknown.
         */
        fun seasonFor(date: LocalDate, latitude: Double?): Season {
            val northern = (latitude ?: 0.0) >= 0.0
            val north = when (date.monthValue) {
                12, 1, 2 -> Season.WINTER
                3, 4, 5 -> Season.SPRING
                6, 7, 8 -> Season.SUMMER
                else -> Season.AUTUMN
            }
            if (northern) return north
            return when (north) {
                Season.WINTER -> Season.SUMMER
                Season.SPRING -> Season.AUTUMN
                Season.SUMMER -> Season.WINTER
                Season.AUTUMN -> Season.SPRING
            }
        }

        /** Maps an Open-Meteo WMO weather code to our condition bucket. */
        fun conditionForWmo(code: Int): WeatherCondition = when (code) {
            0, 1 -> WeatherCondition.CLEAR
            2, 3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOG
            in 51..67 -> WeatherCondition.RAIN
            in 71..77 -> WeatherCondition.SNOW
            in 80..82 -> WeatherCondition.RAIN
            in 85..86 -> WeatherCondition.SNOW
            in 95..99 -> WeatherCondition.STORM
            else -> WeatherCondition.CLOUDY
        }
    }
}
