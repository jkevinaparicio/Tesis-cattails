package backendWebProyectoApp.WebBackendApp.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "inventarios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_variante", "id_sede"}))
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_variante")
    private ProductoVariante variante;

    @ManyToOne
    @JoinColumn(name = "id_sede")
    private Sede sede;

    @Column(nullable = false)
    private Integer stock = 0;
}
