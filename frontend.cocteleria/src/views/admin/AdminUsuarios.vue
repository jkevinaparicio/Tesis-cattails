<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Usuarios</h2>
      <button class="bpri" @click="showForm = !showForm">+ Crear usuario</button>
    </div>

    <!-- USUARIOS CONECTADOS -->
    <div class="fbox" style="margin-bottom:1.25rem;">
      <p class="fep">
        🟢 Usuarios conectados ahora
        <span style="font-size:0.75rem; color:var(--text3); margin-left:8px;">
          {{ conectados.length }} en línea
        </span>
      </p>
      <div v-if="!conectados.length" class="empty" style="padding:0.75rem 0;">
        Ningún usuario conectado.
      </div>
      <div v-else style="display:flex; flex-wrap:wrap; gap:8px; margin-top:0.5rem;">
        <span
          v-for="correo in conectados"
          :key="correo"
          style="
            background: color-mix(in srgb, var(--ok) 15%, transparent);
            border: 1px solid color-mix(in srgb, var(--ok) 30%, transparent);
            color: var(--ok);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 6px;
          "
        >
          <span style="width:7px; height:7px; border-radius:50%; background:var(--ok); display:inline-block;"></span>
          {{ correo }}
        </span>
      </div>
    </div>

    <!-- FORM CREAR -->
    <div v-if="showForm" class="fbox">
      <p class="fep">POST /usuarios/crear</p>
      <div class="frow">
        <div class="ff"><label>Nombre</label><input v-model="form.nombre" placeholder="Juan"/></div>
        <div class="ff"><label>Apellido</label><input v-model="form.apellido" placeholder="Pérez"/></div>
      </div>
      <div class="frow">
        <div class="ff">
          <label>Cédula (ID del usuario)</label>
          <input v-model="form.idUsuario" type="number" placeholder="1234567890"/>
        </div>
        <div class="ff">
          <label>Correo completo</label>
          <input v-model="form.correo" placeholder="juan.perez@cattails.com"/>
        </div>
      </div>
      <div class="frow">
        <div class="ff">
          <label>Contraseña</label>
          <input v-model="form.contraseña" type="password" placeholder="••••••"/>
        </div>
        <div class="ff">
          <label>Rol</label>
          <select v-model="form.rolId">
            <option value="1">Admin</option>
            <option value="2">Empleado</option>
          </select>
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="crear">Guardar</button>
        <button class="bsm bd" @click="showForm = false">Cancelar</button>
      </div>
      <p v-if="msgCrear" class="fmsg" :class="msgCrearOk ? 'ok' : 'err'">{{ msgCrear }}</p>
    </div>

    <!-- FORM EDITAR -->
    <div v-if="editando" class="fbox">
      <p class="fep">PUT /usuarios/modificar/{{ editForm.idUsuario }}</p>
      <div class="frow">
        <div class="ff"><label>Nombre</label><input v-model="editForm.nombre"/></div>
        <div class="ff"><label>Apellido</label><input v-model="editForm.apellido"/></div>
      </div>
      <div class="frow">
        <div class="ff"><label>Correo</label><input v-model="editForm.correo"/></div>
        <div class="ff">
          <label>Contraseña nueva (opcional)</label>
          <input v-model="editForm.contraseña" type="password" placeholder="Dejar vacío para no cambiar"/>
        </div>
      </div>
      <div class="frow s1">
        <div class="ff">
          <label>Rol</label>
          <select v-model="editForm.rolId">
            <option value="1">Admin</option>
            <option value="2">Empleado</option>
          </select>
        </div>
      </div>
      <div class="fact">
        <button class="bpri" @click="guardarEdicion">Guardar cambios</button>
        <button class="bsm bd" @click="editando = null">Cancelar</button>
      </div>
      <p v-if="msgEditar" class="fmsg" :class="msgEditarOk ? 'ok' : 'err'">{{ msgEditar }}</p>
    </div>

    <!-- ✅ BUSCADOR -->
    <div class="fbox" style="margin-bottom:1rem;">
      <div style="display:flex; align-items:center; gap:8px;">
        <span style="color:var(--text3); font-size:1rem;">🔍</span>
        <input
          v-model="busqueda"
          placeholder="Buscar por cédula, nombre o correo..."
          style="flex:1; padding:0.45rem 0.75rem; border:1px solid var(--border); border-radius:6px; font-size:0.875rem; background:var(--bg2); color:var(--text1);"
        />
        <button
          v-if="busqueda"
          class="bsm bd"
          @click="busqueda = ''"
          style="padding:0.3rem 0.6rem; font-size:0.8rem;"
        >✕</button>
      </div>
      <p v-if="busqueda && !usuariosFiltrados.length" style="margin:0.5rem 0 0; font-size:0.8rem; color:var(--text3);">
        Sin resultados para "{{ busqueda }}"
      </p>
    </div>

    <!-- TABLA -->
    <div class="tbl-w">
      <table>
        <thead>
          <tr>
            <th>Cédula</th>
            <th>Nombre</th>
            <th>Correo</th>
            <th>Rol</th>
            <th>Última conexión</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="empty">Cargando...</td>
          </tr>
          <tr v-else-if="!usuariosFiltrados.length && !busqueda">
            <td colspan="7" class="empty">Sin usuarios registrados.</td>
          </tr>
          <tr
            v-for="u in usuariosFiltrados"
            :key="u.idUsuario"
            :style="u.activo === false ? 'opacity: 0.45;' : 'opacity: 1;'"
          >
            <td class="c-purple">{{ u.idUsuario }}</td>
            <td>{{ u.nombre }} {{ u.apellido }}</td>
            <td class="c-muted">{{ u.correo }}</td>
            <td>
              <span class="badge" :class="u.rol?.nombre === 'ROLE_ADMIN' ? 'ba' : 'be'">
                {{ u.rol?.nombre === 'ROLE_ADMIN' ? 'Admin' : 'Empleado' }}
              </span>
            </td>
            <td class="c-muted">{{ formatFecha(u.ultimaConexion) }}</td>
            <td>
              <span
                v-if="conectados.includes(u.correo)"
                style="display:inline-flex; align-items:center; gap:5px; font-size:0.78rem; font-weight:600; color:var(--ok);"
              >
                <span style="width:7px; height:7px; border-radius:50%; background:var(--ok); display:inline-block;"></span>
                En línea
              </span>
              <span
                v-else-if="u.activo === false"
                style="display:inline-flex; align-items:center; gap:5px; font-size:0.78rem; font-weight:600; color:var(--err);"
              >
                <span style="width:7px; height:7px; border-radius:50%; background:var(--err); display:inline-block;"></span>
                Inactivo
              </span>
              <span
                v-else
                style="display:inline-flex; align-items:center; gap:5px; font-size:0.78rem; font-weight:500; color:var(--text3);"
              >
                <span style="width:7px; height:7px; border-radius:50%; background:var(--text3); display:inline-block;"></span>
                Fuera de línea
              </span>
            </td>
            <td>
              <div class="btn-r">
                <button class="bsm be2" @click="iniciarEdicion(u)">Editar</button>
                <button
                  class="bsm"
                  :class="u.activo === false ? 'be2' : 'bd'"
                  @click="toggleActivo(u)"
                >
                  {{ u.activo === false ? 'Activar' : 'Desactivar' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'
import { useWebSocket } from '@/composables/useWebSocket'

const { api, loading } = useAdmin()
const { conectar, desconectar } = useWebSocket()

const usuarios   = ref([])
const conectados = ref([])
const showForm   = ref(false)
const editando   = ref(null)
const busqueda   = ref('')   // ✅ NUEVO

const usuariosFiltrados = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return usuarios.value
  return usuarios.value.filter(u => {
    const nombreCompleto = `${u.nombre ?? ''} ${u.apellido ?? ''}`.toLowerCase()
    const cedula  = String(u.idUsuario ?? '')
    const correo  = (u.correo ?? '').toLowerCase()
    return cedula.includes(q) || nombreCompleto.includes(q) || correo.includes(q)
  })
})

