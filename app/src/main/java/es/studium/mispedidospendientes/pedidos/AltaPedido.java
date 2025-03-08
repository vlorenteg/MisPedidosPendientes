package es.studium.mispedidospendientes.pedidos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import es.studium.mispedidospendientes.BDConexion;
import es.studium.mispedidospendientes.R;
import es.studium.mispedidospendientes.tiendas.Tienda;

public class AltaPedido extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Spinner spinnerTiendas;
    private EditText editTextFechaPedido;
    private EditText editTextFechaEntrega;
    private EditText editTextDescripcion;
    private EditText editTextImporte;
    private Button btnAceptar;
    private Button btnCancelar;
    private List<Tienda> tiendas;
    int result;
    Toast toast;

    private PedidoCallback callback;

    public AltaPedido(PedidoCallback callback) {
        this.callback = callback;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialogo_alta_pedido, null);
        builder.setTitle(R.string.nuevoPedido).setView(dialogView);

        tiendas = BDConexion.consultarTiendas();
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getResources().getString(R.string.spinnerPrompt));
        for (Tienda t : tiendas) {
            spinnerArray.add(t.toString());
        }

        spinnerTiendas = dialogView.findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerTiendas.setAdapter(spinnerArrayAdapter);
        spinnerTiendas.setOnItemSelectedListener(this);

        btnAceptar = dialogView.findViewById(R.id.btnAceptar1);
        btnCancelar = dialogView.findViewById(R.id.btnCancelar1);
        editTextFechaPedido = dialogView.findViewById(R.id.editFechaPedido);
        editTextFechaEntrega = dialogView.findViewById(R.id.editFechaEntrega);
        editTextDescripcion = dialogView.findViewById(R.id.editDescripcion);
        editTextImporte = dialogView.findViewById(R.id.editImporte);

        btnAceptar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);

        editTextFechaPedido.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v == btnCancelar) {
            dismiss();
        } else if (v == btnAceptar) {
            if (spinnerTiendas.getSelectedItemPosition() != 0 &&
                    !editTextFechaPedido.getText().toString().trim().isEmpty() &&
                    !editTextFechaEntrega.getText().toString().trim().isEmpty() &&
                    !editTextDescripcion.getText().toString().trim().isEmpty() &&
                    !editTextImporte.getText().toString().trim().isEmpty()) {

                String importeString = editTextImporte.getText().toString();

                if (!importeString.matches("^\\d+(\\.\\d{1,2})?$")) {
                    toast = Toast.makeText(getContext(), getResources().getString(R.string.importeIncorrecto), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                String nombreTiendaFK = spinnerTiendas.getSelectedItem().toString();
                int idTiendaFK = -1;
                LocalDate fechaPedido;
                LocalDate fechaEntrega;
                String descripcion = editTextDescripcion.getText().toString();
                double importe = Double.parseDouble(importeString);

                fechaPedido = convertToDate(editTextFechaPedido.getText().toString());
                if (fechaPedido == null) {
                    mostrarToast(R.string.fechaPedidoIncorrecta);
                    return;
                }

                fechaEntrega = convertToDate(editTextFechaEntrega.getText().toString());
                if (fechaEntrega == null) {
                    mostrarToast(R.string.fechaEntregaIncorrecta);
                    return;
                }

                if (!fechaPedido.isBefore(fechaEntrega)) {
                    mostrarToast(R.string.fechasIncorrectas);
                    return;
                }

                Optional<Tienda> tiendaOpt = tiendas.stream()
                        .filter(t -> t.getNombreTienda().equals(nombreTiendaFK))
                        .findFirst();

                if (tiendaOpt.isPresent()) {
                    idTiendaFK = tiendaOpt.get().getIdTienda();
                }

                if (idTiendaFK != -1) {
                    Pedido pedido = new Pedido(fechaPedido, fechaEntrega, importe, 0, descripcion, idTiendaFK);
                    result = BDConexion.altaPedido(pedido);
                    if (result == 200) {
                        mostrarToast(R.string.altaCorrecta);
                        callback.onOperacionCorrectaUpdated(true);
                    } else {
                        mostrarToast(R.string.error);
                    }
                    dismiss();
                }
            } else {
                mostrarToast(R.string.rellenaTodos);
            }
        }
    }

    private void mostrarToast(int mensaje) {
        toast = Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    private LocalDate convertToDate(String fecha) {
        LocalDate fechaLD = null;
        try {
            String[] dateSplit = fecha.split("/");
            int day = Integer.parseInt(dateSplit[0]);
            int month = Integer.parseInt(dateSplit[1]);
            int year = Integer.parseInt(dateSplit[2]);
            fechaLD = LocalDate.of(year, month, day);
        } catch (Exception ignored) {}
        return fechaLD;
    }
}
