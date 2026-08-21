package backendWebProyectoApp.WebBackendApp.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_detalle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_venta")
    @JsonBackReference
    private Venta venta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Productos producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tamaño")
    private Tamaños tamaño;

    private Integer cantidad;

    @Column(name = "precioUnitario")
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
