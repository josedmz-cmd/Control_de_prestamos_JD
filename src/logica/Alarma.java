package logica;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Alarma implements Serializable {
	private static final long serialVersionUID = 1L;
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
    
    public boolean debeMostrarse(LocalDateTime ahora) {
        return ahora != null && proximaEjecucion != null && !ahora.isBefore(proximaEjecucion);
    }
    
    public void reprogramar() {
    	if (tipo == TipoAlerta.RECURRENTE && intervaloMinutos > 0) {
            this.proximaEjecucion = this.proximaEjecucion.plusMinutes(intervaloMinutos);
        }
    }
    
    public void cancelar() {
    	this.proximaEjecucion = null;
    }
    
    public boolean isActiva() {
    	return proximaEjecucion != null;
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
