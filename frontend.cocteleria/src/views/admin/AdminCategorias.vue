<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Categorías</h2>
      <button class="bpri" @click="toggleForm">+ Nueva(s) categoría(s)</button>
    </div>

    <!-- FORM CREAR EN LOTE -->
    <div v-if="showForm" class="fbox">
      <p class="fep">POST /categoria/crear — Puedes agregar varias a la vez</p>

      <div v-for="(item, idx) in lote" :key="idx" class="frow s1 lote-row">
        <div class="ff">
          <label>Nombre #{{ idx + 1 }}</label>
          <input v-model="item.nombre" placeholder="Cócteles clásicos"/>
        </div>
        <button v-if="lote.length > 1" class="bsm bd lote-rm" @click="quitarFila(idx)" title="Quitar">✕</button>
      </div>

      <button class="bsm be2 lote-add" @click="agregarFila">+ Agregar otro</button>

      <div class="fact">
        <button class="bpri" @click="crearLote">Guardar todas</button>
        <button class="bsm bd" @click="showForm = false">Cancelar</button>
      </div>
      <p v-if="msg" class="fmsg" :class="msgOk ? 'ok' : 'err'">{{ msg }}</p>
    </div>

    <!-- FORM EDITAR -->
    <div v-if="editando" class="fbox">
      <p class="fep">Editando CATEGORIA: <strong style="color:var(--purple);">{{ editando.nombre }}</strong></p>
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

    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!categorias.length">
            <td colspan="2" class="empty">Sin categorías registradas.</td>
          </tr>
          <tr v-for="c in categorias" :key="c.id">
            <td>{{ c.nombre }}</td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="iniciarEdicion(c)">Editar</button>
                <button class="bsm bd"  @click="eliminar(c.id)">Eliminar</button>
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

const categorias  = ref([])
const showForm    = ref(false)
const lote        = ref([{ nombre: '' }])
const editando    = ref(null)
const editNombre  = ref('')
const msg         = ref(''); const msgOk     = ref(true)
const msgEdit     = ref(''); const msgEditOk = ref(true)

function setMsg(t, ok = true)     { msg.value     = t; msgOk.value     = ok; setTimeout(() => msg.value     = '', 4000) }
function setMsgEdit(t, ok = true) { msgEdit.value = t; msgEditOk.value = ok; setTimeout(() => msgEdit.value = '', 3000) }

onMounted(cargar)

function toggleForm() {
  showForm.value = !showForm.value
  editando.value = null
  lote.value = [{ nombre: '' }]
}

function agregarFila()       { lote.value.push({ nombre: '' }) }
function quitarFila(idx)     { lote.value.splice(idx, 1) }

async function cargar() {
  categorias.value = await api('GET', '/categoria/todas').catch(() => [])
}

async function crearLote() {
  const validos = lote.value.filter(i => i.nombre.trim())
  if (!validos.length) { setMsg('Ingresa al menos un nombre', false); return }

  const resultados = await Promise.allSettled(
    validos.map(i => api('POST', '/categoria/crear', { nombre: i.nombre.trim() }))
  )

  const ok  = resultados.filter(r => r.status === 'fulfilled').length
  const err = resultados.filter(r => r.status === 'rejected').length

  if (err === 0) {
    setMsg(`✓ ${ok} categoría(s) creada(s) correctamente`)
  } else {
    setMsg(`${ok} creadas, ${err} fallaron`, false)
  }

  showForm.value = false
  lote.value = [{ nombre: '' }]
  cargar()
}

function iniciarEdicion(c) {
  editando.value   = c
  editNombre.value = c.nombre
  showForm.value   = false
}

async function guardarEdicion() {
  if (!editNombre.value) { setMsgEdit('El nombre es obligatorio', false); return }
  try {
    await api('PUT', `/categoria/modificar/${editando.value.id}`, { nombre: editNombre.value })
    setMsgEdit('Categoría actualizada')
    setTimeout(() => { editando.value = null }, 1500)
    cargar()
  } catch (e) { setMsgEdit(e.message, false) }
}

async function eliminar(id) {
  if (!confirm('¿Eliminar esta categoría?')) return
  try {
    await api('DELETE', `/categoria/eliminar/${id}`)
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