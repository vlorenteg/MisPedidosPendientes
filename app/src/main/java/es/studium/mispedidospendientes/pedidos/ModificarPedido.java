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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;

import es.studium.mispedidospendientes.BDConexion;
import es.studium.mispedidospendientes.R;
import es.studium.mispedidospendientes.tiendas.Tienda;

public class ModificarPedido extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private int idPedido;
    private Pedido pedidoEditado;
    private Pedido pedidoOriginal;
    private Spinner spinnerTiendas;
    private EditText editFechaPedido;
    private EditText editFechaEntrega;
    private EditText editDescripcion;
    private EditText editImporte;
    private Button btnCancelar;
    private Button btnAceptar;
    private Chip chipRecibido;
    private List<Tienda> tiendas;
    int result;
    private PedidoCallback callback;
    Toast toast;

    public ModificarPedido(int idPedido, PedidoCallback callback) {
        this.idPedido = idPedido;
        this.callback = callback;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // crear un dialog y darle estilo
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // establecer la ventana del dialog
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialogo_modificacion_pedido, null);
        // establecer el título de dialog
        builder.setTitle(getResources().getString(R.string.edicionPedido) + idPedido).setView(dialogView);
        // obtener un arrayList de las tiendas para rellenar el spinner
        tiendas = BDConexion.consultarTiendas();
        // obtener el objeto Pedido por su id
        pedidoOriginal = BDConexion.consultarPedido(idPedido);
        // crear un arrayList para guardar todas las Tiendas y mostrarlos en el Spinner
        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add(getResources().getString(R.string.spinnerPrompt));
        // añadir cada tienda al Spinner
        for (Tienda t : tiendas) {
            spinnerArray.add(t.toString());
        }
        spinnerTiendas = (Spinner) dialogView.findViewById(R.id.spinnerE);
        // crear un adaptador para el Spinner y añadirle la lista de Tiendas
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerTiendas.setAdapter(spinnerArrayAdapter);
        spinnerTiendas.setOnItemSelectedListener(this);
        // pre-seleccionar la tienda del pedido original en el spinner
        Tienda t = BDConexion.consultarTienda(pedidoOriginal.getIdTiendaFK());
        String nombreTienda = t.getNombreTienda();
        int indiceTiendaSeleccionada = spinnerArrayAdapter.getPosition(nombreTienda);
        if (indiceTiendaSeleccionada != -1) {
            spinnerTiendas.setSelection(indiceTiendaSeleccionada);
        }
        btnCancelar = dialogView.findViewById(R.id.btnCancelar3);
        btnAceptar = dialogView.findViewById(R.id.btnAceptar3);
        editFechaPedido = dialogView.findViewById(R.id.editFechaPedidoE);
        editFechaEntrega = dialogView.findViewById(R.id.editFechaEntregaE);
        editDescripcion = dialogView.findViewById(R.id.editDescripcionE);
        editImporte = dialogView.findViewById(R.id.editImporteE);
        chipRecibido = dialogView.findViewById(R.id.chip);
        chipRecibido.setCheckable(true);
        chipRecibido.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnAceptar.setOnClickListener(this);
        // establecer los valores originales por defecto
        editFechaPedido.setText(pedidoOriginal.getFechaPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        editFechaEntrega.setText(pedidoOriginal.getFechaEstimadaPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        editDescripcion.setText(pedidoOriginal.getDescripcionPedido().toString());
        editImporte.setText(String.valueOf(pedidoOriginal.getImportePedido()));
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v == btnCancelar) {
            dismiss();
        } else if (v == btnAceptar) {
            // comprobar si no hay ningún campo sin rellenar
            if (spinnerTiendas.getSelectedItemPosition() != 0 && !editFechaPedido.getText().toString().trim().isEmpty() &&
                    !editFechaEntrega.getText().toString().trim().isEmpty() && !editDescripcion.getText().toString().trim().isEmpty() &&
                    !editImporte.getText().toString().trim().isEmpty()) {

                String importeString = editImporte.getText().toString();

                // Verificar si el importe tiene más de dos decimales
                if (!importeString.matches("^\\d+(\\.\\d{1,2})?$")) {
                    toast = Toast.makeText(getContext(), getResources().getString(R.string.importeIncorrecto), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }

                String nombreTiendaFK = spinnerTiendas.getSelectedItem().toString();
                int idTiendaFK = -1;
                // obtener el id de la tienda seleccionada en spinner
                Optional<Tienda> tiendaOpt = tiendas.stream()
                        .filter(t -> t.getNombreTienda().equals(nombreTiendaFK))
                        .findFirst();
                if (tiendaOpt.isPresent()) {
                    idTiendaFK = tiendaOpt.get().getIdTienda();
                }

                LocalDate fechaPedido = null;
                LocalDate fechaEntrega = null;
                String descripcion = editDescripcion.getText().toString();
                double importe = Double.parseDouble(importeString);
                int estado = 0;

                String fechaPedidoString = editFechaPedido.getText().toString();
                String fechaEntregaString = editFechaEntrega.getText().toString();
                // convertir String con fecha de pedido en LocalDate
                fechaPedido = convertToDate(fechaPedidoString);
                if (fechaPedido == null) {
                    toast = Toast.makeText(getContext(), R.string.fechaPedidoIncorrecta, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    fechaEntrega = convertToDate(fechaEntregaString);
                    if (fechaEntrega == null) {
                        toast = Toast.makeText(getContext(), R.string.fechaEntregaIncorrecta, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        if (fechaPedido.isBefore(fechaEntrega)) {
                            fechaPedido.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            fechaEntrega.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        } else {
                            fechaPedido = null;
                            fechaEntrega = null;
                            toast = Toast.makeText(getContext(), R.string.fechasIncorrectas, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                }

                if (fechaPedido != null && fechaEntrega != null && idTiendaFK != -1) {
                    if (chipRecibido.isChecked()) {
                        estado = 1;
                        result = BDConexion.borradoPedido(idPedido);
                        dismiss();
                    } else {
                        pedidoEditado = new Pedido(idPedido, fechaPedido, fechaEntrega, importe, estado, descripcion, idTiendaFK);
                        result = BDConexion.modificacionPedido(pedidoEditado);
                        dismiss();
                    }
                }
            } else {
                toast = Toast.makeText(getContext(), R.string.rellenaTodos, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            if (result == 200) {
                toast = Toast.makeText(getContext(), R.string.modificacionCorrecta, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                callback.onOperacionCorrectaUpdated(true);
                dismiss();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // bloquear en modo vertical
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        //set rotation to sensor dependent
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private LocalDate convertToDate(String fecha) {
        LocalDate fechaLD = null;
        try {
            // dividir la fecha en día, mes y año
            String[] dateSplit = fecha.split("/");
            int day = Integer.parseInt(dateSplit[0]);
            int month = Integer.parseInt(dateSplit[1]);
            int year = Integer.parseInt(dateSplit[2]);
            // crear una fecha utilizando el día, mes y año
            fechaLD = LocalDate.of(year, month, day);
        } catch (Exception ex) {}
        return fechaLD;
    }
}
