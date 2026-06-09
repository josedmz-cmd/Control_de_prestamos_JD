package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tipo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String nombre;
    private List<Item> items;
    
    public Tipo(String nombre) {
        this.nombre = nombre;
        this.items = new ArrayList<>();
    }
    
    public void agregarItem(Item i) {
    	if (!items.contains(i)) 
    		items.add(i);
    }
    
    public void eliminarItem(Item i) {
    	items.remove(i);
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
