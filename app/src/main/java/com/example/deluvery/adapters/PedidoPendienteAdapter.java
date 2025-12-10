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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidoPendienteAdapter extends RecyclerView.Adapter<PedidoPendienteAdapter.ViewHolder> {

    private List<Pedido> pedidos;
    private OnPedidoClickListener listener;

    public interface OnPedidoClickListener {
        void onAceptarPedido(Pedido pedido);
        void onVerDetalles(Pedido pedido);
    }

    public PedidoPendienteAdapter() {
        this.pedidos = new ArrayList<>();
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
        holder.bind(pedido, listener);
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

        public void bind(Pedido pedido, OnPedidoClickListener listener) {
            tvPedidoId.setText("Pedido #" + pedido.getId());
            tvLugarEntrega.setText(pedido.getSalonEntrega());
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));

            // Calcular tiempo transcurrido
            String tiempoTranscurrido = calcularTiempoTranscurrido(pedido.getFecha());
            tvFecha.setText(tiempoTranscurrido);

            // Efecto visual para pedido reciente (menos de 5 minutos)
            if (esReciente(pedido.getFecha())) {
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }

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

        private String calcularTiempoTranscurrido(Date fecha) {
            if (fecha == null) return "Hace un momento";

            long diff = System.currentTimeMillis() - fecha.getTime();
            long segundos = diff / 1000;
            long minutos = segundos / 60;
            long horas = minutos / 60;
            long dias = horas / 24;

            if (dias > 0) {
                return "Hace " + dias + (dias == 1 ? " día" : " días");
            } else if (horas > 0) {
                return "Hace " + horas + (horas == 1 ? " hora" : " horas");
            } else if (minutos > 0) {
                return "Hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
            } else {
                return "Hace un momento";
            }
        }

        private boolean esReciente(Date fecha) {
            if (fecha == null) return true;

            long diff = System.currentTimeMillis() - fecha.getTime();
            long minutos = diff / (1000 * 60);

            return minutos < 5;
        }
    }
}