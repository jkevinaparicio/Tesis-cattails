package backendWebProyectoApp.WebBackendApp.servicios;

import backendWebProyectoApp.WebBackendApp.repositorios.DetalleVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PrediccionService {

    private final DetalleVentaRepository detalleVentaRepository;

    /**
     * Predice ventas futuras. Si hay poco historial, da un promedio honesto
     * en lugar de una regresión poco confiable.
     */
    public String predecirVentasFuturas() {
        List<Object[]> datos = detalleVentaRepository.ventasPorDia();

        if (datos.isEmpty()) {
            return "No hay ventas registradas todavía, no es posible hacer ninguna predicción.";
        }

        if (datos.size() < 7) {
            double promedioSimple = datos.stream()
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .average().orElse(0);

            double totalHistorico = datos.stream()
                    .mapToDouble(row -> ((Number) row[1]).doubleValue())
                    .sum();

            return String.format("""
                ANÁLISIS DE PREDICCIÓN DE VENTAS:
                - Historial disponible: solo %d día(s) con ventas registradas
                - Total vendido en ese historial: $%.2f
                - Promedio por día con ventas: $%.2f
                - Advertencia: el historial es muy limitado (menos de 7 días) para una predicción estadística confiable mediante regresión lineal.
                - Recomendación: registrar ventas de forma más continua y constante para mejorar la precisión de futuras predicciones. Con más datos podré calcular tendencias reales de crecimiento o caída.
                """, datos.size(), totalHistorico, promedioSimple);
        }

        // ── Con suficiente historial, usar regresión lineal ──
        double[] x = new double[datos.size()];
        double[] y = new double[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            x[i] = i;
            y[i] = ((Number) datos.get(i)[1]).doubleValue();
        }

        double[] coef = regresionLineal(x, y);
        double pendiente = coef[0];
        double intercepto = coef[1];

        double prediccionProximoDia = pendiente * datos.size() + intercepto;
        double prediccionSemana = 0;
        for (int i = 0; i < 7; i++) {
            prediccionSemana += Math.max(0, pendiente * (datos.size() + i) + intercepto);
        }

        double promedioHistorico = Arrays.stream(y).average().orElse(0);
        String tendencia = pendiente > 0.5 ? "creciente 📈"
                : pendiente < -0.5 ? "decreciente 📉"
                  : "estable ➡️";

        return String.format("""
            ANÁLISIS DE PREDICCIÓN DE VENTAS:
            - Promedio histórico diario: $%.2f
            - Tendencia detectada: %s (pendiente: %.2f)
            - Predicción próximo día: $%.2f
            - Predicción próximos 7 días (suma): $%.2f
            - Basado en %d días de historial
            """,
                promedioHistorico, tendencia, pendiente,
                Math.max(0, prediccionProximoDia), Math.max(0, prediccionSemana), datos.size());
    }

    /**
     * Calcula tendencia de rendimiento por empleado.
     * Si un empleado solo tiene 1 mes de datos, se aclara que es preliminar.
     */
    public String predecirRendimientoEmpleados() {
        List<Object[]> datos = detalleVentaRepository.ventasMensualesPorEmpleado();
        if (datos.isEmpty()) {
            return "No hay suficiente historial de ventas por empleado para predecir.";
        }

        Map<String, List<Double>> porEmpleado = new LinkedHashMap<>();
        for (Object[] row : datos) {
            String nombre = row[0] + " " + (row[1] != null ? row[1] : "");
            double total = ((Number) row[3]).doubleValue();
            porEmpleado.computeIfAbsent(nombre, k -> new ArrayList<>()).add(total);
        }

        StringBuilder sb = new StringBuilder("PREDICCIÓN DE RENDIMIENTO POR EMPLEADO:\n");
        for (var entry : porEmpleado.entrySet()) {
            List<Double> meses = entry.getValue();
            double promedio = meses.stream().mapToDouble(d -> d).average().orElse(0);

            if (meses.size() < 2) {
                sb.append(String.format(
                        "  - %s: solo 1 mes de historial ($%.2f). Predicción preliminar basada únicamente en este dato; se necesita más historial para una tendencia confiable.\n",
                        entry.getKey(), promedio));
                continue;
            }

            double[] x = new double[meses.size()];
            double[] y = new double[meses.size()];
            for (int i = 0; i < meses.size(); i++) { x[i] = i; y[i] = meses.get(i); }
            double tendencia = regresionLineal(x, y)[0];

            double prediccionSiguienteMes = promedio + tendencia;
            String direccion = tendencia > 0 ? "subiendo" : tendencia < 0 ? "bajando" : "estable";

            sb.append(String.format(
                    "  - %s: promedio mensual $%.2f | tendencia %s | predicción próximo mes: $%.2f\n",
                    entry.getKey(), promedio, direccion, Math.max(0, prediccionSiguienteMes)));
        }

        return sb.toString();
    }

    /** Regresión lineal por mínimos cuadrados. Retorna [pendiente, intercepto] */
    private double[] regresionLineal(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }
        double denominador = (n * sumX2 - sumX * sumX);
        double pendiente = denominador != 0 ? (n * sumXY - sumX * sumY) / denominador : 0;
        double intercepto = (sumY - pendiente * sumX) / n;
        return new double[]{pendiente, intercepto};
    }
}