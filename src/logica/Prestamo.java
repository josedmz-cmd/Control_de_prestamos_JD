package logica;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prestamo implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
    private Persona persona;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean activo;
    private List<Item> items;
    private Alarma alarma;
    
    public Prestamo(Persona persona) {
        this.persona = persona;
        this.fechaInicio = LocalDateTime.now();
        this.activo = true;
        this.items = new ArrayList<>();
        this.alarma = null;
    }
    
    public boolean agregarItem(Item item) {
    	if (!activo)
    		return false;
    	if (item == null || item.isPrestado())
    		return false;
    	items.add(item);
        item.marcarPrestado(this);
        return true;
    }
    
    public boolean eliminarItem(Item item) {
    	if (!activo)
    		return false;
        if (items.remove(item)) {
            item.marcarDisponible();
            return true;
        }
        return false;
    }
    
    public boolean retornarItem(Item item) {
    	return eliminarItem(item);
    }
    
    public void finalizar() {
    	if (!activo)
    		return;
        for (Item item : items) {
            item.marcarDisponible();
        }
        items.clear();
        this.activo = false;
        this.fechaFin = LocalDateTime.now();
        if (alarma != null) {
            alarma.cancelar();
            this.alarma = null;
        }
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Persona getPersona() {
		return persona;
	}

	public void setPersona(Persona persona) {
		this.persona = persona;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public Alarma getAlarma() {
		return alarma;
	}

	public void setAlarma(Alarma alarma) {
		this.alarma = alarma;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public boolean isActivo() {
		return activo;
	}
}
