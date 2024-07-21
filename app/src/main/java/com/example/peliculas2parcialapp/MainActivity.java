package com.example.peliculas2parcialapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    VideoView videoSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoSplash = findViewById(R.id.splash);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.Splash2);
        videoSplash.setVideoURI(videoUri);

        videoSplash.post(this::animacionVideoView);

        videoSplash.setOnCompletionListener(mp -> {
            Intent ventana = new Intent(MainActivity.this, bienvenida.class);
            startActivity(ventana);
            finish();
        });
    }

    protected void animacionVideoView() {
        final int radio = 300;
        final int duracion = 3000;
        final int repeticiones = 4;

        int contenedorAncho = ((View) videoSplash.getParent()).getWidth();
        int contenedorAlto = ((View) videoSplash.getParent()).getHeight();
        int videoAncho = videoSplash.getWidth();
        int videoAlto = videoSplash.getHeight();

        videoSplash.setX(contenedorAncho - videoAncho);
        videoSplash.setY((contenedorAlto - videoAlto) / 2);

        ValueAnimator animador = ValueAnimator.ofFloat(0, 300 * repeticiones);
        animador.setDuration(duracion * repeticiones);
        animador.setRepeatCount(0);

        animador.addUpdateListener(animation -> {
            float angulo = (float) animation.getAnimatedValue();
            float radianes = (float) Math.toRadians(angulo);

            float x = (float) (radio * Math.cos(radianes)) + (contenedorAncho / 1) - (videoAncho / 1);
            float y = (float) (radio * Math.sin(radianes)) + (contenedorAlto / 3) - (videoAlto / 3);

            videoSplash.setX(x);
            videoSplash.setY(y);
        });

        animador.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                videoSplash.start();
            }
        });

        animador.start();
    }
}