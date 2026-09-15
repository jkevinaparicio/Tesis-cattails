package backendWebProyectoApp.WebBackendApp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CrearVentaDTO {
    private Integer idSede;
    private String metodoPago;
    private String tipoPedido;
    private Boolean esPromocion;
    private BigDecimal valorDomicilio;
    private List<DetalleRequest> detalles;

    @Data
    public static class DetalleRequest {
        private Integer idVariante;
        private Integer cantidad;
    }
}
