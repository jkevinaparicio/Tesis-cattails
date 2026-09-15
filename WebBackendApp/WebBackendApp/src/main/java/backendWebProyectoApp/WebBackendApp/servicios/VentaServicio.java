package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.dto.CrearVentaDTO;
import backendWebProyectoApp.WebBackendApp.dto.DetalleVentaDTO;
import backendWebProyectoApp.WebBackendApp.dto.VentaDTO;
import backendWebProyectoApp.WebBackendApp.entidades.*;
import backendWebProyectoApp.WebBackendApp.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaServicio {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Autowired
    private SedeRepository sedeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ConfigPromocionRepository configPromocionRepository;

    @Transactional
    public VentaDTO crearVenta(CrearVentaDTO dto) {

        Usuario usuario = obtenerUsuarioAutenticado();

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("Usuario inactivo, no puede realizar ventas");
        }
        Sede sede = sedeRepository.findById(dto.getIdSede())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada"));

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setSede(sede);
        venta.setFecha(LocalDateTime.now());
        venta.setMetodoPago(dto.getMetodoPago() != null ? dto.getMetodoPago() : "EFECTIVO");
        venta.setTipoPedido(dto.getTipoPedido() != null ? dto.getTipoPedido() : "LOCAL");

        BigDecimal valorDomicilio = "DOMICILIO".equals(venta.getTipoPedido()) && dto.getValorDomicilio() != null
                ? dto.getValorDomicilio()
                : BigDecimal.ZERO;
        venta.setValorDomicilio(valorDomicilio);

        Venta guardada = ventaRepository.save(venta);
        ConfigPromocion config = configPromocionRepository.findFirstBy().orElse(null);
        DayOfWeek hoy = LocalDateTime.now().getDayOfWeek();

        boolean diaPromo = config != null && switch (hoy) {
            case MONDAY    -> Boolean.TRUE.equals(config.getLunes());
            case TUESDAY   -> Boolean.TRUE.equals(config.getMartes());
            case WEDNESDAY -> Boolean.TRUE.equals(config.getMiercoles());
            case THURSDAY  -> Boolean.TRUE.equals(config.getJueves());
            case FRIDAY    -> Boolean.TRUE.equals(config.getViernes());
            case SATURDAY  -> Boolean.TRUE.equals(config.getSabado());
            case SUNDAY    -> Boolean.TRUE.equals(config.getDomingo());
        };

        boolean aplicarPromo = diaPromo && Boolean.TRUE.equals(dto.getEsPromocion());

        BigDecimal total = BigDecimal.ZERO;

        for (CrearVentaDTO.DetalleRequest item : dto.getDetalles()) {

            ProductoVariante variante = varianteRepository.findById(item.getIdVariante())
                    .orElseThrow(() -> new RuntimeException("Variante no existe"));

            inventarioService.descontarStock(
                    variante,
                    sede.getId(),
                    item.getCantidad()
            );

            BigDecimal subtotal;

            if (aplicarPromo && variante.getPrecioPromo() != null) {
                int unidadesPromo = (item.getCantidad() / 2) * 2;
                int unidadesNormal = item.getCantidad() % 2;

                subtotal = variante.getPrecioPromo()
                        .multiply(BigDecimal.valueOf(unidadesPromo))
                        .add(variante.getPrecio()
                                .multiply(BigDecimal.valueOf(unidadesNormal)));
            } else {
                subtotal = variante.getPrecio()
                        .multiply(BigDecimal.valueOf(item.getCantidad()));
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(variante.getProducto());
            detalle.setTamaño(variante.getTamaño());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(variante.getPrecio());
            detalle.setSubtotal(subtotal);
            detalle.setVenta(guardada);

            detalleVentaRepository.save(detalle);
            total = total.add(subtotal);
        }

        guardada.setTotal(total.add(valorDomicilio));

        return mapearVenta(ventaRepository.save(guardada));
    }

    public List<VentaDTO> verVentas() {
        return ventaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::mapearVenta)
                .toList();
    }

    public List<VentaDTO> verPorNombreUsuario(String nombre) {
        return ventaRepository.findByUsuario_NombreOrderByFechaDesc(nombre)
                .stream()
                .map(this::mapearVenta)
                .toList();
    }

    public List<VentaDTO> verPorSede(Integer idSede) {
        return ventaRepository.findBySede_IdOrderByFechaDesc(idSede)
                .stream()
                .map(this::mapearVenta)
                .toList();
    }

    public List<VentaDTO> verPorFechaNegocio(LocalDate fecha) {
        return ventaRepository.findByFechaNegocioOrderByFechaDesc(fecha)
                .stream()
                .map(this::mapearVenta)
                .toList();
    }

    public List<VentaDTO> verPorSedeYFechaNegocio(Integer idSede, LocalDate fecha) {
        return ventaRepository.findBySede_IdAndFechaNegocioOrderByFechaDesc(idSede, fecha)
                .stream()
                .map(this::mapearVenta)
                .toList();
    }

    public Page<VentaDTO> verVentasPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findAllByOrderByFechaDesc(pageable)
                .map(this::mapearVenta);
    }

    public Page<VentaDTO> verPorNombreUsuarioPaginado(String nombre, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findByUsuario_NombreOrderByFechaDesc(nombre, pageable)
                .map(this::mapearVenta);
    }

    public Page<VentaDTO> verPorSedePaginado(Integer idSede, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findBySede_IdOrderByFechaDesc(idSede, pageable)
                .map(this::mapearVenta);
    }

    public Page<VentaDTO> verPorFechaNegocioPaginado(LocalDate fecha, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findByFechaNegocioOrderByFechaDesc(fecha, pageable)
                .map(this::mapearVenta);
    }

    public Page<VentaDTO> verPorSedeYFechaNegocioPaginado(Integer idSede, LocalDate fecha, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ventaRepository.findBySede_IdAndFechaNegocioOrderByFechaDesc(idSede, fecha, pageable)
                .map(this::mapearVenta);
    }

    @Transactional
    public VentaDTO modificarVenta(Integer id, CrearVentaDTO dto) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no existe"));

        for (DetalleVenta old : venta.getDetalleVentas()) {
            ProductoVariante varianteOld = varianteRepository
                    .findByProducto_IdAndTamaño_Id(
                            old.getProducto().getId(),
                            old.getTamaño().getId()
                    ).orElseThrow(() -> new RuntimeException("Variante no existe"));

            inventarioService.aumentarStock(
                    varianteOld,
                    venta.getSede().getId(),
                    old.getCantidad()
            );
        }

        venta.getDetalleVentas().clear();

        venta.setMetodoPago(dto.getMetodoPago() != null ? dto.getMetodoPago() : venta.getMetodoPago());
        venta.setTipoPedido(dto.getTipoPedido() != null ? dto.getTipoPedido() : venta.getTipoPedido());

        BigDecimal valorDomicilio = "DOMICILIO".equals(venta.getTipoPedido()) && dto.getValorDomicilio() != null
                ? dto.getValorDomicilio()
                : BigDecimal.ZERO;
        venta.setValorDomicilio(valorDomicilio);

        ConfigPromocion config = configPromocionRepository.findFirstBy().orElse(null);
        DayOfWeek hoy = LocalDateTime.now().getDayOfWeek();

        boolean diaPromo = config != null && switch (hoy) {
            case MONDAY    -> Boolean.TRUE.equals(config.getLunes());
            case TUESDAY   -> Boolean.TRUE.equals(config.getMartes());
            case WEDNESDAY -> Boolean.TRUE.equals(config.getMiercoles());
            case THURSDAY  -> Boolean.TRUE.equals(config.getJueves());
            case FRIDAY    -> Boolean.TRUE.equals(config.getViernes());
            case SATURDAY  -> Boolean.TRUE.equals(config.getSabado());
            case SUNDAY    -> Boolean.TRUE.equals(config.getDomingo());
        };

        boolean aplicarPromo = diaPromo && Boolean.TRUE.equals(dto.getEsPromocion());

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (CrearVentaDTO.DetalleRequest dt : dto.getDetalles()) {

            ProductoVariante variante = varianteRepository.findById(dt.getIdVariante())
                    .orElseThrow(() -> new RuntimeException("Variante no existe"));

            inventarioService.validarStock(variante, venta.getSede().getId(), dt.getCantidad());
            inventarioService.descontarStock(variante, venta.getSede().getId(), dt.getCantidad());

            BigDecimal subtotal;

            if (aplicarPromo && variante.getPrecioPromo() != null) {
                int unidadesPromo  = (dt.getCantidad() / 2) * 2;
                int unidadesNormal = dt.getCantidad() % 2;

                subtotal = variante.getPrecioPromo()
                        .multiply(BigDecimal.valueOf(unidadesPromo))
                        .add(variante.getPrecio()
                                .multiply(BigDecimal.valueOf(unidadesNormal)));
            } else {
                subtotal = variante.getPrecio()
                        .multiply(BigDecimal.valueOf(dt.getCantidad()));
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(variante.getProducto());
            detalle.setTamaño(variante.getTamaño());
            detalle.setCantidad(dt.getCantidad());
            detalle.setPrecioUnitario(variante.getPrecio());
            detalle.setSubtotal(subtotal);
            detalle.setVenta(venta);

            detalles.add(detalle);
            total = total.add(subtotal);
        }

        venta.setDetalleVentas(detalles);
        venta.setTotal(total.add(valorDomicilio));
        venta.setFecha(LocalDateTime.now());

        return mapearVenta(ventaRepository.save(venta));
    }

    public List<VentaDTO> verMisVentas() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return ventaRepository.findByUsuarioOrderByFechaDesc(usuario)
                .stream()
                .map(this::mapearVenta)
                .toList();
    }

    private VentaDTO mapearVenta(Venta v) {
        VentaDTO dto = new VentaDTO();
        dto.setId(v.getId());
        dto.setUsuario(v.getUsuario().getNombre());
        dto.setSede(v.getSede().getNombre());
        dto.setTotal(v.getTotal());
        dto.setValorDomicilio(v.getValorDomicilio());
        dto.setFecha(v.getFecha());
        dto.setMetodoPago(v.getMetodoPago());
        dto.setTipoPedido(v.getTipoPedido());

        List<DetalleVentaDTO> detalles = new ArrayList<>();
        List<DetalleVenta> detVentas = detalleVentaRepository.findByVenta(v);

        for (DetalleVenta d : detVentas) {
            DetalleVentaDTO det = new DetalleVentaDTO();
            det.setId(d.getId_detalle());
            det.setProducto(d.getProducto() != null ? d.getProducto().getNombre() : "—");
            det.setTamaño(d.getTamaño()    != null ? d.getTamaño().getNombre()    : "—");
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(d.getPrecioUnitario());
            det.setSubtotal(d.getSubtotal());
            detalles.add(det);
        }

        dto.setDetalles(detalles);
        return dto;
    }

    @Transactional
    public void eliminarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no existe"));

        for (DetalleVenta d : venta.getDetalleVentas()) {
            ProductoVariante variante = varianteRepository
                    .findByProducto_IdAndTamaño_Id(
                            d.getProducto().getId(),
                            d.getTamaño().getId()
                    ).orElseThrow(() -> new RuntimeException("Variante no existe"));

            inventarioService.aumentarStock(
                    variante,
                    venta.getSede().getId(),
                    d.getCantidad()
            );
        }

        ventaRepository.delete(venta);
    }

    private Usuario obtenerUsuarioAutenticado() {
        String correo = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
