package com.example.peliculas2parcialapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class bienvenida extends AppCompatActivity {

    private static final String PREFERENCIAS_ARCHIVO = "DatosRegistro";
    private static final String NOMBRE_CLAVE = "nombre";
    private static final String EDAD_CLAVE = "edad";
    private static final String GENERO_CLAVE = "genero";

    Button btnRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        btnRegistro = findViewById(R.id.btn_registro);

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MostrarDialogoRegistro();
            }
        });
        leerDatosRegistro();
    }

    private void MostrarDialogoRegistro() {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View vistaDialogo = inflater.inflate(R.layout.registro, null);
        constructor.setView(vistaDialogo);

        final EditText campoDeNombre = vistaDialogo.findViewById(R.id.nombre);
        final RadioGroup grupoDeEdades = vistaDialogo.findViewById(R.id.rango_edades);
        final Spinner spinnerGenero = vistaDialogo.findViewById(R.id.spinner_genero);

        ArrayAdapter<CharSequence> adaptador = ArrayAdapter.createFromResource(this, R.array.genero_array, android.R.layout.simple_spinner_item);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adaptador);

        constructor.setTitle(R.string.registro)
                .setPositiveButton(R.string.guardar, null)
                .setNegativeButton(R.string.cancelar, (dialog, id) -> dialog.dismiss());

        AlertDialog dialogo = constructor.create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = campoDeNombre.getText().toString();
                int edadSeleccionadaId = grupoDeEdades.getCheckedRadioButtonId();
                String edad = "";
                if (edadSeleccionadaId == R.id.rg_menor12) {
                    edad = getString(R.string.text_menor12);
                } else if (edadSeleccionadaId == R.id.rg_menor18) {
                    edad = getString(R.string.text_menor18);
                } else if (edadSeleccionadaId == R.id.rg_mayor18) {
                    edad = getString(R.string.text_mayor18);
                }
                String genero = spinnerGenero.getSelectedItem().toString();

                if (nombre.isEmpty() || edadSeleccionadaId == -1 || genero.isEmpty()) {
                    Toast.makeText(bienvenida.this, "Necesita llenar todos los campos para guardar", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences preferencias = getSharedPreferences(PREFERENCIAS_ARCHIVO, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferencias.edit();
                    editor.putString(NOMBRE_CLAVE, nombre);
                    editor.putString(EDAD_CLAVE, edad);
                    editor.putString(GENERO_CLAVE, genero);
                    editor.apply();
                    Toast.makeText(bienvenida.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    dialogo.dismiss();
                }
            }
        });
    }

    private void leerDatosRegistro() {
        SharedPreferences preferencias = getSharedPreferences(PREFERENCIAS_ARCHIVO, MODE_PRIVATE);
        String nombre = preferencias.getString(NOMBRE_CLAVE, "No definido");
        String edad = preferencias.getString(EDAD_CLAVE, "No definido");
        String genero = preferencias.getString(GENERO_CLAVE, "No definido");
    }
}
