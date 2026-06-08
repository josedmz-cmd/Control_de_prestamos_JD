package logica;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prestamo {
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
    	
    }
    
    public boolean eliminarItem(Item item) {
    	
    }
    
    public boolean retornarItem(Item item) {
    	
    }
    
    public void finalizar() {
    	
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
