package backendWebProyectoApp.WebBackendApp.entidades;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FechaNegocioUtil {

    private FechaNegocioUtil() {}

    public static LocalDate calcular(LocalDateTime fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        int hora = fecha.getHour();

        boolean madrugadaTrasVieOSab =
                (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) && hora == 0;

        return madrugadaTrasVieOSab
                ? fecha.toLocalDate().minusDays(1)
                : fecha.toLocalDate();
    }
}
