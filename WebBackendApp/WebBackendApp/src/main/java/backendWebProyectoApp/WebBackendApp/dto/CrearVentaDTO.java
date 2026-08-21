package backendWebProyectoApp.WebBackendApp.dto;

import lombok.Data;

import java.util.List;

@Data
public class CrearVentaDTO {
    private Integer idSede;
    private String metodoPago;
    private String tipoPedido;
    private Boolean esPromocion;
    private List<DetalleRequest> detalles;

    @Data
    public static class DetalleRequest {
        private Integer idVariante;
        private Integer cantidad;
    }
}