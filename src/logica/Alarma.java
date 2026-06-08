package logica;

import java.time.LocalDateTime;

public class Alarma {
	public enum TipoAlerta { UNICA, RECURRENTE }
	private int id;
    private TipoAlerta tipo;
    private int intervaloMinutos;
    private LocalDateTime proximaEjecucion;
    private String mensaje;
}
