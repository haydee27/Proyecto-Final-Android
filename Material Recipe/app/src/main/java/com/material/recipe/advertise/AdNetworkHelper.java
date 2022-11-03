package com.material.recipe.advertise;

import static com.facebook.ads.AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.multidex.BuildConfig;

import com.material.recipe.R;
import com.material.recipe.data.GDPR;
import com.material.recipe.data.SharedPref;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.ads.mediation.facebook.FacebookExtras;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class AdNetworkHelper {

    private static final String TAG = AdNetworkHelper.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;

    //Interstitial
    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private MaxInterstitialAd applovinInterstitialAd;

    public AdNetworkHelper(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
    }

    public static void init(Context context) {
        if (!AdConfig.ad_enable) return;
        if (AdConfig.ad_network == AdConfig.AdNetworkType.ADMOB) {
            // Init firebase ads.
            MobileAds.initialize(context);

        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.FAN) {
            AudienceNetworkAds.initialize(context);
            AdSettings.setIntegrationErrorMode(INTEGRATION_ERROR_CALLBACK_MODE);

        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.UNITY) {
            UnityAds.initialize(context, AdConfig.ad_unity_game_id, BuildConfig.DEBUG, new IUnityAdsInitializationListener() {
                @Override
                public void onInitializationComplete() {
                    Log.d(TAG, "Unity Ads Initialization Complete");
                    Log.d(TAG, "Unity Ads Game ID : " + AdConfig.ad_unity_game_id);
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                    Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                }
            });

        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.APPLOVIN) {
            AppLovinSdk.getInstance(context).setMediationProvider(AppLovinMediationProvider.MAX);
            AppLovinSdk.getInstance(context).getSettings().setVerboseLogging(true);
            AppLovinSdk.getInstance(context).initializeSdk();
            final String sdkKey = AppLovinSdk.getInstance(context).getSdkKey();
            if (!sdkKey.equals(context.getString(R.string.applovin_sdk_key))) {
                Log.e(TAG, "AppLovin ERROR : Please update your sdk key in the manifest file.");
            }
            Log.d(TAG, "AppLovin SDK Key : " + sdkKey);
        }
    }

    public void showGDPR() {
        if (!AdConfig.ad_enable || !AdConfig.ENABLE_GDPR) return;
        if (AdConfig.ad_network == AdConfig.AdNetworkType.ADMOB) {
            GDPR.updateConsentStatus(activity);
        }
    }

    public void loadBannerAd(boolean enable) {
        if (!AdConfig.ad_enable || !enable) return;
        LinearLayout ad_container = activity.findViewById(R.id.ad_container);
        ad_container.removeAllViews();
        if (AdConfig.ad_network == AdConfig.AdNetworkType.ADMOB) {
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(activity)).build();
            ad_container.setVisibility(View.GONE);
            AdView adView = new AdView(activity);
            adView.setAdUnitId(AdConfig.ad_admob_banner_unit_id);
            ad_container.addView(adView);
            adView.setAdSize(getAdmobBannerSize());
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    ad_container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    ad_container.setVisibility(View.GONE);
                }
            });
        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.FAN) {
            com.facebook.ads.AdView adView = new com.facebook.ads.AdView(activity, AdConfig.ad_fan_banner_unit_id, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
            // Add the ad view to your activity layout
            ad_container.addView(adView);
            com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    ad_container.setVisibility(View.GONE);
                    Log.d(TAG, "Failed to load Audience Network : " + adError.getErrorMessage() + " " + adError.getErrorCode());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    ad_container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            com.facebook.ads.AdView.AdViewLoadConfig loadAdConfig = adView.buildLoadAdConfig().withAdListener(adListener).build();
            adView.loadAd(loadAdConfig);

        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.UNITY) {
            BannerView bottomBanner = new BannerView(activity, AdConfig.ad_unity_banner_unit_id, getUnityBannerSize());
            bottomBanner.setListener(new BannerView.IListener() {
                @Override
                public void onBannerLoaded(BannerView bannerView) {
                    ad_container.setVisibility(View.VISIBLE);
                    Log.d(TAG, "ready");
                }

                @Override
                public void onBannerClick(BannerView bannerView) {

                }

                @Override
                public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
                    Log.d(TAG, "Banner Error" + bannerErrorInfo);
                    ad_container.setVisibility(View.GONE);
                }

                @Override
                public void onBannerLeftApplication(BannerView bannerView) {

                }
            });
            ad_container.addView(bottomBanner);
            bottomBanner.load();

        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.APPLOVIN) {
            MaxAdView maxAdView = new MaxAdView(AdConfig.ad_applovin_banner_unit_id, activity);
            maxAdView.setListener(new MaxAdViewAdListener() {
                @Override
                public void onAdExpanded(MaxAd ad) {

                }

                @Override
                public void onAdCollapsed(MaxAd ad) {

                }

                @Override
                public void onAdLoaded(MaxAd ad) {
                    ad_container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {

                }

                @Override
                public void onAdHidden(MaxAd ad) {

                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    ad_container.setVisibility(View.GONE);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                }
            });

            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int heightPx = dpToPx(activity, 50);
            maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
            ad_container.addView(maxAdView);
            maxAdView.loadAd();
        }
    }

    public void loadInterstitialAd(boolean enable) {
        if (!AdConfig.ad_enable || !enable) return;
        if (AdConfig.ad_network == AdConfig.AdNetworkType.ADMOB) {
            InterstitialAd.load(activity, AdConfig.ad_admob_interstitial_unit_id, getAdRequest(false), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    adMobInterstitialAd = interstitialAd;
                    adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            adMobInterstitialAd = null;
                            loadInterstitialAd(enable);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "The ad was shown.");
                            sharedPref.setIntersCounter(0);
                        }
                    });
                    Log.i(TAG, "onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.i(TAG, loadAdError.getMessage());
                    adMobInterstitialAd = null;
                    Log.d(TAG, "Failed load AdMob Interstitial Ad");
                }
            });
        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.FAN) {
            fanInterstitialAd = new com.facebook.ads.InterstitialAd(activity, AdConfig.ad_fan_interstitial_unit_id);

            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    // Interstitial ad displayed callback
                    Log.e(TAG, "Interstitial ad displayed.");
                    sharedPref.setIntersCounter(0);
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    fanInterstitialAd = null;
                    loadInterstitialAd(enable);
                    Log.e(TAG, "Interstitial ad dismissed.");
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                }

                @Override
                public void onAdClicked(Ad ad) {
                    Log.d(TAG, "Interstitial ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.d(TAG, "Interstitial ad impression logged!");
                }
            };

            // load ads
            fanInterstitialAd.loadAd(fanInterstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.UNITY) {

        } else if (AdConfig.ad_network == AdConfig.AdNetworkType.APPLOVIN) {
            applovinInterstitialAd = new MaxInterstitialAd(AdConfig.ad_applovin_interstitial_unit_id, activity);
            applovinInterstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.d(TAG, "AppLovin Interstitial Ad loaded...");
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sharedPref.setIntersCounter(0);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    applovinInterstitialAd.loadAd();
                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.d(TAG, "failed to load AppLovin Interstitial : " + error.getAdLoadFailureInfo());
                    applovinInterstitialAd.loadAd();
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    applovinInterstitialAd.loadAd();
                }
            });

            // Load the first ad
            applovinInterstitialAd.loadAd();
        }
    }

    public boolean showInterstitialAd(boolean enable) {
        if (!AdConfig.ad_enable || !enable) return false;
        int counter = sharedPref.getIntersCounter();
        if (counter > AdConfig.ad_inters_interval) {
            if (AdConfig.ad_network == AdConfig.AdNetworkType.ADMOB) {
                if (adMobInterstitialAd == null) return false;
                adMobInterstitialAd.show(activity);
            } else if (AdConfig.ad_network == AdConfig.AdNetworkType.FAN) {
                if (fanInterstitialAd == null || !fanInterstitialAd.isAdLoaded()) return false;
                fanInterstitialAd.show();
            } else if (AdConfig.ad_network == AdConfig.AdNetworkType.UNITY) {
                //if (!UnityAds.isReady(adConfig.ad_unity_interstitial_unit_id)) return false;
                //DataApp.pref().setIntersCounter(0);
                UnityAds.show(activity, AdConfig.ad_unity_interstitial_unit_id, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {

                    }

                    @Override
                    public void onUnityAdsShowStart(String s) {
                        sharedPref.setIntersCounter(0);
                        loadInterstitialAd(enable);
                    }

                    @Override
                    public void onUnityAdsShowClick(String s) {

                    }

                    @Override
                    public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {

                    }
                });

            } else if (AdConfig.ad_network == AdConfig.AdNetworkType.APPLOVIN) {
                if (applovinInterstitialAd == null || !applovinInterstitialAd.isReady()) {
                    return false;
                }
                applovinInterstitialAd.showAd();
            }
            return true;
        } else {
            sharedPref.setIntersCounter(sharedPref.getIntersCounter() + 1);
        }
        return false;
    }


    private AdSize getAdmobBannerSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    private UnityBannerSize getUnityBannerSize() {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return new UnityBannerSize(adWidth, 50);
    }

    // for facebook bidding ads
    public AdRequest getAdRequest(boolean nativeBanner) {
        Bundle extras = new FacebookExtras().setNativeBanner(nativeBanner).build();
        if (AdConfig.enable_gdpr) {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(activity))
                    .addNetworkExtrasBundle(FacebookAdapter.class, extras)
                    .build();
        } else {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(FacebookAdapter.class, extras)
                    .build();
        }
    }

    private static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}
