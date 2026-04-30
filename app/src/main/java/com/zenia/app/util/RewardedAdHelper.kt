package com.zenia.app.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdHelper(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private val TAG = "RewardedAdHelper"

    // Nomás como dato pues este ID es de prueba
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    fun loadAd() {
        if (rewardedAd != null) {
            return
        }

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "El anuncio falló al cargar: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "¡Anuncio cargado exitosamente y listo para mostrarse!")
                rewardedAd = ad
            }
        })
    }

    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "El usuario cerró el anuncio.")
                    rewardedAd = null
                    loadAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Falló al intentar mostrar el anuncio: ${adError.message}")
                    rewardedAd = null
                    onAdDismissed()
                }
            }

            ad.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "El usuario ganó la recompensa: $rewardAmount $rewardType")

                onRewardEarned()
            }
        } else {
            Log.d(TAG, "Se intentó mostrar el anuncio, pero aún no estaba cargado.")
            loadAd()
            onAdDismissed()
        }
    }
}