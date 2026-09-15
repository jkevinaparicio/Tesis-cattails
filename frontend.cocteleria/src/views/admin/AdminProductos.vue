<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Productos</h2>
      <button class="bpri" @click="abrirCrear">+ Nuevo(s) producto(s)</button>
    </div>

    <!-- FILTROS -->
    <div class="frow s2" style="align-items:flex-end; margin-bottom:1.25rem; gap:12px;">
      <div class="ff" style="margin:0; max-width:260px;">
        <label>Buscar producto</label>
        <input v-model="productoFiltro" placeholder="Ej: Mojito, Corona..." />
      </div>
      <div class="ff" style="margin:0; max-width:220px;">
        <label>Categoría</label>
        <select v-model="categoriaFiltro">
          <option value="">Todas las categorías</option>
          <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
        </select>
      </div>
    </div>

    <!-- PRODUCTOS AGRUPADOS POR CATEGORÍA -->
    <div v-if="!productosPorCategoria.length" class="tbl-w">
      <table>
        <tbody>
          <tr><td class="empty">Sin productos que coincidan con la búsqueda.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-for="grupo in productosPorCategoria" :key="grupo.categoriaId" class="cat-group">
      <div class="cat-group-header">
        <span class="cat-group-title">{{ grupo.categoriaNombre }}</span>
        <span class="cat-group-count">{{ grupo.productos.length }} producto(s)</span>
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
            <tr v-for="p in grupo.productos" :key="p.id">
              <td>{{ p.nombre }}</td>
              <td>
                <div class="btn-r">
                  <button class="bsm be2" @click="abrirEditar(p)">Editar</button>
                  <button class="bsm bd"  @click="abrirConfirmEliminar(p)">Eliminar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ============ MODAL: CREAR EN LOTE ============ -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <p class="modal-title">+ Nuevo(s) producto(s)</p>
          <button class="modal-close" @click="showForm = false">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
          <div v-for="(item, idx) in lote" :key="idx" class="frow lote-row">
            <div class="ff">
              <label>Nombre #{{ idx + 1 }}</label>
              <input v-model="item.nombre" placeholder="Mojito Clásico"/>
            </div>
            <div class="ff">
              <label>Categoría</label>
              <select v-model="item.catId">
                <option value="">Seleccionar...</option>
                <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
              </select>
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
          <p class="modal-title">Editar producto</p>
          <button class="modal-close" @click="editando = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">{{ editando.nombre }}</p>

          <div class="ff" style="margin-top:1rem;">
            <label>Nombre</label>
            <input ref="inputEditar" v-model="editForm.nombre" />
          </div>
          <div class="ff" style="margin-top:0.75rem;">
            <label>Categoría</label>
            <select v-model="editForm.catId">
              <option value="">Seleccionar...</option>
              <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
            </select>
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
          <p class="modal-title" style="color:var(--err);">⚠ Eliminar producto</p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">¿Eliminar el producto "{{ modalConfirm.producto.nombre }}"?</p>
          <p class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            Esta acción no se puede deshacer. Si tiene variantes, precios o inventario asociado, revisa antes que no dependan de él.
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

const productos   = ref([])
const categorias  = ref([])
const showForm    = ref(false)
const editando    = ref(null)

const productoFiltro  = ref('')
const categoriaFiltro = ref('')

const lote     = ref([{ nombre: '', catId: '' }])
const editForm = ref({ nombre: '', catId: '' })
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

const productosPorCategoria = computed(() => {
  let lista = productos.value

  if (productoFiltro.value.trim()) {
    const q = productoFiltro.value.toLowerCase()
    lista = lista.filter(p => p.nombre?.toLowerCase().includes(q))
  }
  if (categoriaFiltro.value) {
    lista = lista.filter(p => p.categoria?.id === parseInt(categoriaFiltro.value))
  }

  const mapa = new Map()
  for (const p of lista) {
    const catId = p.categoria?.id
    const catNombre = p.categoria?.nombre || 'Sin categoría'
    if (!mapa.has(catId)) {
      mapa.set(catId, { categoriaId: catId, categoriaNombre: catNombre, productos: [] })
    }
    mapa.get(catId).productos.push(p)
  }
  return [...mapa.values()].sort((a, b) => a.categoriaNombre.localeCompare(b.categoriaNombre))
})

