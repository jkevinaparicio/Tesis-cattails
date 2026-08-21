package backendWebProyectoApp.WebBackendApp.servicios;


import backendWebProyectoApp.WebBackendApp.entidades.Sede;
import backendWebProyectoApp.WebBackendApp.repositorios.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SedeService {

    private final SedeRepository sedeRepository;

    public List<Sede> listar() {
        return sedeRepository.findAll();
    }

    public Sede crear(Sede sede) {
        return sedeRepository.save(sede);
    }

    public Sede actualizar(Integer id, Sede sede) {
        Sede existente = sedeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));

        existente.setNombre(sede.getNombre());

        return sedeRepository.save(existente);
    }

    public void eliminar(Integer id) {
        sedeRepository.deleteById(id);
    }
}
