package com.example.deluvery.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.deluvery.MainActivity;
import com.example.deluvery.R;
import com.example.deluvery.models.Estudiante;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private ImageView imgProfile;
    private TextView tvCorreo;
    private EditText etNombre;
    private EditText etTelefono;
    private ProgressBar progressBar;
    private Button btnGuardar;
    private Button btnCerrarSesion;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String estudianteId;
    private Estudiante estudianteActual;
    private Uri cameraImageUri;

    // Flag para saber si el documento existe en Firestore
    private boolean documentoExiste = false;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Obtener usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            irALogin();
            return;
        }

        estudianteId = currentUser.getUid();

        // Inicializar vistas
        inicializarVistas();

        // Inicializar launchers
        inicializarLaunchers();

        // Configurar botones
        configurarBotones();

        // Cargar datos del perfil
        cargarPerfil();
    }

    private void inicializarVistas() {
        imgProfile = findViewById(R.id.img_profile);
        tvCorreo = findViewById(R.id.tv_correo);
        etNombre = findViewById(R.id.et_nombre);
        etTelefono = findViewById(R.id.et_telefono);
        progressBar = findViewById(R.id.progress_bar);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
    }

    private void inicializarLaunchers() {
        // Launcher para la camara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (cameraImageUri != null) {
                            uploadImageToFirebase(cameraImageUri);
                        } else {
                            Toast.makeText(this, "Error al capturar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Launcher para la galeria
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        } else {
                            Toast.makeText(this, "Error al seleccionar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Launcher para permiso de camara
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permiso de camara denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher para permiso de almacenamiento
        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void configurarBotones() {
        // Boton para editar foto
        findViewById(R.id.btn_edit_photo).setOnClickListener(v -> mostrarOpcionesFoto());

        // Click en la imagen tambien abre opciones
        imgProfile.setOnClickListener(v -> mostrarOpcionesFoto());

        // Boton guardar
        btnGuardar.setOnClickListener(v -> guardarCambios());

        // Boton cerrar sesion
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void mostrarOpcionesFoto() {
        String[] opciones = {"Tomar foto", "Seleccionar de galeria", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle("Cambiar foto de perfil")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            verificarPermisoCamara();
                            break;
                        case 1:
                            verificarPermisoGaleria();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void verificarPermisoGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openCamera() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "profile_" + System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DESCRIPTION, "Foto de perfil");

            cameraImageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );

            if (cameraImageUri == null) {
                Toast.makeText(this, "Error al crear URI para la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "No hay aplicacion de camara disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir camara", e);
            Toast.makeText(this, "Error al abrir la camara", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            String filename = "profile_" + estudianteId + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageRef.child(filename);

            imageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            progressBar.setVisibility(android.view.View.GONE);
                            updateProfileImage(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        Log.e(TAG, "Error al subir imagen", e);
                        Toast.makeText(this, "Error al subir imagen: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

        } catch (IOException e) {
            progressBar.setVisibility(android.view.View.GONE);
            Log.e(TAG, "Error al procesar imagen", e);
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * METODO CORREGIDO: Actualiza la foto de perfil en Firestore.
     *
     * PROBLEMA ORIGINAL: Usaba .update() que requiere que el documento exista.
     * Si el documento no existia, lanzaba NOT_FOUND.
     *
     * SOLUCION: Usar .set() con SetOptions.merge() que:
     * - Crea el documento si no existe
     * - Actualiza solo los campos especificados si ya existe
     * - Preserva los demas campos existentes
     */
    private void updateProfileImage(String imageUrl) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        // Asegurarse de que estudianteActual tenga todos los datos necesarios
        if (estudianteActual == null) {
            estudianteActual = new Estudiante();
            estudianteActual.setId(estudianteId);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                estudianteActual.setCorreo(currentUser.getEmail());
                estudianteActual.setNombre(currentUser.getDisplayName() != null ?
                        currentUser.getDisplayName() : "");
            }
            estudianteActual.setRol("cliente");
            estudianteActual.setActivo(true);
        }

        // Actualizar la URL de la foto en el objeto local
        estudianteActual.setFotoURL(imageUrl);

        // SOLUCION: Usar set() con merge() en lugar de update()
        // Esto crea el documento si no existe, o actualiza si ya existe
        db.collection("estudiantes")
                .document(estudianteId)
                .set(estudianteActual, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    documentoExiste = true; // Ahora sabemos que existe
                    cargarImagenPerfil(imageUrl);
                    Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Error al actualizar foto en base de datos",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al actualizar foto de perfil", e);
                });
    }

    private void cargarImagenPerfil(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop())
                    .into(imgProfile);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile);
        }
    }

    private void cargarPerfil() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        // Mostrar correo del usuario
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            tvCorreo.setText(currentUser.getEmail());
        }

        db.collection("estudiantes")
                .document(estudianteId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        documentoExiste = true;
                        estudianteActual = task.getResult().toObject(Estudiante.class);
                        if (estudianteActual != null) {
                            mostrarDatosEstudiante(estudianteActual);
                        }
                    } else {
                        // Si no existe el documento, crear uno nuevo en memoria
                        documentoExiste = false;
                        estudianteActual = new Estudiante();
                        estudianteActual.setId(estudianteId);
                        if (currentUser != null) {
                            estudianteActual.setCorreo(currentUser.getEmail());
                            estudianteActual.setNombre(currentUser.getDisplayName() != null ?
                                    currentUser.getDisplayName() : "");
                        }
                        estudianteActual.setRol("cliente");
                        estudianteActual.setActivo(true);

                        // Mostrar datos por defecto
                        mostrarDatosEstudiante(estudianteActual);
                    }
                });
    }

    private void mostrarDatosEstudiante(Estudiante estudiante) {
        etNombre.setText(estudiante.getNombre() != null ? estudiante.getNombre() : "");
        etTelefono.setText(estudiante.getTelefono() != null ? estudiante.getTelefono() : "");
        cargarImagenPerfil(estudiante.getFotoURL());
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Guardar cambios")
                .setMessage("¿Estas seguro de que deseas guardar los cambios?")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    if (estudianteActual == null) {
                        estudianteActual = new Estudiante();
                        estudianteActual.setId(estudianteId);
                        estudianteActual.setCorreo(mAuth.getCurrentUser() != null ?
                                mAuth.getCurrentUser().getEmail() : "");
                        estudianteActual.setRol("cliente");
                    }

                    estudianteActual.setNombre(nombre);
                    estudianteActual.setTelefono(telefono);
                    estudianteActual.setActivo(true);

                    progressBar.setVisibility(android.view.View.VISIBLE);

                    // Usar set() en lugar de update() para manejar ambos casos
                    db.collection("estudiantes")
                            .document(estudianteId)
                            .set(estudianteActual)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                documentoExiste = true;
                                Toast.makeText(this, "Perfil actualizado exitosamente",
                                        Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                Toast.makeText(this, "Error al guardar cambios: " +
                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesion")
                .setMessage("¿Estas seguro de que deseas cerrar sesion?")
                .setPositiveButton("Si", (dialog, which) -> {
                    mAuth.signOut();
                    irALogin();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}