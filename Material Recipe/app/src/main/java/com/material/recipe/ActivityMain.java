package com.material.recipe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.material.recipe.advertise.AdConfig;
import com.material.recipe.advertise.AdNetworkHelper;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.data.GDPR;
import com.material.recipe.fragment.CategoryFragment;
import com.material.recipe.fragment.FavoritesFragment;
import com.material.recipe.fragment.RecipesFragment;
import com.material.recipe.utils.Tools;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ActivityMain extends AppCompatActivity {

    private ImageLoader imgloader = ImageLoader.getInstance();

    private Toolbar toolbar;
    public ActionBar actionBar;
    private NavigationView navigationView;
    private View parent_view;
    private DatabaseHandler db;

    static ActivityMain activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parent_view = findViewById(android.R.id.content);
        activityMain = this;
        prepareAds();

        db = new DatabaseHandler(getApplicationContext());
        Tools.initImageLoader(getApplicationContext());

        initToolbar();
        initDrawerMenu();

        // set initial view
        displayFragment(R.id.nav_recipes, getString(R.string.title_nav_recipes));

        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        Tools.setActionBarColor(this, actionBar);
    }

    private void initDrawerMenu() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                hideKeyboard();
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                updateDrawerCounter();
                showInterstitialAd();
                displayFragment(menuItem.getItemId(), menuItem.getTitle().toString());
                drawer.closeDrawers();
                return true;
            }
        });
    }

    private void displayFragment(int id, String title) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        if(id == R.id.nav_recipes){
            fragment = new RecipesFragment();
        } else if(id == R.id.nav_category){
            fragment = new CategoryFragment();
        } else if(id == R.id.nav_favorites){
            fragment = new FavoritesFragment();
        } else if(id == R.id.nav_setting){
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
        } else if(id == R.id.nav_rate){
            Snackbar.make(parent_view, R.string.rate_this_app, Snackbar.LENGTH_SHORT).show();
            Tools.rateAction(this);
        } else if(id == R.id.nav_more){
            Tools.directBrowser(this, getString(R.string.more_app_url));
        } else if(id == R.id.nav_about){
            Tools.aboutAction(this);
        }
        if (fragment != null) {
            fragment.setArguments(bundle);
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_content, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void updateDrawerCounter() {
        int fav_counter = db.getAllFavorites().size();
        setMenuAdvCounter(R.id.nav_favorites, fav_counter);
    }

    //set counter in drawer
    private void setMenuAdvCounter(@IdRes int itemId, int count) {
        TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView().findViewById(R.id.counter);
        view.setText(count > 0 ? String.valueOf(count) : null);
    }

    @Override
    protected void onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(getApplicationContext());
        updateDrawerCounter();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_setting){
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
        } else if(id == R.id.action_rate){
            Snackbar.make(parent_view, R.string.rate_this_app, Snackbar.LENGTH_SHORT).show();
            Tools.rateAction(this);
        }else if(id == R.id.action_about){
            Tools.aboutAction(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            doExitApp();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Snackbar.make(parent_view, R.string.press_again_exit_app, Snackbar.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private AdNetworkHelper adNetworkHelper;

    private void prepareAds() {
        adNetworkHelper = new AdNetworkHelper(this);
        adNetworkHelper.showGDPR();
        adNetworkHelper.loadBannerAd(AdConfig.ADS_MAIN_BANNER);
        adNetworkHelper.loadInterstitialAd(AdConfig.ADS_MAIN_INTERSTITIAL);
    }

    public void showInterstitialAd() {
        adNetworkHelper.showInterstitialAd(AdConfig.ADS_MAIN_INTERSTITIAL);
    }

    public static ActivityMain getInstance() {
        return activityMain;
    }

    public static FloatingActionButton getFab() {
        return (FloatingActionButton) activityMain.findViewById(R.id.fab);
    }

    public static void animateFab(final boolean hide) {
        FloatingActionButton f_ab = (FloatingActionButton) activityMain.findViewById(R.id.fab);
        int moveY = hide ? (2 * f_ab.getHeight()) : 0;
        f_ab.animate().translationY(moveY).setStartDelay(100).setDuration(400).start();
    }

}
