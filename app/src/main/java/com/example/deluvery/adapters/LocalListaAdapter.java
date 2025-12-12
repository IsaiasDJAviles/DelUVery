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
import com.example.deluvery.models.Local;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar locales en formato de lista vertical
 * con cards que muestran imagen arriba y nombre abajo
 */
public class LocalListaAdapter extends RecyclerView.Adapter<LocalListaAdapter.LocalViewHolder> {

    private List<Local> locales;
    private OnLocalClickListener listener;

    public interface OnLocalClickListener {
        void onLocalClick(Local local);
    }

    public LocalListaAdapter() {
        this.locales = new ArrayList<>();
    }

    public void setOnLocalClickListener(OnLocalClickListener listener) {
        this.listener = listener;
    }

    public void setLocales(List<Local> locales) {
        this.locales = locales != null ? locales : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addLocal(Local local) {
        locales.add(local);
        notifyItemInserted(locales.size() - 1);
    }

    public void clearLocales() {
        locales.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local_lista, parent, false);
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalViewHolder holder, int position) {
        Local local = locales.get(position);
        holder.bind(local, listener);
    }

    @Override
    public int getItemCount() {
        return locales.size();
    }

    static class LocalViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgLocal;
        private final TextView tvNombre;
        private final TextView tvPlaceholder;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLocal = itemView.findViewById(R.id.img_local);
            tvNombre = itemView.findViewById(R.id.tv_local_nombre);
            tvPlaceholder = itemView.findViewById(R.id.tv_imagen_placeholder);
        }

        public void bind(Local local, OnLocalClickListener listener) {
            // Establecer nombre
            tvNombre.setText(local.getNombre());

            // Intentar cargar imagen del local
            // Por ahora mostramos el placeholder
            if (tvPlaceholder != null) {
                tvPlaceholder.setText("Imagen_Local");
                tvPlaceholder.setVisibility(View.VISIBLE);
            }

            // Si el local tiene una URL de imagen, cargarla con Glide
            // y ocultar el placeholder
            // Por ahora usamos el placeholder
            imgLocal.setImageResource(android.R.color.transparent);

            // Efecto visual si esta cerrado
            if (!local.isDisponible()) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocalClick(local);
                }
            });
        }
    }
}