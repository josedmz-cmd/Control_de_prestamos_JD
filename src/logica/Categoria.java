package logica;

import java.util.ArrayList;
import java.util.List;

public class Categoria {
	private String nombre;
    private List<Item> items;
    
    public Categoria(String nombre) {
        this.nombre = nombre;
        this.items = new ArrayList<>();
    }
    
    public void agregarItem(Item i) {
    	
    }
    
    public void eliminarItem(Item i) {
    	
    }

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
}
