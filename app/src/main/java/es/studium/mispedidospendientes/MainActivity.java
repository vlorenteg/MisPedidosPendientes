package es.studium.mispedidospendientes;

import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import es.studium.mispedidospendientes.pedidos.ConsultaPedidos;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fm;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Manejar el fragmento ConsultaPedidos
        fm = getSupportFragmentManager();
        Fragment ConsultaPedidos = fm.findFragmentByTag("ConsultaPedidos");

        if (ConsultaPedidos == null) {
            ft = fm.beginTransaction();
            ft.replace(R.id.fragmentContainer, new ConsultaPedidos(), "ConsultaPedidos");
            ft.commit();
        }

        // Permitir todas las conexiones de red en el hilo principal (No recomendado para producción)
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }
}
