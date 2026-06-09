package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	private String codigo;
    private String nombre;
    private String descripcion;
    private Tipo tipo;
    private List<Categoria> categorias;
    private Prestamo prestamoActual;
    
    public Item(String codigo, String nombre, String descripcion, Tipo tipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.categorias = new ArrayList<>();
        this.prestamoActual = null;
    }
    
    public boolean isPrestado() {
    	return prestamoActual != null;
    }
    
    public boolean puedeSerEliminado() {
    	return !isPrestado();
    }
    
    public void marcarPrestado(Prestamo prestamo) {
    	if (!isPrestado() && prestamo != null) {
            this.prestamoActual = prestamo;
        }
    }
    
    public void marcarDisponible() {
    	this.prestamoActual = null;
    }
    
    public void agregarCategoria(Categoria c) {
    	if (!categorias.contains(c)) categorias.add(c);
    }
    
    public void eliminarCategoria(Categoria c) {
    	categorias.remove(c);
    }

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public List<Categoria> getCategorias() {
		return categorias;
	}

	public void setCategorias(List<Categoria> categorias) {
		this.categorias = categorias;
	}

	public Prestamo getPrestamoActual() {
		return prestamoActual;
	}
}
