package com.example.deluvery.adapters;

import android.graphics.Color;
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

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.LocalViewHolder> {

    private List<Local> locales;
    private OnLocalClickListener listener;

    public interface OnLocalClickListener {
        void onLocalClick(Local local);
    }

    public LocalAdapter() {
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
                .inflate(R.layout.item_local_card, parent, false);
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
        private final TextView tvHorario;
        private final TextView tvEstado;
        private final View indicadorDisponible;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLocal = itemView.findViewById(R.id.img_local);
            tvNombre = itemView.findViewById(R.id.tv_local_nombre);
            tvHorario = itemView.findViewById(R.id.tv_local_horario);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            indicadorDisponible = itemView.findViewById(R.id.indicator_disponible);
        }

        public void bind(Local local, OnLocalClickListener listener) {
            // Establecer nombre
            tvNombre.setText(local.getNombre());

            // Establecer horario
            String horario = local.getHorarioApertura() + " - " + local.getHorarioCierre();
            tvHorario.setText(horario);

            // Establecer estado de disponibilidad
            if (local.isDisponible()) {
                tvEstado.setText("Disponible");
                tvEstado.setTextColor(Color.parseColor("#4CAF50")); // Verde
                indicadorDisponible.setBackgroundResource(R.drawable.bg_indicator_disponible);
            } else {
                tvEstado.setText("Cerrado");
                tvEstado.setTextColor(Color.parseColor("#F44336")); // Rojo
                indicadorDisponible.setBackgroundResource(R.drawable.bg_indicator_cerrado);
            }

            // Cargar imagen del local
            Glide.with(itemView.getContext())
                    .load(R.mipmap.ic_launcher) // Placeholder por ahora
                    .centerCrop()
                    .placeholder(R.drawable.ic_store)
                    .into(imgLocal);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocalClick(local);
                }
            });

            // Efecto visual si está cerrado
            if (!local.isDisponible()) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }
        }
    }
}