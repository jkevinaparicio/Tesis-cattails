package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AsistenteService {

    private final ChatClient.Builder     chatClientBuilder;
    private final VentaRepository        ventaRepository;
    private final ProductosRepository    productosRepository;
    private final UsuarioRepository      usuarioRepository;
    private final CategoriaRepository    categoriaRepository;
    private final PrediccionService prediccionService;
    private final InventarioRepository   inventarioRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    public String preguntar(String pregunta) {
        String contexto = construirContexto();

        String sistemaPrompt = """
            Eres un asistente de negocio inteligente para Cattails Granizados.
            Tienes acceso a los datos reales del negocio en tiempo real.
            Responde siempre en español, de forma clara y concisa.
            Si te preguntan por números, sé preciso con los datos.
            Si no tienes información suficiente para responder, dilo claramente.
            No inventes datos que no estén en el contexto.
            Cuando muestres listas usa formato legible con viñetas o números.
            
            DATOS ACTUALES DEL NEGOCIO:
            """ + contexto;

        return chatClientBuilder.build()
                .prompt()
                .system(sistemaPrompt)
                .user(pregunta)
                .call()
                .content();
    }

    private String construirContexto() {
        StringBuilder ctx = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        ctx.append("Fecha y hora actual: ").append(LocalDateTime.now().format(fmt)).append("\n\n");

        // ── RESUMEN GENERAL DE VENTAS ──
        try {
            var ventas = ventaRepository.findAll();
            ctx.append("=== RESUMEN VENTAS ===\n");
            ctx.append("Total ventas registradas: ").append(ventas.size()).append("\n");

            double totalIngresos = ventas.stream()
                    .mapToDouble(v -> v.getTotal() != null ? v.getTotal().doubleValue() : 0)
                    .sum();
            ctx.append("Ingresos totales: $").append(String.format("%.2f", totalIngresos)).append("\n");

            // Cambia .limit(10) por .limit(100) o quítalo si quieres todas
            ctx.append("Todas las ventas:\n");
            ventas.stream()
                    .filter(v -> v.getFecha() != null)
                    .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                    .limit(100) // ← aumenta según cuántas ventas tengas
                    .forEach(v -> ctx.append("  - ")
                            .append(v.getFecha().format(fmt))
                            .append(" | $").append(v.getTotal())
                            .append(" | Empleado: ").append(v.getUsuario() != null ? v.getUsuario().getNombre() + " " + (v.getUsuario().getApellido() != null ? v.getUsuario().getApellido() : "") : "desconocido")
                            .append(" | Sede: ").append(v.getSede() != null ? v.getSede().getNombre() : "sin sede")
                            .append(" | Pago: ").append(v.getMetodoPago())
                            .append(" | Tipo: ").append(v.getTipoPedido())
                            .append("\n"));
        } catch (Exception e) {
            ctx.append("Ventas generales: no disponibles\n");
        }
            
        try {
            ctx.append("\n=== PREDICCIONES (calculadas estadísticamente) ===\n");
            ctx.append(prediccionService.predecirVentasFuturas()).append("\n");
            ctx.append(prediccionService.predecirRendimientoEmpleados());
        } catch (Exception e) {
            ctx.append("Predicciones: no disponibles\n");
        }
        // ── PRODUCTOS MÁS VENDIDOS ──
        try {
            var top = detalleVentaRepository.topProductosMasVendidos();
            ctx.append("\n=== PRODUCTOS MÁS VENDIDOS (por unidades) ===\n");
            int rank = 1;
            for (Object[] row : top) {
                ctx.append("  ").append(rank++).append(". ")
                        .append(row[0]).append(" → ").append(row[1]).append(" unidades\n");
            }
        } catch (Exception e) {
            ctx.append("Productos más vendidos: no disponibles\n");
        }

        // ── VENTAS POR SEDE ──
        try {
            var porSede = detalleVentaRepository.ventasPorSede();
            ctx.append("\n=== VENTAS POR SEDE ===\n");
            porSede.forEach(row -> ctx.append("  - ")
                    .append(row[0]).append(": ")
                    .append(row[1]).append(" ventas | Total: $")
                    .append(String.format("%.2f", ((Number) row[2]).doubleValue()))
                    .append("\n"));
        } catch (Exception e) {
            ctx.append("Ventas por sede: no disponibles\n");
        }

        // ── VENTAS POR EMPLEADO ──
        try {
            var porEmpleado = detalleVentaRepository.ventasPorEmpleado();
            ctx.append("\n=== RANKING EMPLEADOS POR VENTAS ===\n");
            int rank = 1;
            for (Object[] row : porEmpleado) {
                ctx.append("  ").append(rank++).append(". ")
                        .append(row[0]).append(" ").append(row[1] != null ? row[1] : "")
                        .append(" → ").append(row[2]).append(" ventas | $")
                        .append(String.format("%.2f", ((Number) row[3]).doubleValue()))
                        .append("\n");
            }
        } catch (Exception e) {
            ctx.append("Ranking empleados: no disponibles\n");
        }

        // ── VENTAS POR CATEGORÍA ──
        try {
            var porCategoria = detalleVentaRepository.ventasPorCategoria();
            ctx.append("\n=== VENTAS POR CATEGORÍA ===\n");
            porCategoria.forEach(row -> ctx.append("  - ")
                    .append(row[0]).append(": ").append(row[1]).append(" unidades vendidas\n"));
        } catch (Exception e) {
            ctx.append("Ventas por categoría: no disponibles\n");
        }

        // ── MÉTODOS DE PAGO ──
        try {
            var porPago = detalleVentaRepository.ventasPorMetodoPago();
            ctx.append("\n=== MÉTODOS DE PAGO ===\n");
            porPago.forEach(row -> ctx.append("  - ")
                    .append(row[0]).append(": ").append(row[1]).append(" transacciones | $")
                    .append(String.format("%.2f", ((Number) row[2]).doubleValue()))
                    .append("\n"));
        } catch (Exception e) {
            ctx.append("Métodos de pago: no disponibles\n");
        }

        // ── PRODUCTOS ──
        try {
            var productos = productosRepository.findAll();
            ctx.append("\n=== CATÁLOGO PRODUCTOS ===\n");
            ctx.append("Total: ").append(productos.size()).append(" productos\n");
            productos.forEach(p -> ctx.append("  - ")
                    .append(p.getNombre())
                    .append(" | Categoría: ").append(p.getCategoria() != null ? p.getCategoria().getNombre() : "sin categoría")
                    .append("\n"));
        } catch (Exception e) {
            ctx.append("Productos: no disponibles\n");
        }

        // ── INVENTARIO ──
        try {
            var inventario = inventarioRepository.findAll();
            ctx.append("\n=== INVENTARIO ===\n");
            inventario.forEach(i -> ctx.append("  - ")
                    .append(i.getVariante() != null
                            ? i.getVariante().getProducto().getNombre()
                              + " (" + i.getVariante().getTamaño().getNombre() + ")"
                            : "variante desconocida")
                    .append(" | Stock: ").append(i.getStock())
                    .append(" | Sede: ").append(i.getSede() != null ? i.getSede().getNombre() : "sin sede")
                    .append("\n"));
        } catch (Exception e) {
            ctx.append("Inventario: no disponible\n");
        }

        // ── EMPLEADOS ──
        try {
            var usuarios = usuarioRepository.findAll();
            ctx.append("\n=== EMPLEADOS ===\n");
            ctx.append("Total: ").append(usuarios.size()).append("\n");
            usuarios.forEach(u -> ctx.append("  - ")
                    .append(u.getNombre()).append(" ").append(u.getApellido() != null ? u.getApellido() : "")
                    .append(" | ").append(u.getCorreo())
                    .append(" | Rol: ").append(u.getRol() != null ? u.getRol().getNombre() : "sin rol")
                    .append(" | Activo: ").append(Boolean.TRUE.equals(u.getActivo()) ? "Sí" : "No")
                    .append("\n"));
        } catch (Exception e) {
            ctx.append("Empleados: no disponibles\n");
        }

        return ctx.toString();
    }
}