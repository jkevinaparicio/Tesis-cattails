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
              <select v-model="item.sedeId">
                <option value="">Seleccionar sede...</option>
                <option v-for="s in sedes" :key="s.id" :value="s.id">{{ s.nombre }}</option>
              </select>
            </div>
            <div class="ff">
              <label>Producto</label>
              <select v-model="item.prodId" @change="onProdChange(idx)">
                <option value="">Seleccionar producto...</option>
                <option v-for="p in productos" :key="p.id" :value="p.id">{{ p.nombre }}</option>
              </select>
            </div>
          </div>
          <button v-if="lote.length > 1" class="bsm bd lote-rm" @click="quitarFila(idx)" title="Quitar">✕</button>
        </div>

        <!-- Tamaños del producto elegido: se pueden marcar varios a la vez -->
        <div v-if="item.prodId" class="tam-box">
          <p v-if="item.cargando" class="tam-loading">Cargando tamaños...</p>
          <p v-else-if="!item.variantes.length" class="tam-empty">Este producto no tiene tamaños/variantes creados.</p>
          <div v-else class="tam-grid">
            <label
              v-for="v in item.variantes" :key="v.id"
              class="tam-chip"
              :class="{ on: v.selected }"
            >
              <input type="checkbox" v-model="v.selected" />
              <span class="tam-nombre">{{ v.tamaño?.nombre ?? v.tamano?.nombre }}</span>
              <span class="tam-precio">${{ Number(v.precio).toLocaleString() }}</span>
              <input
                v-model="v.cantidad"
                type="number" min="1" placeholder="100"
                class="tam-cant"
                :disabled="!v.selected"
                @focus="v.selected = true"
              />
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

    <!-- FORM SUMAR STOCK -->
    <div v-if="sumando" class="fbox">
      <p class="fep">
        Sumando stock:
        <strong style="color:var(--purple);">
          {{ sumando.variante?.producto?.nombre }} — {{ sumando.variante?.tamaño?.nombre ?? sumando.variante?.tamano?.nombre }}
        </strong>
      </p>
      <div style="font-size:0.85rem; color:var(--text3); margin-bottom:1rem;">
        Sede: <span class="tag-sede" style="margin-left:4px;">{{ sumando.sede?.nombre }}</span>
        &nbsp;·&nbsp;
        Stock actual: <strong style="color:var(--cyan);">{{ sumando.stock }}</strong>
      </div>
      <div class="frow s1" style="max-width:200px;">
        <div class="ff">
          <label>Cantidad a agregar</label>
          <input v-model="cantSumar" type="number" placeholder="50" min="1"/>
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="confirmarSuma">Confirmar</button>
        <button class="bsm bd" @click="sumando = null">Cancelar</button>
      </div>
      <p v-if="msgSuma" class="fmsg" :class="msgSumaOk ? 'ok' : 'err'">{{ msgSuma }}</p>
    </div>

    <div class="ff" style="max-width:280px; margin-bottom:1.25rem;">
      <label>Ver inventario de sede</label>
      <select v-model="sedeFilter" @change="filtrar">
        <option value="">Seleccionar sede...</option>
        <option v-for="s in sedes" :key="s.id" :value="s.id">{{ s.nombre }}</option>
      </select>
    </div>

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Producto</th><th>Tamaño</th><th>Precio</th><th>Sede</th><th>Stock</th><th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!sedeFilter">
            <td colspan="6" class="empty">Selecciona una sede para ver el inventario.</td>
          </tr>
          <tr v-else-if="!inventario.length">
            <td colspan="6" class="empty">Sin registros para esta sede.</td>
          </tr>
          <tr
            v-for="i in inventario" :key="i.id"
            :style="{ background: sumando?.id === i.id ? 'color-mix(in srgb, var(--purple) 8%, transparent)' : '' }"
          >
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
                <button class="bsm be2" @click="iniciarSuma(i)">+ Stock</button>
                <button class="bsm bd" @click="quitarDeSede(i)">Quitar</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'
import { useWebSocket } from '@/composables/useWebSocket'

const { api } = useAdmin()
const { conectarInventario } = useWebSocket()

const inventario = ref([])
const sedes      = ref([])
const productos  = ref([])
const showForm   = ref(false)
const miSedeId = parseInt(localStorage.getItem('sede_id') || '0')

const sedeFilter = ref('')
const sumando    = ref(null)
const cantSumar  = ref('')

// Cada fila del lote = una sede + un producto, con checkboxes de tamaños dentro
const lote = ref([filaVacia()])

const msg      = ref(''); const msgOk     = ref(true)
const msgSuma  = ref(''); const msgSumaOk = ref(true)

function setMsg(t, ok = true)     { msg.value     = t; msgOk.value     = ok; setTimeout(() => msg.value     = '', 4000) }
function setMsgSuma(t, ok = true) { msgSuma.value = t; msgSumaOk.value = ok; setTimeout(() => msgSuma.value = '', 3000) }

let remover = null

