package com.mcal.studio.activity;

import android.Manifest;
import android.animation.Animator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mcal.studio.R;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.helper.FontsOverride;
import com.mcal.studio.license.EclipseDistributionLicense10;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.psdev.licensesdialog.LicenseResolver;

/**
 * Activity for application splash
 */
public class SplashActivity extends AppCompatActivity {

    private static final int WRITE_PERMISSION_REQUEST = 0;

    /**
     * Layout to handle snackbars
     */
    @BindView(R.id.splash_layout)
    CoordinatorLayout splashLayout;

    @BindView(R.id.hyper_logo)
    ImageView logo;
    @BindView(R.id.hyper_logo_text)
    TextView logoText;

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LicenseResolver.registerLicense(new EclipseDistributionLicense10());
        FontsOverride.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/Inconsolata-Regular.ttf");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        logo.animate().alpha(1).setDuration(1000);
        logoText.animate().alpha(1).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setupPermissions();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setupPermissions();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void startIntro() {
        Class classTo = IntroActivity.class;
        if (Preferences.isIntro()) {
            classTo = MainActivity.class;
        }

        Intent intent = new Intent(SplashActivity.this, classTo);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setupPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                final Snackbar snackbar = Snackbar.make(splashLayout, getString(R.string.permission_storage_rationale), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("GRANT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, WRITE_PERMISSION_REQUEST);
                    }
                });

                snackbar.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_PERMISSION_REQUEST);
            }
        } else {
            startIntro();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startIntro();
            }
        } else {
            final Snackbar snackbar = Snackbar.make(splashLayout, getString(R.string.permission_storage_rationale), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("GRANT", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, WRITE_PERMISSION_REQUEST);
                }
            });

            snackbar.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            setupPermissions();
        }
    }
}
