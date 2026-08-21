package backendWebProyectoApp.WebBackendApp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaDTO {
    private Integer id;
    private String usuario;
    private String sede;
    private BigDecimal total;
    private String metodoPago;
    private String tipoPedido;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime fecha;
    private List<DetalleVentaDTO> detalles;
}
