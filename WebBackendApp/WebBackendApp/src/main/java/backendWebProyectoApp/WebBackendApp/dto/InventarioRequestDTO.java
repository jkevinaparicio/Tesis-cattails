package backendWebProyectoApp.WebBackendApp.dto;

import lombok.Data;

@Data
public class InventarioRequestDTO {

    private Integer idVariante;
    private Integer idSede;
    private Integer stock;
}
