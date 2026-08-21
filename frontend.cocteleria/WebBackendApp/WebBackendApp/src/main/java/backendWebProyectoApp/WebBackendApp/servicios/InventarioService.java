package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.Inventario;
import backendWebProyectoApp.WebBackendApp.entidades.ProductoVariante;
import backendWebProyectoApp.WebBackendApp.entidades.Sede;
import backendWebProyectoApp.WebBackendApp.repositorios.InventarioRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.ProductoVarianteRepository;
import backendWebProyectoApp.WebBackendApp.repositorios.SedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository repo;

    @Autowired
    private ProductoVarianteRepository varianteRepo;

    @Autowired
    private SedeRepository sedeRepo;

    @Transactional
    public void aumentarStock(Integer varianteId, Integer sedeId, int cantidad) {

        Inventario inv = repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));

        inv.setStock((inv.getStock() == null ? 0 : inv.getStock()) + cantidad);

        repo.save(inv);
    }

    @Transactional
    public void descontarStock(Integer varianteId, Integer sedeId, int cantidad) {

        Inventario inv = repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));

        int stockActual = (inv.getStock() == null ? 0 : inv.getStock());

        if (stockActual < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        inv.setStock(stockActual - cantidad);

        repo.save(inv);
    }

    public Inventario crear(Integer varianteId, Integer sedeId, Integer stock) {

        boolean existe = repo.findByVariante_IdAndSede_Id(varianteId, sedeId).isPresent();
        if (existe) {
            throw new RuntimeException("Ya existe inventario para esta variante en esta sede");
        }

        ProductoVariante variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no existe"));

        Sede sede = sedeRepo.findById(sedeId)
                .orElseThrow(() -> new RuntimeException("Sede no existe"));

        Inventario inv = new Inventario();
        inv.setVariante(variante);
        inv.setSede(sede);
        inv.setStock(stock);

        return repo.save(inv);
    }

    @Transactional
    public void eliminarDeSede(Integer varianteId, Integer sedeId) {

        Inventario inv = repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("Inventario no existe en esta sede"));

        repo.delete(inv);
    }

    public List<Inventario> listarPorSede(Integer sedeId) {
        return repo.findBySede_Id(sedeId);
    }

    public Inventario obtenerStock(Integer varianteId, Integer sedeId) {
        return repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));
    }

    public void validarStock(Integer varianteId, Integer sedeId, int cantidad) {

        Inventario inv = repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));

        int stockActual = (inv.getStock() == null ? 0 : inv.getStock());

        if (stockActual < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }
    }
}