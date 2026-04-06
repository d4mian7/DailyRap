package com.dailyrap.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun loadAd() {
        if (rewardedAd != null || isLoading) return
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoading = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                isLoading = false
            }
        })
    }

    fun showAd(activity: Activity, onRewarded: () -> Unit, onFailed: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onFailed()
            loadAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                onFailed()
                loadAd()
            }
        }

        ad.show(activity) { onRewarded() }
    }

    fun isAdReady(): Boolean = rewardedAd != null
}
