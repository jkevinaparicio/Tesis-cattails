package backendWebProyectoApp.WebBackendApp.entidades;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    private BigDecimal total;

    @JsonManagedReference
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalleVentas;

    @Column(name = "local_date_time")
    private LocalDateTime fecha;

    @Column(name = "fecha_negocio")
    private LocalDate fechaNegocio;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "tipo_pedido")
    private String tipoPedido;

    @PrePersist
    @PreUpdate
    private void calcularFechaNegocio() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        this.fechaNegocio = FechaNegocioUtil.calcular(this.fecha);
    }
}
