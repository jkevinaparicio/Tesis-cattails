package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.entidades.*;
import backendWebProyectoApp.WebBackendApp.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository repo;

    @Autowired
    private InventarioTamanoRepository inventarioTamanoRepo;

    @Autowired
    private ProductoVarianteRepository varianteRepo;

    @Autowired
    private SedeRepository sedeRepo;

    @Autowired
    private TamanoRepository tamanoRepo;

    public boolean esCompartidoPorTamano(ProductoVariante variante) {
        return variante != null
                && variante.getProducto() != null
                && variante.getProducto().getCategoria() != null
                && Boolean.TRUE.equals(variante.getProducto().getCategoria().isCompartirStockPorTamano());
    }

    @Transactional
    public void aumentarStock(ProductoVariante variante, Integer sedeId, int cantidad) {
        if (esCompartidoPorTamano(variante)) {
            InventarioTamano invTam = inventarioTamanoRepo
                    .findByTamano_IdAndSede_Id(variante.getTamaño().getId(), sedeId)
                    .orElseThrow(() -> new RuntimeException("No existe inventario"));

            invTam.setStock((invTam.getStock() == null ? 0 : invTam.getStock()) + cantidad);
            inventarioTamanoRepo.save(invTam);
        } else {
            Inventario inv = repo.findByVariante_IdAndSede_Id(variante.getId(), sedeId)
                    .orElseThrow(() -> new RuntimeException("No existe inventario"));

            inv.setStock((inv.getStock() == null ? 0 : inv.getStock()) + cantidad);
            repo.save(inv);
        }
    }

    @Transactional
    public void aumentarStock(Integer varianteId, Integer sedeId, int cantidad) {
        ProductoVariante variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no existe"));
        aumentarStock(variante, sedeId, cantidad);
    }

    @Transactional
    public void descontarStock(ProductoVariante variante, Integer sedeId, int cantidad) {
        if (esCompartidoPorTamano(variante)) {
            InventarioTamano invTam = inventarioTamanoRepo
                    .findByTamano_IdAndSede_Id(variante.getTamaño().getId(), sedeId)
                    .orElseThrow(() -> new RuntimeException("No existe inventario"));

            int stockActual = (invTam.getStock() == null ? 0 : invTam.getStock());
            if (stockActual < cantidad) {
                throw new RuntimeException("Stock insuficiente");
            }

            invTam.setStock(stockActual - cantidad);
            inventarioTamanoRepo.save(invTam);
        } else {
            Inventario inv = repo.findByVariante_IdAndSede_Id(variante.getId(), sedeId)
                    .orElseThrow(() -> new RuntimeException("No existe inventario"));

            int stockActual = (inv.getStock() == null ? 0 : inv.getStock());
            if (stockActual < cantidad) {
                throw new RuntimeException("Stock insuficiente");
            }

            inv.setStock(stockActual - cantidad);
            repo.save(inv);
        }
    }

    @Transactional
    public void descontarStock(Integer varianteId, Integer sedeId, int cantidad) {
        ProductoVariante variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no existe"));
        descontarStock(variante, sedeId, cantidad);
    }

    public void validarStock(ProductoVariante variante, Integer sedeId, int cantidad) {
        if (esCompartidoPorTamano(variante)) {
            InventarioTamano invTam = inventarioTamanoRepo
                    .findByTamano_IdAndSede_Id(variante.getTamaño().getId(), sedeId)
                    .orElseThrow(() -> new RuntimeException("No existe inventario"));

            int stockActual = (invTam.getStock() == null ? 0 : invTam.getStock());
            if (stockActual < cantidad) {
                throw new RuntimeException("Stock insuficiente");
            }
        } else {
            Inventario inv = repo.findByVariante_IdAndSede_Id(variante.getId(), sedeId)
                    .orElseThrow(() -> new RuntimeException("No existe inventario"));

            int stockActual = (inv.getStock() == null ? 0 : inv.getStock());
            if (stockActual < cantidad) {
                throw new RuntimeException("Stock insuficiente");
            }
        }
    }

    public void validarStock(Integer varianteId, Integer sedeId, int cantidad) {
        ProductoVariante variante = varianteRepo.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no existe"));
        validarStock(variante, sedeId, cantidad);
    }

    @Transactional
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

        if (esCompartidoPorTamano(variante)) {
            InventarioTamano it = inventarioTamanoRepo
                    .findByTamano_IdAndSede_Id(variante.getTamaño().getId(), sedeId)
                    .orElseGet(() -> {
                        InventarioTamano nuevo = new InventarioTamano();
                        nuevo.setTamano(variante.getTamaño());
                        nuevo.setSede(sede);
                        nuevo.setStock(stock != null ? stock : 0);
                        return inventarioTamanoRepo.save(nuevo);
                    });
            inv.setStock(it.getStock());
        } else {
            inv.setStock(stock != null ? stock : 0);
        }

        return repo.save(inv);
    }

    @Transactional
    public void eliminarDeSede(Integer varianteId, Integer sedeId) {
        Inventario inv = repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("Inventario no existe en esta sede"));

        repo.delete(inv);
    }

    public List<Inventario> listarPorSede(Integer sedeId) {
        List<Inventario> lista = repo.findBySede_Id(sedeId);
        for (Inventario inv : lista) {
            if (esCompartidoPorTamano(inv.getVariante())) {
                Integer stockTam = inventarioTamanoRepo
                        .findByTamano_IdAndSede_Id(inv.getVariante().getTamaño().getId(), sedeId)
                        .map(InventarioTamano::getStock)
                        .orElse(0);
                inv.setStock(stockTam);
            }
        }
        return lista;
    }

    public Inventario obtenerStock(Integer varianteId, Integer sedeId) {
        Inventario inv = repo.findByVariante_IdAndSede_Id(varianteId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));

        if (esCompartidoPorTamano(inv.getVariante())) {
            Integer stockTam = inventarioTamanoRepo
                    .findByTamano_IdAndSede_Id(inv.getVariante().getTamaño().getId(), sedeId)
                    .map(InventarioTamano::getStock)
                    .orElse(0);
            inv.setStock(stockTam);
        }

        return inv;
    }

    // ── GESTIÓN ESPECÍFICA DE INVENTARIO POR TAMAÑO ──

    public List<InventarioTamano> listarTamanoPorSede(Integer sedeId) {
        return inventarioTamanoRepo.findBySede_Id(sedeId);
    }

    public InventarioTamano obtenerStockTamano(Integer tamanoId, Integer sedeId) {
        return inventarioTamanoRepo.findByTamano_IdAndSede_Id(tamanoId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));
    }

    @Transactional
    public InventarioTamano crearOActualizarTamano(Integer tamanoId, Integer sedeId, Integer stock) {
        Sede sede = sedeRepo.findById(sedeId)
                .orElseThrow(() -> new RuntimeException("Sede no existe"));
        Tamano tamano = tamanoRepo.findById(tamanoId)
                .orElseThrow(() -> new RuntimeException("Tamaño no existe"));

        InventarioTamano invTam = inventarioTamanoRepo.findByTamano_IdAndSede_Id(tamanoId, sedeId)
                .orElseGet(() -> {
                    InventarioTamano nuevo = new InventarioTamano();
                    nuevo.setTamano(tamano);
                    nuevo.setSede(sede);
                    return nuevo;
                });

        invTam.setStock(stock != null ? stock : 0);
        return inventarioTamanoRepo.save(invTam);
    }

    @Transactional
    public void aumentarStockTamano(Integer tamanoId, Integer sedeId, int cantidad) {
        InventarioTamano invTam = inventarioTamanoRepo.findByTamano_IdAndSede_Id(tamanoId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));

        invTam.setStock((invTam.getStock() == null ? 0 : invTam.getStock()) + cantidad);
        inventarioTamanoRepo.save(invTam);
    }

    @Transactional
    public void descontarStockTamano(Integer tamanoId, Integer sedeId, int cantidad) {
        InventarioTamano invTam = inventarioTamanoRepo.findByTamano_IdAndSede_Id(tamanoId, sedeId)
                .orElseThrow(() -> new RuntimeException("No existe inventario"));

        int stockActual = (invTam.getStock() == null ? 0 : invTam.getStock());
        if (stockActual < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        invTam.setStock(stockActual - cantidad);
        inventarioTamanoRepo.save(invTam);
    }

    @Transactional
    public void eliminarTamanoDeSede(Integer tamanoId, Integer sedeId) {
        InventarioTamano invTam = inventarioTamanoRepo.findByTamano_IdAndSede_Id(tamanoId, sedeId)
                .orElseThrow(() -> new RuntimeException("Inventario no existe en esta sede"));

        inventarioTamanoRepo.delete(invTam);
    }
}