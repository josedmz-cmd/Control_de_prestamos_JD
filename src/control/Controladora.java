package control;

import logica.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class Controladora implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Controladora instance = null;
	private Map<String, Persona> personas;
    private Map<String, Item> items;
    private Map<String, Categoria> categorias;
    private Map<String, Tipo> tipos;
    private List<Prestamo> prestamos;
    private List<Alarma> alarmas;
    private int nextIdPrestamo;
    private int nextIdAlerta;
    
    private Controladora() {
    	personas = new TreeMap<>();
        items = new TreeMap<>();
        categorias = new TreeMap<>();
        tipos = new TreeMap<>();
        prestamos = new ArrayList<>();
        alarmas = new ArrayList<>();
        nextIdPrestamo = 1;
        nextIdAlerta = 1;
        if (!tipos.containsKey("Genérico")) {
            Tipo generico = new Tipo("Genérico");
            tipos.put("Genérico", generico);
        }
    }
    
    public static Controladora getInstance() {
        if (instance == null) 
            instance = new Controladora();
        return instance;
    }
    
    public void crearPersona(String nombre, String telefono, String correo) {
        if (personas.containsKey(telefono)) {
            throw new IllegalArgumentException("Ya existe una persona con ese teléfono.");
        }
        Persona p = new Persona(nombre, telefono, correo);
        personas.put(telefono, p);
    }
    
    public void modificarPersona(String telefono, String nuevoNombre, String nuevoTelefono, String nuevoCorreo) {
        Persona p = personas.get(telefono);
        if (p == null) 
        	throw new IllegalArgumentException("Persona no encontrada.");
        if (!telefono.equals(nuevoTelefono) && personas.containsKey(nuevoTelefono)) {
            throw new IllegalArgumentException("El nuevo teléfono ya está registrado.");
        }
        if (!telefono.equals(nuevoTelefono)) {
            personas.remove(telefono);
            p.setTelefono(nuevoTelefono);
            personas.put(nuevoTelefono, p);
        }
        p.setNombre(nuevoNombre);
        p.setCorreo(nuevoCorreo);
    }
    
    public void borrarPersona(String telefono) {
        Persona p = personas.get(telefono);
        if (p == null) 
        	return;
        if (!p.puedeSerEliminada()) {
            throw new IllegalStateException("No se puede eliminar la persona porque tiene préstamos activos.");
        }
        personas.remove(telefono);
    }
    
    public Persona consultarPersona(String telefono) {
        return personas.get(telefono);
    }
    
    public List<Persona> listarPersonas() {
        return new ArrayList<>(personas.values());
    }
    
    public void crearItem(String codigo, String nombre, String descripcion, String nombreTipo, List<String> nombresCategorias) {
        if (items.containsKey(codigo)) {
            throw new IllegalArgumentException("Ya existe un ítem con ese código.");
        }
        Tipo tipo = tipos.get(nombreTipo);
        if (tipo == null)
        	throw new IllegalArgumentException("El tipo especificado no existe.");
        Item item = new Item(codigo, nombre, descripcion, tipo);
        for (String catNombre : nombresCategorias) {
            Categoria cat = categorias.get(catNombre);
            if (cat != null) {
                item.agregarCategoria(cat);
                cat.agregarItem(item);
            }
        }
        tipo.agregarItem(item);
        items.put(codigo, item);
    }

    public void modificarItem(String codigo, String nuevoCodigo, String nuevoNombre, String nuevaDescripcion, String nuevoNombreTipo, List<String> nuevosNombresCategorias) {
        Item item = items.get(codigo);
        if (item == null)
        	throw new IllegalArgumentException("Ítem no encontrado.");
        if (!codigo.equals(nuevoCodigo) && items.containsKey(nuevoCodigo)) {
            throw new IllegalArgumentException("El nuevo código ya está en uso.");
        }
        Tipo nuevoTipo = tipos.get(nuevoNombreTipo);
        if (nuevoTipo == null)
        	throw new IllegalArgumentException("Tipo no existe.");
        if (nuevoTipo != item.getTipo()) {
            item.getTipo().eliminarItem(item);
            item.setTipo(nuevoTipo);
            nuevoTipo.agregarItem(item);
        }
        Set<Categoria> viejas = new HashSet<>(item.getCategorias());
        for (Categoria cat : viejas) {
            cat.eliminarItem(item);
        }
        item.getCategorias().clear();
        for (String catNombre : nuevosNombresCategorias) {
            Categoria cat = categorias.get(catNombre);
            if (cat != null) {
                item.agregarCategoria(cat);
                cat.agregarItem(item);
            }
        }
        if (!codigo.equals(nuevoCodigo)) {
            items.remove(codigo);
            item.setCodigo(nuevoCodigo);
            items.put(nuevoCodigo, item);
        }
        item.setNombre(nuevoNombre);
        item.setDescripcion(nuevaDescripcion);
    }

    public void borrarItem(String codigo) {
        Item item = items.get(codigo);
        if (item == null) 
        	return;
        if (!item.puedeSerEliminado()) {
            throw new IllegalStateException("No se puede eliminar el ítem porque está prestado.");
        }
        for (Categoria cat : item.getCategorias()) {
            cat.eliminarItem(item);
        }
        item.getTipo().eliminarItem(item);
        items.remove(codigo);
    }

    public Item consultarItem(String codigo) {
        return items.get(codigo);
    }

    public List<Item> listarItems() {
        return new ArrayList<>(items.values());
    }
    
    public void crearCategoria(String nombre) {
        if (categorias.containsKey(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        Categoria c = new Categoria(nombre);
        categorias.put(nombre, c);
    }

    public void modificarCategoria(String nombre, String nuevoNombre) {
        Categoria c = categorias.get(nombre);
        if (c == null)
        	throw new IllegalArgumentException("Categoría no encontrada.");
        if (!nombre.equals(nuevoNombre) && categorias.containsKey(nuevoNombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con el nuevo nombre.");
        }
        if (!nombre.equals(nuevoNombre)) {
            categorias.remove(nombre);
            c.setNombre(nuevoNombre);
            categorias.put(nuevoNombre, c);
        }
    }

    public void borrarCategoria(String nombre) {
        Categoria c = categorias.get(nombre);
        if (c == null)
        	return;
        for (Item item : c.getItems()) {
            item.eliminarCategoria(c);
        }
        categorias.remove(nombre);
    }

    public Categoria consultarCategoria(String nombre) {
        return categorias.get(nombre);
    }

    public List<Categoria> listarCategorias() {
        return new ArrayList<>(categorias.values());
    }
    
    public void crearTipo(String nombre) {
        if (tipos.containsKey(nombre)) {
            throw new IllegalArgumentException("Ya existe un tipo con ese nombre.");
        }
        Tipo t = new Tipo(nombre);
        tipos.put(nombre, t);
    }

    public void modificarTipo(String nombre, String nuevoNombre) {
        if ("Genérico".equals(nombre)) {
            throw new IllegalStateException("No se puede modificar el nombre del tipo Genérico.");
        }
        Tipo t = tipos.get(nombre);
        if (t == null)
        	throw new IllegalArgumentException("Tipo no encontrado.");
        if (!nombre.equals(nuevoNombre) && tipos.containsKey(nuevoNombre)) {
            throw new IllegalArgumentException("Ya existe un tipo con el nuevo nombre.");
        }
        if (!nombre.equals(nuevoNombre)) {
            tipos.remove(nombre);
            t.setNombre(nuevoNombre);
            tipos.put(nuevoNombre, t);
        }
    }

    public void borrarTipo(String nombre) {
        if ("Genérico".equals(nombre)) {
            throw new IllegalStateException("No se puede borrar el tipo Genérico.");
        }
        Tipo t = tipos.get(nombre);
        if (t == null)
        	return;
        Tipo generico = tipos.get("Genérico");
        List<Item> itemsATrasladar = new ArrayList<>(t.getItems());
        for (Item item : itemsATrasladar) {
            t.eliminarItem(item);
            item.setTipo(generico);
            generico.agregarItem(item);
        }
        tipos.remove(nombre);
    }

    public Tipo consultarTipo(String nombre) {
        return tipos.get(nombre);
    }

    public List<Tipo> listarTipos() {
        return new ArrayList<>(tipos.values());
    }
    
    public Prestamo crearPrestamo(String telefonoPersona) {
        Persona persona = personas.get(telefonoPersona);
        if (persona == null)
        	throw new IllegalArgumentException("Persona no encontrada.");
        Prestamo p = new Prestamo(persona);
        p.setId(nextIdPrestamo++);
        prestamos.add(p);
        persona.agregarPrestamo(p);
        return p;
    }

    public void agregarItemAPrestamo(int idPrestamo, String codigoItem) {
        Prestamo prestamo = buscarPrestamoPorId(idPrestamo);
        Item item = items.get(codigoItem);
        if (prestamo == null)
        	throw new IllegalArgumentException("Préstamo no encontrado.");
        if (item == null)
        	throw new IllegalArgumentException("Ítem no encontrado.");
        if (!prestamo.isActivo())
        	throw new IllegalStateException("El préstamo ya está finalizado.");
        if (item.isPrestado())
        	throw new IllegalStateException("El ítem ya está prestado.");
        prestamo.agregarItem(item);
    }

    public void eliminarItemDePrestamo(int idPrestamo, String codigoItem) {
        Prestamo prestamo = buscarPrestamoPorId(idPrestamo);
        Item item = items.get(codigoItem);
        if (prestamo != null && item != null && prestamo.isActivo()) {
            prestamo.eliminarItem(item);
        }
    }

    public void retornarItemDePrestamo(int idPrestamo, String codigoItem) {
        eliminarItemDePrestamo(idPrestamo, codigoItem);
    }

    public void finalizarPrestamo(int idPrestamo) {
        Prestamo prestamo = buscarPrestamoPorId(idPrestamo);
        if (prestamo != null && prestamo.isActivo()) {
            prestamo.finalizar();
        }
    }

    public List<Prestamo> listarPrestamos() {
        return new ArrayList<>(prestamos);
    }

    public Prestamo consultarPrestamo(int id) {
        return buscarPrestamoPorId(id);
    }
    
    public void crearAlarmaParaPrestamo(int idPrestamo, Alarma.TipoAlarma tipo, int intervaloMinutos,
            LocalDateTime primeraEjecucion, String mensaje) {
    	Prestamo prestamo = buscarPrestamoPorId(idPrestamo);
    	if (prestamo == null)
    		throw new IllegalArgumentException("Préstamo no existe.");
    	if (!prestamo.isActivo()) throw new IllegalStateException("No se pueden agregar alertas a un préstamo finalizado.");
    	Alarma alerta = new Alarma(tipo, intervaloMinutos, primeraEjecucion, mensaje);
    	alerta.setId(nextIdAlerta++);
    	alarmas.add(alerta);
    	prestamo.setAlarma(alerta);
    }

    public List<Alarma> getAlarmasPendientes(LocalDateTime ahora) {
    	List<Alarma> pendientes = new ArrayList<>();
        for (Alarma a : alarmas) {
            if (a.debeMostrarse(ahora)) {
                pendientes.add(a);
            }
        }
        return pendientes;
    }

    public void reprogramarAlarma(int idAlarma) {
	Alarma alarma = buscarAlarmaPorId(idAlarma);
	if (alarma != null && alarma.getTipo() == Alarma.TipoAlarma.RECURRENTE) {
		alarma.reprogramar();
		}
	}
    
    
    public String reportePorUsuario() {
    	String resultado = "REPORTE POR USUARIO\n";
        List<Persona> sorted = new ArrayList<>(personas.values());
        sorted.sort(Comparator.comparing(Persona::getNombre));
        for (Persona p : sorted) {
            resultado += "Persona: " + p.getNombre() + ", Tel: " + p.getTelefono() + ", Email: " + p.getCorreo() + "\n";
            List<Prestamo> activos = p.getPrestamosActivos();
            if (activos.isEmpty()) {
                resultado += "  Sin préstamos activos\n";
            } else {
                for (Prestamo prest : activos) {
                    resultado += "  Préstamo #" + prest.getId() + " (Inicio: " + prest.getFechaInicio() + ")\n";
                    for (Item item : prest.getItems()) {
                        resultado += "    - " + item.getNombre() + " (Código: " + item.getCodigo() + ")\n";
                    }
                }
            }
            resultado += "\n";
        }
        return resultado;
    }

    public String reportePorItem() {
    	String resultado = "REPORTE POR ÍTEM\n";
    	List<Item> sorted = new ArrayList<>(items.values());
        sorted.sort(Comparator.comparing(Item::getNombre));
        for (Item i : sorted) {
            resultado += "Ítem: " + i.getNombre() + ", Código: " + i.getCodigo() + ", Descripción: " + i.getDescripcion() + ", Prestado: " + (i.isPrestado() ? "Sí" : "No");
            if (i.isPrestado() && i.getPrestamoActual() != null) {
                resultado += ", Usuario: " + i.getPrestamoActual().getPersona().getNombre();
            }
            resultado += "\n";
        }
        return resultado;
    }

    public String reportePorCategoria() {
    	String resultado = "REPORTE POR CATEGORÍA\n";
    	List<Categoria> sorted = new ArrayList<>(categorias.values());
        sorted.sort(Comparator.comparing(Categoria::getNombre));
        for (Categoria cat : sorted) {
            resultado += "Categoría: " + cat.getNombre() + "\n";
            if (cat.getItems().isEmpty()) {
                resultado += "  Sin ítems\n";
            } else {
                for (Item i : cat.getItems()) {
                    resultado += "  - " + i.getNombre() + " (Código: " + i.getCodigo() + ")\n";
                }
            }
            resultado += "\n";
        }
        return resultado;
    }

    public String reportePorTipo() {
    	String resultado = "REPORTE POR TIPO\n";
    	List<Tipo> sorted = new ArrayList<>(tipos.values());
        sorted.sort(Comparator.comparing(Tipo::getNombre));
        for (Tipo t : sorted) {
            resultado += "Tipo: " + t.getNombre() + "\n";
            if (t.getItems().isEmpty()) {
                resultado += "  Sin ítems\n";
            } else {
                for (Item i : t.getItems()) {
                    resultado += "  - " + i.getNombre() + " (Código: " + i.getCodigo() + ")\n";
                }
            }
            resultado += "\n";
        }
        return resultado;
    }
    
    private Prestamo buscarPrestamoPorId(int id) {
    	for (Prestamo p : prestamos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private Alarma buscarAlarmaPorId(int id) {
    	for (Alarma a : alarmas) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }
    
    public static void guardarDatos() throws IOException {
        FileOutputStream file = new FileOutputStream("Prestamos.dat");
        ObjectOutputStream stream = new ObjectOutputStream(file);
        stream.writeObject(instance);
        stream.close();
        file.close();
    }
    
    public static void cargarDatos() throws IOException, ClassNotFoundException {
        FileInputStream file = new FileInputStream("Prestamos.dat");
        ObjectInputStream stream = new ObjectInputStream(file);
        instance = (Controladora) stream.readObject();
        stream.close();
        file.close();
    }
}
