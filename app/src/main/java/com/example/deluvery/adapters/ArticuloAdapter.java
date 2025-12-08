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
import com.example.deluvery.models.Articulo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArticuloAdapter extends RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder> {

    private List<Articulo> articulos;
    private OnArticuloClickListener listener;

    public interface OnArticuloClickListener {
        void onArticuloClick(Articulo articulo);
        void onAgregarCarritoClick(Articulo articulo);
    }

    public ArticuloAdapter() {
        this.articulos = new ArrayList<>();
    }

    public void setOnArticuloClickListener(OnArticuloClickListener listener) {
        this.listener = listener;
    }

    public void setArticulos(List<Articulo> articulos) {
        this.articulos = articulos != null ? articulos : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addArticulo(Articulo articulo) {
        articulos.add(articulo);
        notifyItemInserted(articulos.size() - 1);
    }

    public void updateArticulo(int position, Articulo articulo) {
        articulos.set(position, articulo);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ArticuloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_articulo, parent, false);
        return new ArticuloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticuloViewHolder holder, int position) {
        Articulo articulo = articulos.get(position);
        holder.bind(articulo, listener);
    }

    @Override
    public int getItemCount() {
        return articulos.size();
    }

    static class ArticuloViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgArticulo;
        private final TextView tvNombre;
        private final TextView tvDescripcion;
        private final TextView tvPrecio;
        private final View btnAgregarCarrito;
        private final View viewNoDisponible;

        public ArticuloViewHolder(@NonNull View itemView) {
            super(itemView);
            imgArticulo = itemView.findViewById(R.id.img_articulo);
            tvNombre = itemView.findViewById(R.id.tv_articulo_nombre);
            tvDescripcion = itemView.findViewById(R.id.tv_articulo_descripcion);
            tvPrecio = itemView.findViewById(R.id.tv_articulo_precio);
            btnAgregarCarrito = itemView.findViewById(R.id.btn_agregar_carrito);
            viewNoDisponible = itemView.findViewById(R.id.view_no_disponible);
        }

        public void bind(Articulo articulo, OnArticuloClickListener listener) {
            tvNombre.setText(articulo.getNombre());
            tvDescripcion.setText(articulo.getDescripcion());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", articulo.getPrecio()));

            // Mostrar overlay si no está disponible
            if (!articulo.isDisponible()) {
                viewNoDisponible.setVisibility(View.VISIBLE);
                btnAgregarCarrito.setEnabled(false);
                btnAgregarCarrito.setAlpha(0.5f);
            } else {
                viewNoDisponible.setVisibility(View.GONE);
                btnAgregarCarrito.setEnabled(true);
                btnAgregarCarrito.setAlpha(1.0f);
            }

            // Cargar imagen
            if (articulo.getImagenURL() != null && !articulo.getImagenURL().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(articulo.getImagenURL())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imgArticulo);
            } else {
                imgArticulo.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Click en el item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onArticuloClick(articulo);
                }
            });

            // Click en botón agregar
            btnAgregarCarrito.setOnClickListener(v -> {
                if (listener != null && articulo.isDisponible()) {
                    listener.onAgregarCarritoClick(articulo);
                }
            });
        }
    }
}