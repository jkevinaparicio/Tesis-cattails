<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Variantes y Precios</h2>
      <div style="display:flex; gap:8px;">
        <button class="bsm be2" @click="abrirEditarBloque">✎ Editar precios por producto</button>
        <button class="bpri" @click="abrirCrear">+ Asignar precios</button>
      </div>
    </div>

    <!-- FILTRO -->
    <div style="display:flex; gap:10px; margin-bottom:1.25rem; flex-wrap:wrap; align-items:flex-end;">
      <div class="ff" style="margin:0; min-width:220px;">
        <label>Filtrar por producto</label>
        <select v-model="filtroProducto" @change="filtrar">
          <option value="">Todos los productos</option>
          <option v-for="p in productos" :key="p.id" :value="p.id">
            {{ p.nombre }}
          </option>
        </select>
      </div>
      <button class="bsm be2" @click="filtroProducto = ''; cargar()">Ver todos</button>
    </div>

    <!-- TABLA -->
    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Producto</th>
            <th>Categoría</th>
            <th>Tamaño</th>
            <th>Precio</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="cargando">
            <td colspan="5" class="empty">Cargando...</td>
          </tr>
          <tr v-else-if="!variantes.length">
            <td colspan="5" class="empty">Sin variantes registradas.</td>
          </tr>
          <tr v-for="v in variantes" :key="v.id">
            <td style="font-weight:600; color:var(--text);">{{ v.producto?.nombre }}</td>
            <td class="c-muted">{{ v.producto?.categoria?.nombre }}</td>
            <td style="color:var(--purple); font-weight:500;">
              {{ v.tamaño?.nombre ?? v.tamano?.nombre }}
            </td>
            <td class="c-cyan">${{ Number(v.precio).toLocaleString() }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="abrirEditar(v)">Editar precio</button>
                <button class="bsm bd"  @click="abrirConfirmEliminar(v)">Eliminar</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ============ MODAL: ASIGNAR PRECIOS POR TAMAÑO ============ -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <p class="modal-title">+ Asignar precios por tamaño</p>
          <button class="modal-close" @click="showForm = false">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
          <div class="ff" style="max-width:280px; margin-bottom:1.25rem;">
            <label>Producto</label>
            <select v-model="form.prodId" @change="onProdFormChange">
              <option value="">Seleccionar...</option>
              <option v-for="p in productos" :key="p.id" :value="p.id">
                {{ p.nombre }}
              </option>
            </select>
          </div>

          <div v-if="form.prodId && tamanos.length" style="
            background: var(--bg3);
            border: 1px solid var(--border);
            border-radius: 10px;
            overflow: hidden;
            margin-bottom: 1.25rem;
          ">
            <div style="
              padding: 10px 16px;
              border-bottom: 1px solid var(--border);
              font-size: 0.78rem;
              font-weight: 700;
              color: var(--text3);
              text-transform: uppercase;
              letter-spacing: 0.06em;
              display: grid;
              grid-template-columns: 1fr 160px 80px;
              gap: 12px;
            ">
              <span>Tamaño</span>
              <span>Precio</span>
              <span>Incluir</span>
            </div>

            <div
              v-for="t in tamanos"
              :key="t.id"
              style="
                padding: 10px 16px;
                border-bottom: 1px solid var(--border2);
                display: grid;
                grid-template-columns: 1fr 160px 80px;
                gap: 12px;
                align-items: center;
              "
              :style="{ opacity: tamanosExistentes.has(t.id) ? 0.45 : 1 }"
            >
              <span style="font-weight:600; color:var(--purple);">{{ t.nombre }}</span>
              <div style="position:relative;">
                <span style="
                  position:absolute; left:10px; top:50%; transform:translateY(-50%);
                  color:var(--text3); font-size:0.85rem; pointer-events:none;
                ">$</span>
                <input
                  v-model="form.precios[t.id]"
                  type="number"
                  placeholder="0"
                  min="0"
                  :disabled="!form.activos[t.id] || tamanosExistentes.has(t.id)"
                  style="
                    width:100%; padding:6px 8px 6px 22px;
                    border-radius:6px;
                    border:1px solid var(--border);
                    background:var(--bg2);
                    color:var(--cyan);
                    font-weight:600;
                    font-size:0.9rem;
                  "
                />
              </div>
              <div style="display:flex; align-items:center; gap:6px;">
                <input
                  type="checkbox"
                  :id="`chk-${t.id}`"
                  v-model="form.activos[t.id]"
                  :disabled="tamanosExistentes.has(t.id)"
                  style="width:16px; height:16px; cursor:pointer;"
                />
                <label
                  :for="`chk-${t.id}`"
                  style="font-size:0.75rem; color:var(--text3); cursor:pointer;"
                >
                  {{ tamanosExistentes.has(t.id) ? 'Ya existe' : '' }}
                </label>
              </div>
            </div>
          </div>

          <p v-if="modalCrear.error" class="modal-err">{{ modalCrear.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="showForm = false">Cancelar</button>
          <button class="bpri" :disabled="!form.prodId || guardando" @click="crearBloque">
            {{ guardando ? 'Guardando...' : '💾 Guardar variantes' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: EDITAR PRECIOS EN BLOQUE (todos los tamaños de un producto) ============ -->
    <div v-if="showEditarBloque" class="modal-overlay" @click.self="showEditarBloque = false">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <p class="modal-title">✎ Editar precios por producto</p>
          <button class="modal-close" @click="showEditarBloque = false">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
          <div class="ff" style="max-width:280px; margin-bottom:1.25rem;">
            <label>Producto</label>
            <select v-model="bloqueForm.prodId" @change="onProdBloqueChange">
              <option value="">Seleccionar...</option>
              <option v-for="p in productos" :key="p.id" :value="p.id">{{ p.nombre }}</option>
            </select>
          </div>

          <p v-if="bloqueForm.prodId && bloqueCargando" class="c-muted" style="font-size:0.85rem;">
            Cargando tamaños...
          </p>
          <p v-else-if="bloqueForm.prodId && !bloqueForm.variantes.length" class="c-muted" style="font-size:0.85rem;">
            Este producto no tiene tamaños/precios asignados todavía.
          </p>

          <div v-if="bloqueForm.variantes.length" style="
            background: var(--bg3);
            border: 1px solid var(--border);
            border-radius: 10px;
            overflow: hidden;
          ">
            <div style="
              padding: 10px 16px;
              border-bottom: 1px solid var(--border);
              font-size: 0.78rem;
              font-weight: 700;
              color: var(--text3);
              text-transform: uppercase;
              letter-spacing: 0.06em;
              display: grid;
              grid-template-columns: 1fr 160px;
              gap: 12px;
            ">
              <span>Tamaño</span>
              <span>Precio</span>
            </div>

            <div
              v-for="v in bloqueForm.variantes" :key="v.id"
              style="
                padding: 10px 16px;
                border-bottom: 1px solid var(--border2);
                display: grid;
                grid-template-columns: 1fr 160px;
                gap: 12px;
                align-items: center;
              "
            >
              <span style="font-weight:600; color:var(--purple);">
                {{ v.tamaño?.nombre ?? v.tamano?.nombre }}
              </span>
              <div style="position:relative;">
                <span style="
                  position:absolute; left:10px; top:50%; transform:translateY(-50%);
                  color:var(--text3); font-size:0.85rem; pointer-events:none;
                ">$</span>
                <input
                  v-model="v.precioNuevo"
                  type="number" min="0"
                  style="
                    width:100%; padding:6px 8px 6px 22px;
                    border-radius:6px;
                    border:1px solid var(--border);
                    background:var(--bg2);
                    color:var(--cyan);
                    font-weight:600;
                    font-size:0.9rem;
                  "
                />
              </div>
            </div>
          </div>

          <p v-if="modalBloque.error" class="modal-err">{{ modalBloque.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="showEditarBloque = false">Cancelar</button>
          <button class="bpri" :disabled="!bloqueForm.variantes.length || guardando" @click="guardarBloque">
            {{ guardando ? 'Guardando...' : '💾 Guardar todos los precios' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: EDITAR PRECIO ============ -->
    <div v-if="editando" class="modal-overlay" @click.self="editando = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title">Editar precio</p>
          <button class="modal-close" @click="editando = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">
            {{ editando.producto?.nombre }} — {{ editando.tamaño?.nombre ?? editando.tamano?.nombre }}
          </p>

          <div class="ff" style="margin-top:1rem;">
            <label>Nuevo precio</label>
            <div style="position:relative;">
              <span style="
                position:absolute; left:10px; top:50%; transform:translateY(-50%);
                color:var(--text3); font-size:0.85rem; pointer-events:none;
              ">$</span>
              <input
                ref="inputEditar"
                v-model="editPrecio"
                type="number"
                placeholder="15000"
                min="0"
                @keyup.enter="guardarEdicion"
                style="
                  width:100%; padding:8px 8px 8px 22px;
                  border-radius:6px;
                  border:1px solid var(--border);
                  background:var(--bg2);
                  color:var(--cyan);
                  font-weight:600;
                  font-size:0.95rem;
                "
              />
            </div>
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
          <p class="modal-title" style="color:var(--err);">⚠ Eliminar variante</p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">
            ¿Eliminar "{{ modalConfirm.variante.producto?.nombre }} — {{ modalConfirm.variante.tamaño?.nombre ?? modalConfirm.variante.tamano?.nombre }}"?
          </p>
          <p class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            Esta acción no se puede deshacer. Si hay inventario o ventas asociadas a esta variante, revisa antes que no dependan de ella.
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

const variantes         = ref([])
const productos         = ref([])
const tamanos           = ref([])
const showForm          = ref(false)
const editando          = ref(null)
const editPrecio        = ref('')
const filtroProducto    = ref('')
const cargando          = ref(false)
const tamanosExistentes = ref(new Set())
const inputEditar       = ref(null)

const form = ref({ prodId: '', precios: {}, activos: {} })

const modalCrear   = ref({ error: '' })
const modalEditar  = ref({ error: '' })
const modalConfirm = ref(null)
const guardando    = ref(false)
const toast        = ref(null)

const showEditarBloque = ref(false)
const bloqueCargando   = ref(false)
const bloqueForm       = ref({ prodId: '', variantes: [] })
const modalBloque      = ref({ error: '' })

function setToast(texto, ok = true) {
  toast.value = { texto, ok }
  setTimeout(() => { toast.value = null }, 3000)
}

onMounted(async () => {
  const [p, t] = await Promise.all([
    api('GET', '/productos/todos').catch(() => []),
    api('GET', '/tamanos/todos').catch(() => [])
  ])
  productos.value = p
  tamanos.value   = t
  cargar()
})

async function cargar() {
  cargando.value = true
  try {
    variantes.value = await api('GET', '/variantes')
  } catch { variantes.value = [] }
  finally { cargando.value = false }
}

async function filtrar() {
  if (!filtroProducto.value) { cargar(); return }
  cargando.value = true
  try {
    variantes.value = await api('GET', `/variantes/producto/${filtroProducto.value}`)
  } catch { variantes.value = [] }
  finally { cargando.value = false }
}

function abrirCrear() {
  showForm.value = true
  form.value = { prodId: '', precios: {}, activos: {} }
  tamanosExistentes.value = new Set()
  modalCrear.value = { error: '' }
}

function abrirEditarBloque() {
  showEditarBloque.value = true
  bloqueForm.value = { prodId: '', variantes: [] }
  modalBloque.value = { error: '' }
}

async function onProdBloqueChange() {
  bloqueForm.value.variantes = []
  modalBloque.value = { error: '' }
  if (!bloqueForm.value.prodId) return

  bloqueCargando.value = true
  try {
    const vars = await api('GET', `/variantes/producto/${bloqueForm.value.prodId}`)
    bloqueForm.value.variantes = vars.map(v => ({ ...v, precioNuevo: v.precio }))
  } catch {
    bloqueForm.value.variantes = []
  } finally {
    bloqueCargando.value = false
  }
}

async function guardarBloque() {
  // solo manda las que de verdad cambiaron de precio
  const cambiadas = bloqueForm.value.variantes.filter(v =>
    v.precioNuevo !== '' && v.precioNuevo !== null && Number(v.precioNuevo) !== Number(v.precio)
  )

  if (!cambiadas.length) {
    modalBloque.value.error = 'No hay cambios de precio para guardar'
    return
  }

  guardando.value = true
  try {
    const resultados = await Promise.allSettled(
      cambiadas.map(v => api('PUT', `/variantes/${v.id}`, {
        idProducto: v.producto?.id,
        idTamano:   v.tamaño?.id ?? v.tamano?.id,
        precio:     parseFloat(v.precioNuevo)
      }))
    )

    const ok  = resultados.filter(r => r.status === 'fulfilled').length
    const err = resultados.filter(r => r.status === 'rejected').length

    if (err === 0) {
      showEditarBloque.value = false
      setToast(`✓ ${ok} precio(s) actualizado(s)`)
    } else {
      modalBloque.value.error = `${ok} actualizados, ${err} fallaron`
    }
    cargar()
  } finally {
    guardando.value = false
  }
}

async function onProdFormChange() {
  const precios = {}
  const activos = {}
  tamanos.value.forEach(t => {
    precios[t.id] = ''
    activos[t.id] = true
  })
  form.value.precios = precios
  form.value.activos = activos
  tamanosExistentes.value = new Set()

  if (!form.value.prodId) return

  try {
    const vars = await api('GET', `/variantes/producto/${form.value.prodId}`)
    const existentes = new Set(vars.map(v => v.tamaño?.id ?? v.tamano?.id))
    tamanosExistentes.value = existentes
    existentes.forEach(id => { activos[id] = false })
    form.value.activos = { ...activos }
  } catch {}
}

async function crearBloque() {
  if (!form.value.prodId) { modalCrear.value.error = 'Selecciona un producto'; return }

  const seleccionados = tamanos.value.filter(t =>
    form.value.activos[t.id] && !tamanosExistentes.value.has(t.id)
  )

  if (!seleccionados.length) { modalCrear.value.error = 'Selecciona al menos un tamaño'; return }

  const sinPrecio = seleccionados.filter(t => !form.value.precios[t.id])
  if (sinPrecio.length) {
    modalCrear.value.error = `Falta precio en: ${sinPrecio.map(t => t.nombre).join(', ')}`
    return
  }

  guardando.value = true
  try {
    await Promise.all(
      seleccionados.map(t =>
        api('POST', '/variantes', {
          idProducto: parseInt(form.value.prodId),
          idTamano:   t.id,
          precio:     parseFloat(form.value.precios[t.id])
        })
      )
    )
    showForm.value = false
    setToast(`✓ ${seleccionados.length} variante(s) creada(s) correctamente`)
    cargar()
  } catch (e) {
    modalCrear.value.error = e.response?.data || e.message || 'Error al guardar'
  } finally {
    guardando.value = false
  }
}

async function abrirEditar(v) {
  editando.value    = v
  editPrecio.value  = v.precio
  modalEditar.value = { error: '' }
  await nextTick()
  inputEditar.value?.focus()
}

async function guardarEdicion() {
  if (!editPrecio.value) { modalEditar.value.error = 'Ingresa el precio'; return }

  guardando.value = true
  try {
    await api('PUT', `/variantes/${editando.value.id}`, {
      idProducto: editando.value.producto?.id,
      idTamano:   editando.value.tamaño?.id ?? editando.value.tamano?.id,
      precio:     parseFloat(editPrecio.value)
    })
    editando.value = null
    setToast('✓ Precio actualizado')
    cargar()
  } catch (e) {
    modalEditar.value.error = e.response?.data || e.message || 'Error al guardar'
  } finally {
    guardando.value = false
  }
}

function abrirConfirmEliminar(v) {
  modalConfirm.value = { variante: v, error: '' }
}

async function confirmarEliminacion() {
  guardando.value = true
  try {
    await api('DELETE', `/variantes/${modalConfirm.value.variante.id}`)
    variantes.value = variantes.value.filter(v => v.id !== modalConfirm.value.variante.id)
    modalConfirm.value = null
    setToast('✓ Variante eliminada')
  } catch (e) {
    modalConfirm.value.error = e.response?.data || e.message || 'Error al eliminar'
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
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
.modal-sm { max-width: 440px; }
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