onMounted(async () => {
  const [s, p] = await Promise.all([
    api('GET', '/sedes/listar').catch(() => []),
    api('GET', '/productos/todos').catch(() => [])
  ])
  sedes.value     = s
  productos.value = p

  if (miSedeId) {
    sedeFilter.value = miSedeId
    filtrar()
  }

  remover = conectarInventario((sedeId) => {
    if (String(sedeFilter.value) === String(sedeId)) filtrar()
  })
})

onUnmounted(() => remover?.())

function filaVacia() {
  return { sedeId: miSedeId || '', prodId: '', variantes: [], cargando: false }
}

function agregarFila()   { lote.value.push(filaVacia()) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function onProdChange(idx) {
  const item = lote.value[idx]
  item.variantes = []
  if (!item.prodId) return
  item.cargando = true
  const data = await api('GET', `/variantes/producto/${item.prodId}`).catch(() => [])
  // cada variante trae su propio flag de selección y cantidad, así puedes
  // marcar varios tamaños del mismo producto en una sola pasada
  item.variantes = data.map(v => ({ ...v, selected: false, cantidad: 100 }))
  item.cargando = false
}

async function toggleForm() {
  showForm.value = !showForm.value
  sumando.value  = null
  lote.value     = [filaVacia()]
}

async function filtrar() {
  if (!sedeFilter.value) { inventario.value = []; return }
  inventario.value = await api('GET', `/inventario/sede/${sedeFilter.value}`).catch(() => [])
}

async function crearLote() {
  // aplana: por cada fila (sede+producto), por cada tamaño marcado con cantidad válida
  const registros = []
  for (const item of lote.value) {
    if (!item.sedeId) continue
    for (const v of item.variantes) {
      if (v.selected && v.cantidad && parseInt(v.cantidad) > 0) {
        registros.push({
          idVariante: parseInt(v.id),
          idSede:     parseInt(item.sedeId),
          stock:      parseInt(v.cantidad)
        })
      }
    }
  }

  if (!registros.length) {
    setMsg('Marca al menos un tamaño con cantidad válida', false); return
  }

  const resultados = await Promise.allSettled(
    registros.map(r => api('POST', '/inventario/crear', r))
  )

  const ok  = resultados.filter(r => r.status === 'fulfilled').length
  const err = resultados.filter(r => r.status === 'rejected').length

  setMsg(err === 0 ? `✓ ${ok} registro(s) creado(s)` : `${ok} creados, ${err} fallaron`, err === 0)
  showForm.value = false
  lote.value = [filaVacia()]
  if (sedeFilter.value) filtrar()
}

function iniciarSuma(item) {
  sumando.value   = item
  cantSumar.value = ''
  showForm.value  = false
}

async function confirmarSuma() {
  if (!cantSumar.value || parseInt(cantSumar.value) <= 0) {
    setMsgSuma('Ingresa una cantidad válida', false); return
  }
  try {
    await api('POST', '/inventario/aumentar', {
      varianteId: sumando.value.variante?.id,
      sedeId:     sumando.value.sede?.id,
      cantidad:   parseInt(cantSumar.value)
    })
    setMsgSuma('Stock actualizado')
    setTimeout(() => { sumando.value = null }, 1200)
    filtrar()
  } catch (e) { setMsgSuma(e.response?.data || e.message, false) }
}

async function quitarDeSede(item) {
  if (!confirm(
    `¿Quitar "${item.variante?.producto?.nombre} — ` +
    `${item.variante?.tamaño?.nombre ?? item.variante?.tamano?.nombre}"` +
    ` de ${item.sede?.nombre}?`
  )) return
  try {
    await api('DELETE', `/inventario/sede/${item.sede?.id}/variante/${item.variante?.id}`)
    filtrar()
  } catch (e) { setMsg(e.response?.data || e.message, false) }
}
</script>

<style scoped src="@/assets/admin.css">
</style>

<style scoped>
.lote-inv-row {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid color-mix(in srgb, var(--border, #333) 60%, transparent);
}
.lote-inv-row:last-of-type {
  border-bottom: none;
}
.lote-head {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
}
.lote-num {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--purple);
  min-width: 22px;
  padding-bottom: 0.6rem;
}
.lote-rm {
  flex-shrink: 0;
  margin-bottom: 0;
}
.lote-add {
  margin-bottom: 1rem;
}

.tam-box {
  margin-top: 0.75rem;
  padding-left: 30px;
}
.tam-loading, .tam-empty {
  font-size: 0.8rem;
  color: var(--text3);
}
.tam-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.tam-chip {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.6rem;
  border: 1px solid color-mix(in srgb, var(--border, #333) 70%, transparent);
  border-radius: 8px;
  cursor: pointer;
  transition: border-color .15s, background .15s;
}
.tam-chip.on {
  border-color: var(--purple);
  background: color-mix(in srgb, var(--purple) 8%, transparent);
}
.tam-chip input[type="checkbox"] {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}
.tam-nombre {
  font-weight: 600;
  color: var(--text);
  font-size: 0.85rem;
}
.tam-precio {
  font-size: 0.78rem;
  color: var(--cyan);
}
.tam-cant {
  width: 64px;
  font-size: 0.8rem;
  padding: 2px 4px;
}
</style>
