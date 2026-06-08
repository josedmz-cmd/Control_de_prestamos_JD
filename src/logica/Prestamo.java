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
}
