package backendWebProyectoApp.WebBackendApp.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Entity
@Table(name = "producto_variantes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_producto", "id_tamano"}))
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Productos producto;

    @ManyToOne
    @JoinColumn(name = "id_tamaño")
    private Tamaños tamaño;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "precio_promo")
    private BigDecimal precioPromo;
}
