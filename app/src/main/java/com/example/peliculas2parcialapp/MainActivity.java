package com.example.peliculas2parcialapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    VideoView videoSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoSplash = findViewById(R.id.splash);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash2);
        videoSplash.setVideoURI(videoUri);

        videoSplash.post(this::animacionVideoView);

        videoSplash.setOnCompletionListener(mp -> {
            Intent ventana = new Intent(MainActivity.this, bienvenida.class);
            startActivity(ventana);
            finish();
        });
    }

    protected void animacionVideoView() {
        final int duracionBajada = 1000;
        final int duracionZoom = 1000;

        View parent = (View) videoSplash.getParent();
        int videoAlto = videoSplash.getHeight();

        ObjectAnimator bajar = ObjectAnimator.ofFloat(videoSplash, "translationY", -videoAlto, 0);
        bajar.setDuration(duracionBajada);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(videoSplash, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(videoSplash, "scaleY", 1f, 1.4f);

        scaleX.setDuration(duracionZoom);
        scaleY.setDuration(duracionZoom);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(bajar).before(scaleX).before(scaleY);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                videoSplash.start();
            }
        });

        animatorSet.start();
    }
}
