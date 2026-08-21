package backendWebProyectoApp.WebBackendApp.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tamaños")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tamaños {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tamaño")
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "tamaño")
    private List<DetalleVenta> detalleVentas;

}