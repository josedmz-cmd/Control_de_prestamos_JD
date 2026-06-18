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

	private JTable tablePersonas;
    private DefaultTableModel modelPersonas;
    private JTextField txtNombrePersona, txtTelefonoPersona, txtCorreoPersona;
    private JTable tableItems;
    private DefaultTableModel modelItems;
    private JTextField txtCodigoItem, txtNombreItem, txtDescripcionItem;
    private JComboBox<String> cbTipoItem;
    private JList<String> listCategoriasItem;
    private DefaultListModel<String> listModelCategoriasItem;
    private JTable tableTipos;
    private DefaultTableModel modelTipos;
    private JTextField txtNombreTipo;
    private JTable tableCategorias;
    private DefaultTableModel modelCategorias;
    private JTextField txtNombreCategoria;
    private JTable tablePrestamos;
    private DefaultTableModel modelPrestamos;
    private JComboBox<String> cbPersonaPrestamo;
    private JList<String> listItemsDisponibles, listItemsPrestamo;
    private DefaultListModel<String> listModelDisponibles, listModelPrestamo;
    private int currentPrestamoId = -1;
    private JTextArea txtReporte;
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
	    JPanel Administración = new JPanel(new BorderLayout());
	    JTabbedPane subTabs = new JTabbedPane();

	    subTabs.addTab("Personas", crearPanelPersonas());
        subTabs.addTab("Ítems", crearPanelItems());
        subTabs.addTab("Tipos", crearPanelTipos());
        subTabs.addTab("Categorías", crearPanelCategorias());

	    Administración.add(subTabs, BorderLayout.CENTER);
	    return Administración;
	}

	private JPanel crearPanelPersonas() {
	    JPanel Personas = new JPanel(new BorderLayout(10, 10));
	    Personas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    modelPersonas = new DefaultTableModel(new String[]{"Nombre", "Teléfono", "Correo"}, 0);
	    tablePersonas = new JTable(modelPersonas);
	    Personas.add(new JScrollPane(tablePersonas), BorderLayout.CENTER);
	  
	    JPanel Datos = new JPanel(new GridLayout(4, 2, 5, 5));
	    Datos.setBorder(BorderFactory.createTitledBorder("Datos de Persona"));
	    Datos.add(new JLabel("Nombre:"));
	    txtNombrePersona = new JTextField();
	    Datos.add(txtNombrePersona);
	    Datos.add(new JLabel("Teléfono:"));
	    txtTelefonoPersona = new JTextField();
	    Datos.add(txtTelefonoPersona);
	    Datos.add(new JLabel("Correo:"));
	    txtCorreoPersona = new JTextField();
	    Datos.add(txtCorreoPersona);

	    JPanel Botones = new JPanel(new FlowLayout());
	    JButton btnCrear = new JButton("Crear");
	    JButton btnModificar = new JButton("Modificar");
	    JButton btnBorrar = new JButton("Borrar");
	    JButton btnLimpiar = new JButton("Limpiar");
	    Botones.add(btnCrear);
	    Botones.add(btnModificar);
	    Botones.add(btnBorrar);
	    Botones.add(btnLimpiar);
	    Datos.add(Botones);

	    Personas.add(Datos, BorderLayout.SOUTH);

	    btnCrear.addActionListener(e -> crearPersona());
	    btnModificar.addActionListener(e -> modificarPersona());
	    btnBorrar.addActionListener(e -> borrarPersona());
	    btnLimpiar.addActionListener(e -> limpiarFormPersonas());
	    tablePersonas.getSelectionModel().addListSelectionListener(e -> cargarPersonaSeleccionada());

	    actualizarTablaPersonas();
	    return Personas;
	}
	
	private void actualizarTablaPersonas() {
	    modelPersonas.setRowCount(0);
	    for (Persona p : control.listarPersonas()) {
	        modelPersonas.addRow(new Object[]{p.getNombre(), p.getTelefono(), p.getCorreo()});
	    }
	    actualizarCombosPrestamos();
	}

	private void cargarPersonaSeleccionada() {
	    int row = tablePersonas.getSelectedRow();
	    if (row != -1) {
	        txtNombrePersona.setText((String) modelPersonas.getValueAt(row, 0));
	        txtTelefonoPersona.setText((String) modelPersonas.getValueAt(row, 1));
	        txtCorreoPersona.setText((String) modelPersonas.getValueAt(row, 2));
	    }
	}

	private void limpiarFormPersonas() {
	    txtNombrePersona.setText("");
	    txtTelefonoPersona.setText("");
	    txtCorreoPersona.setText("");
	    tablePersonas.clearSelection();
	}

	private void crearPersona() {
	    try {
	        control.crearPersona(txtNombrePersona.getText(), txtTelefonoPersona.getText(), txtCorreoPersona.getText());
	        actualizarTablaPersonas();
	        limpiarFormPersonas();
	        JOptionPane.showMessageDialog(frame, "Persona creada exitosamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
	
	private void modificarPersona() {
        int row = tablePersonas.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione una persona para modificar.");
            return;
        }
        String telefonoAntiguo = (String) modelPersonas.getValueAt(row, 1);
        try {
            control.modificarPersona(telefonoAntiguo, txtNombrePersona.getText(),
                    txtTelefonoPersona.getText(), txtCorreoPersona.getText());
            actualizarTablaPersonas();
            limpiarFormPersonas();
            JOptionPane.showMessageDialog(frame, "Persona modificada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarPersona() {
        int row = tablePersonas.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione una persona para borrar.");
            return;
        }
        String telefono = (String) modelPersonas.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(frame, "¿Borrar a " + modelPersonas.getValueAt(row, 0) + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                control.borrarPersona(telefono);
                actualizarTablaPersonas();
                limpiarFormPersonas();
                JOptionPane.showMessageDialog(frame, "Persona borrada.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

	private JPanel crearPanelItems() {
	    JPanel Ítems = new JPanel(new BorderLayout(10, 10));
	    Ítems.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    modelItems = new DefaultTableModel(new String[]{"Código", "Nombre", "Descripción", "Tipo", "Prestado"}, 0);
	    tableItems = new JTable(modelItems);
	    Ítems.add(new JScrollPane(tableItems), BorderLayout.CENTER);

	    JPanel Datos = new JPanel(new GridLayout(6, 2, 5, 5));
        Datos.setBorder(BorderFactory.createTitledBorder("Datos del Ítem"));
        Datos.add(new JLabel("Código:"));
        txtCodigoItem = new JTextField();
        Datos.add(txtCodigoItem);
        Datos.add(new JLabel("Nombre:"));
        txtNombreItem = new JTextField();
        Datos.add(txtNombreItem);
        Datos.add(new JLabel("Descripción:"));
        txtDescripcionItem = new JTextField();
        Datos.add(txtDescripcionItem);
        Datos.add(new JLabel("Tipo:"));
        cbTipoItem = new JComboBox<>();
        Datos.add(cbTipoItem);
        Datos.add(new JLabel("Categorías (Ctrl+clic múltiple):"));
        listModelCategoriasItem = new DefaultListModel<>();
        listCategoriasItem = new JList<>(listModelCategoriasItem);
        listCategoriasItem.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listCategoriasItem.setVisibleRowCount(3);
        Datos.add(new JScrollPane(listCategoriasItem));

	    JPanel Botones = new JPanel(new FlowLayout());
	    JButton btnCrear = new JButton("Crear");
	    JButton btnModificar = new JButton("Modificar");
	    JButton btnBorrar = new JButton("Borrar");
	    JButton btnLimpiar = new JButton("Limpiar");
	    Botones.add(btnCrear);
        Botones.add(btnModificar);
        Botones.add(btnBorrar);
        Botones.add(btnLimpiar);
        Datos.add(Botones);
        
	    Ítems.add(Datos, BorderLayout.SOUTH);

	    btnCrear.addActionListener(e -> crearItem());
        btnModificar.addActionListener(e -> modificarItem());
        btnBorrar.addActionListener(e -> borrarItem());
        btnLimpiar.addActionListener(e -> limpiarFormItems());
        tableItems.getSelectionModel().addListSelectionListener(e -> cargarItemSeleccionado());

        actualizarTablaItems();
        actualizarCombosTipos();
        actualizarListaCategoriasDisponibles();
        return Ítems;
	}
	
	private void actualizarTablaItems() {
        modelItems.setRowCount(0);
        for (Item i : control.listarItems()) {
            modelItems.addRow(new Object[]{i.getCodigo(), i.getNombre(), i.getDescripcion(),
                    i.getTipo().getNombre(), i.isPrestado() ? "Sí" : "No"});
        }
        actualizarListasItemsDisponibles();
    }

    private void cargarItemSeleccionado() {
        int row = tableItems.getSelectedRow();
        if (row != -1) {
            String codigo = (String) modelItems.getValueAt(row, 0);
            Item item = control.consultarItem(codigo);
            if (item != null) {
                txtCodigoItem.setText(item.getCodigo());
                txtNombreItem.setText(item.getNombre());
                txtDescripcionItem.setText(item.getDescripcion());
                cbTipoItem.setSelectedItem(item.getTipo().getNombre());
                List<String> cats = item.getCategorias().stream().map(Categoria::getNombre).toList();
                listCategoriasItem.clearSelection();
                for (int i = 0; i < listModelCategoriasItem.size(); i++) {
                    if (cats.contains(listModelCategoriasItem.get(i))) {
                        listCategoriasItem.addSelectionInterval(i, i);
                    }
                }
            }
        }
    }

    private void limpiarFormItems() {
        txtCodigoItem.setText("");
        txtNombreItem.setText("");
        txtDescripcionItem.setText("");
        cbTipoItem.setSelectedIndex(0);
        listCategoriasItem.clearSelection();
        tableItems.clearSelection();
    }

    private void crearItem() {
        try {
            List<String> cats = listCategoriasItem.getSelectedValuesList();
            control.crearItem(txtCodigoItem.getText(), txtNombreItem.getText(), txtDescripcionItem.getText(),
                    (String) cbTipoItem.getSelectedItem(), cats);
            actualizarTablaItems();
            limpiarFormItems();
            JOptionPane.showMessageDialog(frame, "Ítem creado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarItem() {
        int row = tableItems.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un ítem para modificar.");
            return;
        }
        String codigoAntiguo = (String) modelItems.getValueAt(row, 0);
        try {
            List<String> cats = listCategoriasItem.getSelectedValuesList();
            control.modificarItem(codigoAntiguo, txtCodigoItem.getText(), txtNombreItem.getText(),
                    txtDescripcionItem.getText(), (String) cbTipoItem.getSelectedItem(), cats);
            actualizarTablaItems();
            limpiarFormItems();
            JOptionPane.showMessageDialog(frame, "Ítem modificado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarItem() {
        int row = tableItems.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un ítem para borrar.");
            return;
        }
        String codigo = (String) modelItems.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(frame, "¿Borrar ítem " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                control.borrarItem(codigo);
                actualizarTablaItems();
                limpiarFormItems();
                JOptionPane.showMessageDialog(frame, "Ítem borrado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JPanel crearPanelTipos() {
        JPanel Tipos = new JPanel(new BorderLayout(10, 10));
        Tipos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelTipos = new DefaultTableModel(new String[]{"Nombre"}, 0);
        tableTipos = new JTable(modelTipos);
        Tipos.add(new JScrollPane(tableTipos), BorderLayout.CENTER);

        JPanel Datos = new JPanel(new GridLayout(2, 2, 5, 5));
        Datos.setBorder(BorderFactory.createTitledBorder("Datos del Tipo"));
        Datos.add(new JLabel("Nombre:"));
        txtNombreTipo = new JTextField();
        Datos.add(txtNombreTipo);

        JPanel Botones = new JPanel(new FlowLayout());
        JButton btnCrear = new JButton("Crear");
        JButton btnModificar = new JButton("Modificar");
        JButton btnBorrar = new JButton("Borrar");
        JButton btnLimpiar = new JButton("Limpiar");
        Botones.add(btnCrear);
        Botones.add(btnModificar);
        Botones.add(btnBorrar);
        Botones.add(btnLimpiar);
        Datos.add(Botones);

        Tipos.add(Datos, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearTipo());
        btnModificar.addActionListener(e -> modificarTipo());
        btnBorrar.addActionListener(e -> borrarTipo());
        btnLimpiar.addActionListener(e -> limpiarFormTipos());
        tableTipos.getSelectionModel().addListSelectionListener(e -> cargarTipoSeleccionado());

        actualizarTablaTipos();
        return Tipos;
    }

    private void actualizarTablaTipos() {
        modelTipos.setRowCount(0);
        for (Tipo t : control.listarTipos()) {
            modelTipos.addRow(new Object[]{t.getNombre()});
        }
        actualizarCombosTipos();
    }

    private void cargarTipoSeleccionado() {
        int row = tableTipos.getSelectedRow();
        if (row != -1) {
            txtNombreTipo.setText((String) modelTipos.getValueAt(row, 0));
        }
    }

    private void limpiarFormTipos() {
        txtNombreTipo.setText("");
        tableTipos.clearSelection();
    }

    private void crearTipo() {
        try {
            control.crearTipo(txtNombreTipo.getText());
            actualizarTablaTipos();
            limpiarFormTipos();
            JOptionPane.showMessageDialog(frame, "Tipo creado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarTipo() {
        int row = tableTipos.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un tipo para modificar.");
            return;
        }
        String nombreAntiguo = (String) modelTipos.getValueAt(row, 0);
        try {
            control.modificarTipo(nombreAntiguo, txtNombreTipo.getText());
            actualizarTablaTipos();
            limpiarFormTipos();
            JOptionPane.showMessageDialog(frame, "Tipo modificado.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarTipo() {
        int row = tableTipos.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione un tipo para borrar.");
            return;
        }
        String nombre = (String) modelTipos.getValueAt(row, 0);
        if ("Genérico".equals(nombre)) {
            JOptionPane.showMessageDialog(frame, "No se puede borrar el tipo Genérico.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(frame, "¿Borrar tipo '" + nombre + "'? Los ítems pasarán al tipo 'Genérico'.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                control.borrarTipo(nombre);
                actualizarTablaTipos();
                limpiarFormTipos();
                JOptionPane.showMessageDialog(frame, "Tipo borrado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
	
    private JPanel crearPanelCategorias() {
        JPanel Categorías = new JPanel(new BorderLayout(10, 10));
        Categorías.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelCategorias = new DefaultTableModel(new String[]{"Nombre"}, 0);
        tableCategorias = new JTable(modelCategorias);
        Categorías.add(new JScrollPane(tableCategorias), BorderLayout.CENTER);

        JPanel Datos = new JPanel(new GridLayout(2, 2, 5, 5));
        Datos.setBorder(BorderFactory.createTitledBorder("Datos de la Categoría"));
        Datos.add(new JLabel("Nombre:"));
        txtNombreCategoria = new JTextField();
        Datos.add(txtNombreCategoria);

        JPanel Botones = new JPanel(new FlowLayout());
        JButton btnCrear = new JButton("Crear");
        JButton btnModificar = new JButton("Modificar");
        JButton btnBorrar = new JButton("Borrar");
        JButton btnLimpiar = new JButton("Limpiar");
        Botones.add(btnCrear);
        Botones.add(btnModificar);
        Botones.add(btnBorrar);
        Botones.add(btnLimpiar);
        Datos.add(Botones);

        Categorías.add(Datos, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crearCategoria());
        btnModificar.addActionListener(e -> modificarCategoria());
        btnBorrar.addActionListener(e -> borrarCategoria());
        btnLimpiar.addActionListener(e -> limpiarFormCategorias());
        tableCategorias.getSelectionModel().addListSelectionListener(e -> cargarCategoriaSeleccionada());

        actualizarTablaCategorias();
        return Categorías;
    }

    private void actualizarTablaCategorias() {
        modelCategorias.setRowCount(0);
        for (Categoria c : control.listarCategorias()) {
            modelCategorias.addRow(new Object[]{c.getNombre()});
        }
        actualizarListaCategoriasDisponibles();
    }

    private void cargarCategoriaSeleccionada() {
        int row = tableCategorias.getSelectedRow();
        if (row != -1) {
            txtNombreCategoria.setText((String) modelCategorias.getValueAt(row, 0));
        }
    }

    private void limpiarFormCategorias() {
        txtNombreCategoria.setText("");
        tableCategorias.clearSelection();
    }

    private void crearCategoria() {
        try {
            control.crearCategoria(txtNombreCategoria.getText());
            actualizarTablaCategorias();
            limpiarFormCategorias();
            JOptionPane.showMessageDialog(frame, "Categoría creada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarCategoria() {
        int row = tableCategorias.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione una categoría para modificar.");
            return;
        }
        String nombreAntiguo = (String) modelCategorias.getValueAt(row, 0);
        try {
            control.modificarCategoria(nombreAntiguo, txtNombreCategoria.getText());
            actualizarTablaCategorias();
            limpiarFormCategorias();
            JOptionPane.showMessageDialog(frame, "Categoría modificada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarCategoria() {
        int row = tableCategorias.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(frame, "Seleccione una categoría para borrar.");
            return;
        }
        String nombre = (String) modelCategorias.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(frame, "¿Borrar categoría '" + nombre + "'?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                control.borrarCategoria(nombre);
                actualizarTablaCategorias();
                limpiarFormCategorias();
                JOptionPane.showMessageDialog(frame, "Categoría borrada.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
	
	private void actualizarCombosTipos() {
	    cbTipoItem.removeAllItems();
	    for (Tipo t : control.listarTipos()) {
	        cbTipoItem.addItem(t.getNombre());
	    }
	}

	private void actualizarListaCategoriasDisponibles() {
	    listModelCategoriasItem.clear();
	    for (Categoria c : control.listarCategorias()) {
	        listModelCategoriasItem.addElement(c.getNombre());
	    }
	}
	
	private JPanel crearPanelReportes() {
        JPanel Reportes = new JPanel(new BorderLayout(10, 10));
        Reportes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel Botones = new JPanel(new FlowLayout());
        JButton btnUsuarios = new JButton("Por Usuario");
        JButton btnItems = new JButton("Por Ítem");
        JButton btnCategorias = new JButton("Por Categoría");
        JButton btnTipos = new JButton("Por Tipo");
        Botones.add(btnUsuarios);
        Botones.add(btnItems);
        Botones.add(btnCategorias);
        Botones.add(btnTipos);
        Reportes.add(Botones, BorderLayout.NORTH);

        txtReporte = new JTextArea();
        txtReporte.setEditable(false);
        Reportes.add(new JScrollPane(txtReporte), BorderLayout.CENTER);

        btnUsuarios.addActionListener(e -> txtReporte.setText(control.reportePorUsuario()));
        btnItems.addActionListener(e -> txtReporte.setText(control.reportePorItem()));
        btnCategorias.addActionListener(e -> txtReporte.setText(control.reportePorCategoria()));
        btnTipos.addActionListener(e -> txtReporte.setText(control.reportePorTipo()));

        return Reportes;
    }
	
	private void cargarDatosIniciales() {
        actualizarTablaPersonas();
        actualizarTablaItems();
        actualizarTablaCategorias();
        actualizarTablaTipos();
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
