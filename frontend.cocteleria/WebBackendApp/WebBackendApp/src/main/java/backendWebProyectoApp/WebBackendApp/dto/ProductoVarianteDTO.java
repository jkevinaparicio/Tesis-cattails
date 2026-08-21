package backendWebProyectoApp.WebBackendApp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoVarianteDTO {

    private Integer idProducto;
    private Integer idTamano;
    private BigDecimal precio;
}