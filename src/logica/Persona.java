package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Persona implements Serializable {
	private static final long serialVersionUID = 1L;
	private String nombre;
    private String telefono;
    private String correo;
    private List<Prestamo> prestamos;
    
    public Persona(String nombre, String telefono, String correo) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.prestamos = new ArrayList<>();
    }

    public boolean puedeSerEliminada() {
    	for (Prestamo p : prestamos) {
    		if (p.isActivo()) {
                return false;
    		}
    	}
    	return true;
    }
    
    public void agregarPrestamo(Prestamo p) {
        if (!prestamos.contains(p)) {
            prestamos.add(p);
        }
    }

    public void eliminarPrestamo(Prestamo p) {
        prestamos.remove(p);
    }
    
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public List<Prestamo> getPrestamos() {
		return prestamos;
	}
	
	public List<Prestamo> getPrestamosActivos() {
	    List<Prestamo> activos = new ArrayList<>();
	    for (Prestamo p : prestamos) {
	        if (p.isActivo()) {
	            activos.add(p);
	        }
	    }
	    return activos;
	}
}
