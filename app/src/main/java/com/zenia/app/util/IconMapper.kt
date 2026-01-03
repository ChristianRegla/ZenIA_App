package com.zenia.app.util

import com.zenia.app.R

object IconMapper {

    fun getDrawable (iconName: String): Int {
        return when(iconName) {
            "nube_feliz" -> R.drawable.ic_nube_feli
            "sol_feliz" -> R.drawable.ic_sol_feli
            "nube_triste" -> R.drawable.ic_nube_tite
            "sol_triste" -> R.drawable.ic_sol_tite
            else -> R.drawable.ic_sol_feli
        }
    }

    val availableIcons = listOf(
        "nube_feliz", "sol_feliz", "nube_triste", "sol_triste"
    )
}