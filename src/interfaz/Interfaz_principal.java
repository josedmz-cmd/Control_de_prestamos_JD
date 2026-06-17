package interfaz;

import control.Controladora;
import logica.*;
import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Interfaz_principal {

	private JFrame frame;
	private Controladora control;

	private JTable tablePrestamos;
    private DefaultTableModel modelPrestamos;
    private JComboBox<String> cbPersonaPrestamo;
    private JList<String> listItemsDisponibles, listItemsPrestamo;
    private DefaultListModel<String> listModelDisponibles, listModelPrestamo;
    private int currentPrestamoId = -1;
    private Timer timerAlertas;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            try {
                Controladora.cargarDatos();
            } catch (Exception e) {
                System.out.println("No se encontraron datos previos.");
            }
            Interfaz_principal window = new Interfaz_principal();
            window.frame.setVisible(true);
        });
    }

	/**
	 * Create the application.
	 */
	public Interfaz_principal() {
		control = Controladora.getInstance();
        initialize();
        cargarDatosIniciales();
        configurarCierre();
        iniciarTimerAlertas();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
        frame = new JFrame("Sistema de Control de Préstamos");
        frame.setBounds(100, 100, 950, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addTab("Préstamos", crearPanelPrestamos());
        tabbedPane.addTab("Administración", crearPanelAdministracion());
        tabbedPane.addTab("Reportes", crearPanelReportes());
    }
	
	private JPanel crearPanelPrestamos() {
        JPanel Prestamos = new JPanel(new BorderLayout(10, 10));
        Prestamos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelPrestamos = new DefaultTableModel(new String[]{"ID", "Persona", "Fecha Inicio", "Ítems", "Alerta"}, 0);
        tablePrestamos = new JTable(modelPrestamos);
        Prestamos.add(new JScrollPane(tablePrestamos), BorderLayout.CENTER);

        JPanel Gestion = new JPanel(new BorderLayout(5, 5));
        Gestion.setBorder(BorderFactory.createTitledBorder("Gestión de Préstamos"));

        JPanel Top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Top.add(new JLabel("Persona:"));
        cbPersonaPrestamo = new JComboBox<>();
        Top.add(cbPersonaPrestamo);
        JButton btnNuevo = new JButton("Nuevo Préstamo");
        JButton btnFinalizar = new JButton("Finalizar");
        JButton btnAlerta = new JButton("Agregar Alerta");
        Top.add(btnNuevo);
        Top.add(btnFinalizar);
        Top.add(btnAlerta);
        Gestion.add(Top, BorderLayout.NORTH);

        JPanel Listas = new JPanel(new GridLayout(1, 3, 10, 10));
        listModelDisponibles = new DefaultListModel<>();
        listItemsDisponibles = new JList<>(listModelDisponibles);
        listItemsDisponibles.setBorder(BorderFactory.createTitledBorder("Ítems Disponibles"));
        listModelPrestamo = new DefaultListModel<>();
        listItemsPrestamo = new JList<>(listModelPrestamo);
        listItemsPrestamo.setBorder(BorderFactory.createTitledBorder("Ítems en el Préstamo"));

        JPanel botonesItems = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton btnAgregar = new JButton(">> Agregar");
        JButton btnQuitar = new JButton("<< Quitar");
        botonesItems.add(btnAgregar);
        botonesItems.add(btnQuitar);

        Listas.add(new JScrollPane(listItemsDisponibles));
        Listas.add(botonesItems);
        Listas.add(new JScrollPane(listItemsPrestamo));
        Gestion.add(Listas, BorderLayout.CENTER);

        Prestamos.add(Gestion, BorderLayout.SOUTH);

        btnNuevo.addActionListener(e -> nuevoPrestamo());
        btnFinalizar.addActionListener(e -> finalizarPrestamo());
        btnAlerta.addActionListener(e -> agregarAlerta());
        btnAgregar.addActionListener(e -> agregarItemAlPrestamo());
        btnQuitar.addActionListener(e -> quitarItemDelPrestamo());
        tablePrestamos.getSelectionModel().addListSelectionListener(e -> cargarPrestamoSeleccionado());

        actualizarCombosPrestamos();
        actualizarTablaPrestamos();
        actualizarListasItemsDisponibles();
        return Prestamos;
    }

    private void actualizarCombosPrestamos() {
        cbPersonaPrestamo.removeAllItems();
        for (Persona p : control.listarPersonas()) {
            cbPersonaPrestamo.addItem(p.getNombre() + " (" + p.getTelefono() + ")");
        }
    }

    private void actualizarTablaPrestamos() {
        modelPrestamos.setRowCount(0);
        for (Prestamo p : control.listarPrestamos()) {
            if (p.isActivo()) {
                StringBuilder itemsStr = new StringBuilder();
                for (Item i : p.getItems()) {
                    itemsStr.append(i.getNombre()).append(", ");
                }
                if (itemsStr.length() > 2) itemsStr.setLength(itemsStr.length() - 2);
                String alertaStr = p.getAlarma() != null ? p.getAlarma().getTipo().toString() : "Ninguna";
                modelPrestamos.addRow(new Object[]{
                        p.getId(),
                        p.getPersona().getNombre(),
                        p.getFechaInicio().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        itemsStr.toString(),
                        alertaStr
                });
            }
        }
    }

    private void actualizarListasItemsDisponibles() {
        listModelDisponibles.clear();
        for (Item i : control.listarItems()) {
            if (!i.isPrestado()) {
                listModelDisponibles.addElement(i.getCodigo() + " - " + i.getNombre());
            }
        }
        if (currentPrestamoId != -1) {
            Prestamo p = control.consultarPrestamo(currentPrestamoId);
            if (p != null) {
                for (Item i : p.getItems()) {
                    String entry = i.getCodigo() + " - " + i.getNombre();
                    if (!listModelDisponibles.contains(entry)) {
                        listModelDisponibles.addElement(entry);
                    }
                }
            }
        }
    }

    private void cargarPrestamoSeleccionado() {
        int row = tablePrestamos.getSelectedRow();
        if (row != -1) {
            currentPrestamoId = (int) modelPrestamos.getValueAt(row, 0);
            Prestamo p = control.consultarPrestamo(currentPrestamoId);
            if (p != null) {
                String personaStr = p.getPersona().getNombre() + " (" + p.getPersona().getTelefono() + ")";
                cbPersonaPrestamo.setSelectedItem(personaStr);
                listModelPrestamo.clear();
                for (Item i : p.getItems()) {
                    listModelPrestamo.addElement(i.getCodigo() + " - " + i.getNombre());
                }
                actualizarListasItemsDisponibles();
            }
        } else {
            currentPrestamoId = -1;
            listModelPrestamo.clear();
            actualizarListasItemsDisponibles();
        }
    }

    private void nuevoPrestamo() {
        if (cbPersonaPrestamo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione una persona.");
            return;
        }
        String selected = (String) cbPersonaPrestamo.getSelectedItem();
        String telefono = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
        try {
            Prestamo p = control.crearPrestamo(telefono);
            currentPrestamoId = p.getId();
            actualizarTablaPrestamos();
            for (int i = 0; i < modelPrestamos.getRowCount(); i++) {
                if ((int) modelPrestamos.getValueAt(i, 0) == currentPrestamoId) {
                    tablePrestamos.setRowSelectionInterval(i, i);
                    break;
                }
            }
            JOptionPane.showMessageDialog(frame, "Nuevo préstamo creado con ID " + currentPrestamoId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarItemAlPrestamo() {
        if (currentPrestamoId == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un préstamo o cree uno nuevo.");
            return;
        }
        String selected = listItemsDisponibles.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "Seleccione un ítem disponible.");
            return;
        }
        String codigo = selected.split(" - ")[0];
        try {
            control.agregarItemAPrestamo(currentPrestamoId, codigo);
            cargarPrestamoSeleccionado();
            actualizarTablaPrestamos();
            actualizarTablaItems();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarItemDelPrestamo() {
        if (currentPrestamoId == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un préstamo.");
            return;
        }
        String selected = listItemsPrestamo.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "Seleccione un ítem del préstamo.");
            return;
        }
        String codigo = selected.split(" - ")[0];
        try {
            control.eliminarItemDePrestamo(currentPrestamoId, codigo);
            cargarPrestamoSeleccionado();
            actualizarTablaPrestamos();
            actualizarTablaItems();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizarPrestamo() {
        if (currentPrestamoId == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un préstamo para finalizar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(frame, "¿Finalizar préstamo #" + currentPrestamoId + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                control.finalizarPrestamo(currentPrestamoId);
                currentPrestamoId = -1;
                actualizarTablaPrestamos();
                actualizarListasItemsDisponibles();
                listModelPrestamo.clear();
                actualizarTablaItems();
                JOptionPane.showMessageDialog(frame, "Préstamo finalizado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarAlerta() {
        if (currentPrestamoId == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un préstamo para agregar alerta.");
            return;
        }
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"UNICA", "RECURRENTE"});
        JTextField txtIntervalo = new JTextField("0");
        JTextField txtFecha = new JTextField(
                LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        JTextField txtMensaje = new JTextField("Recordatorio de préstamo");

        panel.add(new JLabel("Tipo:"));
        panel.add(cbTipo);
        panel.add(new JLabel("Intervalo (minutos, solo recurrente):"));
        panel.add(txtIntervalo);
        panel.add(new JLabel("Primera ejecución (yyyy-MM-dd HH:mm):"));
        panel.add(txtFecha);
        panel.add(new JLabel("Mensaje:"));
        panel.add(txtMensaje);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Crear Alerta", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Alarma.TipoAlarma tipo = Alarma.TipoAlarma.valueOf((String) cbTipo.getSelectedItem());
                int intervalo = Integer.parseInt(txtIntervalo.getText());
                LocalDateTime fecha = LocalDateTime.parse(txtFecha.getText(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                control.crearAlarmaParaPrestamo(currentPrestamoId, tipo, intervalo, fecha, txtMensaje.getText());
                actualizarTablaPrestamos();
                JOptionPane.showMessageDialog(frame, "Alerta creada.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
	
    private JPanel crearPanelAdministracion() {
        JPanel panel = new JPanel(new BorderLayout());
        return panel;
    }
	
    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout());
        return panel;
    }
	
	private void cargarDatosIniciales() {
        actualizarCombosPrestamos();
        actualizarListasItemsDisponibles();
        actualizarTablaPrestamos();
    }

	private void configurarCierre() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Controladora.guardarDatos();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void iniciarTimerAlertas() {
        timerAlertas = new Timer(60000, e -> verificarAlertas());
        timerAlertas.start();
        verificarAlertas();
    }

    private void verificarAlertas() {
        List<Alarma> pendientes = control.getAlarmasPendientes(LocalDateTime.now());
        for (Alarma a : pendientes) {
            JOptionPane.showMessageDialog(frame, "ALERTA: " + a.getMensaje(),
                    "Recordatorio", JOptionPane.INFORMATION_MESSAGE);
            if (a.getTipo() == Alarma.TipoAlarma.RECURRENTE) {
                control.reprogramarAlarma(a.getId());
            }
        }
    }
}
