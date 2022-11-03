package com.material.recipe.advertise;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.Serializable;

public class AdConfig implements Serializable {

    // if you not use ads you can set this to false
    public static final boolean ENABLE_GDPR = true;

    // flag for display ads
    public static final boolean ADS_ENABLE = true;
    public static final boolean ADS_MAIN_INTERSTITIAL = ADS_ENABLE && true;
    public static final boolean ADS_MAIN_BANNER = ADS_ENABLE && true;
    public static final boolean ADS_RECIPE_DETAILS_BANNER = ADS_ENABLE && true;

    public enum AdNetworkType {
        ADMOB, FAN, APPLOVIN, UNITY
    }

    public static boolean ad_enable = true;
    public static boolean enable_gdpr = true;
    public static AdNetworkType ad_network = AdNetworkType.UNITY;
    public static int ad_inters_interval = 5;

    public static String ad_admob_publisher_id = "pub-3239677920600357";
    public static String ad_admob_banner_unit_id = "ca-app-pub-3940256099942544/6300978111";
    public static String ad_admob_interstitial_unit_id = "ca-app-pub-3940256099942544/1033173712";

    public static String ad_fan_banner_unit_id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
    public static String ad_fan_interstitial_unit_id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";

    public static String ad_applovin_banner_unit_id = "1849649d520e613b";
    public static String ad_applovin_interstitial_unit_id = "d990fd82bd2f007b";

    public static String ad_unity_game_id = "4655483";
    public static String ad_unity_banner_unit_id = "Banner_Android";
    public static String ad_unity_interstitial_unit_id = "Interstitial_Android";


    // Set data from remote config
    public static void setFromRemoteConfig(FirebaseRemoteConfig remote){
        String ad_network = remote.getString("ad_network");
        if(!remote.getString("ad_enable").isEmpty()) AdConfig.ad_enable = Boolean.parseBoolean(remote.getString("ad_enable"));
        if(!remote.getString("enable_gdpr").isEmpty()) AdConfig.enable_gdpr = Boolean.parseBoolean(remote.getString("enable_gdpr"));
        if(!remote.getString("ad_network").isEmpty()) AdConfig.ad_network = AdNetworkType.valueOf(remote.getString("ad_network"));
        if(!remote.getString("ad_inters_interval").isEmpty()) AdConfig.ad_inters_interval = Integer.parseInt(remote.getString("ad_inters_interval"));

        if(!remote.getString("ad_admob_publisher_id").isEmpty()) AdConfig.ad_admob_publisher_id = remote.getString("ad_inters_interval");
        if(!remote.getString("ad_admob_banner_unit_id").isEmpty()) AdConfig.ad_admob_banner_unit_id = remote.getString("ad_admob_banner_unit_id");
        if(!remote.getString("ad_admob_interstitial_unit_id").isEmpty()) AdConfig.ad_admob_interstitial_unit_id = remote.getString("ad_admob_interstitial_unit_id");

        if(!remote.getString("ad_fan_banner_unit_id").isEmpty()) AdConfig.ad_fan_banner_unit_id = remote.getString("ad_fan_banner_unit_id");
        if(!remote.getString("ad_fan_interstitial_unit_id").isEmpty()) AdConfig.ad_fan_banner_unit_id = remote.getString("ad_fan_banner_unit_id");

        if(!remote.getString("ad_applovin_banner_unit_id").isEmpty()) AdConfig.ad_applovin_banner_unit_id = remote.getString("ad_applovin_banner_unit_id");
        if(!remote.getString("ad_applovin_interstitial_unit_id").isEmpty()) AdConfig.ad_applovin_interstitial_unit_id = remote.getString("ad_applovin_interstitial_unit_id");

        if(!remote.getString("ad_unity_game_id").isEmpty()) AdConfig.ad_unity_game_id = remote.getString("ad_unity_game_id");
        if(!remote.getString("ad_unity_banner_unit_id").isEmpty()) AdConfig.ad_unity_banner_unit_id = remote.getString("ad_unity_banner_unit_id");
        if(!remote.getString("ad_unity_interstitial_unit_id").isEmpty()) AdConfig.ad_unity_interstitial_unit_id = remote.getString("ad_unity_interstitial_unit_id");
    }

}
