package es.studium.mispedidospendientes.pedidos;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.studium.mispedidospendientes.BDConexion;
import es.studium.mispedidospendientes.MainActivity;
import es.studium.mispedidospendientes.R;
import es.studium.mispedidospendientes.RecyclerViewOnItemClickListener;
import es.studium.mispedidospendientes.tiendas.ConsultaTiendas;

public class ConsultaPedidos extends Fragment implements View.OnClickListener {

    private List<Pedido> pedidos;
    private RecyclerView recyclerView;
    private Button btnTiendas;
    private Button btnNuevoPedido;

    private FragmentManager fm;
    private FragmentTransaction ft;
    private PedidoCallback pedidoCallback;

    public ConsultaPedidos() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Callback para actualizar la lista tras una operación
        pedidoCallback = new PedidoCallback() {
            @Override
            public void onOperacionCorrectaUpdated(boolean operacionCorrecta) {
                if (operacionCorrecta) {
                    setUpRecyclerView();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consulta_pedidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnTiendas = view.findViewById(R.id.btnTiendas);
        btnTiendas.setOnClickListener(this);

        btnNuevoPedido = view.findViewById(R.id.btnNuevoPedido);
        btnNuevoPedido.setOnClickListener(this);

        fm = getActivity().getSupportFragmentManager();
        setUpRecyclerView();
        setUpActionBar();
    }

    // Configurar RecyclerView con los pedidos
    private void setUpRecyclerView() {
        recyclerView = requireView().findViewById(R.id.recyclerViewPedidos);
        pedidos = BDConexion.consultarPedidos();

        if (recyclerView != null) {
            recyclerView.setAdapter(new PedidosAdapter(pedidos, new RecyclerViewOnItemClickListener() {
                @Override
                public void onClick(View v, int position) {
                    ModificarPedido dialogEdicionPedido = new ModificarPedido(pedidos.get(position).getIdPedido(), pedidoCallback);
                    dialogEdicionPedido.setCancelable(false);
                    dialogEdicionPedido.show(getActivity().getSupportFragmentManager(), "Edición Pedido");
                }

                @Override
                public void onLongClick(View v, int position) {
                    BajaPedido dialogBorradoPedido = new BajaPedido(pedidos.get(position).getIdPedido(), pedidoCallback);
                    dialogBorradoPedido.setCancelable(false);
                    dialogBorradoPedido.show(getActivity().getSupportFragmentManager(), "Borrado Pedido");
                }
            }));

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                    DividerItemDecoration.VERTICAL));
        }
    }

    // Configurar la barra de acción con título y color
    private void setUpActionBar() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.olivine)));
                activity.getSupportActionBar().setTitle(R.string.app_action_bar_pedidos);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnTiendas) {
            Fragment fragmentListadoTiendas = new ConsultaTiendas();
            ft = fm.beginTransaction();
            ft.replace(R.id.fragmentContainer, fragmentListadoTiendas, "listadoTiendas")
                    .addToBackStack(null)
                    .commit();
        } else if (v == btnNuevoPedido) {
            AltaPedido dialogNuevoPedido = new AltaPedido(pedidoCallback);
            dialogNuevoPedido.setCancelable(false);
            dialogNuevoPedido.show(getActivity().getSupportFragmentManager(), "Nuevo Pedido");
        }
    }
}
