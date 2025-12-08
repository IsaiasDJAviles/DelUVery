package com.example.deluvery.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;
    private OnPedidoClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnPedidoClickListener {
        void onPedidoClick(Pedido pedido);
        void onVerDetallesClick(Pedido pedido);
    }

    public PedidoAdapter() {
        this.pedidos = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    public void setOnPedidoClickListener(OnPedidoClickListener listener) {
        this.listener = listener;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos != null ? pedidos : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPedido(Pedido pedido) {
        pedidos.add(0, pedido); // Agregar al inicio
        notifyItemInserted(0);
    }

    public void updatePedido(int position, Pedido pedido) {
        pedidos.set(position, pedido);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.bind(pedido, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final TextView tvPedidoId;
        private final TextView tvEstado;
        private final TextView tvTotal;
        private final TextView tvFecha;
        private final TextView tvSalon;
        private final View btnVerDetalles;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_pedido);
            tvPedidoId = itemView.findViewById(R.id.tv_pedido_id);
            tvEstado = itemView.findViewById(R.id.tv_pedido_estado);
            tvTotal = itemView.findViewById(R.id.tv_pedido_total);
            tvFecha = itemView.findViewById(R.id.tv_pedido_fecha);
            tvSalon = itemView.findViewById(R.id.tv_pedido_salon);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_detalles);
        }

        public void bind(Pedido pedido, OnPedidoClickListener listener, SimpleDateFormat dateFormat) {
            tvPedidoId.setText("Pedido #" + pedido.getId());
            tvEstado.setText(formatearEstado(pedido.getEstado()));
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));

            if (pedido.getFecha() != null) {
                tvFecha.setText(dateFormat.format(pedido.getFecha()));
            }

            tvSalon.setText("Entrega: " + pedido.getSalonEntrega());

            // Color según estado
            int colorEstado = getColorEstado(pedido.getEstado());
            tvEstado.setTextColor(colorEstado);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPedidoClick(pedido);
                }
            });

            btnVerDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetallesClick(pedido);
                }
            });
        }

        private String formatearEstado(String estado) {
            switch (estado.toLowerCase()) {
                case "pendiente":
                    return "PENDIENTE";
                case "asignado":
                    return "ASIGNADO";
                case "en_camino":
                    return "EN CAMINO";
                case "entregado":
                    return "ENTREGADO";
                case "cancelado":
                    return "CANCELADO";
                default:
                    return estado.toUpperCase();
            }
        }

        private int getColorEstado(String estado) {
            switch (estado.toLowerCase()) {
                case "pendiente":
                    return Color.parseColor("#FF9800"); // Naranja
                case "asignado":
                    return Color.parseColor("#2196F3"); // Azul
                case "en_camino":
                    return Color.parseColor("#9C27B0"); // Morado
                case "entregado":
                    return Color.parseColor("#4CAF50"); // Verde
                case "cancelado":
                    return Color.parseColor("#F44336"); // Rojo
                default:
                    return Color.parseColor("#757575"); // Gris
            }
        }
    }
}