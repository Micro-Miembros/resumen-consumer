package co.analisys.gimnasio.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public class ResumenEntrenamiento {
    private String miembroId;
    private int totalSesiones;
    private int totalMinutos;
    private int totalCalorias;
    private double promedioSesiones;
}