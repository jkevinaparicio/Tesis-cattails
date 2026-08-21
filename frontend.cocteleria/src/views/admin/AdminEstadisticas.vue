<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Estadísticas</h2>
      <div style="display:flex; gap:8px; flex-wrap:wrap;">
        <button class="bsm be2" @click="exportarPDF">📄 Exportar PDF</button>
        <button class="bpri" @click="cargar">↻ Actualizar</button>
      </div>
    </div>

    <div class="fbox" style="margin-bottom:1.25rem;">
      <div style="display:flex; gap:10px; flex-wrap:wrap; align-items:flex-end;">
        <div class="ff" style="margin:0;">
          <label>Período</label>
          <select v-model="periodo" @change="aplicarFiltro">
            <option value="hoy">Hoy</option>
            <option value="semana">Esta semana</option>
            <option value="mes">Este mes</option>
            <option value="año">Este año</option>
            <option value="personalizado">Personalizado</option>
          </select>
        </div>
        <template v-if="periodo === 'personalizado'">
          <div class="ff" style="margin:0;">
            <label>Desde</label>
            <input v-model="fechaDesde" type="date" @change="aplicarFiltro"/>
          </div>
          <div class="ff" style="margin:0;">
            <label>Hasta</label>
            <input v-model="fechaHasta" type="date" @change="aplicarFiltro"/>
          </div>
        </template>
        <div class="ff" style="margin:0;">
          <label>Sede</label>
          <select v-model="sedeSeleccionada" @change="aplicarFiltro">
            <option value="">Todas las sedes</option>
            <option v-for="s in sedes" :key="s.id" :value="s.id">{{ s.nombre }}</option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="cargando" class="empty">Cargando estadísticas...</div>

    <template v-else>
      <div class="stats-grid">
        <div class="stat-card">
          <p class="stat-label">Total ganancias</p>
          <p class="stat-val">${{ Number(totalGanancias).toLocaleString() }}</p>
          <p class="stat-sub">{{ ventasFiltradas.length }} ventas</p>
        </div>
        <div class="stat-card">
          <p class="stat-label">Promedio por venta</p>
          <p class="stat-val">${{ Number(promedioPorVenta).toLocaleString() }}</p>
          <p class="stat-sub">en el período</p>
        </div>
        <div class="stat-card">
          <p class="stat-label">Mejor día</p>
          <p class="stat-val">${{ Number(mejorDia.total).toLocaleString() }}</p>
          <p class="stat-sub">{{ mejorDia.fecha }}</p>
        </div>
        <div class="stat-card">
          <p class="stat-label">Productos vendidos</p>
          <p class="stat-val">{{ totalProductos }}</p>
          <p class="stat-sub">unidades totales</p>
        </div>
      </div>

      <div style="display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-bottom:1.25rem;">
        <div class="fbox" style="margin:0;">
          <p class="fep">Métodos de pago</p>
          <div style="display:flex; flex-direction:column; gap:10px; margin-top:0.5rem;">
            <div v-for="m in metodosPago" :key="m.metodo" style="display:flex; flex-direction:column; gap:4px;">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <span style="font-size:0.88rem; font-weight:600; color:var(--text);">{{ metodosMap[m.metodo] || m.metodo }}</span>
                <span style="font-size:0.82rem; color:var(--text3);">{{ m.ventas }} ventas · ${{ Number(m.total).toLocaleString() }}</span>
              </div>
              <div style="display:flex; align-items:center; gap:8px;">
                <div style="flex:1; height:8px; background:var(--bg3); border-radius:4px; overflow:hidden;">
                  <div :style="{ width: m.pct + '%', height: '100%', borderRadius: '4px', background: metodosColor[m.metodo] || 'var(--cyan)' }"></div>
                </div>
                <span style="font-size:0.75rem; color:var(--text3); font-weight:600; min-width:32px;">{{ m.pct }}%</span>
              </div>
            </div>
            <div v-if="!metodosPago.length" class="empty" style="padding:1rem 0;">Sin datos.</div>
          </div>
        </div>

        <div class="fbox" style="margin:0;">
          <p class="fep">Local vs Domicilio</p>
          <div style="display:flex; flex-direction:column; gap:10px; margin-top:0.5rem;">
            <div v-for="t in tiposPedido" :key="t.tipo" style="display:flex; flex-direction:column; gap:4px;">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <span style="font-size:0.88rem; font-weight:600; color:var(--text);">{{ t.tipo === 'LOCAL' ? '🏠 Local' : '🛵 Domicilio' }}</span>
                <span style="font-size:0.82rem; color:var(--text3);">{{ t.ventas }} ventas · ${{ Number(t.total).toLocaleString() }}</span>
              </div>
              <div style="display:flex; align-items:center; gap:8px;">
                <div style="flex:1; height:8px; background:var(--bg3); border-radius:4px; overflow:hidden;">
                  <div :style="{ width: t.pct + '%', height: '100%', borderRadius: '4px', background: t.tipo === 'LOCAL' ? 'var(--cyan)' : 'var(--purple)' }"></div>
                </div>
                <span style="font-size:0.75rem; color:var(--text3); font-weight:600; min-width:32px;">{{ t.pct }}%</span>
              </div>
            </div>
            <div v-if="!tiposPedido.length" class="empty" style="padding:1rem 0;">Sin datos.</div>
          </div>
        </div>
      </div>

      <div class="fbox" id="seccion-grafico">
        <p class="fep">Ganancias por día</p>
        <div class="chart-wrap">
          <div class="chart-bars">
            <div v-for="(dia, i) in datosGrafico" :key="i" class="bar-col" :title="`${dia.fecha}: $${Number(dia.total).toLocaleString()}`">
              <div class="bar-label-top">${{ formatMini(dia.total) }}</div>
              <div class="bar" :style="{ height: barHeight(dia.total) + '%' }"></div>
              <div class="bar-label">{{ dia.fechaCorta }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="fbox">
        <p class="fep">Comparación de períodos</p>
        <div class="compare-grid">
          <div class="compare-card">
            <p class="compare-label">Hoy</p>
            <p class="compare-val">${{ Number(comparacion.hoy).toLocaleString() }}</p>
          </div>
          <div class="compare-card">
            <p class="compare-label">Esta semana</p>
            <p class="compare-val">${{ Number(comparacion.semana).toLocaleString() }}</p>
          </div>
          <div class="compare-card">
            <p class="compare-label">Este mes</p>
            <p class="compare-val">${{ Number(comparacion.mes).toLocaleString() }}</p>
          </div>
          <div class="compare-card">
            <p class="compare-label">Este año</p>
            <p class="compare-val">${{ Number(comparacion.año).toLocaleString() }}</p>
          </div>
        </div>
      </div>

      <div class="fbox">
        <p class="fep">Productos más vendidos</p>
        <div class="tbl-w" style="margin:0; box-shadow:none;">
          <table>
            <thead>
              <tr><th>#</th><th>Producto</th><th>Tamaño</th><th>Unidades</th><th>Ingresos</th><th>% del total</th></tr>
            </thead>
            <tbody>
              <tr v-if="!topProductos.length"><td colspan="6" class="empty">Sin datos.</td></tr>
              <tr v-for="(p, i) in topProductos" :key="i">
                <td class="c-muted">{{ i + 1 }}</td>
                <td style="font-weight:600; color:var(--text);">{{ p.producto }}</td>
                <td style="color:var(--purple);">{{ p.tamano }}</td>
                <td class="c-cyan">{{ p.cantidad }}</td>
                <td class="c-cyan">${{ Number(p.ingresos).toLocaleString() }}</td>
                <td>
                  <div class="pct-bar-wrap">
                    <div class="pct-bar" :style="{ width: p.pct + '%' }"></div>
                    <span class="pct-label">{{ p.pct }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="fbox">
        <p class="fep">Ventas por empleado</p>
        <div class="tbl-w" style="margin:0; box-shadow:none;">
          <table>
            <thead>
              <tr><th>Empleado</th><th>Ventas</th><th>Total generado</th><th>Promedio</th></tr>
            </thead>
            <tbody>
              <tr v-if="!topEmpleados.length"><td colspan="4" class="empty">Sin datos.</td></tr>
              <tr v-for="(e, i) in topEmpleados" :key="i">
                <td style="font-weight:600; color:var(--text);">{{ e.nombre }}</td>
                <td class="c-muted">{{ e.ventas }}</td>
                <td class="c-cyan">${{ Number(e.total).toLocaleString() }}</td>
                <td style="color:var(--purple);">${{ Number(e.promedio).toLocaleString() }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="fbox" v-if="!sedeSeleccionada">
        <p class="fep">Ventas por sede</p>
        <div class="tbl-w" style="margin:0; box-shadow:none;">
          <table>
            <thead>
              <tr><th>Sede</th><th>Ventas</th><th>Total</th><th>% del total</th></tr>
            </thead>
            <tbody>
              <tr v-for="(s, i) in ventasPorSede" :key="i">
                <td><span class="tag-sede">{{ s.sede }}</span></td>
                <td class="c-muted">{{ s.ventas }}</td>
                <td class="c-cyan">${{ Number(s.total).toLocaleString() }}</td>
                <td>
                  <div class="pct-bar-wrap">
                    <div class="pct-bar" :style="{ width: s.pct + '%' }"></div>
                    <span class="pct-label">{{ s.pct }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'
import { useWebSocket } from '@/composables/useWebSocket'

const { api } = useAdmin()
const { conectar, desconectar } = useWebSocket()

const todasVentas      = ref([])
const ventasFiltradas  = ref([])
const sedes            = ref([])
const cargando         = ref(false)
const periodo          = ref('mes')
const fechaDesde       = ref('')
const fechaHasta       = ref('')
const sedeSeleccionada = ref('')

const metodosMap = {
  EFECTIVO:    '💵 Efectivo',
  NEQUI:       '📱 Nequi',
  BANCOLOMBIA: '🏦 Bancolombia',
}

const metodosColor = {
  EFECTIVO:    'linear-gradient(90deg, #34d399, #059669)',
  NEQUI:       'linear-gradient(90deg, #a78bfa, #7c3aed)',
  BANCOLOMBIA: 'linear-gradient(90deg, #60a5fa, #1d4ed8)',
}

onMounted(async () => {
  cargando.value = true
  try {
    const [v, s] = await Promise.all([
      api('GET', '/ventas/todas').catch(() => []),
      api('GET', '/sedes/listar').catch(() => [])
    ])
    todasVentas.value = v
    sedes.value       = s
    aplicarFiltro()
  } finally {
    cargando.value = false
  }

  conectar((ventaNueva) => {
    const existe = todasVentas.value.find(v => v.id === ventaNueva.id)
    if (!existe) {
      todasVentas.value.unshift(ventaNueva)
      aplicarFiltro()
    }
  })
})

onUnmounted(() => desconectar())

async function cargar() {
  cargando.value = true
  try {
    todasVentas.value = await api('GET', '/ventas/todas').catch(() => [])
    aplicarFiltro()
  } finally {
    cargando.value = false
  }
}

function aplicarFiltro() {
  const ahora = new Date()
  let desde   = null
  let hasta   = new Date()
  hasta.setHours(23, 59, 59)

  if (periodo.value === 'hoy') {
    desde = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate())
  } else if (periodo.value === 'semana') {
    const dia = ahora.getDay() || 7
    desde = new Date(ahora)
    desde.setDate(ahora.getDate() - dia + 1)
    desde.setHours(0, 0, 0)
  } else if (periodo.value === 'mes') {
    desde = new Date(ahora.getFullYear(), ahora.getMonth(), 1)
  } else if (periodo.value === 'año') {
    desde = new Date(ahora.getFullYear(), 0, 1)
  } else if (periodo.value === 'personalizado') {
    desde = fechaDesde.value ? new Date(fechaDesde.value) : null
    hasta = fechaHasta.value ? new Date(fechaHasta.value + 'T23:59:59') : hasta
  }

  let filtradas = todasVentas.value.filter(v => {
    const fecha = parseFecha(v.fecha)
    if (!fecha) return false
    if (desde && fecha < desde) return false
    if (hasta && fecha > hasta) return false
    return true
  })

  if (sedeSeleccionada.value) {
    filtradas = filtradas.filter(v => {
      const sede = sedes.value.find(s => s.id === parseInt(sedeSeleccionada.value))
      return sede && v.sede === sede.nombre
    })
  }

  ventasFiltradas.value = filtradas
}

function parseFecha(fechaStr) {
  if (!fechaStr) return null
  const [fecha, hora] = fechaStr.split(' ')
  if (!fecha) return null
  const [dd, mm, yyyy] = fecha.split('/')
  return new Date(`${yyyy}-${mm}-${dd}T${hora || '00:00'}`)
}

const totalGanancias = computed(() =>
  ventasFiltradas.value.reduce((acc, v) => acc + parseFloat(v.total || 0), 0)
)

const promedioPorVenta = computed(() =>
  ventasFiltradas.value.length
    ? Math.round(totalGanancias.value / ventasFiltradas.value.length)
    : 0
)

const totalProductos = computed(() =>
  ventasFiltradas.value.reduce((acc, v) =>
    acc + (v.detalles || []).reduce((a, d) => a + d.cantidad, 0), 0)
)

const mejorDia = computed(() => {
  const porDia = {}
  ventasFiltradas.value.forEach(v => {
    const fecha = v.fecha?.split(' ')[0] || '—'
    porDia[fecha] = (porDia[fecha] || 0) + parseFloat(v.total || 0)
  })
  let max = { fecha: '—', total: 0 }
  Object.entries(porDia).forEach(([fecha, total]) => {
    if (total > max.total) max = { fecha, total }
  })
  return max
})

const metodosPago = computed(() => {
  const mapa = {}
  ventasFiltradas.value.forEach(v => {
    const m = v.metodoPago || 'EFECTIVO'
    if (!mapa[m]) mapa[m] = { metodo: m, ventas: 0, total: 0 }
    mapa[m].ventas++
    mapa[m].total += parseFloat(v.total || 0)
  })
  const lista    = Object.values(mapa).sort((a, b) => b.total - a.total)
  const totalGen = lista.reduce((acc, m) => acc + m.total, 0)
  return lista.map(m => ({ ...m, pct: totalGen > 0 ? Math.round(m.total / totalGen * 100) : 0 }))
})

const tiposPedido = computed(() => {
  const mapa = {}
  ventasFiltradas.value.forEach(v => {
    const t = v.tipoPedido || 'LOCAL'
    if (!mapa[t]) mapa[t] = { tipo: t, ventas: 0, total: 0 }
    mapa[t].ventas++
    mapa[t].total += parseFloat(v.total || 0)
  })
  const lista    = Object.values(mapa).sort((a, b) => b.ventas - a.ventas)
  const totalGen = lista.reduce((acc, t) => acc + t.ventas, 0)
  return lista.map(t => ({ ...t, pct: totalGen > 0 ? Math.round(t.ventas / totalGen * 100) : 0 }))
})

const datosGrafico = computed(() => {
  const porDia = {}
  ventasFiltradas.value.forEach(v => {
    const fecha = v.fecha?.split(' ')[0] || '—'
    porDia[fecha] = (porDia[fecha] || 0) + parseFloat(v.total || 0)
  })
  return Object.entries(porDia)
    .sort(([a], [b]) => {
      const pa = a.split('/').reverse().join('-')
      const pb = b.split('/').reverse().join('-')
      return pa.localeCompare(pb)
    })
    .slice(-30)
    .map(([fecha, total]) => ({ fecha, fechaCorta: fecha.substring(0, 5), total }))
})

const comparacion = computed(() => {
  const ahora     = new Date()
  const hoyInicio = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate())
  const semInicio = new Date(ahora)
  semInicio.setDate(ahora.getDate() - (ahora.getDay() || 7) + 1)
  semInicio.setHours(0, 0, 0)
  const mesInicio = new Date(ahora.getFullYear(), ahora.getMonth(), 1)
  const anoInicio = new Date(ahora.getFullYear(), 0, 1)

  const sum = (desde) => todasVentas.value
    .filter(v => { const f = parseFecha(v.fecha); return f && f >= desde })
    .reduce((acc, v) => acc + parseFloat(v.total || 0), 0)

  return { hoy: sum(hoyInicio), semana: sum(semInicio), mes: sum(mesInicio), año: sum(anoInicio) }
})

const topProductos = computed(() => {
  const mapa = {}
  ventasFiltradas.value.forEach(v => {
    (v.detalles || []).forEach(d => {
      const key = `${d.producto}||${d.tamaño}`
      if (!mapa[key]) mapa[key] = { producto: d.producto, tamano: d.tamaño, cantidad: 0, ingresos: 0 }
      mapa[key].cantidad += d.cantidad
      mapa[key].ingresos += parseFloat(d.subtotal || 0)
    })
  })
  const lista    = Object.values(mapa).sort((a, b) => b.ingresos - a.ingresos)
  const totalIng = lista.reduce((acc, p) => acc + p.ingresos, 0)
  return lista.slice(0, 10).map(p => ({ ...p, pct: totalIng > 0 ? Math.round(p.ingresos / totalIng * 100) : 0 }))
})

const topEmpleados = computed(() => {
  const mapa = {}
  ventasFiltradas.value.forEach(v => {
    if (!mapa[v.usuario]) mapa[v.usuario] = { nombre: v.usuario, ventas: 0, total: 0 }
    mapa[v.usuario].ventas++
    mapa[v.usuario].total += parseFloat(v.total || 0)
  })
  return Object.values(mapa)
    .sort((a, b) => b.total - a.total)
    .map(e => ({ ...e, promedio: e.ventas > 0 ? Math.round(e.total / e.ventas) : 0 }))
})

const ventasPorSede = computed(() => {
  const mapa = {}
  ventasFiltradas.value.forEach(v => {
    if (!mapa[v.sede]) mapa[v.sede] = { sede: v.sede, ventas: 0, total: 0 }
    mapa[v.sede].ventas++
    mapa[v.sede].total += parseFloat(v.total || 0)
  })
  const lista    = Object.values(mapa).sort((a, b) => b.total - a.total)
  const totalGen = lista.reduce((acc, s) => acc + s.total, 0)
  return lista.map(s => ({ ...s, pct: totalGen > 0 ? Math.round(s.total / totalGen * 100) : 0 }))
})

const tituloFiltro = computed(() => {
  const ahora = new Date().toLocaleDateString('es-CO')
  let periodoTexto = ''
  if (periodo.value === 'hoy') periodoTexto = `Hoy — ${ahora}`
  else if (periodo.value === 'semana') periodoTexto = `Esta semana`
  else if (periodo.value === 'mes') periodoTexto = `Este mes — ${new Date().toLocaleString('es-CO', { month: 'long', year: 'numeric' })}`
  else if (periodo.value === 'año') periodoTexto = `Año ${new Date().getFullYear()}`
  else if (periodo.value === 'personalizado') {
    const desde = fechaDesde.value ? new Date(fechaDesde.value).toLocaleDateString('es-CO') : '—'
    const hasta = fechaHasta.value ? new Date(fechaHasta.value).toLocaleDateString('es-CO') : '—'
    periodoTexto = `Del ${desde} al ${hasta}`
  }
  const sedeTexto = sedeSeleccionada.value
    ? sedes.value.find(s => s.id === parseInt(sedeSeleccionada.value))?.nombre || 'Sede'
    : 'Todas las sedes'
  return { periodo: periodoTexto, sede: sedeTexto }
})

function barHeight(total) {
  const max = Math.max(...datosGrafico.value.map(d => d.total), 1)
  return Math.max((total / max) * 100, 2)
}

function formatMini(val) {
  if (val >= 1000000) return (val / 1000000).toFixed(1) + 'M'
  if (val >= 1000)    return (val / 1000).toFixed(0) + 'K'
  return Number(val).toLocaleString()
}

async function exportarPDF() {
  const contenido = `
    <html><head><meta charset="UTF-8">
    <style>
      body { font-family: Arial, sans-serif; padding: 24px; color: #0a1a3a; }
      .header { border-bottom: 3px solid #1d4ed8; padding-bottom: 16px; margin-bottom: 20px; }
      h1 { color: #1d4ed8; font-size: 24px; margin: 0 0 6px; }
      .header-meta { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 10px; }
      .header-chip { background: #e0eeff; border-radius: 20px; padding: 4px 14px; font-size: 12px; color: #1d4ed8; font-weight: 600; }
      .generado { font-size: 11px; color: #8aa0c0; margin-top: 6px; }
      h2 { color: #1d4ed8; font-size: 15px; margin: 20px 0 8px; border-bottom: 2px solid #e0eeff; padding-bottom: 4px; }
      .stats { display: flex; gap: 16px; margin-bottom: 16px; flex-wrap: wrap; }
      .stat { background: #f0f6ff; border-radius: 8px; padding: 12px 16px; min-width: 140px; }
      .stat-label { font-size: 11px; color: #5a80aa; text-transform: uppercase; letter-spacing: 0.08em; }
      .stat-val { font-size: 20px; font-weight: bold; color: #1d4ed8; margin-top: 4px; }
      table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 16px; }
      th { background: #e0eeff; color: #1d4ed8; padding: 8px 10px; text-align: left; }
      td { padding: 7px 10px; border-bottom: 1px solid #e0eeff; }
    </style></head><body>
      <div class="header">
        <h1>🍹 Cattails Granizados</h1>
        <div class="header-meta">
          <span class="header-chip">📅 ${tituloFiltro.value.periodo}</span>
          <span class="header-chip">📍 ${tituloFiltro.value.sede}</span>
        </div>
        <p class="generado">Generado el ${new Date().toLocaleString('es-CO')}</p>
      </div>
      <div class="stats">
        <div class="stat"><div class="stat-label">Total ganancias</div><div class="stat-val">$${Number(totalGanancias.value).toLocaleString()}</div></div>
        <div class="stat"><div class="stat-label">Ventas</div><div class="stat-val">${ventasFiltradas.value.length}</div></div>
        <div class="stat"><div class="stat-label">Promedio/venta</div><div class="stat-val">$${Number(promedioPorVenta.value).toLocaleString()}</div></div>
        <div class="stat"><div class="stat-label">Unidades</div><div class="stat-val">${totalProductos.value}</div></div>
      </div>
      <h2>Métodos de pago</h2>
      <table><tr><th>Método</th><th>Ventas</th><th>Total</th><th>%</th></tr>
        ${metodosPago.value.map(m => `<tr><td>${metodosMap[m.metodo] || m.metodo}</td><td>${m.ventas}</td><td>$${Number(m.total).toLocaleString()}</td><td>${m.pct}%</td></tr>`).join('')}
      </table>
      <h2>Local vs Domicilio</h2>
      <table><tr><th>Tipo</th><th>Ventas</th><th>Total</th><th>%</th></tr>
        ${tiposPedido.value.map(t => `<tr><td>${t.tipo === 'LOCAL' ? 'Local' : 'Domicilio'}</td><td>${t.ventas}</td><td>$${Number(t.total).toLocaleString()}</td><td>${t.pct}%</td></tr>`).join('')}
      </table>
      <h2>Productos más vendidos</h2>
      <table><tr><th>Producto</th><th>Tamaño</th><th>Unidades</th><th>Ingresos</th></tr>
        ${topProductos.value.map(p => `<tr><td>${p.producto}</td><td>${p.tamano}</td><td>${p.cantidad}</td><td>$${Number(p.ingresos).toLocaleString()}</td></tr>`).join('')}
      </table>
      <h2>Ventas por empleado</h2>
      <table><tr><th>Empleado</th><th>Ventas</th><th>Total</th><th>Promedio</th></tr>
        ${topEmpleados.value.map(e => `<tr><td>${e.nombre}</td><td>${e.ventas}</td><td>$${Number(e.total).toLocaleString()}</td><td>$${Number(e.promedio).toLocaleString()}</td></tr>`).join('')}
      </table>
      <h2>Ventas por sede</h2>
      <table><tr><th>Sede</th><th>Ventas</th><th>Total</th><th>%</th></tr>
        ${ventasPorSede.value.map(s => `<tr><td>${s.sede}</td><td>${s.ventas}</td><td>$${Number(s.total).toLocaleString()}</td><td>${s.pct}%</td></tr>`).join('')}
      </table>
    </body></html>
  `
  const ventana = window.open('', '_blank')
  ventana.document.write(contenido)
  ventana.document.close()
  ventana.focus()
  setTimeout(() => { ventana.print(); ventana.close() }, 500)
}
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 1.25rem; }
@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .stats-grid { grid-template-columns: 1fr; } }
.stat-card { background: var(--bg2); border: 1px solid var(--border); border-radius: var(--radius); padding: 1.25rem; box-shadow: var(--shadow); transition: all 0.2s; }
.stat-card:hover { border-color: var(--purple); box-shadow: var(--shadow-lg); transform: translateY(-2px); }
.stat-label { font-size: 0.68rem; letter-spacing: 0.12em; text-transform: uppercase; color: var(--text3); font-weight: 700; margin-bottom: 8px; }
.stat-val { font-size: 1.6rem; font-family: var(--font-brand); color: var(--cyan); font-weight: 600; line-height: 1; margin-bottom: 4px; }
.stat-sub { font-size: 0.75rem; color: var(--text3); font-weight: 500; }
.chart-wrap { overflow-x: auto; padding-bottom: 0.5rem; }
.chart-bars { display: flex; align-items: flex-end; gap: 6px; height: 180px; min-width: 100%; padding: 1rem 0 0; }
.bar-col { display: flex; flex-direction: column; align-items: center; flex: 1; min-width: 28px; max-width: 52px; height: 100%; justify-content: flex-end; gap: 4px; }
.bar { width: 100%; background: linear-gradient(180deg, #60a5fa, #1d4ed8); border-radius: 4px 4px 0 0; transition: height 0.4s ease; min-height: 4px; }
.bar-label-top { font-size: 0.58rem; color: var(--text3); font-weight: 600; white-space: nowrap; }
.bar-label { font-size: 0.6rem; color: var(--text3); font-weight: 500; white-space: nowrap; margin-top: 4px; }
.compare-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
@media (max-width: 640px) { .compare-grid { grid-template-columns: repeat(2, 1fr); } }
.compare-card { background: var(--bg3); border-radius: var(--radius-sm); padding: 1rem; text-align: center; border: 1px solid var(--border2); transition: all 0.2s; }
.compare-card:hover { border-color: var(--purple); }
.compare-label { font-size: 0.68rem; letter-spacing: 0.1em; text-transform: uppercase; color: var(--text3); font-weight: 700; margin-bottom: 6px; }
.compare-val { font-size: 1.15rem; font-family: var(--font-brand); color: var(--purple); font-weight: 600; }
.pct-bar-wrap { display: flex; align-items: center; gap: 8px; }
.pct-bar { height: 6px; background: linear-gradient(90deg, #60a5fa, #1d4ed8); border-radius: 3px; min-width: 4px; transition: width 0.4s ease; }
.pct-label { font-size: 0.72rem; color: var(--text3); font-weight: 600; white-space: nowrap; }
</style>