const form = ref({
  idUsuario: '', nombre: '', apellido: '',
  correo: '', contraseña: '', rolId: 2
})

const editForm = ref({
  idUsuario: '', nombre: '', apellido: '',
  correo: '', contraseña: '', rolId: 2
})

const msgCrear    = ref(''); const msgCrearOk  = ref(true)
const msgEditar   = ref(''); const msgEditarOk = ref(true)

function setMsgCrear(t, ok = true)  { msgCrear.value  = t; msgCrearOk.value  = ok; setTimeout(() => msgCrear.value  = '', 3500) }
function setMsgEditar(t, ok = true) { msgEditar.value = t; msgEditarOk.value = ok; setTimeout(() => msgEditar.value = '', 3500) }

function formatFecha(fecha) {
  if (!fecha) return 'Nunca'
  return new Date(fecha).toLocaleString('es-CO', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

let intervalo = null

onMounted(async () => {
  await cargar()
  await cargarConectados()
  intervalo = setInterval(cargarConectados, 10000)
  conectar(() => { cargarConectados() })
})

onUnmounted(() => {
  clearInterval(intervalo)
  desconectar()
})

async function cargar() {
  loading.value = true
  try {
    usuarios.value = await api('GET', '/usuarios')
  } catch (e) {
    setMsgCrear(e.message, false)
  } finally {
    loading.value = false
  }
}

async function cargarConectados() {
  try {
    conectados.value = await api('GET', '/usuarios/conectados')
  } catch {
    conectados.value = []
  }
}

async function crear() {
  if (!form.value.idUsuario)  { setMsgCrear('La cédula es obligatoria', false); return }
  if (!form.value.nombre)     { setMsgCrear('El nombre es obligatorio', false); return }
  if (!form.value.correo)     { setMsgCrear('El correo es obligatorio', false); return }
  if (!form.value.contraseña) { setMsgCrear('La contraseña es obligatoria', false); return }

  try {
    await api('POST', '/usuarios/crear', {
      idUsuario:  parseInt(form.value.idUsuario),
      nombre:     form.value.nombre,
      apellido:   form.value.apellido,
      correo:     form.value.correo,
      contraseña: form.value.contraseña,
      rol:        { id: parseInt(form.value.rolId) }
    })
    setMsgCrear('Usuario creado correctamente')
    showForm.value = false
    form.value = { idUsuario: '', nombre: '', apellido: '', correo: '', contraseña: '', rolId: 2 }
    cargar()
  } catch (e) {
    setMsgCrear(e.response?.data || e.message, false)
  }
}

function iniciarEdicion(u) {
  editando.value = u.idUsuario
  editForm.value = {
    idUsuario:  u.idUsuario,
    nombre:     u.nombre,
    apellido:   u.apellido || '',
    correo:     u.correo,
    contraseña: '',
    rolId:      u.rol?.id || 2
  }
  showForm.value = false
}

async function guardarEdicion() {
  if (!editForm.value.nombre) { setMsgEditar('El nombre es obligatorio', false); return }

  const body = {
    nombre:   editForm.value.nombre,
    apellido: editForm.value.apellido,
    correo:   editForm.value.correo,
    rol:      { id: parseInt(editForm.value.rolId) }
  }
  if (editForm.value.contraseña) body.contraseña = editForm.value.contraseña

  try {
    await api('PUT', `/usuarios/modificar/${editForm.value.idUsuario}`, body)
    setMsgEditar('Cambios guardados')
    setTimeout(() => { editando.value = null }, 1500)
    cargar()
  } catch (e) {
    setMsgEditar(e.response?.data || e.message, false)
  }
}

async function toggleActivo(u) {
  const nuevoEstado = u.activo === false ? true : false
  const accion = nuevoEstado ? 'activar' : 'desactivar'
  if (!confirm(`¿Deseas ${accion} a ${u.nombre}?`)) return

  try {
    await api('PUT', `/usuarios/modificar/${u.idUsuario}`, {
      nombre:   u.nombre,
      apellido: u.apellido || '',
      correo:   u.correo,
      rol:      { id: u.rol?.id || 2 },
      activo:   nuevoEstado
    })
    setMsgCrear(`Usuario ${nuevoEstado ? 'activado' : 'desactivado'} correctamente`)
    cargar()
  } catch (e) {
    setMsgCrear(e.response?.data || e.message, false)
  }
}
</script>

<style scoped src="@/assets/admin.css" />