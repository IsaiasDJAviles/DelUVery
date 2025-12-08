package com.example.deluvery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.deluvery.R;
import com.example.deluvery.models.Estudiante;

import java.util.ArrayList;
import java.util.List;

public class EstudianteAdapter extends RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder> {

    private List<Estudiante> estudiantes;
    private OnEstudianteClickListener listener;

    public interface OnEstudianteClickListener {
        void onEstudianteClick(Estudiante estudiante);
        void onEstudianteLongClick(Estudiante estudiante);
    }

    public EstudianteAdapter() {
        this.estudiantes = new ArrayList<>();
    }

    public void setOnEstudianteClickListener(OnEstudianteClickListener listener) {
        this.listener = listener;
    }

    public void setEstudiantes(List<Estudiante> estudiantes) {
        this.estudiantes = estudiantes != null ? estudiantes : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addEstudiante(Estudiante estudiante) {
        estudiantes.add(estudiante);
        notifyItemInserted(estudiantes.size() - 1);
    }

    public void removeEstudiante(int position) {
        estudiantes.remove(position);
        notifyItemRemoved(position);
    }

    public void updateEstudiante(int position, Estudiante estudiante) {
        estudiantes.set(position, estudiante);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public EstudianteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_estudiante, parent, false);
        return new EstudianteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EstudianteViewHolder holder, int position) {
        Estudiante estudiante = estudiantes.get(position);
        holder.bind(estudiante, listener);
    }

    @Override
    public int getItemCount() {
        return estudiantes.size();
    }

    static class EstudianteViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgFoto;
        private final TextView tvNombre;
        private final TextView tvCorreo;
        private final TextView tvRol;
        private final TextView tvTelefono;
        private final View indicadorActivo;

        public EstudianteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.img_estudiante_foto);
            tvNombre = itemView.findViewById(R.id.tv_estudiante_nombre);
            tvCorreo = itemView.findViewById(R.id.tv_estudiante_correo);
            tvRol = itemView.findViewById(R.id.tv_estudiante_rol);
            tvTelefono = itemView.findViewById(R.id.tv_estudiante_telefono);
            indicadorActivo = itemView.findViewById(R.id.indicador_activo);
        }

        public void bind(Estudiante estudiante, OnEstudianteClickListener listener) {
            tvNombre.setText(estudiante.getNombre());
            tvCorreo.setText(estudiante.getCorreo());
            tvRol.setText(estudiante.getRol().toUpperCase());
            tvTelefono.setText(estudiante.getTelefono());

            // Mostrar indicador de activo
            indicadorActivo.setVisibility(estudiante.isActivo() ? View.VISIBLE : View.GONE);

            // Cargar imagen con Glide
            if (estudiante.getFotoURL() != null && !estudiante.getFotoURL().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(estudiante.getFotoURL())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(imgFoto);
            } else {
                imgFoto.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEstudianteClick(estudiante);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onEstudianteLongClick(estudiante);
                }
                return true;
            });
        }
    }
}