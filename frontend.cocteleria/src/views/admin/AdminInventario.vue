<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Inventario por sede</h2>
      <button class="bpri" @click="toggleForm">+ Agregar stock(s)</button>
    </div>

    <!-- FORM CREAR EN LOTE -->
    <div v-if="showForm" class="fbox">
      <p class="fep">Agregar stock al inventario — Puedes agregar varios a la vez</p>

      <div v-for="(item, idx) in lote" :key="idx" class="lote-inv-row">
        <span class="lote-num">#{{ idx + 1 }}</span>
        <div class="frow s3" style="flex:1;">
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
          <div class="ff">
            <label>Variante (tamaño)</label>
            <select v-model="item.varianteId" :disabled="!item.variantes?.length">
              <option value="">{{ item.prodId ? 'Seleccionar...' : 'Primero elige producto' }}</option>
              <option v-for="v in item.variantes" :key="v.id" :value="v.id">
                {{ v.tamaño?.nombre ?? v.tamano?.nombre }} — ${{ Number(v.precio).toLocaleString() }}
              </option>
            </select>
          </div>
          <div class="ff" style="max-width:140px;">
            <label>Cantidad</label>
            <input v-model="item.cantidad" type="number" placeholder="100" min="1"/>
          </div>
        </div>
        <button v-if="lote.length > 1" class="bsm bd lote-rm" @click="quitarFila(idx)" title="Quitar">✕</button>
      </div>

      <button class="bsm be2 lote-add" @click="agregarFila">+ Agregar otro</button>

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

// Cada fila del lote tiene sus propias variantes cargadas
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
  return { sedeId: miSedeId || '', prodId: '', varianteId: '', cantidad: 100, variantes: [] }
}

function agregarFila()   { lote.value.push(filaVacia()) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function onProdChange(idx) {
  const item = lote.value[idx]
  item.varianteId = ''
  item.variantes  = []
  if (!item.prodId) return
  item.variantes = await api('GET', `/variantes/producto/${item.prodId}`).catch(() => [])
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
  const validos = lote.value.filter(i =>
    i.sedeId && i.varianteId && i.cantidad && parseInt(i.cantidad) > 0
  )
  if (!validos.length) {
    setMsg('Cada fila necesita sede, variante y cantidad válida', false); return
  }

  const resultados = await Promise.allSettled(
    validos.map(i => api('POST', '/inventario/crear', {
      idVariante: parseInt(i.varianteId),
      idSede:     parseInt(i.sedeId),
      stock:      parseInt(i.cantidad)
    }))
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
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid color-mix(in srgb, var(--border, #333) 60%, transparent);
}
.lote-inv-row:last-of-type {
  border-bottom: none;
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
</style>