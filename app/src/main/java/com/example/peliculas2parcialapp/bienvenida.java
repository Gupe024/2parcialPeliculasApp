package com.example.peliculas2parcialapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

public class bienvenida extends AppCompatActivity {

    Button btnRegistro;
    Button btnUsuario;
    Button btnMenu;

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
        crearArchivoFoto();
        crearArchivoCategoria();
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

    private void crearArchivoFoto() {
        try {
            FileOutputStream fos = openFileOutput("foto_usuario.png", MODE_PRIVATE);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void crearArchivoCategoria() {
        try {
            FileOutputStream fos = openFileOutput("categoria_seleccionada.txt", MODE_PRIVATE);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void MostrarDialogoRegistro() {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View vistaDialogo = inflater.inflate(R.layout.registro, null);
        constructor.setView(vistaDialogo);

        final EditText campoDeNombre = vistaDialogo.findViewById(R.id.nombre);
        final Spinner spinnerRangoEdades = vistaDialogo.findViewById(R.id.spinner_rango_edades);
        final EditText edadAdicional = vistaDialogo.findViewById(R.id.edad_adicional);
        final Spinner spinnerGenero = vistaDialogo.findViewById(R.id.spinner_genero);

        ArrayAdapter<CharSequence> adaptadorEdad = ArrayAdapter.createFromResource(this, R.array.rango_edad, android.R.layout.simple_spinner_item);
        adaptadorEdad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRangoEdades.setAdapter(adaptadorEdad);

        ArrayAdapter<CharSequence> adaptadorGenero = ArrayAdapter.createFromResource(this, R.array.genero_array, android.R.layout.simple_spinner_item);
        adaptadorGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adaptadorGenero);

        spinnerRangoEdades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == spinnerRangoEdades.getCount() - 1) {
                    edadAdicional.setVisibility(View.VISIBLE);
                } else {
                    edadAdicional.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        constructor.setTitle(R.string.registro)
                .setPositiveButton(R.string.guardar, null)
                .setNegativeButton(R.string.cancelar, (dialog, id) -> dialog.dismiss());

        AlertDialog dialogo = constructor.create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = campoDeNombre.getText().toString();
                String rangoEdad = spinnerRangoEdades.getSelectedItem().toString();
                String edad = (edadAdicional.getVisibility() == View.VISIBLE) ? edadAdicional.getText().toString() : rangoEdad;
                String genero = spinnerGenero.getSelectedItem().toString();

                if (nombre.isEmpty() || genero.isEmpty() || (edadAdicional.getVisibility() == View.VISIBLE && edad.isEmpty())) {
                    Toast.makeText(bienvenida.this, "Necesita llenar todos los campos para guardar", Toast.LENGTH_SHORT).show();
                } else {
                    String usuarioData = nombre + "|" + edad + "|" + genero;
                    Set<String> usuarios = leerUsuarios();
                    usuarios.add(usuarioData);
                    guardarUsuarios(usuarios);
                    Toast.makeText(bienvenida.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    verificarRegistro();
                    mostrarContenidoArchivo("usuarios.txt");
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
                    mostrarContenidoArchivo("usuario_actual.txt");
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
            Toast.makeText(this, "Error al guardar el archivo: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarUsuarios(Set<String> usuariosAEliminar) {
        Set<String> usuarios = leerUsuarios();

        if (usuarios.isEmpty()) {
            Toast.makeText(this, "No hay usuarios para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String usuario : usuariosAEliminar) {
            usuarios.remove(usuario);
            eliminarFotoUsuario(usuario);
            eliminarUsuarioActualSiCoincide(usuario);
        }

        guardarUsuarios(usuarios);

        if (usuarios.isEmpty()) {
            btnUsuario.setVisibility(View.GONE);
            btnMenu.setVisibility(View.GONE);
        }

        Toast.makeText(this, "Usuarios eliminados correctamente", Toast.LENGTH_SHORT).show();
    }

    private void eliminarFotoUsuario(String usuario) {
        try {
            File fotoFile = new File(getFilesDir(), usuario + "_foto.png");
            if (fotoFile.exists() && fotoFile.delete()) {
                Toast.makeText(this, "Foto eliminada: " + usuario + "_foto.png", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eliminarUsuarioActualSiCoincide(String usuarioEliminado) {
        try {
            File usuarioActualFile = new File(getFilesDir(), "usuario_actual.txt");
            if (usuarioActualFile.exists()) {
                BufferedReader buffered = new BufferedReader(new InputStreamReader(new FileInputStream(usuarioActualFile)));
                String usuarioActual = buffered.readLine();
                buffered.close();
                if (usuarioActual != null && usuarioActual.equals(usuarioEliminado)) {
                    if (usuarioActualFile.delete()) {
                        Toast.makeText(this, "El usuario actual ha sido eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el archivo de usuario actual", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Set<String> leerUsuarios() {
        Set<String> usuarios = new HashSet<>();
        try {
            FileInputStream fis = openFileInput("usuarios.txt");
            BufferedReader buffered = new BufferedReader(new InputStreamReader(fis));
            String linea;
            while ((linea = buffered.readLine()) != null) {
                usuarios.add(linea);
            }
            buffered.close();
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarContenidoArchivo(String fileName) {
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader buffered = new BufferedReader(new InputStreamReader(fis));
            StringBuilder contenido = new StringBuilder();
            String linea;
            while ((linea = buffered.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
            buffered.close();
            Toast.makeText(this, contenido.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

