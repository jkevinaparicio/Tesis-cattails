package backendWebProyectoApp.WebBackendApp.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "compartir_stock_por_tamano", nullable = false)
    private boolean compartirStockPorTamano = false;

    @OneToMany
    private List<Productos> productos;
}
