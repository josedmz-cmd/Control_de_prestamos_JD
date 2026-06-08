package logica;

import java.time.LocalDateTime;

public class Alarma {
	public enum TipoAlerta { UNICA, RECURRENTE }
	private int id;
    private TipoAlerta tipo;
    private int intervaloMinutos;
    private LocalDateTime proximaEjecucion;
    private String mensaje;
    
    public Alarma(TipoAlerta tipo, int intervaloMinutos, LocalDateTime primeraEjecucion, String mensaje) {
        this.tipo = tipo;
        this.intervaloMinutos = (tipo == TipoAlerta.RECURRENTE) ? intervaloMinutos : 0;
        this.proximaEjecucion = primeraEjecucion;
        this.mensaje = mensaje;
    }
    
    public void reprogramar() {
    	
    }
    
    public void cancelar() {
    	
    }
    
    public boolean isActiva() {
    	
    }
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TipoAlerta getTipo() {
		return tipo;
	}

	public void setTipo(TipoAlerta tipo) {
		this.tipo = tipo;
	}

	public int getIntervaloMinutos() {
		return intervaloMinutos;
	}

	public void setIntervaloMinutos(int intervaloMinutos) {
		this.intervaloMinutos = intervaloMinutos;
	}

	public LocalDateTime getProximaEjecucion() {
		return proximaEjecucion;
	}

	public void setProximaEjecucion(LocalDateTime proximaEjecucion) {
		this.proximaEjecucion = proximaEjecucion;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
    
    
}
