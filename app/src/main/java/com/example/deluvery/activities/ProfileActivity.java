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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.deluvery.R;
import com.example.deluvery.models.Estudiante;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String estudianteId;
    private Estudiante estudianteActual;
    private Uri cameraImageUri;

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
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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
    }

    private void inicializarLaunchers() {
        // Launcher para la cámara
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

        // Launcher para la galería
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

        // Launcher para permisos de cámara
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher para permisos de almacenamiento
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
        Button btnGuardar = findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(v -> guardarCambios());

        findViewById(R.id.btn_edit_photo).setOnClickListener(v -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        final CharSequence[] options = {"Tomar foto", "Elegir de la galería", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elegir opción");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Tomar foto")) {
                checkCameraPermission();
            } else if (options[item].equals("Elegir de la galería")) {
                checkStoragePermission();
            } else if (options[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermission() {
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
            // Crear valores para la imagen
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "profile_" + System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DESCRIPTION, "Foto de perfil");

            // Crear URI para guardar la imagen
            cameraImageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );

            if (cameraImageUri == null) {
                Toast.makeText(this, "Error al crear URI para la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear intent para la cámara
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir cámara: " + e.getMessage(), e);
            Toast.makeText(this, "Error al abrir cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Error: URI de imagen no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progress bar
        progressBar.setVisibility(android.view.View.VISIBLE);

        try {
            // Verificar que Firebase Storage esté inicializado
            FirebaseStorage storage = FirebaseStorage.getInstance();
            String bucketUrl = storage.getReference().getBucket();
            Log.d(TAG, "Storage bucket: " + bucketUrl);

            if (bucketUrl == null || bucketUrl.isEmpty()) {
                progressBar.setVisibility(android.view.View.GONE);
                mostrarDialogoConfiguracionStorage();
                return;
            }

            // Cargar la imagen como bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Redimensionar si es muy grande
            int maxSize = 1024;
            if (bitmap.getWidth() > maxSize || bitmap.getHeight() > maxSize) {
                float scale = Math.min(
                        ((float) maxSize / bitmap.getWidth()),
                        ((float) maxSize / bitmap.getHeight())
                );
                int newWidth = Math.round(bitmap.getWidth() * scale);
                int newHeight = Math.round(bitmap.getHeight() * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            // Comprimir el bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            Log.d(TAG, "Tamaño de imagen comprimida: " + data.length + " bytes");

            // Crear referencia en Firebase Storage con estructura correcta
            String filename = "profile_" + System.currentTimeMillis() + ".jpg";
            StorageReference fileRef = storage.getReference()
                    .child("profile_images")
                    .child(estudianteId)
                    .child(filename);

            Log.d(TAG, "Ruta de subida: " + fileRef.getPath());

            // Subir el archivo
            fileRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "Imagen subida exitosamente");
                        // Obtener la URL de descarga
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Log.d(TAG, "URL obtenida: " + uri.toString());
                                    updateProfileImage(uri.toString());
                                    progressBar.setVisibility(android.view.View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(android.view.View.GONE);
                                    Log.e(TAG, "Error al obtener URL: " + e.getMessage(), e);
                                    Toast.makeText(this, "Error al obtener URL de la imagen",
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(android.view.View.GONE);
                        Log.e(TAG, "Error detallado al subir imagen: " + e.getClass().getName(), e);
                        Log.e(TAG, "Mensaje: " + e.getMessage());

                        String errorMsg = "Error al subir imagen";
                        if (e.getMessage() != null) {
                            if (e.getMessage().contains("404") || e.getMessage().contains("Not Found")) {
                                errorMsg = "Firebase Storage no está configurado. Por favor, habilita Storage en Firebase Console.";
                                mostrarDialogoConfiguracionStorage();
                            } else if (e.getMessage().contains("Permission denied")) {
                                errorMsg = "Permisos de Storage insuficientes. Verifica las reglas de seguridad.";
                            } else {
                                errorMsg = e.getMessage();
                            }
                        }

                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "Progreso de subida: " + (int) progress + "%");
                    });

        } catch (IOException e) {
            progressBar.setVisibility(android.view.View.GONE);
            Log.e(TAG, "Error al procesar imagen: " + e.getMessage(), e);
            Toast.makeText(this, "Error al procesar la imagen: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            progressBar.setVisibility(android.view.View.GONE);
            Log.e(TAG, "Error inesperado: " + e.getMessage(), e);
            Toast.makeText(this, "Error inesperado: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarDialogoConfiguracionStorage() {
        new AlertDialog.Builder(this)
                .setTitle("Firebase Storage no configurado")
                .setMessage("Para usar esta función debes:\n\n" +
                        "1. Ir a Firebase Console\n" +
                        "2. Seleccionar tu proyecto\n" +
                        "3. Habilitar Storage\n" +
                        "4. Configurar reglas de seguridad\n\n" +
                        "¿Deseas continuar sin foto de perfil?")
                .setPositiveButton("Continuar", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Salir", (dialog, which) -> finish())
                .show();
    }

    private void updateProfileImage(String imageUrl) {
        db.collection("estudiantes")
                .document(estudianteId)
                .update("fotoURL", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    if (estudianteActual != null) {
                        estudianteActual.setFotoURL(imageUrl);
                        cargarImagenPerfil(imageUrl);
                    }
                    Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
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

        db.collection("estudiantes")
                .document(estudianteId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(android.view.View.GONE);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            estudianteActual = document.toObject(Estudiante.class);
                            if (estudianteActual != null) {
                                mostrarDatos(estudianteActual);
                            }
                        } else {
                            crearNuevoPerfil();
                        }
                    } else {
                        Toast.makeText(this, "Error al cargar perfil: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearNuevoPerfil() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            estudianteActual = new Estudiante();
            estudianteActual.setId(user.getUid());
            estudianteActual.setCorreo(user.getEmail() != null ? user.getEmail() : "");
            estudianteActual.setNombre(user.getDisplayName() != null ? user.getDisplayName() : "");
            estudianteActual.setActivo(true);
            estudianteActual.setRol("cliente");

            db.collection("estudiantes")
                    .document(estudianteActual.getId())
                    .set(estudianteActual)
                    .addOnSuccessListener(aVoid -> {
                        mostrarDatos(estudianteActual);
                        Toast.makeText(this, "Perfil creado exitosamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al crear perfil: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void mostrarDatos(@NonNull Estudiante estudiante) {
        tvCorreo.setText(estudiante.getCorreo() != null ? estudiante.getCorreo() : "");
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
                .setMessage("¿Estás seguro de que deseas guardar los cambios?")
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

                    db.collection("estudiantes")
                            .document(estudianteId)
                            .set(estudianteActual)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(android.view.View.GONE);
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
}