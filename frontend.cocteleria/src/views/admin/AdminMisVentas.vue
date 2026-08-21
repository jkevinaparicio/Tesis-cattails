<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Mis ventas</h2>
      <button class="bpri" @click="cargar">↻ Actualizar</button>
    </div>

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>#</th>
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
            <td colspan="7" class="empty">Cargando...</td>
          </tr>
          <tr v-else-if="!ventas.length">
            <td colspan="7" class="empty">No tienes ventas registradas.</td>
          </tr>
          <tr v-for="v in ventas" :key="v.id">
            <td class="c-muted">#{{ v.id }}</td>
            <td><span class="tag-sede">{{ v.sede }}</span></td>
            <td>
              <span class="tag-sede">
                {{ metodosMap[v.metodoPago] || v.metodoPago }}
              </span>
            </td>
            <td>
              <span :style="{
                color: v.tipoPedido === 'DOMICILIO' ? 'var(--purple)' : 'var(--cyan)',
                fontSize: '0.82rem',
                fontWeight: 600
              }">
                {{ v.tipoPedido === 'DOMICILIO' ? '🛵 Domicilio' : '🏠 Local' }}
              </span>
            </td>
            <td class="c-cyan">
              ${{ Number(v.total).toLocaleString() }}
              <span v-if="v.esPromocion"
                style="font-size:0.72rem; color:var(--ok); margin-left:4px;">
                🎉
              </span>
            </td>
            <td class="c-muted">{{ v.fecha }}</td>
            <td>
              <button class="bsm be2" @click="verDetalle(v)">Ver detalle</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- MODAL -->
    <div v-if="ventaDetalle" class="modal-overlay" @click.self="ventaDetalle = null">
      <div class="modal-box">
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
              <span class="tag-sede">{{ ventaDetalle.sede }}</span>
              <span class="tag-sede">
                {{ metodosMap[ventaDetalle.metodoPago] || ventaDetalle.metodoPago }}
              </span>
              <span style="font-size:0.8rem;">
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

        <div class="modal-footer">
          <p class="modal-fecha">{{ ventaDetalle.fecha }}</p>
          <div class="modal-total">
            <span class="ct-lbl">Total</span>
            <span class="ct-val">${{ Number(ventaDetalle.total).toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api, loading } = useAdmin()
const ventas       = ref([])
const ventaDetalle = ref(null)

const metodosMap = {
  EFECTIVO:    '💵 Efectivo',
  NEQUI:       '📱 Nequi',
  BANCOLOMBIA: '🏦 Bancolombia',
}

onMounted(cargar)

async function cargar() {
  loading.value = true
  try {
    ventas.value = await api('GET', '/ventas/mis-ventas')
  } catch {
    ventas.value = []
  } finally {
    loading.value = false
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
  width: 100%; max-width: 640px;
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
</style>