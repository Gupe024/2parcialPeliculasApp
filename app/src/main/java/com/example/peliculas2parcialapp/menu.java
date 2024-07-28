package com.example.peliculas2parcialapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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

        mostrarUsuarioEdad();

        btnCaricaturas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana = new Intent(menu.this, reproductor_de_video.class);
                startActivity(ventana);

            }
        });

        btnAccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana2 = new Intent(menu.this, reproductor_de_video.class);
                startActivity(ventana2);

            }
        });

        btnTerror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana3 = new Intent(menu.this, reproductor_de_video.class);
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
                        btnCaricaturas.setVisibility(View.GONE);
                        textCaricaturas.setVisibility(View.GONE);
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
                }
            } else {
                textViewSaludo.setText("No hay usuario registrado");
            }
        } catch (Exception ex) {
            textViewSaludo.setText("Error al leer los datos del usuario: " + ex.getMessage());
        }
    }
}
