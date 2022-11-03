package com.material.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.material.recipe.advertise.AdConfig;
import com.material.recipe.advertise.AdNetworkHelper;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.Constant;
import com.material.recipe.data.DatabaseHandler;
import com.material.recipe.data.GDPR;
import com.material.recipe.data.SharedPref;
import com.material.recipe.data.ThisApplication;
import com.material.recipe.model.Recipe;
import com.material.recipe.utils.Tools;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ActivityRecipeDetails extends AppCompatActivity {

    public static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    private Recipe recipe;
    private FloatingActionButton fab;
    private View parent_view;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        parent_view = findViewById(android.R.id.content);

        prepareAds();

        db = new DatabaseHandler(getApplicationContext());
        Tools.initImageLoader(getApplicationContext());

        recipe = (Recipe) getIntent().getSerializableExtra(EXTRA_OBJECT);
        setupToolbar(recipe.name);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabToggle();

        ((TextView) findViewById(R.id.duration)).setText(recipe.duration + " " + getString(R.string.duration_unit));
        ((TextView) findViewById(R.id.category)).setText(recipe.category_name);
        WebView webview = (WebView) findViewById(R.id.instructions);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL(null, recipe.instruction, "text/html; charset=UTF-8", "utf-8", null);
        ImageView image = (ImageView) findViewById(R.id.image);
        imgloader.displayImage(Constant.getURLimgRecipe(recipe.image), image);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (db.isFavoritesExist(recipe.id)) {
                    db.deleteFavorites(recipe);
                    Snackbar.make(parent_view, recipe.name + " " + getString(R.string.remove_favorites), Snackbar.LENGTH_SHORT).show();
                    // analytics tracking
                    ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "REMOVE", recipe.name);
                } else {
                    db.addOneFavorite(recipe);
                    Snackbar.make(parent_view, recipe.name + " " + getString(R.string.add_favorites), Snackbar.LENGTH_SHORT).show();
                    // analytics tracking
                    ThisApplication.getInstance().trackEvent(Constant.Event.FAVORITES.name(), "ADD", recipe.name);
                }
                fabToggle();
            }
        });

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View recipe : " + recipe.name);
    }

    private void setupToolbar(String name) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(name);
        CollapsingToolbarLayout collapsing_toolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setContentScrimColor(new SharedPref(this).getThemeColorInt());
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void fabToggle() {
        if (db.isFavoritesExist(recipe.id)) {
            fab.setImageResource(R.drawable.ic_nav_favorites);
        } else {
            fab.setImageResource(R.drawable.ic_nav_favorites_outline);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
        } else if (id == R.id.action_rate) {
            Snackbar.make(parent_view, R.string.rate_this_app, Snackbar.LENGTH_SHORT).show();
            Tools.rateAction(this);
        } else if (id == R.id.action_about) {
            Tools.aboutAction(this);
        } else if (id == R.id.action_share) {
            Snackbar.make(parent_view, R.string.share_action, Snackbar.LENGTH_SHORT).show();
            Tools.methodShare(ActivityRecipeDetails.this, recipe);
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_recipe_details, menu);
        return true;
    }

    private void prepareAds() {
        AdNetworkHelper adNetworkHelper = new AdNetworkHelper(this);
        adNetworkHelper.showGDPR();
        adNetworkHelper.loadBannerAd(AdConfig.ADS_RECIPE_DETAILS_BANNER);
    }

    @Override
    protected void onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(getApplicationContext());
        super.onResume();
    }

}
