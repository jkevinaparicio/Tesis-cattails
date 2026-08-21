// ConfigPromocion.java
package backendWebProyectoApp.WebBackendApp.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "config_promocion")
@Data
public class ConfigPromocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Boolean lunes     = false;
    private Boolean martes    = true;
    private Boolean miercoles = false;
    private Boolean jueves    = true;
    private Boolean viernes   = false;
    private Boolean sabado    = false;
    private Boolean domingo   = false;
}