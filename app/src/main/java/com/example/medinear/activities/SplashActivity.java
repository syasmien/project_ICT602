package com.example.medinear.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medinear.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000;
    private ImageView logo;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);

        playAnimation();

        handler.postDelayed(this::goNext, SPLASH_TIME);
    }

    private void goNext() {

        if (!isActive) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent;

        if (currentUser != null) {
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void playAnimation() {

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f);
        fadeIn.setDuration(1200);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.6f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.6f, 1f);

        scaleX.setDuration(1200);
        scaleY.setDuration(1200);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleX, scaleY);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
        handler.removeCallbacksAndMessages(null);
    }
}