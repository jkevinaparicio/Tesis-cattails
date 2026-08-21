<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Tamaños</h2>
      <button class="bpri" @click="toggleForm">+ Nuevo(s) tamaño(s)</button>
    </div>

    <!-- FORM CREAR EN LOTE -->
    <div v-if="showForm" class="fbox">
      <p class="fep">POST /tamanos/crear — Puedes agregar varios a la vez</p>

      <div v-for="(item, idx) in lote" :key="idx" class="frow s1 lote-row">
        <div class="ff">
          <label>Nombre #{{ idx + 1 }}</label>
          <input v-model="item.nombre" placeholder="Grande"/>
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
      <p class="fep">Editando TAMAÑO: <strong style="color:var(--purple);">{{ editando.nombre }}</strong></p>
      <div class="frow s1">
        <div class="ff">
          <label>Nuevo nombre</label>
          <input v-model="editNombre"/>
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="guardarEdicion">Guardar</button>
        <button class="bsm bd" @click="editando = null">Cancelar</button>
      </div>
      <p v-if="msgEdit" class="fmsg" :class="msgEditOk ? 'ok' : 'err'">{{ msgEdit }}</p>
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
                <button class="bsm be2" @click="iniciarEdicion(t)">Editar</button>
                <button class="bsm bd"  @click="eliminar(t.id)">Eliminar</button>
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

const tamanos    = ref([])
const showForm   = ref(false)
const lote       = ref([{ nombre: '' }])
const editando   = ref(null)
const editNombre = ref('')
const busqueda   = ref('')

const msg         = ref(''); const msgOk     = ref(true)
const msgEdit     = ref(''); const msgEditOk = ref(true)

function setMsg(t, ok = true)     { msg.value     = t; msgOk.value     = ok; setTimeout(() => msg.value     = '', 4000) }
function setMsgEdit(t, ok = true) { msgEdit.value = t; msgEditOk.value = ok; setTimeout(() => msgEdit.value = '', 3000) }

const tamanosFiltrados = computed(() => {
  if (!busqueda.value.trim()) return tamanos.value
  return tamanos.value.filter(t =>
    t.nombre.toLowerCase().includes(busqueda.value.toLowerCase())
  )
})

onMounted(cargar)

function toggleForm() {
  showForm.value = !showForm.value
  editando.value = null
  lote.value = [{ nombre: '' }]
}

function agregarFila()   { lote.value.push({ nombre: '' }) }
function quitarFila(idx) { lote.value.splice(idx, 1) }

async function cargar() {
  tamanos.value = await api('GET', '/tamanos/todos').catch(() => [])
}

async function crearLote() {
  const validos = lote.value.filter(i => i.nombre.trim())
  if (!validos.length) { setMsg('Ingresa al menos un nombre', false); return }

  const resultados = await Promise.allSettled(
    validos.map(i => api('POST', '/tamanos/crear', { nombre: i.nombre.trim() }))
  )

  const ok  = resultados.filter(r => r.status === 'fulfilled').length
  const err = resultados.filter(r => r.status === 'rejected').length

  setMsg(err === 0 ? `✓ ${ok} tamaño(s) creado(s)` : `${ok} creados, ${err} fallaron`, err === 0)
  showForm.value = false
  lote.value = [{ nombre: '' }]
  cargar()
}

function iniciarEdicion(t) {
  editando.value   = t
  editNombre.value = t.nombre
  showForm.value   = false
}

async function guardarEdicion() {
  if (!editNombre.value) { setMsgEdit('El nombre es obligatorio', false); return }
  try {
    await api('PUT', `/tamanos/modificar/${editando.value.id}`, { nombre: editNombre.value })
    setMsgEdit('Tamaño actualizado')
    setTimeout(() => { editando.value = null }, 1500)
    cargar()
  } catch (e) { setMsgEdit(e.message, false) }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar este tamaño?')) return
  try {
    await api('DELETE', `/tamanos/eliminar/${id}`)
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