<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Usuarios</h2>
      <button class="bpri" @click="abrirCrear">+ Crear usuario</button>
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
                <button class="bsm be2" @click="abrirEditar(u)">Editar</button>
                <button
                  class="bsm"
                  :class="u.activo === false ? 'be2' : 'bd'"
                  @click="abrirConfirmToggle(u)"
                >
                  {{ u.activo === false ? 'Activar' : 'Desactivar' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ============ MODAL: CREAR USUARIO ============ -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <p class="modal-title">+ Crear usuario</p>
          <button class="modal-close" @click="showForm = false">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
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

          <p v-if="modalCrear.error" class="modal-err">{{ modalCrear.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="showForm = false">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="crear">
            {{ guardando ? 'Guardando...' : 'Guardar' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: EDITAR USUARIO ============ -->
    <div v-if="editando" class="modal-overlay" @click.self="editando = null">
      <div class="modal-box modal-lg">
        <div class="modal-header">
          <p class="modal-title">Editar usuario — {{ editForm.idUsuario }}</p>
          <button class="modal-close" @click="editando = null">✕</button>
        </div>

        <div class="modal-body-pad modal-scroll">
          <div class="frow">
            <div class="ff"><label>Nombre</label><input ref="inputEditar" v-model="editForm.nombre"/></div>
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

          <p v-if="modalEditar.error" class="modal-err">{{ modalEditar.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="editando = null">Cancelar</button>
          <button class="bpri" :disabled="guardando" @click="guardarEdicion">
            {{ guardando ? 'Guardando...' : 'Guardar cambios' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ============ MODAL: CONFIRMAR ACTIVAR/DESACTIVAR ============ -->
    <div v-if="modalConfirm" class="modal-overlay" @click.self="modalConfirm = null">
      <div class="modal-box modal-sm">
        <div class="modal-header">
          <p class="modal-title" :style="{ color: modalConfirm.nuevoEstado ? 'var(--ok)' : 'var(--err)' }">
            {{ modalConfirm.nuevoEstado ? '✓ Activar usuario' : '⚠ Desactivar usuario' }}
          </p>
          <button class="modal-close" @click="modalConfirm = null">✕</button>
        </div>

        <div class="modal-body-pad">
          <p class="modal-target">
            ¿{{ modalConfirm.nuevoEstado ? 'Activar' : 'Desactivar' }} a {{ modalConfirm.usuario.nombre }} {{ modalConfirm.usuario.apellido }}?
          </p>
          <p v-if="!modalConfirm.nuevoEstado" class="c-muted" style="font-size:0.8rem; margin-top:8px;">
            El usuario no podrá iniciar sesión mientras esté desactivado, pero su historial se conserva.
          </p>
          <p v-if="modalConfirm.error" class="modal-err">{{ modalConfirm.error }}</p>
        </div>

        <div class="modal-footer-pad">
          <button class="bsm bd" @click="modalConfirm = null">Cancelar</button>
          <button class="bpri"
                  :style="!modalConfirm.nuevoEstado ? 'background:var(--err); border-color:var(--err);' : ''"
                  :disabled="guardando" @click="confirmarToggle">
            {{ guardando ? 'Guardando...' : (modalConfirm.nuevoEstado ? 'Sí, activar' : 'Sí, desactivar') }}
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
import { useWebSocket } from '@/composables/useWebSocket'

const { api, loading } = useAdmin()
const { conectar, desconectar } = useWebSocket()

const usuarios   = ref([])
const conectados = ref([])
const showForm   = ref(false)
const editando   = ref(null)
const busqueda   = ref('')
const inputEditar = ref(null)

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

const modalCrear   = ref({ error: '' })
const modalEditar  = ref({ error: '' })
const modalConfirm = ref(null)
const guardando    = ref(false)
const toast        = ref(null)

function setToast(texto, ok = true) {
  toast.value = { texto, ok }
  setTimeout(() => { toast.value = null }, 3000)
}

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
    setToast(e.message, false)
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

function abrirCrear() {
  showForm.value = true
  form.value = { idUsuario: '', nombre: '', apellido: '', correo: '', contraseña: '', rolId: 2 }
  modalCrear.value = { error: '' }
}

async function crear() {
  if (!form.value.idUsuario)  { modalCrear.value.error = 'La cédula es obligatoria'; return }
  if (!form.value.nombre)     { modalCrear.value.error = 'El nombre es obligatorio'; return }
  if (!form.value.correo)     { modalCrear.value.error = 'El correo es obligatorio'; return }
  if (!form.value.contraseña) { modalCrear.value.error = 'La contraseña es obligatoria'; return }

  guardando.value = true
  try {
    await api('POST', '/usuarios/crear', {
      idUsuario:  parseInt(form.value.idUsuario),
      nombre:     form.value.nombre,
      apellido:   form.value.apellido,
      correo:     form.value.correo,
      contraseña: form.value.contraseña,
      rol:        { id: parseInt(form.value.rolId) }
    })
    showForm.value = false
    setToast('✓ Usuario creado correctamente')
    cargar()
  } catch (e) {
    modalCrear.value.error = e.response?.data || e.message || 'Error al crear'
  } finally {
    guardando.value = false
  }
}

async function abrirEditar(u) {
  editando.value = u.idUsuario
  editForm.value = {
    idUsuario:  u.idUsuario,
    nombre:     u.nombre,
    apellido:   u.apellido || '',
    correo:     u.correo,
    contraseña: '',
    rolId:      u.rol?.id || 2
  }
  modalEditar.value = { error: '' }
  await nextTick()
  inputEditar.value?.focus()
}

async function guardarEdicion() {
  if (!editForm.value.nombre) { modalEditar.value.error = 'El nombre es obligatorio'; return }

  const body = {
    nombre:   editForm.value.nombre,
    apellido: editForm.value.apellido,
    correo:   editForm.value.correo,
    rol:      { id: parseInt(editForm.value.rolId) }
  }
  if (editForm.value.contraseña) body.contraseña = editForm.value.contraseña

  guardando.value = true
  try {
    await api('PUT', `/usuarios/modificar/${editForm.value.idUsuario}`, body)
    editando.value = null
    setToast('✓ Cambios guardados')
    cargar()
  } catch (e) {
    modalEditar.value.error = e.response?.data || e.message || 'Error al guardar'
  } finally {
    guardando.value = false
  }
}

function abrirConfirmToggle(u) {
  const nuevoEstado = u.activo === false ? true : false
  modalConfirm.value = { usuario: u, nuevoEstado, error: '' }
}

async function confirmarToggle() {
  const { usuario: u, nuevoEstado } = modalConfirm.value
  guardando.value = true
  try {
    await api('PUT', `/usuarios/modificar/${u.idUsuario}`, {
      nombre:   u.nombre,
      apellido: u.apellido || '',
      correo:   u.correo,
      rol:      { id: u.rol?.id || 2 },
      activo:   nuevoEstado
    })
    modalConfirm.value = null
    setToast(`✓ Usuario ${nuevoEstado ? 'activado' : 'desactivado'} correctamente`)
    cargar()
  } catch (e) {
    modalConfirm.value.error = e.response?.data || e.message || 'Error al actualizar'
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped src="@/assets/admin.css" />
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
.modal-sm { max-width: 420px; }
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
