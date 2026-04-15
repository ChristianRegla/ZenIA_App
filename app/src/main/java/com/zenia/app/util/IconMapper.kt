package com.zenia.app.util

import com.zenia.app.R

object IconMapper {

    fun getDrawable (iconName: String): Int {
        return when(iconName) {
            "nube_muy_feliz" -> R.drawable.nube_muh_feliz_de_mrd_pero_en_512
            "nube_feliz" -> R.drawable.nube_sonriente_de_mrd_pero_en_512
            "nube_mid" -> R.drawable.nube_mid_de_mrd_pero_en_512
            "nube_triste" -> R.drawable.nube_triste_de_mrd_pero_en_512
            "sol_muy_feliz" -> R.drawable.sol_muy_feliz_de_mrd_pero_en_512
            "sol_feliz" -> R.drawable.sol_sonriente_de_mrd_pero_en_512
            "sol_mid" -> R.drawable.sol_mid_de_mrd_pero_en_512
            "sol_triste" -> R.drawable.sol_triste_de_mrd_pero_en_512
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

    val iconStyles = listOf(
        listOf("nube_muy_feliz", "nube_feliz", "nube_mid", "nube_triste"),
        listOf("sol_muy_feliz", "sol_feliz", "sol_mid", "sol_triste"),
        listOf("happy1", "happy2", "sad", "sad2"),
        listOf("superhappy", "happyface", "sadface", "supersad")
    )

    val availableIcons = iconStyles.flatten()
}