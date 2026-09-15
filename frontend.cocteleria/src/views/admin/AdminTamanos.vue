<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Tamaños</h2>
      <button class="bpri" @click="abrirCrear">+ Nuevo(s) tamaño(s)</button>
    </div>

    <!-- BÚSQUEDA -->
    <div class="ff" style="max-width:280px; margin-bottom:1.25rem;">
      <label>Buscar tamaño</label>
      <input v-model="busqueda" placeholder="Ej: Grande, Pequeño..."/>
    </div>

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!tamanosFiltrados.length">
            <td colspan="2" class="empty">Sin tamaños registrados.</td>
          </tr>
          <tr v-for="t in tamanosFiltrados" :key="t.id">
            <td>{{ t.nombre }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="abrirEditar(t)">Editar</button>
                <button class="bsm bd"  @click="abrirConfirmEliminar(t)">Eliminar</button>
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
          <p class="modal-title">+ Nuevo(s) tamaño(s)</p>
          <button class="modal-close" @click="showForm = false">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
          <div v-for="(item, idx) in lote" :key="idx" class="frow s1 lote-row">
            <div class="ff">
              <label>Nombre #{{ idx + 1 }}</label>
              <input v-model="item.nombre" placeholder="Grande"/>
            </div>
            <button v-if="lote.length > 1" class="bsm bd lote-rm" @click="quitarFila(idx)" title="Quitar">✕</button>
          </div>

          <button class="bsm be2 lote-add" @click="agregarFila">+ Agregar otro</button>

          <p v-if="modalCrear.error" class="modal-err">{{ modalCrear.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="showForm = false">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="crearLote">
            {{ guardando ? 'Guardando...' : 'Guardar todos' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: EDITAR ============ -->
    <div v-if="editando" class="modal-overlay" @click.self="editando = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title">Editar tamaño</p>
          <button class="modal-close" @click="editando = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">{{ editando.nombre }}</p>

          <div class="ff" style="margin-top:1rem;">
            <label>Nuevo nombre</label>
            <input ref="inputEditar" v-model="editNombre" @keyup.enter="guardarEdicion" />
          </div>

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
          <p class="modal-title" style="color:var(--err);">⚠ Eliminar tamaño</p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">¿Eliminar el tamaño "{{ modalConfirm.tamano.nombre }}"?</p>
          <p class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            Esta acción no se puede deshacer. Si hay variantes o inventario usando este tamaño, revisa antes que no dependan de él.
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api } = useAdmin()

const tamanos    = ref([])
const showForm   = ref(false)
const lote       = ref([{ nombre: '' }])
const editando   = ref(null)
const editNombre = ref('')
const busqueda   = ref('')
const inputEditar = ref(null)

const modalCrear   = ref({ error: '' })
const modalEditar  = ref({ error: '' })
const modalConfirm = ref(null)
const guardando    = ref(false)
const toast        = ref(null)

function setToast(texto, ok = true) {
  toast.value = { texto, ok }
  setTimeout(() => { toast.value = null }, 3000)
}

const tamanosFiltrados = computed(() => {
  if (!busqueda.value.trim()) return tamanos.value
  return tamanos.value.filter(t =>
    t.nombre.toLowerCase().includes(busqueda.value.toLowerCase())
  )
})

onMounted(cargar)

async function cargar() {
  tamanos.value = await api('GET', '/tamanos/todos').catch(() => [])
}

function abrirCrear() {
  showForm.value = true
  lote.value = [{ nombre: '' }]
  modalCrear.value = { error: '' }
}

function agregarFila()   { lote.value.push({ nombre: '' }) }
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
      validos.map(i => api('POST', '/tamanos/crear', { nombre: i.nombre.trim() }))
    )

    const ok  = resultados.filter(r => r.status === 'fulfilled').length
    const err = resultados.filter(r => r.status === 'rejected').length

    if (err === 0) {
      showForm.value = false
      setToast(`✓ ${ok} tamaño(s) creado(s)`)
    } else {
      modalCrear.value.error = `${ok} creados, ${err} fallaron`
    }
    cargar()
  } finally {
    guardando.value = false
  }
}

async function abrirEditar(t) {
  editando.value    = t
  editNombre.value  = t.nombre
  modalEditar.value = { error: '' }
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
    await api('PUT', `/tamanos/modificar/${editando.value.id}`, { nombre: editNombre.value.trim() })
    editando.value = null
    setToast('✓ Tamaño actualizado')
    cargar()
  } catch (e) {
    modalEditar.value.error = e.response?.data || e.message || 'Error al guardar'
  } finally {
    guardando.value = false
  }
}

function abrirConfirmEliminar(t) {
  modalConfirm.value = { tamano: t, error: '' }
}

async function confirmarEliminacion() {
  guardando.value = true
  try {
    await api('DELETE', `/tamanos/eliminar/${modalConfirm.value.tamano.id}`)
    modalConfirm.value = null
    setToast('✓ Tamaño eliminado')
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