package com.material.recipe.data;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.material.recipe.advertise.AdConfig;
import com.material.recipe.advertise.AdNetworkHelper;
import com.material.recipe.connection.API;
import com.material.recipe.connection.RestAdapter;
import com.material.recipe.connection.callbacks.CallbackDevice;
import com.material.recipe.model.DeviceInfo;
import com.material.recipe.utils.Tools;

import retrofit2.Call;
import retrofit2.Response;

public class ThisApplication extends Application {

    private Call<CallbackDevice> callback = null;
    private static ThisApplication mInstance;
    private SharedPref sharedPref;
    private FirebaseAnalytics firebaseAnalytics;

    private int fcm_count = 0;
    private final int FCM_MAX_COUNT = 10;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constant.LOG_TAG, "onCreate : ThisApplication");
        mInstance = this;
        sharedPref = new SharedPref(this);

        // initialize firebase
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

        // Init firebase ads.
        AdNetworkHelper.init(this);

        // obtain regId & registering device to server
        obtainFirebaseToken(firebaseApp);

        //init image loader
        Tools.initImageLoader(getApplicationContext());

        // fetch firebase remote config
        fetchRemoteConfig();

        // activate analytics tracker
        getFirebaseAnalytics();
    }

    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }

    private void obtainFirebaseToken(final FirebaseApp firebaseApp) {
        if (!sharedPref.isOpenAppCounterReach() || !Tools.cekConnection(this)) return;
        fcm_count++;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e(Constant.LOG_TAG, "Failed obtain fcmID : " + task.getException());
                    if (fcm_count > FCM_MAX_COUNT) return;
                    obtainFirebaseToken(firebaseApp);
                    return;
                }
                String regId = task.getResult();
                sharedPref.setFcmRegId(regId);
                if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId);
            }
        });
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Firebase Cloud Messaging
     */
    private void sendRegistrationToServer(String token) {
        if (Tools.cekConnection(this) && !TextUtils.isEmpty(token)) {
            API api = RestAdapter.createAPI();
            DeviceInfo deviceInfo = Tools.getDeviceInfo(this);
            deviceInfo.setRegid(token);

            callback = api.registerDevice(deviceInfo);
            callback.enqueue(new retrofit2.Callback<CallbackDevice>() {
                @Override
                public void onResponse(Call<CallbackDevice> call, Response<CallbackDevice> response) {
                    CallbackDevice resp = response.body();
                    if (resp.status.equals("success")) {
                        sharedPref.setOpenAppCounter(0);
                    }
                }

                @Override
                public void onFailure(Call<CallbackDevice> call, Throwable t) {
                }
            });
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * For Remote Config
     */

    private void fetchRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate();
        AdConfig.setFromRemoteConfig(mFirebaseRemoteConfig);
    }

    /** ---------------------------------------- For Remote Config ----------------------------------
     * */

    /**
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    public synchronized FirebaseAnalytics getFirebaseAnalytics() {
        if (firebaseAnalytics == null && AppConfig.ENABLE_ANALYTICS) {
            // Obtain the Firebase Analytics.
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return firebaseAnalytics;
    }

    public void trackScreenView(String event) {
        if (firebaseAnalytics == null || !AppConfig.ENABLE_ANALYTICS) return;
        Bundle params = new Bundle();
        event = event.replaceAll("[^A-Za-z0-9_]", "");
        params.putString("event", event);
        firebaseAnalytics.logEvent(event, params);
    }

    public void trackEvent(String category, String action, String label) {
        if (firebaseAnalytics == null || !AppConfig.ENABLE_ANALYTICS) return;
        Bundle params = new Bundle();
        category = category.replaceAll("[^A-Za-z0-9_]", "");
        action = action.replaceAll("[^A-Za-z0-9_]", "");
        label = label.replaceAll("[^A-Za-z0-9_]", "");
        params.putString("category", category);
        params.putString("action", action);
        params.putString("label", label);
        firebaseAnalytics.logEvent("EVENT", params);
    }

    /** ---------------------------------------- End of analytics --------------------------------- */
}
