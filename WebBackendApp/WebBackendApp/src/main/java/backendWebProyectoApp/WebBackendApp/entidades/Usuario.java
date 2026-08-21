package backendWebProyectoApp.WebBackendApp.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    private String nombre;
    private String apellido;

    @Column(unique = true)
    private String correo;

    private String contraseña;

    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "usuario")
    private List<Venta> ventas;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;
    
}