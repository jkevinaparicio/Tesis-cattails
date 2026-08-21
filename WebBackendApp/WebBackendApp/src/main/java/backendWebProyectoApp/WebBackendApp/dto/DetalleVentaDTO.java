package backendWebProyectoApp.WebBackendApp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaDTO {

    private Integer id;
    private String producto;
    private String tamaño;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}