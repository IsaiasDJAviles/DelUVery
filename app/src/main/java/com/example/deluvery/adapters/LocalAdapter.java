package com.example.deluvery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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

    @NonNull
    @Override
    public LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local, parent, false);
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

        private final CardView cardView;
        private final TextView tvNombre;
        private final TextView tvHorario;
        private final View indicadorDisponible;

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_local);
            tvNombre = itemView.findViewById(R.id.tv_local_nombre);
            tvHorario = itemView.findViewById(R.id.tv_local_horario);
            indicadorDisponible = itemView.findViewById(R.id.indicador_disponible);
        }

        public void bind(Local local, OnLocalClickListener listener) {
            tvNombre.setText(local.getNombre());

            String horario = local.getHorarioApertura() + " - " + local.getHorarioCierre();
            tvHorario.setText(horario);

            indicadorDisponible.setVisibility(
                    local.isDisponible() ? View.VISIBLE : View.GONE
            );

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocalClick(local);
                }
            });
        }
    }
}