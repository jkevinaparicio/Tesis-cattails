<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Sedes</h2>
      <button class="bpri" @click="showForm = !showForm">+ Nueva sede</button>
    </div>

    <!-- FORM CREAR -->
    <div v-if="showForm" class="fbox">
      <p class="fep">POST /sedes/crear</p>
      <div class="frow s1">
        <div class="ff">
          <label>Nombre de la sede</label>
          <input v-model="nombre" placeholder="Sede Centro" />
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="crear">Guardar</button>
        <button class="bsm bd" @click="showForm = false">Cancelar</button>
      </div>
      <p v-if="msg" class="fmsg" :class="msgOk ? 'ok' : 'err'">{{ msg }}</p>
    </div>

    <!-- FORM EDITAR -->
    <div v-if="editando" class="fbox">
      <p class="fep">PUT /sedes/modificar/{{ editando.id }}</p>
      <div class="frow s1">
        <div class="ff">
          <label>Nuevo nombre</label>
          <input v-model="editNombre" />
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="guardarEdicion">Guardar</button>
        <button class="bsm bd" @click="editando = null">Cancelar</button>
      </div>
      <p v-if="msgEdit" class="fmsg" :class="msgEditOk ? 'ok' : 'err'">{{ msgEdit }}</p>
    </div>

    <!-- TABLA -->
    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!sedes.length">
            <td colspan="2" class="empty">Sin sedes registradas.</td>
          </tr>
          <tr v-for="s in sedes" :key="s.id">
            <td>{{ s.nombre }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="iniciarEdicion(s)">Editar</button>
                <button class="bsm bd" @click="eliminar(s.id)">Eliminar</button>
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

const sedes    = ref([])
const showForm = ref(false)
const nombre   = ref('')
const editando   = ref(null)
const editNombre = ref('')

const msg        = ref(''); const msgOk     = ref(true)
const msgEdit    = ref(''); const msgEditOk = ref(true)

function setMsg(t, ok = true)     { msg.value     = t; msgOk.value     = ok; setTimeout(() => msg.value     = '', 3000) }
function setMsgEdit(t, ok = true) { msgEdit.value = t; msgEditOk.value = ok; setTimeout(() => msgEdit.value = '', 3000) }

onMounted(cargar)

async function cargar() {
  sedes.value = await api('GET', '/sedes/listar').catch(() => [])
}

async function crear() {
  if (!nombre.value) { setMsg('El nombre es obligatorio', false); return }
  try {
    await api('POST', '/sedes/crear', { nombre: nombre.value })
    setMsg('Sede creada correctamente')
    showForm.value = false
    nombre.value = ''
    cargar()
  } catch (e) { setMsg(e.message, false) }
}

function iniciarEdicion(s) {
  editando.value   = s
  editNombre.value = s.nombre
  showForm.value   = false
}

async function guardarEdicion() {
  if (!editNombre.value) { setMsgEdit('El nombre es obligatorio', false); return }
  try {
    await api('PUT', `/sedes/modificar/${editando.value.id}`, { nombre: editNombre.value })
    setMsgEdit('Sede actualizada')
    setTimeout(() => { editando.value = null }, 1500)
    cargar()
  } catch (e) { setMsgEdit(e.message, false) }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta sede?')) return
  try {
    await api('DELETE', `/sedes/eliminar/${id}`)
    cargar()
  } catch (e) { setMsg(e.message, false) }
}
</script>

<style scoped src="@/assets/admin.css" />