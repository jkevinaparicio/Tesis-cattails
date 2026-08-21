<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Productos</h2>
      <button class="bpri" @click="toggleForm">+ Nuevo(s) producto(s)</button>
    </div>

    <div v-if="showForm" class="fbox">
      <p class="fep">POST /productos/crear — Puedes agregar varios a la vez</p>

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

      <div class="fact">
        <button class="bpri" @click="crearLote">Guardar todos</button>
        <button class="bsm bd" @click="showForm = false">Cancelar</button>
      </div>
      <p v-if="msg" class="fmsg" :class="msgOk ? 'ok' : 'err'">{{ msg }}</p>
    </div>

    <!-- FORM EDITAR -->
    <div v-if="editando" class="fbox">
      <p class="fep">Editando producto: <strong style="color:var(--purple);">{{ editando.nombre }}</strong></p>
      <div class="frow">
        <div class="ff">
          <label>Nombre</label>
          <input v-model="editForm.nombre"/>
        </div>
        <div class="ff">
          <label>Categoría</label>
          <select v-model="editForm.catId">
            <option value="">Seleccionar...</option>
            <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
          </select>
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="guardarEdicion">Guardar</button>
        <button class="bsm bd" @click="editando = null">Cancelar</button>
      </div>
      <p v-if="msgEdit" class="fmsg" :class="msgEditOk ? 'ok' : 'err'">{{ msgEdit }}</p>
    </div>

    <!-- FILTRO POR CATEGORÍA -->
    <div class="ff" style="max-width:280px; margin-bottom:1.25rem;">
      <label>Filtrar por categoría</label>
      <select v-model="catFilter">
        <option value="">Todas las categorías</option>
        <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
      </select>
    </div>

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Categoría</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!productosFiltrados.length">
            <td colspan="3" class="empty">Sin productos registrados.</td>
          </tr>
          <tr v-for="p in productosFiltrados" :key="p.id">
            <td>{{ p.nombre }}</td>
            <td class="c-purple">{{ p.categoria?.nombre }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="iniciarEdicion(p)">Editar</button>
                <button class="bsm bd"  @click="eliminar(p.id)">Eliminar</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api } = useAdmin()

const productos   = ref([])
const categorias  = ref([])
const showForm    = ref(false)
const editando    = ref(null)
const catFilter   = ref('')

const lote     = ref([{ nombre: '', catId: '' }])
const editForm = ref({ nombre: '', catId: '' })

const msg         = ref(''); const msgOk     = ref(true)
const msgEdit     = ref(''); const msgEditOk = ref(true)

function setMsg(t, ok = true)     { msg.value     = t; msgOk.value     = ok; setTimeout(() => msg.value     = '', 4000) }
function setMsgEdit(t, ok = true) { msgEdit.value = t; msgEditOk.value = ok; setTimeout(() => msgEdit.value = '', 3000) }

const productosFiltrados = computed(() => {
  if (!catFilter.value) return productos.value
  return productos.value.filter(p => p.categoria?.id === catFilter.value)
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

async function toggleForm() {
  showForm.value = !showForm.value
  editando.value = null
  lote.value = [{ nombre: '', catId: '' }]
  if (showForm.value && !categorias.value.length) {
    categorias.value = await api('GET', '/categoria/todas').catch(() => [])
  }
}

function agregarFila()   { lote.value.push({ nombre: '', catId: '' }) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function crearLote() {
  const validos = lote.value.filter(i => i.nombre.trim() && i.catId)
  const sinCat  = lote.value.filter(i => i.nombre.trim() && !i.catId)

  if (!validos.length) {
    setMsg('Cada producto necesita nombre y categoría', false); return
  }
  if (sinCat.length) {
    setMsg(`${sinCat.length} producto(s) sin categoría serán omitidos`, false)
  }

  const resultados = await Promise.allSettled(
    validos.map(i => api('POST', '/productos/crear', {
      nombre:    i.nombre.trim(),
      categoria: { id: parseInt(i.catId) }
    }))
  )

  const ok  = resultados.filter(r => r.status === 'fulfilled').length
  const err = resultados.filter(r => r.status === 'rejected').length

  setMsg(err === 0 ? `✓ ${ok} producto(s) creado(s)` : `${ok} creados, ${err} fallaron`, err === 0)
  showForm.value = false
  lote.value = [{ nombre: '', catId: '' }]
  cargar()
}

function iniciarEdicion(p) {
  editando.value = p
  editForm.value = { nombre: p.nombre, catId: p.categoria?.id || '' }
  showForm.value = false
}

async function guardarEdicion() {
  if (!editForm.value.nombre) { setMsgEdit('El nombre es obligatorio', false); return }
  if (!editForm.value.catId)  { setMsgEdit('Selecciona una categoría', false); return }
  try {
    await api('PUT', `/productos/modificar/${editando.value.id}`, {
      nombre:    editForm.value.nombre,
      categoria: { id: parseInt(editForm.value.catId) }
    })
    setMsgEdit('Producto actualizado')
    setTimeout(() => { editando.value = null }, 1500)
    cargar()
  } catch (e) { setMsgEdit(e.message, false) }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar este producto?')) return
  try {
    await api('DELETE', `/productos/eliminar/${id}`)
    cargar()
  } catch (e) { setMsg(e.message, false) }
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
</style>