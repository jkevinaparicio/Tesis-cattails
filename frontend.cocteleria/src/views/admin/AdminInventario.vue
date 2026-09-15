<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Inventario por sede</h2>
      <button class="bpri" @click="toggleForm">+ Agregar stock(s)</button>
    </div>

    <!-- FORM CREAR EN LOTE -->
    <div v-if="showForm" class="fbox">
      <p class="fep">Agregar stock al inventario — elige sede y producto, y marca los tamaños que quieras cargar</p>

      <div v-for="(item, idx) in lote" :key="idx" class="lote-inv-row">
        <div class="lote-head">
          <span class="lote-num">#{{ idx + 1 }}</span>
          <div class="frow s2" style="flex:1;">
            <div class="ff">
              <label>Sede</label>
              <select v-model="item.sedeId" @change="onSedeItemChange(idx)">
                <option value="">Seleccionar sede...</option>
                <option v-for="s in sedes" :key="s.id" :value="s.id">{{ s.nombre }}</option>
              </select>
            </div>
            <div class="ff">
              <label>Producto</label>
              <select v-model="item.prodId" @change="onProdChange(idx)">
                <option value="">Seleccionar producto...</option>
                <option v-for="p in productos" :key="p.id" :value="p.id">
                  {{ p.nombre }}
                  <template v-if="p.categoria?.compartirStockPorTamano"> (🍹 Stock compartido por tamaño)</template>
                </option>
              </select>
            </div>
          </div>
          <button v-if="lote.length > 1" class="bsm bd lote-rm" @click="quitarFila(idx)" title="Quitar">✕</button>
        </div>

        <div v-if="item.esCompartido" class="tam-shared-hint">
          🍹 <strong>Categoría con stock compartido:</strong> El stock que asignes aquí se consolidará a nivel de tamaño para todos los productos de esta categoría en la sede seleccionada.
        </div>

        <div v-if="item.prodId" class="tam-box">
          <p v-if="item.cargando" class="tam-loading">Cargando tamaños...</p>
          <p v-else-if="!item.variantes.length" class="tam-empty">Este producto no tiene tamaños/variantes creados.</p>
          <div v-else class="tam-grid">
            <label
              v-for="v in item.variantes" :key="v.id"
              class="tam-chip"
              :class="{ on: v.selected }"
            >
              <input type="checkbox" v-model="v.selected" @change="onTamToggle(idx, v)" />
              <span class="tam-nombre">{{ v.tamaño?.nombre ?? v.tamano?.nombre }}</span>
              <span class="tam-precio">${{ Number(v.precio).toLocaleString() }}</span>

              <input
                v-if="!item.esCompartido"
                v-model="v.cantidad"
                type="number" min="1" placeholder="100"
                class="tam-cant"
                :disabled="!v.selected"
                @focus="v.selected = true"
              />

              <template v-else-if="v.selected">
                <span v-if="v.poolInfo === null" class="tam-pool-msg">Verificando...</span>
                <span v-else-if="v.poolInfo?.existe" class="tam-pool-msg ok">
                  🔗 Pool existente: {{ v.poolInfo.stock }} — solo se vincula
                </span>
                <input
                  v-else
                  v-model="v.cantidad"
                  type="number" min="1" placeholder="Stock inicial del pool"
                  class="tam-cant"
                  style="width:120px;"
                />
              </template>
            </label>
          </div>
        </div>
      </div>

      <button class="bsm be2 lote-add" @click="agregarFila">+ Agregar otra sede/producto</button>

      <div class="fact">
        <button class="bpri" @click="crearLote">Guardar todos</button>
        <button class="bsm bd" @click="showForm = false">Cancelar</button>
      </div>
      <p v-if="msg" class="fmsg" :class="msgOk ? 'ok' : 'err'">{{ msg }}</p>
    </div>

    <!-- FILTRO DE SEDE -->
    <div class="ff" style="max-width:280px; margin-bottom:1.5rem;">
      <label>Ver inventario de sede</label>
      <select v-model="sedeFilter" @change="filtrar">
        <option value="">Seleccionar sede...</option>
        <option v-for="s in sedes" :key="s.id" :value="s.id">{{ s.nombre }}</option>
      </select>
    </div>

    <div v-if="!sedeFilter" class="tbl-w">
      <table>
        <tbody>
          <tr><td class="empty">Selecciona una sede para ver el inventario.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-else>
      <!-- BLOQUE A: STOCK COMPARTIDO POR TAMAÑO -->
      <div class="sec-subhead">
        <h3 class="sec-subhead-title">🍹 Stock compartido por tamaño</h3>
      </div>

      <div class="tbl-w" style="margin-bottom:2rem;">
        <table>
          <thead>
            <tr><th>Tamaño</th><th>Sede</th><th>Stock</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            <tr v-if="!inventarioTamano.length">
              <td colspan="4" class="empty">Sin stock compartido por tamaño para esta sede.</td>
            </tr>
            <tr v-for="i in inventarioTamano" :key="i.id">
              <td style="font-weight:600; color:var(--purple);">🍹 {{ i.tamano?.nombre }}</td>
              <td><span class="tag-sede">{{ i.sede?.nombre }}</span></td>
              <td>
                <span :style="{ color: i.stock < 10 ? 'var(--err)' : 'var(--ok)', fontWeight: '600' }">{{ i.stock }}</span>
                <span v-if="i.stock < 10" style="font-size:0.72rem; color:var(--err); margin-left:4px;">⚠ bajo</span>
              </td>
              <td>
                <div class="btn-r">
                  <button class="bsm be2" @click="abrirStock(i, true, 'sumar')">+ Stock</button>
                  <button class="bsm bd" @click="abrirStock(i, true, 'restar')">- Stock</button>
                  <button class="bsm bd" @click="abrirConfirmTamano(i)">Quitar</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- BLOQUE B: STOCK PROPIO -->
      <div class="sec-subhead">
        <h3 class="sec-subhead-title">📦 Stock propio por producto</h3>
      </div>

      <div class="tbl-w" style="margin-bottom:2rem;">
        <table>
          <thead>
            <tr><th>Producto</th><th>Tamaño</th><th>Precio</th><th>Sede</th><th>Stock</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            <tr v-if="!variantesPropias.length">
              <td colspan="6" class="empty">Sin productos con stock propio para esta sede.</td>
            </tr>
            <tr v-for="i in variantesPropias" :key="i.id">
              <td style="font-weight:600; color:var(--text);">{{ i.variante?.producto?.nombre }}</td>
              <td style="color:var(--purple); font-weight:500;">{{ i.variante?.tamaño?.nombre ?? i.variante?.tamano?.nombre }}</td>
              <td class="c-cyan">${{ Number(i.variante?.precio).toLocaleString() }}</td>
              <td><span class="tag-sede">{{ i.sede?.nombre }}</span></td>
              <td>
                <span :style="{ color: i.stock < 10 ? 'var(--err)' : 'var(--ok)', fontWeight: '600' }">{{ i.stock }}</span>
                <span v-if="i.stock < 10" style="font-size:0.72rem; color:var(--err); margin-left:4px;">⚠ bajo</span>
              </td>
              <td>
                <div class="btn-r">
                  <button class="bsm be2" @click="abrirStock(i, false, 'sumar')">+ Stock</button>
                  <button class="bsm bd" @click="abrirStock(i, false, 'restar')">- Stock</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- BLOQUE C: PRODUCTOS -->
      <div class="sec-subhead">
        <h3 class="sec-subhead-title">🗂 Productos en la sede</h3>
      </div>

      <div class="ff" style="max-width:280px; margin-bottom:1rem;">
        <label>Buscar producto</label>
        <input v-model="productoFilter" placeholder="Ej: Mojito, Corona..." />
      </div>

      <div class="tbl-w">
        <table>
          <thead>
            <tr><th>Producto</th><th>Tipo</th><th>Tamaños en inventario</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            <tr v-if="!productosConTamanos.length">
              <td colspan="4" class="empty">Sin productos para esta sede.</td>
            </tr>
            <tr v-for="g in productosConTamanos" :key="g.productoId">
              <td style="font-weight:600; color:var(--text);">{{ g.nombre }}</td>
              <td>
                <span v-if="g.compartido" class="tag-sede"
                      style="background:color-mix(in srgb, var(--purple) 15%, transparent); color:var(--purple);">
                  🔗 Compartido
                </span>
                <span v-else class="c-muted" style="font-size:0.78rem;">📦 Propio</span>
              </td>
              <td>
                <div style="display:flex; flex-wrap:wrap; gap:6px;">
                  <button
                    v-for="t in g.tamanos" :key="t.varianteId"
                    class="tam-toggle"
                    :class="{ on: t.enInventario }"
                    :title="t.enInventario ? 'Click para quitar del inventario' : 'Click para agregar al inventario'"
                    @click="clickTamano(g, t)"
                  >
                    <span>{{ t.enInventario ? '✓' : '+' }}</span>
                    {{ t.nombre }}
                  </button>
                </div>
              </td>
              <td>
                <button class="bsm bd" :disabled="!g.tamanos.some(t => t.enInventario)"
                        @click="abrirConfirmProducto(g)">
                  Quitar producto
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ============ MODAL: SUMAR / RESTAR STOCK ============ -->
    <div v-if="modalStock" class="modal-overlay" @click.self="modalStock = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title">
            {{ modalStock.modo === 'restar' ? '➖ Restar stock' : '➕ Agregar stock' }}
          </p>
          <button class="modal-close" @click="modalStock = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">
            <template v-if="modalStock.esTamano">
              🍹 {{ modalStock.item.tamano?.nombre }} <span class="c-muted">(pool compartido)</span>
            </template>
            <template v-else>
              {{ modalStock.item.variante?.producto?.nombre }} —
              {{ modalStock.item.variante?.tamaño?.nombre ?? modalStock.item.variante?.tamano?.nombre }}
            </template>
          </p>

          <div class="modal-meta">
            <span class="tag-sede">{{ modalStock.item.sede?.nombre }}</span>
            <span>Stock actual: <strong class="c-cyan">{{ modalStock.item.stock }}</strong></span>
          </div>

          <div class="ff" style="margin-top:1rem;">
            <label>Cantidad a {{ modalStock.modo === 'restar' ? 'restar' : 'agregar' }}</label>
            <input ref="inputStock" v-model="cantStock" type="number" min="1" placeholder="50"
                   @keyup.enter="confirmarStock" />
          </div>

          <p v-if="modalStock.error" class="modal-err">{{ modalStock.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="modalStock = null">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="confirmarStock">
            {{ guardando ? 'Guardando...' : 'Confirmar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: STOCK INICIAL AL AGREGAR TAMAÑO ============ -->
    <div v-if="modalAgregar" class="modal-overlay" @click.self="modalAgregar = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title">➕ Agregar tamaño al inventario</p>
          <button class="modal-close" @click="modalAgregar = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">
            {{ modalAgregar.grupo.nombre }} — {{ modalAgregar.tamano.nombre }}
          </p>

          <div class="ff" style="margin-top:1rem;">
            <label>Stock inicial</label>
            <input v-model="cantInicial" type="number" min="0" placeholder="100"
                   @keyup.enter="confirmarAgregar" />
          </div>

          <p v-if="modalAgregar.error" class="modal-err">{{ modalAgregar.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="modalAgregar = null">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="confirmarAgregar">
            {{ guardando ? 'Guardando...' : 'Agregar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: CONFIRMAR ELIMINACIÓN ============ -->
    <div v-if="modalConfirm" class="modal-overlay" @click.self="modalConfirm = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title" style="color:var(--err);">⚠ {{ modalConfirm.titulo }}</p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">{{ modalConfirm.mensaje }}</p>
          <p v-if="modalConfirm.detalle" class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            {{ modalConfirm.detalle }}
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
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api } = useAdmin()

const inventario       = ref([])
const inventarioTamano = ref([])
const sedes            = ref([])
const productos        = ref([])
const variantesTodas   = ref([])
const showForm         = ref(false)
const miSedeId         = parseInt(localStorage.getItem('sede_id') || '0')

const sedeFilter     = ref('')
const productoFilter = ref('')

const lote = ref([filaVacia()])

const msg      = ref(''); const msgOk = ref(true)

// estado de los modales
const modalStock   = ref(null)   // { item, esTamano, modo, error }
const modalAgregar = ref(null)   // { grupo, tamano, error }
const modalConfirm = ref(null)   // { titulo, mensaje, detalle, accion, error }
const cantStock    = ref('')
const cantInicial  = ref('100')
const guardando    = ref(false)
const inputStock   = ref(null)
const toast        = ref(null)

function setMsg(t, ok = true) { msg.value = t; msgOk.value = ok; setTimeout(() => msg.value = '', 4000) }
function setToast(texto, ok = true) {
  toast.value = { texto, ok }
  setTimeout(() => { toast.value = null }, 3000)
}

let remover = null

const variantesPropias = computed(() =>
  inventario.value.filter(i => !i.variante?.producto?.categoria?.compartirStockPorTamano)
)

const productosConTamanos = computed(() => {
  const enInv = new Set(inventario.value.map(i => i.variante?.id))

  let lista = variantesTodas.value
  if (productoFilter.value.trim()) {
    const q = productoFilter.value.toLowerCase()
    lista = lista.filter(v => v.producto?.nombre?.toLowerCase().includes(q))
  }

  const mapa = new Map()
  lista.forEach(v => {
    const prodId = v.producto?.id
    if (!prodId) return

    if (!mapa.has(prodId)) {
      mapa.set(prodId, {
        productoId: prodId,
        nombre: v.producto?.nombre,
        compartido: !!v.producto?.categoria?.compartirStockPorTamano,
        tamanos: []
      })
    }

    mapa.get(prodId).tamanos.push({
      varianteId:   v.id,
      tamanoId:     v.tamaño?.id ?? v.tamano?.id,
      nombre:       v.tamaño?.nombre ?? v.tamano?.nombre,
      enInventario: enInv.has(v.id)
    })
  })

  return Array.from(mapa.values())
})

onMounted(async () => {
  const [s, p, vars] = await Promise.all([
    api('GET', '/sedes/listar').catch(() => []),
    api('GET', '/productos/todos').catch(() => []),
    api('GET', '/variantes').catch(() => [])
  ])
  sedes.value          = s
  productos.value      = p
  variantesTodas.value = vars

  remover = null

  if (miSedeId) {
    sedeFilter.value = miSedeId
    filtrar()
  }
})

onUnmounted(() => remover?.())

function filaVacia() {
  return { sedeId: miSedeId || '', prodId: '', variantes: [], cargando: false, esCompartido: false }
}

function agregarFila()   { lote.value.push(filaVacia()) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function onProdChange(idx) {
  const item = lote.value[idx]
  item.variantes = []
  item.esCompartido = false
  if (!item.prodId) return

  const prodObj = productos.value.find(p => p.id === parseInt(item.prodId))
  item.esCompartido = !!prodObj?.categoria?.compartirStockPorTamano

  item.cargando = true
  const data = await api('GET', `/variantes/producto/${item.prodId}`).catch(() => [])
  item.variantes = data.map(v => ({ ...v, selected: false, cantidad: 100, poolInfo: null }))
  item.cargando = false
}

async function checkPool(idx, v) {
  const item = lote.value[idx]
  const tamId = v.tamaño?.id ?? v.tamano?.id
  if (!tamId || !item.sedeId) { v.poolInfo = null; return }

  try {
    const res = await api('GET', `/inventario/tamano/stock?tamanoId=${tamId}&sedeId=${item.sedeId}`)
    v.poolInfo = { existe: true, stock: res.stock }
  } catch {
    v.poolInfo = { existe: false }
  }
}

function onTamToggle(idx, v) {
  if (v.selected && lote.value[idx].esCompartido) checkPool(idx, v)
  else v.poolInfo = null
}

function onSedeItemChange(idx) {
  const item = lote.value[idx]
  item.variantes.forEach(v => {
    if (v.selected && item.esCompartido) checkPool(idx, v)
  })
}

async function toggleForm() {
  showForm.value = !showForm.value
  lote.value     = [filaVacia()]
}

async function filtrar() {
  if (!sedeFilter.value) {
    inventario.value = []
    inventarioTamano.value = []
    return
  }
  const [inv, invTam] = await Promise.all([
    api('GET', `/inventario/sede/${sedeFilter.value}`).catch(() => []),
    api('GET', `/inventario/tamano/sede/${sedeFilter.value}`).catch(() => [])
  ])
  inventario.value = inv
  inventarioTamano.value = invTam
}

async function crearLote() {
  const registrosVariante = []

  for (const item of lote.value) {
    if (!item.sedeId) continue
    for (const v of item.variantes) {
      if (!v.selected) continue
      const poolYaExiste = item.esCompartido && v.poolInfo?.existe

      if (poolYaExiste || (v.cantidad && parseInt(v.cantidad) > 0)) {
        registrosVariante.push({
          idVariante: parseInt(v.id),
          idSede:     parseInt(item.sedeId),
          stock:      poolYaExiste ? 0 : parseInt(v.cantidad)
        })
      }
    }
  }

  if (!registrosVariante.length) {
    setMsg('Marca al menos un tamaño con cantidad válida', false); return
  }

  const resultados = await Promise.allSettled(
    registrosVariante.map(r => api('POST', '/inventario/crear', r))
  )

  const ok  = resultados.filter(r => r.status === 'fulfilled').length
  const err = resultados.filter(r => r.status === 'rejected').length

  setMsg(err === 0 ? `✓ ${ok} registro(s) procesado(s)` : `${ok} procesados, ${err} con avisos`, err === 0)
  showForm.value = false
  lote.value = [filaVacia()]
  if (sedeFilter.value) filtrar()
}

// ---------- MODAL DE STOCK ----------
async function abrirStock(item, esTamano, modo) {
  modalStock.value = { item, esTamano, modo, error: '' }
  cantStock.value = ''
  await nextTick()
  inputStock.value?.focus()
}

async function confirmarStock() {
  const cantidad = parseInt(cantStock.value)
  if (!cantidad || cantidad <= 0) {
    modalStock.value.error = 'Ingresa una cantidad válida'
    return
  }

  const { item, esTamano, modo } = modalStock.value
  const accion = modo === 'restar' ? 'disminuir' : 'aumentar'

  guardando.value = true
  try {
    if (esTamano) {
      await api('POST', `/inventario/tamano/${accion}`, {
        tamanoId: item.tamano?.id,
        sedeId:   item.sede?.id,
        cantidad
      })
    } else {
      await api('POST', `/inventario/${accion}`, {
        varianteId: item.variante?.id,
        sedeId:     item.sede?.id,
        cantidad
      })
    }
    modalStock.value = null
    setToast(modo === 'restar' ? 'Stock restado correctamente' : 'Stock agregado correctamente')
    filtrar()
  } catch (e) {
    modalStock.value.error = e.response?.data || e.message || 'Error al actualizar el stock'
  } finally {
    guardando.value = false
  }
}

// ---------- CLICK EN CHIP DE TAMAÑO ----------
function clickTamano(grupo, tamano) {
  if (tamano.enInventario) {
    modalConfirm.value = {
      titulo: 'Quitar tamaño del inventario',
      mensaje: `¿Quitar "${tamano.nombre}" de ${grupo.nombre} del inventario de esta sede?`,
      detalle: grupo.compartido
        ? 'Este producto usa stock compartido — el pool del tamaño no se elimina, solo se desvincula este producto.'
        : null,
      error: '',
      accion: async () => {
        await api('DELETE', `/inventario/sede/${sedeFilter.value}/variante/${tamano.varianteId}`)
      }
    }
  } else if (grupo.compartido) {
    // compartido: se vincula directo, el pool decide el stock
    modalAgregar.value = { grupo, tamano, error: '', autoCompartido: true }
    cantInicial.value = '0'
    confirmarAgregar()
  } else {
    modalAgregar.value = { grupo, tamano, error: '' }
    cantInicial.value = '100'
  }
}

async function confirmarAgregar() {
  const { grupo, tamano } = modalAgregar.value
  const stock = grupo.compartido ? 0 : parseInt(cantInicial.value)

  if (!grupo.compartido && (isNaN(stock) || stock < 0)) {
    modalAgregar.value.error = 'Ingresa un stock válido'
    return
  }

  guardando.value = true
  try {
    await api('POST', '/inventario/crear', {
      idVariante: tamano.varianteId,
      idSede:     parseInt(sedeFilter.value),
      stock
    })
    modalAgregar.value = null
    setToast(`"${tamano.nombre}" agregado al inventario`)
    filtrar()
  } catch (e) {
    modalAgregar.value.error = e.response?.data || e.message || 'Error al agregar'
  } finally {
    guardando.value = false
  }
}

// ---------- MODALES DE CONFIRMACIÓN ----------
function abrirConfirmTamano(item) {
  modalConfirm.value = {
    titulo: 'Quitar stock compartido',
    mensaje: `¿Quitar el inventario compartido del tamaño "${item.tamano?.nombre}" en ${item.sede?.nombre}?`,
    detalle: 'Todos los productos que usan este tamaño se quedarán sin stock disponible.',
    error: '',
    accion: async () => {
      await api('DELETE', `/inventario/tamano/sede/${item.sede?.id}/tamano/${item.tamano?.id}`)
    }
  }
}

function abrirConfirmProducto(grupo) {
  const enInv = grupo.tamanos.filter(t => t.enInventario)
  modalConfirm.value = {
    titulo: 'Quitar producto del inventario',
    mensaje: `¿Quitar "${grupo.nombre}" completo del inventario de esta sede?`,
    detalle: `Se quitarán ${enInv.length} tamaño(s): ${enInv.map(t => t.nombre).join(', ')}`,
    error: '',
    accion: async () => {
      const resultados = await Promise.allSettled(
        enInv.map(t => api('DELETE', `/inventario/sede/${sedeFilter.value}/variante/${t.varianteId}`))
      )
      const fallidos = resultados.filter(r => r.status === 'rejected')
      if (fallidos.length) {
        throw new Error(`${fallidos.length} de ${enInv.length} tamaño(s) no se pudieron quitar`)
      }
    }
  }
}

async function confirmarEliminacion() {
  guardando.value = true
  try {
    await modalConfirm.value.accion()
    modalConfirm.value = null
    setToast('Eliminado correctamente')
    filtrar()
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
.lote-inv-row {
  margin-bottom: 1rem; padding-bottom: 1rem;
  border-bottom: 1px solid color-mix(in srgb, var(--border, #333) 60%, transparent);
}
.lote-inv-row:last-of-type { border-bottom: none; }
.lote-head { display: flex; align-items: flex-end; gap: 0.5rem; }
.lote-num {
  font-size: 0.75rem; font-weight: 700; color: var(--purple);
  min-width: 22px; padding-bottom: 0.6rem;
}
.lote-rm { flex-shrink: 0; margin-bottom: 0; }
.lote-add { margin-bottom: 1rem; }

.tam-shared-hint {
  margin: 0.5rem 0 0.5rem 30px; padding: 8px 12px; border-radius: 8px;
  background: color-mix(in srgb, var(--purple) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--purple) 25%, transparent);
  font-size: 0.8rem; color: var(--text2);
}

.tam-box { margin-top: 0.75rem; padding-left: 30px; }
.tam-loading, .tam-empty { font-size: 0.8rem; color: var(--text3); }
.tam-grid { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.tam-chip {
  display: flex; align-items: center; gap: 0.5rem; padding: 0.4rem 0.6rem;
  border: 1px solid color-mix(in srgb, var(--border, #333) 70%, transparent);
  border-radius: 8px; cursor: pointer; transition: border-color .15s, background .15s;
}
.tam-chip.on {
  border-color: var(--purple);
  background: color-mix(in srgb, var(--purple) 8%, transparent);
}
.tam-chip input[type="checkbox"] { width: 14px; height: 14px; flex-shrink: 0; }
.tam-nombre { font-weight: 600; color: var(--text); font-size: 0.85rem; }
.tam-precio { font-size: 0.78rem; color: var(--cyan); }
.tam-cant { width: 64px; font-size: 0.8rem; padding: 2px 4px; }
.tam-pool-msg { font-size: 0.75rem; color: var(--text3); font-style: italic; }
.tam-pool-msg.ok { color: var(--purple); font-style: normal; font-weight: 600; }

.tam-toggle {
  display: inline-flex; align-items: center; gap: 5px; padding: 4px 10px;
  border-radius: 6px;
  border: 1px dashed color-mix(in srgb, var(--border, #333) 80%, transparent);
  background: transparent; color: var(--text3);
  font-size: 0.78rem; font-weight: 500; cursor: pointer; transition: all .15s;
}
.tam-toggle:hover { border-color: var(--purple); color: var(--purple); }
.tam-toggle.on {
  border-style: solid; border-color: var(--purple);
  background: color-mix(in srgb, var(--purple) 12%, transparent);
  color: var(--purple); font-weight: 600;
}

.sec-subhead { display: flex; align-items: baseline; gap: 10px; margin-bottom: 0.75rem; }
.sec-subhead-title { font-size: 1rem; font-weight: 600; color: var(--text); margin: 0; }

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
}
.modal-sm { max-width: 420px; }
@keyframes modal-in {
  from { opacity:0; transform: translateY(14px) scale(0.97); }
  to   { opacity:1; transform: translateY(0) scale(1); }
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border2);
  gap: 12px;
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
.modal-target { font-size: 0.92rem; font-weight: 600; color: var(--purple); margin: 0; }
.modal-meta {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
  margin-top: 8px; font-size: 0.82rem; color: var(--text3);
}
.modal-err {
  margin-top: 10px; font-size: 0.82rem; color: var(--err); font-weight: 500;
}
.modal-footer-pad {
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--border2);
  background: var(--bg3);
  display: flex; align-items: center; justify-content: flex-end; gap: 10px;
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