onMounted(cargar)

async function cargar() {
  const [p, c] = await Promise.all([
    api('GET', '/productos/todos').catch(() => []),
    api('GET', '/categoria/todas').catch(() => [])
  ])
  productos.value  = p
  categorias.value = c
}

function abrirCrear() {
  showForm.value = true
  lote.value = [{ nombre: '', catId: '' }]
  modalCrear.value = { error: '' }
}

function agregarFila()   { lote.value.push({ nombre: '', catId: '' }) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function crearLote() {
  const validos = lote.value.filter(i => i.nombre.trim() && i.catId)
  const sinCat  = lote.value.filter(i => i.nombre.trim() && !i.catId)

  if (!validos.length) {
    modalCrear.value.error = 'Cada producto necesita nombre y categoría'
    return
  }

  guardando.value = true
  try {
    const resultados = await Promise.allSettled(
      validos.map(i => api('POST', '/productos/crear', {
        nombre:    i.nombre.trim(),
        categoria: { id: parseInt(i.catId) }
      }))
    )

    const ok  = resultados.filter(r => r.status === 'fulfilled').length
    const err = resultados.filter(r => r.status === 'rejected').length

    if (err === 0 && !sinCat.length) {
      showForm.value = false
      setToast(`✓ ${ok} producto(s) creado(s)`)
    } else {
      const partes = []
      if (ok) partes.push(`${ok} creado(s)`)
      if (err) partes.push(`${err} fallaron`)
      if (sinCat.length) partes.push(`${sinCat.length} omitido(s) por falta de categoría`)
      modalCrear.value.error = partes.join(', ')
    }
    cargar()
  } finally {
    guardando.value = false
  }
}

async function abrirEditar(p) {
  editando.value    = p
  editForm.value    = { nombre: p.nombre, catId: p.categoria?.id || '' }
  modalEditar.value = { error: '' }
  await nextTick()
  inputEditar.value?.focus()
}

async function guardarEdicion() {
  if (!editForm.value.nombre.trim()) { modalEditar.value.error = 'El nombre es obligatorio'; return }
  if (!editForm.value.catId)         { modalEditar.value.error = 'Selecciona una categoría'; return }

  guardando.value = true
  try {
    await api('PUT', `/productos/modificar/${editando.value.id}`, {
      nombre:    editForm.value.nombre.trim(),
      categoria: { id: parseInt(editForm.value.catId) }
    })
    editando.value = null
    setToast('✓ Producto actualizado')
    cargar()
  } catch (e) {
    modalEditar.value.error = e.response?.data || e.message || 'Error al guardar'
  } finally {
    guardando.value = false
  }
}

function abrirConfirmEliminar(p) {
  modalConfirm.value = { producto: p, error: '' }
}

async function confirmarEliminacion() {
  guardando.value = true
  try {
    await api('DELETE', `/productos/eliminar/${modalConfirm.value.producto.id}`)
    modalConfirm.value = null
    setToast('✓ Producto eliminado')
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
.cat-group {
  margin-bottom: 1.75rem;
}
.cat-group-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
  padding: 0.4rem 0.75rem;
  background: color-mix(in srgb, var(--purple) 10%, transparent);
  border-left: 3px solid var(--purple);
  border-radius: 0 6px 6px 0;
}
.cat-group-title {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--purple);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.cat-group-count {
  font-size: 0.75rem;
  color: var(--text2);
  background: color-mix(in srgb, var(--purple) 15%, transparent);
  padding: 2px 8px;
  border-radius: 99px;
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