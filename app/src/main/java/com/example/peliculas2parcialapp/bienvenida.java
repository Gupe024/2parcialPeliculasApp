package com.example.peliculas2parcialapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

public class bienvenida extends AppCompatActivity {

    Button btnRegistro, btnUsuario, btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        btnRegistro = findViewById(R.id.btn_registro);
        btnUsuario = findViewById(R.id.btn_usuario);
        btnMenu = findViewById(R.id.btn_menu);

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MostrarDialogoRegistro();
            }
        });

        btnUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoUsuarios();
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ventana = new Intent(bienvenida.this, menu.class);
                startActivity(ventana);
            }
        });

        verificarRegistro();
    }

    private void verificarRegistro() {
        Set<String> usuarios = leerUsuarios();

        if (usuarios.isEmpty()) {
            btnUsuario.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);
        } else {
            btnUsuario.setVisibility(View.VISIBLE);
            btnMenu.setVisibility(View.VISIBLE);
        }
        btnRegistro.setVisibility(View.VISIBLE);
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
                    Set<String> usuarios = leerUsuarios();
                    usuarios.add(nombre);
                    guardarUsuarios(usuarios);
                    Toast.makeText(bienvenida.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    verificarRegistro();
                    dialogo.dismiss();
                }
            }
        });
    }

    private void mostrarDialogoUsuarios() {
        Set<String> usuarios = leerUsuarios();

        final String[] arrayUsuarios = usuarios.toArray(new String[0]);
        boolean[] checkedItems = new boolean[arrayUsuarios.length];
        final Set<String> usuariosAEliminar = new HashSet<>();

        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        constructor.setTitle("Seleccione Usuarios")
                .setMultiChoiceItems(arrayUsuarios, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        usuariosAEliminar.add(arrayUsuarios[which]);
                    } else {
                        usuariosAEliminar.remove(arrayUsuarios[which]);
                    }
                })
                .setPositiveButton("Eliminar", (dialog, id) -> {
                    eliminarUsuarios(usuariosAEliminar);
                    dialog.dismiss();
                })
                .setNeutralButton("Cambiar Usuario", (dialog, which) -> {
                    if (arrayUsuarios.length > 0) {
                        mostrarDialogoCambioUsuario(arrayUsuarios);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());

        constructor.create().show();
    }

    private void mostrarDialogoCambioUsuario(final String[] arrayUsuarios) {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        constructor.setTitle("Seleccione Usuario")
                .setItems(arrayUsuarios, (dialog, which) -> {
                    String usuarioSeleccionado = arrayUsuarios[which];
                    cambiarUsuario(usuarioSeleccionado);
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());

        constructor.create().show();
    }

    private void cambiarUsuario(String usuario) {
        try {
            FileOutputStream fos = openFileOutput("usuario_actual.txt", MODE_PRIVATE);
            BufferedWriter buffered = new BufferedWriter(new OutputStreamWriter(fos));
            buffered.write(usuario);
            buffered.close();
            Toast.makeText(this, "Has cambiado al usuario " + usuario, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.e("Ficheros", "Error al guardar el archivo " + ex);
        }
    }

    private void eliminarUsuarios(Set<String> usuariosAEliminar) {
        Set<String> usuarios = leerUsuarios();

        if (usuarios.isEmpty()) {
            Toast.makeText(this, "No hay usuarios para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarios.removeAll(usuariosAEliminar);
        guardarUsuarios(usuarios);

        if (usuarios.isEmpty()) {
            btnUsuario.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Usuarios eliminados", Toast.LENGTH_SHORT).show();
        verificarRegistro();
    }

    private Set<String> leerUsuarios() {
        Set<String> usuarios = new HashSet<>();
        try {
            BufferedReader buffered = new BufferedReader(new InputStreamReader(openFileInput("usuarios.txt")));
            String linea;
            while ((linea = buffered.readLine()) != null) {
                usuarios.add(linea);
            }
            buffered.close();
        } catch (Exception ex) {
            Log.e("Ficheros", "Error al leer el archivo " + ex);
        }
        return usuarios;
    }

    private void guardarUsuarios(Set<String> usuarios) {
        try {
            FileOutputStream fos = openFileOutput("usuarios.txt", MODE_PRIVATE);
            BufferedWriter buffered = new BufferedWriter(new OutputStreamWriter(fos));
            for (String usuario : usuarios) {
                buffered.write(usuario);
                buffered.newLine();
            }
            buffered.close();
        } catch (Exception ex) {
            Log.e("Ficheros", "Error al guardar el archivo " + ex);
        }
    }
}
