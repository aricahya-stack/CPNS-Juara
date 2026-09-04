package com.cpnsjuara.app

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdsManager {
    fun initialize(context: Context) {
        if (!PremiumPrefs.isPremium(context)) MobileAds.initialize(context)
    }


    fun attachBanner(activity: Activity, container: ViewGroup) {
        container.removeAllViews()
        if (PremiumPrefs.isPremium(activity)) return
        val adView = AdView(activity)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = activity.getString(R.string.admob_banner_id)
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    fun maybeShowInterstitial(activity: Activity, afterQuestions: Int, onDone: () -> Unit) {
        if (PremiumPrefs.isPremium(activity) || afterQuestions < 5) { onDone(); return }
        InterstitialAd.load(activity, activity.getString(R.string.admob_interstitial_id),
            AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() { onDone() }
                    }
                    ad.show(activity)
                }
                override fun onAdFailedToLoad(error: LoadAdError) { onDone() }
            })
    }

    fun showRewardedHint(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        if (PremiumPrefs.isPremium(activity)) { onReward(); return }
        RewardedAd.load(activity, activity.getString(R.string.admob_rewarded_id),
            AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { ad.show(activity) { onReward() } }
                override fun onAdFailedToLoad(error: LoadAdError) { onUnavailable() }
            })
    }
}
