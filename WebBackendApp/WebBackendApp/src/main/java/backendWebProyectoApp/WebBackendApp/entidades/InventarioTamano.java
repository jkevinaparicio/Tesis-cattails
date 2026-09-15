package backendWebProyectoApp.WebBackendApp.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventarios_tamano",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_tamano", "id_sede"}))
public class InventarioTamano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tamano")
    private Tamano tamano;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sede")
    private Sede sede;

    @Column(nullable = false)
    private Integer stock = 0;
}
