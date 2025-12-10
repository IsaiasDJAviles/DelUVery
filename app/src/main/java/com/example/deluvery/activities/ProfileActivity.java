package com.example.deluvery.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private ImageView imgProfile;
    private TextView tvCorreo;
    private EditText etNombre;
    private EditText etTelefono;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private String estudianteId;
    private Estudiante estudianteActual;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ocultar ActionBar
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
            // Usuario no autenticado, redirigir al login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        estudianteId = currentUser.getUid();

        // Inicializar vistas
        imgProfile = findViewById(R.id.img_profile);
        tvCorreo = findViewById(R.id.tv_correo);
        etNombre = findViewById(R.id.et_nombre);
        etTelefono = findViewById(R.id.et_telefono);

        // Configurar botón de guardar
        Button btnGuardar = findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(v -> guardarCambios());

        // Configurar botón de editar foto
        findViewById(R.id.btn_edit_photo).setOnClickListener(v -> showImagePickerDialog());

        // Cargar datos del perfil
        cargarPerfil();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            // Para versiones anteriores a Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
            } else {
                Toast.makeText(this, "Se requieren los permisos para cambiar la foto de perfil", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                // La imagen se capturó con la cámara
                Bundle extras = data.getExtras();
                if (extras != null && extras.get("data") != null) {
                    // Obtener la imagen como bitmap
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    // Convertir a URI
                    String path = MediaStore.Images.Media.insertImage(
                            getContentResolver(),
                            imageBitmap,
                            "profile_" + System.currentTimeMillis(),
                            null
                    );
                    if (path != null) {
                        uploadImageToFirebase(Uri.parse(path));
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                // La imagen se seleccionó de la galería
                imageUri = data.getData();
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Error: URI de imagen no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar diálogo de carga
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_loading);
        AlertDialog dialog = builder.create();
        dialog.setMessage("Subiendo imagen...");
        dialog.setCancelable(false);
        dialog.show();

        try {
            // Crear referencia al archivo en Firebase Storage
            String filename = "profile_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference fileRef = storageRef.child(estudianteId).child(filename);

            // Subir el archivo
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtener la URL de descarga
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Actualizar la URL de la imagen en Firestore
                            updateProfileImage(uri.toString());
                            dialog.dismiss();
                        }).addOnFailureListener(e -> {
                            dialog.dismiss();
                            Log.e(TAG, "Error al obtener URL de descarga: " + e.getMessage(), e);
                            Toast.makeText(this, "Error al obtener URL de la imagen", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Log.e(TAG, "Error al subir la imagen: " + e.getMessage(), e);
                        Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        // Mostrar progreso de la carga
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        dialog.setMessage("Subiendo: " + (int) progress + "%");
                    });
        } catch (Exception e) {
            dialog.dismiss();
            Log.e(TAG, "Error inesperado: " + e.getMessage(), e);
            Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateProfileImage(String imageUrl) {
        db.collection("estudiantes")
                .document(estudianteId)
                .update("fotoURL", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar la imagen en la interfaz
                    if (estudianteActual != null) {
                        estudianteActual.setFotoURL(imageUrl);
                        cargarImagenPerfil(imageUrl);
                    }
                    Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar la foto de perfil", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al actualizar la foto de perfil", e);
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
        db.collection("estudiantes")
                .document(estudianteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Convertir el documento a objeto Estudiante
                            estudianteActual = document.toObject(Estudiante.class);
                            if (estudianteActual != null) {
                                mostrarDatos(estudianteActual);
                            }
                        } else {
                            // Si no existe el perfil, crear uno nuevo
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

            // Guardar el nuevo perfil
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
        // Mostrar correo electrónico
        tvCorreo.setText(estudiante.getCorreo() != null ? estudiante.getCorreo() : "");

        // Llenar campos editables
        etNombre.setText(estudiante.getNombre() != null ? estudiante.getNombre() : "");
        etTelefono.setText(estudiante.getTelefono() != null ? estudiante.getTelefono() : "");

        // Cargar imagen de perfil
        cargarImagenPerfil(estudiante.getFotoURL());
    }

    private void guardarCambios() {
        // Validar campos
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }
        
        // Mostrar diálogo de confirmación
        new AlertDialog.Builder(this)
                .setTitle("Guardar cambios")
                .setMessage("¿Estás seguro de que deseas guardar los cambios?")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // Actualizar datos del estudiante
                    if (estudianteActual == null) {
                        estudianteActual = new Estudiante();
                        estudianteActual.setId(estudianteId);
                        estudianteActual.setCorreo(mAuth.getCurrentUser() != null ? 
                                mAuth.getCurrentUser().getEmail() : "");
                    }
                    
                    estudianteActual.setNombre(nombre);
                    estudianteActual.setTelefono(telefono);
                    estudianteActual.setActivo(true);
                    
                    // Guardar en Firestore
                    db.collection("estudiantes")
                            .document(estudianteId)
                            .set(estudianteActual)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Perfil actualizado exitosamente", 
                                        Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al guardar cambios: " + 
                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}