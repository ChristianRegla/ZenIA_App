package com.zenia.app.util

import com.zenia.app.R

object IconMapper {

    fun getDrawable (iconName: String): Int {
        return when(iconName) {
            "nube_feliz" -> R.drawable.ic_nube_feli
            "sol_feliz" -> R.drawable.ic_sol_feli
            "nube_triste" -> R.drawable.ic_nube_tite
            "sol_triste" -> R.drawable.ic_sol_tite
            "happy1" -> R.drawable.happy
            "happy2" -> R.drawable.happy2
            "sad" -> R.drawable.sad
            "sad2" -> R.drawable.sad2
            "superhappy" -> R.drawable.superhappy
            "happyface" -> R.drawable.happyface
            "sadface" -> R.drawable.sadface
            "supersad" -> R.drawable.supersad
            else -> R.drawable.ic_sol_feli
        }
    }

    val availableIcons = listOf(
        "nube_feliz", "sol_feliz", "nube_triste", "sol_triste", "happy1",
        "happy2", "sad", "sad2", "superhappy", "happyface", "sadface", "supersad"
    )
}