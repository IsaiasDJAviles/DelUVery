package com.example.deluvery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.deluvery.R;
import com.example.deluvery.models.CarritoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    private List<CarritoItem> items;
    private OnCarritoItemListener listener;

    public interface OnCarritoItemListener {
        void onIncrementar(CarritoItem item);
        void onDecrementar(CarritoItem item);
        void onEliminar(CarritoItem item);
    }

    public CarritoAdapter() {
        this.items = new ArrayList<>();
    }

    public void setOnCarritoItemListener(OnCarritoItemListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CarritoItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateItem(int position, CarritoItem item) {
        if (position >= 0 && position < items.size()) {
            items.set(position, item);
            notifyItemChanged(position);
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrito_detalle, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        CarritoItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CarritoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProducto;
        private final TextView tvNombre;
        private final TextView tvDescripcion;
        private final TextView tvPrecio;
        private final TextView tvCantidad;
        private final TextView tvSubtotal;
        private final ImageButton btnDecrementar;
        private final ImageButton btnIncrementar;
        private final ImageButton btnEliminar;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProducto = itemView.findViewById(R.id.img_carrito_producto);
            tvNombre = itemView.findViewById(R.id.tv_carrito_nombre);
            tvDescripcion = itemView.findViewById(R.id.tv_carrito_descripcion);
            tvPrecio = itemView.findViewById(R.id.tv_carrito_precio);
            tvCantidad = itemView.findViewById(R.id.tv_carrito_cantidad);
            tvSubtotal = itemView.findViewById(R.id.tv_carrito_subtotal);
            btnDecrementar = itemView.findViewById(R.id.btn_decrementar);
            btnIncrementar = itemView.findViewById(R.id.btn_incrementar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }

        public void bind(CarritoItem item, OnCarritoItemListener listener) {
            tvNombre.setText(item.getNombre());
            tvDescripcion.setText(item.getDescripcion());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrecio()));
            tvCantidad.setText(String.valueOf(item.getCantidad()));
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", item.getSubtotal()));

            // Cargar imagen
            if (item.getImagenURL() != null && !item.getImagenURL().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImagenURL())
                        .placeholder(R.drawable.ic_store)
                        .centerCrop()
                        .into(imgProducto);
            } else {
                imgProducto.setImageResource(R.drawable.ic_store);
            }

            // Listeners
            btnIncrementar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncrementar(item);
                }
            });

            btnDecrementar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecrementar(item);
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminar(item);
                }
            });
        }
    }
}