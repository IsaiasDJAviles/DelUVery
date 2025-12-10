package com.example.deluvery.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class PedidoPendienteAdapter extends RecyclerView.Adapter<PedidoPendienteAdapter.ViewHolder> {

    private List<Pedido> pedidos;
    private OnPedidoClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnPedidoClickListener {
        void onAceptarPedido(Pedido pedido);
        void onVerDetalles(Pedido pedido);
    }

    public PedidoPendienteAdapter() {
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido_pendiente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.bind(pedido, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final TextView tvPedidoId;
        private final TextView tvLugarEntrega;
        private final TextView tvTotal;
        private final TextView tvFecha;
        private final Button btnAceptar;
        private final Button btnVerDetalles;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_pedido_pendiente);
            tvPedidoId = itemView.findViewById(R.id.tv_pedido_pendiente_id);
            tvLugarEntrega = itemView.findViewById(R.id.tv_lugar_entrega);
            tvTotal = itemView.findViewById(R.id.tv_pedido_pendiente_total);
            tvFecha = itemView.findViewById(R.id.tv_pedido_pendiente_fecha);
            btnAceptar = itemView.findViewById(R.id.btn_aceptar_pedido);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_detalles_pedido);
        }

        public void bind(Pedido pedido, OnPedidoClickListener listener, SimpleDateFormat dateFormat) {
            tvPedidoId.setText("Pedido #" + pedido.getId());
            tvLugarEntrega.setText("📍 " + pedido.getSalonEntrega());
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));

            if (pedido.getFecha() != null) {
                tvFecha.setText(dateFormat.format(pedido.getFecha()));
            }

            // Efecto visual para pedido nuevo
            cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"));

            btnAceptar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAceptarPedido(pedido);
                }
            });

            btnVerDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetalles(pedido);
                }
            });
        }
    }
}