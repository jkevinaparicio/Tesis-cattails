package backendWebProyectoApp.WebBackendApp.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sedes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sede")
    private Integer id;

    @Column(nullable = false)
    private String nombre;

}
