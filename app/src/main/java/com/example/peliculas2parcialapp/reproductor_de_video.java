package com.example.peliculas2parcialapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
    File fotoArchivo;
    private String categoriaSeleccionada;
    private String nombreUsuarioActual;
    private static final String FOTO_TOMADA_KEY = "fotoTomada";
    private static final String NOMBRE_USUARIO_KEY = "nombreUsuario";

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

        if (savedInstanceState != null) {
            FotoTomada = savedInstanceState.getBoolean(FOTO_TOMADA_KEY, false);
            nombreUsuarioActual = savedInstanceState.getString(NOMBRE_USUARIO_KEY);
            if (FotoTomada) {
                cargarFoto();
                habilitarBotones(true);
            }
        } else {
            mostrarUsuarioEdad();
        }

        // Obtener la categoría seleccionada desde el Intent
        categoriaSeleccionada = getIntent().getStringExtra("categoria");

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                new ConfigurarMediaPlayerTask().execute(holder);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FOTO_TOMADA_KEY, FotoTomada);
        outState.putString(NOMBRE_USUARIO_KEY, nombreUsuarioActual);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        FotoTomada = savedInstanceState.getBoolean(FOTO_TOMADA_KEY, false);
        nombreUsuarioActual = savedInstanceState.getString(NOMBRE_USUARIO_KEY);
        if (FotoTomada) {
            cargarFoto();
            habilitarBotones(true);
        }
    }

    private void verificarFotoYMostrarDialogo() {
        resetFotoTomada();
        new CargarFotoTask().execute();
    }

    private void mostrarDialogoTomarFoto() {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        constructor.setMessage("Debes tomar una foto antes de reproducir el video")
                .setPositiveButton("Aceptar", (dialogo, id) -> tomarFoto())
                .setNegativeButton("Cancelar", (dialogo, id) -> {
                    Intent menuIntent = new Intent(reproductor_de_video.this, menu.class);
                    startActivity(menuIntent);
                    finish();
                });
        AlertDialog dialogo = constructor.create();
        dialogo.show();
    }

    private void tomarFoto() {
        Intent tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        tomarFotoIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
                Toast.makeText(this, "Error al tomar la foto, por favor intenta nuevamente", Toast.LENGTH_SHORT).show();
                mostrarDialogoTomarFoto();
            }
        } else {
            Toast.makeText(this, "Error al tomar la foto, por favor intenta nuevamente", Toast.LENGTH_SHORT).show();
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

    private class ConfigurarMediaPlayerTask extends AsyncTask<SurfaceHolder, Void, Boolean> {
        private SurfaceHolder holder;

        @Override
        protected Boolean doInBackground(SurfaceHolder... holders) {
            holder = holders[0];
            try {
                mediaPlayer.reset();
                mediaPlayer.setDisplay(holder);

                // Obtener el URI del video según la categoría seleccionada
                Uri videoUri = obtenerUriVideoPorCategoria(categoriaSeleccionada);

                mediaPlayer.setDataSource(reproductor_de_video.this, videoUri);
                mediaPlayer.prepare();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(reproductor_de_video.this, "Error al preparar el video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri obtenerUriVideoPorCategoria(String categoria) {
        int videoResId;
        switch (categoria) {
            case "caricatura":
                videoResId = R.raw.caricatura;
                break;
            case "accion":
                videoResId = R.raw.accion;
                break;
            case "terror":
                videoResId = R.raw.terror;
                break;
            default:
                videoResId = R.raw.caricatura;
                break;
        }
        return Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
    }

    private void mostrarUsuarioEdad() {
        try {
            File archivoUsuarios = new File(getFilesDir(), "usuarios.txt");
            if (archivoUsuarios.exists()) {
                BufferedReader lector = new BufferedReader(new InputStreamReader(openFileInput("usuarios.txt")));
                String usuarioData = lector.readLine();
                lector.close();
                if (usuarioData != null) {
                    String[] partes = usuarioData.split("\\|");
                    if (partes.length >= 2) {
                        String nombre = partes[0];
                        String edad = partes[1];
                        nombreUsuarioActual = nombre;
                        textNombre.setText("Nombre: " + nombre);
                        textEdad.setText("Edad: " + edad);
                    } else {
                        textNombre.setText("Datos de usuario no válidos");
                        resetFotoTomada();
                    }
                } else {
                    textNombre.setText("No se encontraron datos de usuario");
                    resetFotoTomada();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            textNombre.setText("Error al leer los datos de usuario");
            resetFotoTomada();
        }
    }

    private void resetFotoTomada() {
        FotoTomada = false;
        habilitarBotones(false);
    }

    private void guardarFoto(Bitmap bitmap) {
        File directorioFotos = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directorioFotos != null) {
            fotoArchivo = new File(directorioFotos, nombreUsuarioActual + "_foto.png");
            try (FileOutputStream out = new FileOutputStream(fotoArchivo)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cargarFoto() {
        new CargarFotoTask().execute();
    }

    private class CargarFotoTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                File archivo = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombreUsuarioActual + "_foto.png");
                if (archivo.exists()) {
                    return BitmapFactory.decodeFile(archivo.getAbsolutePath());
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imagen_foto.setImageBitmap(bitmap);
                FotoTomada = true;
                habilitarBotones(true);
            } else {
                habilitarBotones(false);
                mostrarDialogoTomarFoto();
            }
        }
    }

    private void reproducirVideo() {
        if (FotoTomada) {
            try {
                if (Stopped) {
                    mediaPlayer.prepare();
                    Stopped = false;
                }
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mostrarDialogoTomarFoto();
        }
    }
}
