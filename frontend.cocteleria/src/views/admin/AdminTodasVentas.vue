<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Todas las ventas</h2>
      <button class="bpri" @click="cargar">↻ Actualizar</button>
    </div>

    <div style="display:flex; gap:10px; margin-bottom:1.25rem; flex-wrap:wrap; align-items:flex-end;">
      <button class="bsm be2" @click="cargar">Todas</button>
      <div class="ff" style="margin:0; min-width:180px;">
        <label>Por empleado</label>
        <select v-model="filtroUsuario" @change="filtrarUsuario">
          <option value="">Todos...</option>
          <option v-for="u in usuarios" :key="u.idUsuario" :value="u.nombre">
            {{ u.nombre }} {{ u.apellido }}
          </option>
        </select>
      </div>
      <div class="ff" style="margin:0; min-width:180px;">
        <label>Por sede</label>
        <select v-model="filtroSede" @change="filtrarPorFechaOSede">
          <option value="">Todas...</option>
          <option v-for="s in sedes" :key="s.id" :value="s.id">
            {{ s.nombre }}
          </option>
        </select>
      </div>
      <div class="ff" style="margin:0; min-width:170px;">
        <label>Día (cierre de caja)</label>
        <input v-model="filtroFecha" type="date" @change="filtrarPorFechaOSede" />
      </div>
      <button v-if="filtroFecha" class="bsm bd" @click="limpiarFecha">✕ Quitar fecha</button>
    </div>

    <p v-if="filtroFecha" class="c-muted" style="font-size:0.78rem; margin:-0.75rem 0 1rem;">
      Viernes y sábado el corte es a la 1am, así que ese día incluye lo vendido en la madrugada siguiente.
    </p>

    <div class="resumen-cierre">
      <div class="resumen-card">
        <span class="resumen-label">Ventas totales</span>
        <span class="resumen-valor">{{ totalElementos }}</span>
      </div>
      <div class="resumen-card">
        <span class="resumen-label">{{ filtroFecha ? 'Total de esta página' : 'Suma de esta página' }}</span>
        <span class="resumen-valor c-cyan">${{ totalPagina.toLocaleString() }}</span>
      </div>
    </div>

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Empleado</th>
            <th>Sede</th>
            <th>Pago</th>
            <th>Tipo</th>
            <th>Total</th>
            <th>Fecha</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="empty">Cargando...</td>
          </tr>
          <tr v-else-if="!ventas.length">
            <td colspan="8" class="empty">Sin ventas registradas.</td>
          </tr>
          <tr v-for="v in ventas" :key="v.id">
            <td class="c-muted">#{{ v.id }}</td>
            <td style="font-weight:500;">{{ v.usuario }}</td>
            <td><span class="tag-sede">{{ v.sede }}</span></td>
            <td>
              <span class="tag-sede">
                {{ metodosMap[v.metodoPago] || v.metodoPago }}
              </span>
            </td>
            <td>
              <span :style="{ color: v.tipoPedido === 'DOMICILIO' ? 'var(--purple)' : 'var(--cyan)', fontSize: '0.82rem', fontWeight: 600 }">
                {{ v.tipoPedido === 'DOMICILIO' ? '🛵 Domicilio' : '🏠 Local' }}
              </span>
            </td>
            <td class="c-cyan">
              ${{ Number(v.total).toLocaleString() }}
              <span v-if="v.esPromocion" style="font-size:0.72rem; color:var(--ok); margin-left:4px;">🎉</span>
            </td>
            <td class="c-muted">{{ v.fecha }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="verDetalle(v)">Ver detalle</button>
                <button class="bsm bd"  @click="abrirConfirmEliminar(v)">Eliminar</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="paginador" v-if="totalPaginas > 1">
      <button class="bsm bd" :disabled="pagina === 0" @click="irAPagina(pagina - 1)">‹ Anterior</button>
      <span class="pag-info">Página {{ pagina + 1 }} de {{ totalPaginas }}</span>
      <button class="bsm bd" :disabled="pagina >= totalPaginas - 1" @click="irAPagina(pagina + 1)">Siguiente ›</button>
    </div>

    <!-- MODAL: VER DETALLE -->
    <div v-if="ventaDetalle" class="modal-overlay" @click.self="ventaDetalle = null">
      <div class="modal-box" style="max-width:640px;">
        <div class="modal-header">
          <div>
            <p class="modal-title">
              Venta <span class="c-cyan">#{{ ventaDetalle.id }}</span>
              <span v-if="ventaDetalle.esPromocion"
                style="font-size:0.75rem; color:var(--ok); margin-left:8px;">
                🎉 Con promoción
              </span>
            </p>
            <p class="modal-sub">
              {{ ventaDetalle.usuario }}
              <span class="tag-sede" style="margin-left:6px;">{{ ventaDetalle.sede }}</span>
              <span class="tag-sede" style="margin-left:6px;">
                {{ metodosMap[ventaDetalle.metodoPago] || ventaDetalle.metodoPago }}
              </span>
              <span style="margin-left:6px; font-size:0.8rem;">
                {{ ventaDetalle.tipoPedido === 'DOMICILIO' ? '🛵 Domicilio' : '🏠 Local' }}
              </span>
            </p>
          </div>
          <button class="modal-close" @click="ventaDetalle = null">✕</button>
        </div>

        <div class="modal-body">
          <div class="tbl-w" style="margin:0; box-shadow:none;">
            <table>
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Tamaño</th>
                  <th>Cant.</th>
                  <th>Precio unit.</th>
                  <th>Subtotal</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="d in ventaDetalle.detalles" :key="d.id">
                  <td style="font-weight:500; color:var(--text);">{{ d.producto }}</td>
                  <td style="color:var(--purple);">{{ d.tamaño }}</td>
                  <td style="color:var(--text2);">{{ d.cantidad }}</td>
                  <td style="color:var(--text2);">${{ Number(d.precioUnitario).toLocaleString() }}</td>
                  <td class="c-cyan">${{ Number(d.subtotal).toLocaleString() }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="modal-footer" style="flex-direction:column; align-items:stretch; gap:10px;">
          <p class="modal-fecha">{{ ventaDetalle.fecha }}</p>
          <div v-if="ventaDetalle.tipoPedido === 'DOMICILIO' && Number(ventaDetalle.valorDomicilio) > 0"
               style="display:flex; flex-direction:column; gap:4px; align-items:flex-end;">
            <div style="display:flex; gap:12px; align-items:baseline;">
              <span style="font-size:0.78rem; color:var(--text3);">Subtotal productos</span>
              <span style="font-size:0.85rem; color:var(--text2);">${{ subtotalProductosDetalle.toLocaleString() }}</span>
            </div>
            <div style="display:flex; gap:12px; align-items:baseline;">
              <span style="font-size:0.78rem; color:var(--text3);">🛵 Domicilio</span>
              <span style="font-size:0.85rem; color:var(--purple); font-weight:600;">
                ${{ Number(ventaDetalle.valorDomicilio).toLocaleString() }}
              </span>
            </div>
          </div>
          <div class="modal-total" style="align-self:flex-end;">
            <span class="ct-lbl">Total</span>
            <span class="ct-val">${{ Number(ventaDetalle.total).toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL: CONFIRMAR ELIMINACIÓN -->
    <div v-if="modalConfirm" class="modal-overlay" @click.self="modalConfirm = null">
      <div class="modal-box" style="max-width:420px;">
        <div class="modal-header">
          <p class="modal-title" style="color:var(--err);">⚠ Eliminar venta</p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body" style="padding:1.25rem;">
          <p style="font-size:0.92rem; font-weight:600; color:var(--purple); margin:0;">
            ¿Eliminar la venta #{{ modalConfirm.venta.id }}?
          </p>
          <p class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            {{ modalConfirm.venta.usuario }} — {{ modalConfirm.venta.sede }} — ${{ Number(modalConfirm.venta.total).toLocaleString() }}
          </p>
          <p class="c-muted" style="font-size:0.8rem; margin-top:4px;">
            El stock de los productos vendidos se devuelve automáticamente al inventario. Esta acción no se puede deshacer.
          </p>
          <p v-if="modalConfirm.error" style="margin-top:10px; font-size:0.82rem; color:var(--err); font-weight:500;">
            {{ modalConfirm.error }}
          </p>
        </div>

        <div class="modal-footer" style="justify-content:flex-end; gap:10px;">
          <button class="bsm bd" @click="modalConfirm = null">Cancelar</button>
          <button class="bpri" style="background:var(--err); border-color:var(--err);"
                  :disabled="eliminando" @click="confirmarEliminacion">
            {{ eliminando ? 'Eliminando...' : 'Sí, eliminar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- TOAST -->
    <div v-if="toast" class="toast" :class="toast.ok ? 'ok' : 'err'">{{ toast.texto }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'
import { useWebSocket } from '@/composables/useWebSocket'

const { api, loading } = useAdmin()
const { conectar, desconectar } = useWebSocket()

const TAMANO_PAGINA = 50

const ventas         = ref([])
const usuarios       = ref([])
const sedes          = ref([])
const filtroUsuario  = ref('')
const filtroSede     = ref('')
const filtroFecha    = ref('')
const ventaDetalle   = ref(null)

const pagina          = ref(0)
const totalPaginas    = ref(0)
const totalElementos  = ref(0)

const modalConfirm = ref(null)
const eliminando   = ref(false)
const toast        = ref(null)

const metodosMap = {
  EFECTIVO:    '💵 Efectivo',
  NEQUI:       '📱 Nequi',
  BANCOLOMBIA: '🏦 Bancolombia',
}

function setToast(texto, ok = true) {
  toast.value = { texto, ok }
  setTimeout(() => { toast.value = null }, 3000)
}

const totalPagina = computed(() =>
  ventas.value.reduce((acc, v) => acc + Number(v.total || 0), 0)
)

const subtotalProductosDetalle = computed(() => {
  if (!ventaDetalle.value?.detalles) return 0
  return ventaDetalle.value.detalles.reduce((acc, d) => acc + Number(d.subtotal || 0), 0)
})

onMounted(async () => {
  const [u, s] = await Promise.all([
    api('GET', '/usuarios').catch(() => []),
    api('GET', '/sedes/listar').catch(() => [])
  ])
  usuarios.value = u
  sedes.value    = s
  await cargar()

  conectar((ventaNueva) => {
    const sinFiltros = !filtroUsuario.value && !filtroSede.value && !filtroFecha.value
    if (sinFiltros && pagina.value === 0) {
      const existe = ventas.value.find(v => v.id === ventaNueva.id)
      if (!existe) ventas.value.unshift(ventaNueva)
    }
  })
})

onUnmounted(() => desconectar())

async function aplicarPagina() {
  loading.value = true
  try {
    const qp = `page=${pagina.value}&size=${TAMANO_PAGINA}`
    let url

    if (filtroUsuario.value) {
      url = `/ventas/usuario/${filtroUsuario.value}?${qp}`
    } else if (filtroFecha.value && filtroSede.value) {
      url = `/ventas/sede/${filtroSede.value}/fecha/${filtroFecha.value}?${qp}`
    } else if (filtroFecha.value) {
      url = `/ventas/fecha/${filtroFecha.value}?${qp}`
    } else if (filtroSede.value) {
      url = `/ventas/sede/${filtroSede.value}?${qp}`
    } else {
      url = `/ventas/todas?${qp}`
    }

    const res = await api('GET', url)
    ventas.value          = res.content ?? []
    totalPaginas.value    = res.totalPages ?? 0
    totalElementos.value  = res.totalElements ?? ventas.value.length
  } catch {
    ventas.value = []
    totalPaginas.value = 0
    totalElementos.value = 0
  } finally {
    loading.value = false
  }
}

async function cargar() {
  filtroUsuario.value = ''
  filtroSede.value    = ''
  filtroFecha.value   = ''
  pagina.value        = 0
  await aplicarPagina()
}

async function filtrarUsuario() {
  filtroSede.value  = ''
  filtroFecha.value = ''
  pagina.value      = 0
  if (!filtroUsuario.value) { await cargar(); return }
  await aplicarPagina()
}

async function filtrarPorFechaOSede() {
  filtroUsuario.value = ''
  pagina.value        = 0
  await aplicarPagina()
}

function limpiarFecha() {
  filtroFecha.value = ''
  pagina.value = 0
  aplicarPagina()
}

function irAPagina(nueva) {
  if (nueva < 0 || nueva >= totalPaginas.value) return
  pagina.value = nueva
  aplicarPagina()
}

function abrirConfirmEliminar(v) {
  modalConfirm.value = { venta: v, error: '' }
}

async function confirmarEliminacion() {
  eliminando.value = true
  try {
    await api('DELETE', `/ventas/eliminar/${modalConfirm.value.venta.id}`)
    modalConfirm.value = null
    setToast('✓ Venta eliminada, stock devuelto al inventario')
    await aplicarPagina()
  } catch (e) {
    modalConfirm.value.error = e.response?.data || e.message || 'Error al eliminar'
  } finally {
    eliminando.value = false
  }
}

function verDetalle(v) {
  ventaDetalle.value = v
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex; align-items: center; justify-content: center;
  padding: 1rem;
}
.modal-box {
  background: var(--bg2);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  width: 100%;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  animation: modal-in 0.2s ease;
}
@keyframes modal-in {
  from { opacity:0; transform: translateY(16px) scale(0.97); }
  to   { opacity:1; transform: translateY(0) scale(1); }
}
.modal-header {
  display: flex; align-items: flex-start;
  justify-content: space-between;
  padding: 1.25rem 1.5rem 1rem;
  border-bottom: 1px solid var(--border2);
  gap: 12px;
}
.modal-title {
  font-size: 1rem; font-weight: 700;
  color: var(--text); margin-bottom: 6px;
}
.modal-sub {
  font-size: 0.82rem; color: var(--text2);
  display: flex; align-items: center;
  gap: 8px; flex-wrap: wrap;
}
.modal-close {
  background: rgba(244,63,94,0.1);
  border: 1px solid rgba(244,63,94,0.3);
  color: var(--err); border-radius: var(--radius-xs);
  cursor: pointer; padding: 4px 10px;
  font-size: 0.85rem; font-family: var(--font);
  font-weight: 600; transition: all 0.15s; flex-shrink: 0;
}
.modal-close:hover { background: rgba(244,63,94,0.2); }
.modal-body { max-height: 320px; overflow-y: auto; }
.modal-body::-webkit-scrollbar { width: 4px; }
.modal-body::-webkit-scrollbar-thumb { background: var(--border); border-radius: 2px; }
.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border2);
  background: var(--bg3);
  display: flex; align-items: center;
  justify-content: space-between;
  flex-wrap: wrap; gap: 10px;
}
.modal-fecha { font-size: 0.78rem; color: var(--text3); font-weight: 500; }
.modal-total { display: flex; align-items: baseline; gap: 12px; }

.resumen-cierre {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.resumen-card {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem 1.25rem;
  border: 1px solid color-mix(in srgb, var(--border, #333) 70%, transparent);
  border-radius: 8px;
}
.resumen-label {
  font-size: 0.75rem;
  color: var(--text3);
}
.resumen-valor {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text);
}

.paginador {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.25rem;
}
.pag-info {
  font-size: 0.85rem;
  color: var(--text2);
  font-weight: 500;
}

.toast {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  padding: 10px 20px; border-radius: 8px;
  font-size: 0.88rem; font-weight: 600;
  z-index: 1100; box-shadow: var(--shadow-lg);
  animation: toast-in 0.2s ease;
}
.toast.ok  { background: var(--ok);  color: #fff; }
.toast.err { background: var(--err); color: #fff; }
@keyframes toast-in {
  from { opacity: 0; transform: translateX(-50%) translateY(10px); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0); }
}
</style>
