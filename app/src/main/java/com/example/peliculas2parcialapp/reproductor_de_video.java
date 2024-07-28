package com.example.peliculas2parcialapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class reproductor_de_video extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    SurfaceView surfaceView;
    MediaPlayer mediaPlayer;
    ImageButton play;
    ImageButton pausa;
    ImageButton detener;
    ImageButton retroceder;
    ImageButton adelantar;
    Button regresar;
    ImageView imagen_foto;
    TextView textNombre;
    TextView textEdad;

    boolean Stopped = false;
    boolean FotoTomada = false;
    File fotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor_de_video);

        surfaceView = findViewById(R.id.reproductor);
        play = findViewById(R.id.btn_Play);
        pausa = findViewById(R.id.btn_Pausa);
        detener = findViewById(R.id.btn_Detener);
        retroceder = findViewById(R.id.btn_Retroceder);
        adelantar = findViewById(R.id.btn_Adelantar);
        regresar = findViewById(R.id.btn_Regresar);
        imagen_foto = findViewById(R.id.imagen_foto);
        textNombre = findViewById(R.id.text_nombre);
        textEdad = findViewById(R.id.text_edad);

        mostrarUsuarioEdad();

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                configurarMediaPlayer(holder);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });

        mediaPlayer = new MediaPlayer();
        habilitarBotones(false);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reproducirVideo();
            }
        });

        pausa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        detener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        Stopped = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        retroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int posicion = mediaPlayer.getCurrentPosition();
                    if (posicion - 5000 > 0) {
                        mediaPlayer.seekTo(posicion - 5000);
                    } else {
                        mediaPlayer.seekTo(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        adelantar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int posicion = mediaPlayer.getCurrentPosition();
                    int duracion = mediaPlayer.getDuration();
                    if (posicion + 5000 < duracion) {
                        mediaPlayer.seekTo(posicion + 5000);
                    } else {
                        mediaPlayer.seekTo(duracion);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        verificarFotoYMostrarDialogo();
    }

    private void verificarFotoYMostrarDialogo() {
        if (!cargarFotoSiExiste()) {
            mostrarDialogoTomarFoto();
        } else {
            habilitarBotones(true);
        }
    }

    private void mostrarDialogoTomarFoto() {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        constructor.setMessage("Debes tomarte una foto antes de reproducir el video")
                .setPositiveButton("Aceptar", (dialog, id) -> tomarFoto())
                .setNegativeButton("Cancelar", (dialog, id) -> {
                    Intent menuIntent = new Intent(reproductor_de_video.this, menu.class);
                    startActivity(menuIntent);
                    finish();
                });
        AlertDialog dialogo = constructor.create();
        dialogo.show();
    }

    private void tomarFoto() {
        Intent tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (tomarFotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(tomarFotoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                FotoTomada = true;
                imagen_foto.setImageBitmap(imageBitmap);
                habilitarBotones(true);
                guardarFoto(imageBitmap);
                Toast.makeText(this, "Foto tomada con éxito", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al tomar la foto, por favor intente nuevamente", Toast.LENGTH_SHORT).show();
                mostrarDialogoTomarFoto();
            }
        } else {
            Toast.makeText(this, "Error al tomar la foto, por favor intente nuevamente", Toast.LENGTH_SHORT).show();
            mostrarDialogoTomarFoto();
        }
    }

    private void habilitarBotones(boolean habilitar) {
        play.setEnabled(habilitar);
        pausa.setEnabled(habilitar);
        detener.setEnabled(habilitar);
        retroceder.setEnabled(habilitar);
        adelantar.setEnabled(habilitar);
    }

    private void configurarMediaPlayer(SurfaceHolder holder) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDisplay(holder);
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.caricatura);
            mediaPlayer.setDataSource(this, videoUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarUsuarioEdad() {
        try {
            BufferedReader buffered = new BufferedReader(new InputStreamReader(openFileInput("usuario_actual.txt")));
            String usuarioData = buffered.readLine();
            buffered.close();

            if (usuarioData != null) {
                String[] partes = usuarioData.split("\\|");
                if (partes.length >= 2) {
                    String nombre = partes[0];
                    String edad = partes[1];
                    textNombre.setText("Nombre: " + nombre);
                    textEdad.setText("Edad: " + edad);
                } else {
                    textNombre.setText("Datos de usuario no válidos");
                    resetFotoTomada();
                }
            } else {
                textNombre.setText("No hay usuario registrado");
                resetFotoTomada();
            }
        } catch (Exception ex) {
            textNombre.setText("Error al leer los datos del usuario: " + ex.getMessage());
            resetFotoTomada();
        }
    }

    private void resetFotoTomada() {
        FotoTomada = false;
        imagen_foto.setImageDrawable(null);
        habilitarBotones(false);
    }

    private void guardarFoto(Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            String nombreUsuario = obtenerNombreUsuarioActual();
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            fotoFile = new File(storageDir, "foto_" + nombreUsuario + ".jpg");
            fos = new FileOutputStream(fotoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean cargarFotoSiExiste() {
        String nombreUsuario = obtenerNombreUsuarioActual();
        if (nombreUsuario != null) {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            fotoFile = new File(storageDir, "foto_" + nombreUsuario + ".jpg");
            if (fotoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(fotoFile.getAbsolutePath());
                imagen_foto.setImageBitmap(bitmap);
                FotoTomada = true;
                return true;
            } else {
                resetFotoTomada();
                return false;
            }
        } else {
            resetFotoTomada();
            return false;
        }
    }

    private String obtenerNombreUsuarioActual() {
        try {
            BufferedReader buffered = new BufferedReader(new InputStreamReader(openFileInput("usuario_actual.txt")));
            String usuarioData = buffered.readLine();
            buffered.close();
            if (usuarioData != null) {
                String[] partes = usuarioData.split("\\|");
                if (partes.length >= 2) {
                    return partes[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void reproducirVideo() {
        try {
            if (!mediaPlayer.isPlaying()) {
                if (Stopped) {
                    mediaPlayer.reset();
                    configurarMediaPlayer(surfaceView.getHolder());
                    Stopped = false;
                }
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
