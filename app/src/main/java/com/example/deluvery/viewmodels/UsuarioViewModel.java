package com.example.deluvery.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.deluvery.models.Estudiante;
import com.example.deluvery.repositories.EstudianteRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UsuarioViewModel extends ViewModel {

    private static final String TAG = "UsuarioViewModel";

    private final EstudianteRepository repository;
    private final FirebaseAuth mAuth;

    private final MutableLiveData<Estudiante> usuarioActualLiveData;
    private final MutableLiveData<Boolean> cargandoLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<String> mensajeExitoLiveData;
    private final MutableLiveData<Boolean> sesionCerradaLiveData;

    public UsuarioViewModel() {
        repository = new EstudianteRepository();
        mAuth = FirebaseAuth.getInstance();
        usuarioActualLiveData = new MutableLiveData<>();
        cargandoLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();
        mensajeExitoLiveData = new MutableLiveData<>();
        sesionCerradaLiveData = new MutableLiveData<>(false);
    }

    public LiveData<Estudiante> getUsuarioActual() {
        return usuarioActualLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getMensajeExito() {
        return mensajeExitoLiveData;
    }

    public LiveData<Boolean> getSesionCerrada() {
        return sesionCerradaLiveData;
    }

    public void cargarUsuarioActual() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("No hay sesión activa");
            return;
        }

        cargandoLiveData.setValue(true);
        String userId = currentUser.getUid();

        repository.obtenerEstudiantePorId(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    Estudiante estudiante = EstudianteRepository.documentToEstudiante(documentSnapshot);

                    if (estudiante == null) {
                        estudiante = new Estudiante(
                                userId,
                                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario",
                                currentUser.getEmail() != null ? currentUser.getEmail() : "",
                                "cliente",
                                "",
                                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "",
                                true
                        );
                        Estudiante finalEstudiante = estudiante;
                        repository.crearEstudiante(estudiante)
                                .addOnSuccessListener(aVoid -> {
                                    usuarioActualLiveData.setValue(finalEstudiante);
                                    cargandoLiveData.setValue(false);
                                })
                                .addOnFailureListener(e -> {
                                    errorLiveData.setValue("Error al crear perfil: " + e.getMessage());
                                    cargandoLiveData.setValue(false);
                                });
                    } else {
                        usuarioActualLiveData.setValue(estudiante);
                        cargandoLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cargar usuario: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cargar usuario", e);
                });
    }

    public void actualizarNombre(String nuevoNombre) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("No hay sesión activa");
            return;
        }

        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            errorLiveData.setValue("El nombre no puede estar vacío");
            return;
        }

        cargandoLiveData.setValue(true);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nuevoNombre.trim())
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    Estudiante estudiante = usuarioActualLiveData.getValue();
                    if (estudiante != null) {
                        estudiante.setNombre(nuevoNombre.trim());
                        repository.actualizarEstudiante(estudiante)
                                .addOnSuccessListener(aVoid2 -> {
                                    usuarioActualLiveData.setValue(estudiante);
                                    mensajeExitoLiveData.setValue("Nombre actualizado");
                                    cargandoLiveData.setValue(false);
                                })
                                .addOnFailureListener(e -> {
                                    errorLiveData.setValue("Error al guardar nombre: " + e.getMessage());
                                    cargandoLiveData.setValue(false);
                                });
                    } else {
                        cargandoLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar nombre: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al actualizar nombre", e);
                });
    }

    public void actualizarTelefono(String nuevoTelefono) {
        Estudiante estudiante = usuarioActualLiveData.getValue();
        if (estudiante == null) {
            errorLiveData.setValue("No hay usuario cargado");
            return;
        }

        if (nuevoTelefono != null && !nuevoTelefono.trim().isEmpty()) {
            if (!nuevoTelefono.matches("\\d{10}")) {
                errorLiveData.setValue("El teléfono debe tener 10 dígitos");
                return;
            }
        }

        cargandoLiveData.setValue(true);
        estudiante.setTelefono(nuevoTelefono != null ? nuevoTelefono.trim() : "");

        repository.actualizarEstudiante(estudiante)
                .addOnSuccessListener(aVoid -> {
                    usuarioActualLiveData.setValue(estudiante);
                    mensajeExitoLiveData.setValue("Teléfono actualizado");
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar teléfono: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al actualizar teléfono", e);
                });
    }

    public void actualizarCorreo(String nuevoCorreo) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("No hay sesión activa");
            return;
        }

        if (nuevoCorreo == null || nuevoCorreo.trim().isEmpty()) {
            errorLiveData.setValue("El correo no puede estar vacío");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
            errorLiveData.setValue("Correo electrónico inválido");
            return;
        }

        cargandoLiveData.setValue(true);

        currentUser.updateEmail(nuevoCorreo.trim())
                .addOnSuccessListener(aVoid -> {
                    Estudiante estudiante = usuarioActualLiveData.getValue();
                    if (estudiante != null) {
                        estudiante.setCorreo(nuevoCorreo.trim());
                        repository.actualizarEstudiante(estudiante)
                                .addOnSuccessListener(aVoid2 -> {
                                    usuarioActualLiveData.setValue(estudiante);
                                    mensajeExitoLiveData.setValue("Correo actualizado");
                                    cargandoLiveData.setValue(false);
                                })
                                .addOnFailureListener(e -> {
                                    errorLiveData.setValue("Error al guardar correo: " + e.getMessage());
                                    cargandoLiveData.setValue(false);
                                });
                    } else {
                        cargandoLiveData.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar correo: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al actualizar correo", e);
                });
    }

    public void actualizarFotoURL(String nuevaFotoURL) {
        Estudiante estudiante = usuarioActualLiveData.getValue();
        if (estudiante == null) {
            errorLiveData.setValue("No hay usuario cargado");
            return;
        }

        cargandoLiveData.setValue(true);
        estudiante.setFotoURL(nuevaFotoURL != null ? nuevaFotoURL : "");

        repository.actualizarFotoURL(estudiante.getId(), nuevaFotoURL)
                .addOnSuccessListener(aVoid -> {
                    usuarioActualLiveData.setValue(estudiante);
                    mensajeExitoLiveData.setValue("Foto actualizada");
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al actualizar foto: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al actualizar foto", e);
                });
    }

    public void cambiarContrasena(String contrasenaActual, String nuevaContrasena) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("No hay sesión activa");
            return;
        }

        if (nuevaContrasena == null || nuevaContrasena.length() < 6) {
            errorLiveData.setValue("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        cargandoLiveData.setValue(true);

        currentUser.updatePassword(nuevaContrasena)
                .addOnSuccessListener(aVoid -> {
                    mensajeExitoLiveData.setValue("Contraseña actualizada");
                    cargandoLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al cambiar contraseña: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al cambiar contraseña", e);
                });
    }

    public void cerrarSesion() {
        mAuth.signOut();
        sesionCerradaLiveData.setValue(true);
        usuarioActualLiveData.setValue(null);
        Log.d(TAG, "Sesión cerrada");
    }

    public void eliminarCuenta() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("No hay sesión activa");
            return;
        }

        cargandoLiveData.setValue(true);

        String userId = currentUser.getUid();
        repository.eliminarEstudiante(userId)
                .addOnSuccessListener(aVoid -> {
                    currentUser.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                mensajeExitoLiveData.setValue("Cuenta eliminada");
                                sesionCerradaLiveData.setValue(true);
                                cargandoLiveData.setValue(false);
                            })
                            .addOnFailureListener(e -> {
                                errorLiveData.setValue("Error al eliminar cuenta de autenticación: " + e.getMessage());
                                cargandoLiveData.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    errorLiveData.setValue("Error al eliminar datos: " + e.getMessage());
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error al eliminar estudiante", e);
                });
    }

    public void limpiarMensajes() {
        errorLiveData.setValue(null);
        mensajeExitoLiveData.setValue(null);
    }
}