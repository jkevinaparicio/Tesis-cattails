<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Variantes y Precios</h2>
      <button class="bpri" @click="toggleForm">
        {{ showForm ? 'Cancelar' : '+ Asignar precios' }}
      </button>
    </div>

    <!-- FORM CREAR EN BLOQUE -->
    <div v-if="showForm" class="fbox">
      <p class="fep">Asignar precios por tamaño</p>

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

      <div v-if="form.prodId" class="fact">
        <button class="bpri" @click="crearBloque">💾 Guardar variantes</button>
        <button class="bsm bd" @click="toggleForm">Cancelar</button>
      </div>
      <p v-if="msg" class="fmsg" :class="msgOk ? 'ok' : 'err'">{{ msg }}</p>
    </div>

    <!-- FORM EDITAR -->
    <div v-if="editando" class="fbox">
      <p class="fep">
        Editando:
        <strong style="color:var(--purple);">
          {{ editando.producto?.nombre }} — {{ editando.tamaño?.nombre ?? editando.tamano?.nombre }}
        </strong>
      </p>
      <div class="frow s1" style="max-width:260px;">
        <div class="ff">
          <label>Nuevo precio</label>
          <div style="position:relative;">
            <span style="
              position:absolute; left:10px; top:50%; transform:translateY(-50%);
              color:var(--text3); font-size:0.85rem; pointer-events:none;
            ">$</span>
            <input
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
      </div>
      <div class="fact">
        <button class="bpri" @click="guardarEdicion">Guardar</button>
        <button class="bsm bd" @click="editando = null">Cancelar</button>
      </div>
      <p v-if="msgEdit" class="fmsg" :class="msgEditOk ? 'ok' : 'err'">{{ msgEdit }}</p>
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
          <tr
            v-for="v in variantes"
            :key="v.id"
            :style="{
              background: editando?.id === v.id
                ? 'color-mix(in srgb, var(--purple) 8%, transparent)'
                : ''
            }"
          >
            <td style="font-weight:600; color:var(--text);">{{ v.producto?.nombre }}</td>
            <td class="c-muted">{{ v.producto?.categoria?.nombre }}</td>
            <td style="color:var(--purple); font-weight:500;">
              {{ v.tamaño?.nombre ?? v.tamano?.nombre }}
            </td>
            <td class="c-cyan">${{ Number(v.precio).toLocaleString() }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="iniciarEdicion(v)">Editar precio</button>
                <button class="bsm bd"  @click="eliminar(v.id)">Eliminar</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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

const form = ref({ prodId: '', precios: {}, activos: {} })

const msg     = ref(''); const msgOk     = ref(true)
const msgEdit = ref(''); const msgEditOk = ref(true)

function setMsg(t, ok = true)     { msg.value     = t; msgOk.value     = ok; setTimeout(() => msg.value     = '', 3000) }
function setMsgEdit(t, ok = true) { msgEdit.value = t; msgEditOk.value = ok; setTimeout(() => msgEdit.value = '', 3000) }

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

async function toggleForm() {
  showForm.value = !showForm.value
  editando.value = null
  form.value = { prodId: '', precios: {}, activos: {} }
  tamanosExistentes.value = new Set()
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
  if (!form.value.prodId) { setMsg('Selecciona un producto', false); return }

  const seleccionados = tamanos.value.filter(t =>
    form.value.activos[t.id] && !tamanosExistentes.value.has(t.id)
  )

  if (!seleccionados.length) { setMsg('Selecciona al menos un tamaño', false); return }

  const sinPrecio = seleccionados.filter(t => !form.value.precios[t.id])
  if (sinPrecio.length) {
    setMsg(`Falta precio en: ${sinPrecio.map(t => t.nombre).join(', ')}`, false); return
  }

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
    setMsg(`✓ ${seleccionados.length} variante(s) creada(s) correctamente`)
    showForm.value = false
    form.value = { prodId: '', precios: {}, activos: {} }
    tamanosExistentes.value = new Set()
    cargar()
  } catch (e) {
    setMsg(e.response?.data || e.message, false)
  }
}

function iniciarEdicion(v) {
  editando.value   = v
  editPrecio.value = v.precio
  showForm.value   = false
}

async function guardarEdicion() {
  if (!editPrecio.value) { setMsgEdit('Ingresa el precio', false); return }
  try {
    await api('PUT', `/variantes/${editando.value.id}`, {
      idProducto: editando.value.producto?.id,
      idTamano:   editando.value.tamaño?.id ?? editando.value.tamano?.id,
      precio:     parseFloat(editPrecio.value)
    })
    setMsgEdit('Precio actualizado')
    setTimeout(() => { editando.value = null }, 1200)
    cargar()
  } catch (e) { setMsgEdit(e.response?.data || e.message, false) }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta variante?')) return
  try {
    await api('DELETE', `/variantes/${id}`)
    variantes.value = variantes.value.filter(v => v.id !== id)
  } catch (e) { setMsg(e.response?.data || e.message, false) }
}
</script>

<style scoped></style>