package backendWebProyectoApp.WebBackendApp.dto;

import lombok.Data;

@Data
public class MovimientoStockDTO {
    private Integer varianteId;
    private Integer sedeId;
    private Integer cantidad;
}