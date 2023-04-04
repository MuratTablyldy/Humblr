package ru.skillbox.humblr.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.browser.customtabs.CustomTabsIntent;

public class CustomTabActivity extends Activity {
    public static final String URI = "uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String url = intent.getExtras().getString(URI);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setColorScheme(CustomTabsIntent.COLOR_SCHEME_LIGHT);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setResult(RESULT_CANCELED);
        finish();
    }
}