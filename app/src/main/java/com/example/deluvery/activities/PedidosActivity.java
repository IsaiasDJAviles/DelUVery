package com.example.deluvery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deluvery.R;
import com.example.deluvery.adapters.PedidoAdapter;
import com.example.deluvery.models.Pedido;
import com.example.deluvery.viewmodels.PedidoViewModel;

public class PedidosActivity extends AppCompatActivity {

    private PedidoViewModel viewModel;
    private PedidoAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mis Pedidos");
        }

        // Inicializar vistas
        recyclerView = findViewById(R.id.recycler_pedidos);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(PedidoViewModel.class);

        // Observar LiveData
        observarViewModel();

        // Obtener clienteID del intent
        String clienteID = getIntent().getStringExtra("clienteID");
        if (clienteID == null) {
            clienteID = "EST001"; // Valor por defecto
        }

        // Cargar datos
        viewModel.cargarPedidosCliente(clienteID);
    }

    private void setupRecyclerView() {
        adapter = new PedidoAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar listeners del adapter
        adapter.setOnPedidoClickListener(new PedidoAdapter.OnPedidoClickListener() {
            @Override
            public void onPedidoClick(Pedido pedido) {
                Toast.makeText(PedidosActivity.this,
                        "Pedido: " + pedido.getId(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerDetallesClick(Pedido pedido) {
                mostrarDetallesPedido(pedido);
            }
        });
    }

    private void observarViewModel() {
        // Observar lista de pedidos
        viewModel.getPedidos().observe(this, pedidos -> {
            if (pedidos != null && !pedidos.isEmpty()) {
                adapter.setPedidos(pedidos);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Observar estado de carga
        viewModel.getCargando().observe(this, cargando -> {
            progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        });

        // Observar errores
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.limpiarMensajes();
            }
        });

        // Observar mensajes de éxito
        viewModel.getMensajeExito().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                viewModel.limpiarMensajes();
            }
        });
    }

    private void mostrarDetallesPedido(Pedido pedido) {
        // Cargar artículos del pedido
        viewModel.cargarArticulosPedido(pedido.getId());

        // Aquí puedes abrir un dialog o nueva activity con los detalles
        Toast.makeText(this,
                "Mostrando detalles de: " + pedido.getId(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}