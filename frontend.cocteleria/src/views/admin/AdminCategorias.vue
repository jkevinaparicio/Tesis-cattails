<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Categorías</h2>
      <button class="bpri" @click="abrirCrear">+ Nueva(s) categoría(s)</button>
    </div>

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Tipo de Stock</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!categorias.length">
            <td colspan="3" class="empty">Sin categorías registradas.</td>
          </tr>
          <tr v-for="c in categorias" :key="c.id">
            <td style="font-weight: 600; color: var(--text);">{{ c.nombre }}</td>
            <td>
              <span v-if="c.compartirStockPorTamano" class="tag-shared">🍹 Compartido por tamaño</span>
              <span v-else class="tag-indiv">📦 Individual por producto</span>
            </td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="abrirEditar(c)">Editar</button>
                <button class="bsm bd"  @click="abrirConfirmEliminar(c)">Eliminar</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ============ MODAL: CREAR EN LOTE ============ -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <p class="modal-title">+ Nueva(s) categoría(s)</p>
          <button class="modal-close" @click="showForm = false">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
          <div v-for="(item, idx) in lote" :key="idx" class="frow s1 lote-row">
            <div class="ff" style="flex: 2;">
              <label>Nombre #{{ idx + 1 }}</label>
              <input v-model="item.nombre" placeholder="Cócteles clásicos"/>
            </div>
            <div class="ff" style="flex: 1.5; justify-content: flex-end; padding-bottom: 0.6rem;">
              <label class="check-inline">
                <input type="checkbox" v-model="item.compartirStockPorTamano" />
                <span>🍹 Stock compartido por tamaño</span>
              </label>
            </div>
            <button v-if="lote.length > 1" class="bsm bd lote-rm" @click="quitarFila(idx)" title="Quitar">✕</button>
          </div>

          <button class="bsm be2 lote-add" @click="agregarFila">+ Agregar otro</button>

          <p v-if="modalCrear.error" class="modal-err">{{ modalCrear.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="showForm = false">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="crearLote">
            {{ guardando ? 'Guardando...' : 'Guardar todas' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: EDITAR ============ -->
    <div v-if="editando" class="modal-overlay" @click.self="editando = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title">Editar categoría</p>
          <button class="modal-close" @click="editando = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">{{ editando.nombre }}</p>

          <div class="ff" style="margin-top:1rem;">
            <label>Nuevo nombre</label>
            <input ref="inputEditar" v-model="editNombre" @keyup.enter="guardarEdicion" />
          </div>

          <label class="check-inline" style="margin-top:1rem;">
            <input type="checkbox" v-model="editCompartirStock" />
            <span>🍹 Compartir stock por tamaño (pool compartido entre productos)</span>
          </label>

          <p v-if="modalEditar.error" class="modal-err">{{ modalEditar.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="editando = null">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="guardarEdicion">
            {{ guardando ? 'Guardando...' : 'Guardar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: CONFIRMAR ELIMINACIÓN ============ -->
    <div v-if="modalConfirm" class="modal-overlay" @click.self="modalConfirm = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title" style="color:var(--err);">⚠ Eliminar categoría</p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">¿Eliminar la categoría "{{ modalConfirm.categoria.nombre }}"?</p>
          <p class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            Esta acción no se puede deshacer. Si hay productos usando esta categoría, revisa antes que no dependan de ella.
          </p>
          <p v-if="modalConfirm.error" class="modal-err">{{ modalConfirm.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="modalConfirm = null">Cancelar</button>
          <button class="bpri" style="background:var(--err); border-color:var(--err);"
                  :disabled="guardando" @click="confirmarEliminacion">
            {{ guardando ? 'Eliminando...' : 'Sí, eliminar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- TOAST GLOBAL -->
    <div v-if="toast" class="toast" :class="toast.ok ? 'ok' : 'err'">{{ toast.texto }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api } = useAdmin()

const categorias = ref([])
const showForm   = ref(false)
const lote       = ref([{ nombre: '', compartirStockPorTamano: false }])

const editando           = ref(null)
const editNombre         = ref('')
const editCompartirStock = ref(false)
const inputEditar        = ref(null)

const modalCrear   = ref({ error: '' })
const modalEditar  = ref({ error: '' })
const modalConfirm = ref(null)
const guardando    = ref(false)
const toast        = ref(null)

function setToast(texto, ok = true) {
  toast.value = { texto, ok }
  setTimeout(() => { toast.value = null }, 3000)
}

onMounted(cargar)

async function cargar() {
  categorias.value = await api('GET', '/categoria/todas').catch(() => [])
}

function abrirCrear() {
  showForm.value = true
  lote.value = [{ nombre: '', compartirStockPorTamano: false }]
  modalCrear.value = { error: '' }
}

function agregarFila()   { lote.value.push({ nombre: '', compartirStockPorTamano: false }) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function crearLote() {
  const validos = lote.value.filter(i => i.nombre.trim())
  if (!validos.length) {
    modalCrear.value.error = 'Ingresa al menos un nombre'
    return
  }

  guardando.value = true
  try {
    const resultados = await Promise.allSettled(
      validos.map(i => api('POST', '/categoria/crear', {
        nombre: i.nombre.trim(),
        compartirStockPorTamano: !!i.compartirStockPorTamano
      }))
    )

    const ok  = resultados.filter(r => r.status === 'fulfilled').length
    const err = resultados.filter(r => r.status === 'rejected').length

    if (err === 0) {
      showForm.value = false
      setToast(`✓ ${ok} categoría(s) creada(s) correctamente`)
    } else {
      modalCrear.value.error = `${ok} creadas, ${err} fallaron`
    }
    cargar()
  } finally {
    guardando.value = false
  }
}

async function abrirEditar(c) {
  editando.value           = c
  editNombre.value         = c.nombre
  editCompartirStock.value = !!c.compartirStockPorTamano
  modalEditar.value        = { error: '' }
  await nextTick()
  inputEditar.value?.focus()
}

async function guardarEdicion() {
  if (!editNombre.value.trim()) {
    modalEditar.value.error = 'El nombre es obligatorio'
    return
  }
  guardando.value = true
  try {
    await api('PUT', `/categoria/modificar/${editando.value.id}`, {
      nombre: editNombre.value.trim(),
      compartirStockPorTamano: editCompartirStock.value
    })
    editando.value = null
    setToast('✓ Categoría actualizada')
    cargar()
  } catch (e) {
    modalEditar.value.error = e.response?.data || e.message || 'Error al guardar'
  } finally {
    guardando.value = false
  }
}

function abrirConfirmEliminar(c) {
  modalConfirm.value = { categoria: c, error: '' }
}

async function confirmarEliminacion() {
  guardando.value = true
  try {
    await api('DELETE', `/categoria/eliminar/${modalConfirm.value.categoria.id}`)
    modalConfirm.value = null
    setToast('✓ Categoría eliminada')
    cargar()
  } catch (e) {
    modalConfirm.value.error = e.response?.data || e.message || 'Error al eliminar'
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped src="@/assets/admin.css">
</style>

<style scoped>
.lote-row {
  align-items: flex-end;
  gap: 0.5rem;
}
.lote-rm {
  margin-bottom: 0;
  flex-shrink: 0;
}
.lote-add {
  margin-bottom: 1rem;
}
.check-inline {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--text2);
  cursor: pointer;
  user-select: none;
}
.check-inline input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--purple);
  cursor: pointer;
}
.tag-shared {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
  background: color-mix(in srgb, var(--purple) 15%, transparent);
  color: var(--purple);
  border: 1px solid color-mix(in srgb, var(--purple) 30%, transparent);
}
.tag-indiv {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 6px;
  background: color-mix(in srgb, var(--text3) 12%, transparent);
  color: var(--text2);
  border: 1px solid color-mix(in srgb, var(--text3) 25%, transparent);
}

/* ---------- MODALES ---------- */
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
  width: 100%; box-shadow: var(--shadow-lg);
  overflow: hidden; animation: modal-in 0.18s ease;
  display: flex; flex-direction: column;
}
.modal-sm { max-width: 420px; }
.modal-lg { max-width: 640px; max-height: 85vh; }
@keyframes modal-in {
  from { opacity:0; transform: translateY(14px) scale(0.97); }
  to   { opacity:1; transform: translateY(0) scale(1); }
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border2);
  gap: 12px;
  flex-shrink: 0;
}
.modal-title { font-size: 0.98rem; font-weight: 700; color: var(--text); margin: 0; }
.modal-close {
  background: rgba(244,63,94,0.1);
  border: 1px solid rgba(244,63,94,0.3);
  color: var(--err); border-radius: var(--radius-xs);
  cursor: pointer; padding: 3px 9px; font-size: 0.85rem;
  font-family: var(--font); font-weight: 600; transition: all 0.15s; flex-shrink: 0;
}
.modal-close:hover { background: rgba(244,63,94,0.2); }
.modal-body-pad { padding: 1.25rem; }
.modal-scroll { overflow-y: auto; }
.modal-target { font-size: 0.92rem; font-weight: 600; color: var(--purple); margin: 0; }
.modal-err {
  margin-top: 10px; font-size: 0.82rem; color: var(--err); font-weight: 500;
}
.modal-footer-pad {
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--border2);
  background: var(--bg3);
  display: flex; align-items: center; justify-content: flex-end; gap: 10px;
  flex-shrink: 0;
}

/* ---------- TOAST ---------- */
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