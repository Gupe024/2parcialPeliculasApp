package com.example.peliculas2parcialapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class menu extends AppCompatActivity {

    TextView textViewSaludo;
    ImageButton btnCaricaturas;
    TextView textCaricaturas;
    ImageButton btnAccion;
    TextView textAccion;
    ImageButton btnTerror;
    TextView textTerror;
    Button btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        textViewSaludo = findViewById(R.id.textViewSaludo);
        btnCaricaturas = findViewById(R.id.btn_caricaturas);
        btnAccion = findViewById(R.id.btn_accion);
        btnTerror = findViewById(R.id.btn_terror);
        btnRegresar = findViewById(R.id.btnregresar);
        textCaricaturas = findViewById(R.id.etiqueta_caricaturas);
        textAccion = findViewById(R.id.etiqueta_accion);
        textTerror = findViewById(R.id.etiqueta_terror);

        verificarUsuarios();

        btnCaricaturas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana = new Intent(menu.this, reproductor_de_video.class);
                ventana.putExtra("categoria", "caricaturas");
                startActivity(ventana);
            }
        });

        btnAccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana2 = new Intent(menu.this, reproductor_de_video.class);
                ventana2.putExtra("categoria", "accion");
                startActivity(ventana2);
            }
        });

        btnTerror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana3 = new Intent(menu.this, reproductor_de_video.class);
                ventana3.putExtra("categoria", "terror");
                startActivity(ventana3);
            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mostrarUsuarioEdad();
    }

    private void verificarUsuarios() {
        List<String> usuarios = obtenerUsuarios();
        if (usuarios.isEmpty()) {
            textViewSaludo.setText("No hay usuarios registrados");
            ocultarBotonesCategorias();
        } else if (usuarios.size() == 1) {
            // Guardar el único usuario como el usuario actual
            guardarUsuarioActual(usuarios.get(0));
            mostrarUsuarioEdad();
        } else {
            mostrarDialogoSeleccionarUsuario(usuarios);
        }
    }

    private List<String> obtenerUsuarios() {
        List<String> usuarios = new ArrayList<>();
        File usuariosFile = new File(getFilesDir(), "usuarios.txt");
        if (usuariosFile.exists()) {
            try {
                BufferedReader buffered = new BufferedReader(new InputStreamReader(new FileInputStream(usuariosFile)));
                String linea;
                while ((linea = buffered.readLine()) != null) {
                    usuarios.add(linea);
                }
                buffered.close();
            } catch (Exception ex) {
            }
        }
        return usuarios;
    }

    private void guardarUsuarioActual(String usuarioData) {
        try {
            FileOutputStream fos = openFileOutput("usuario_actual.txt", MODE_PRIVATE);
            fos.write(usuarioData.getBytes());
            fos.close();
        } catch (Exception ex) {
        }
    }

    private void mostrarUsuarioEdad() {
        File usuarioFile = new File(getFilesDir(), "usuario_actual.txt");
        if (usuarioFile.exists()) {
            try {
                BufferedReader buffered = new BufferedReader(new InputStreamReader(openFileInput("usuario_actual.txt")));
                String usuarioData = buffered.readLine();
                buffered.close();

                if (usuarioData != null) {
                    String[] partes = usuarioData.split("\\|");
                    if (partes.length >= 2) {
                        String nombre = partes[0];
                        String edad = partes[1];
                        int edadEntero = Integer.parseInt(edad);
                        textViewSaludo.setText("Hola " + nombre + ", de acuerdo a tu edad (" + edad + ") tienes disponible:");

                        if (edadEntero < 12) {
                            btnCaricaturas.setVisibility(View.VISIBLE);
                            textCaricaturas.setVisibility(View.VISIBLE);
                            btnAccion.setVisibility(View.GONE);
                            textAccion.setVisibility(View.GONE);
                            btnTerror.setVisibility(View.GONE);
                            textTerror.setVisibility(View.GONE);
                        } else if (edadEntero < 18) {
                            btnCaricaturas.setVisibility(View.VISIBLE);
                            textCaricaturas.setVisibility(View.VISIBLE);
                            btnAccion.setVisibility(View.VISIBLE);
                            textAccion.setVisibility(View.VISIBLE);
                            btnTerror.setVisibility(View.GONE);
                            textTerror.setVisibility(View.GONE);
                        } else {
                            btnCaricaturas.setVisibility(View.VISIBLE);
                            textCaricaturas.setVisibility(View.VISIBLE);
                            btnAccion.setVisibility(View.VISIBLE);
                            textAccion.setVisibility(View.VISIBLE);
                            btnTerror.setVisibility(View.VISIBLE);
                            textTerror.setVisibility(View.VISIBLE);
                        }
                    } else {
                        textViewSaludo.setText("Datos de usuario no válidos");
                        ocultarBotonesCategorias();
                    }
                } else {
                    textViewSaludo.setText("No hay usuario registrado");
                    ocultarBotonesCategorias();
                }
            } catch (Exception ex) {
                textViewSaludo.setText("Error al leer los datos del usuario: " + ex.getMessage());
                ocultarBotonesCategorias();
            }
        } else {
            textViewSaludo.setText("No hay usuario registrado");
            ocultarBotonesCategorias();
        }
    }

    private void mostrarDialogoSeleccionarUsuario(List<String> usuarios) {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        constructor.setTitle("Seleccione Usuario")
                .setItems(usuarios.toArray(new String[0]), (dialog, which) -> {
                    String usuarioSeleccionado = usuarios.get(which);
                    guardarUsuarioActual(usuarioSeleccionado);
                    mostrarUsuarioEdad();
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());

        constructor.create().show();
    }

    private void ocultarBotonesCategorias() {
        btnCaricaturas.setVisibility(View.GONE);
        textCaricaturas.setVisibility(View.GONE);
        btnAccion.setVisibility(View.GONE);
        textAccion.setVisibility(View.GONE);
        btnTerror.setVisibility(View.GONE);
        textTerror.setVisibility(View.GONE);
    }
}
