<template>
  <div class="sede-wrap" :class="theme">
    <div class="sede-bg">
      <div class="blob b1"></div>
      <div class="blob b2"></div>
    </div>

    <div class="sede-card">
      <div class="sede-header">
        <div class="sede-icon">
          <svg width="32" height="32" viewBox="0 0 56 56">
            <polygon points="28,6 52,46 4,46" fill="none" stroke="#60a5fa" stroke-width="1.8"/>
            <polygon points="28,14 45,43 11,43" fill="none" stroke="#93c5fd" stroke-width="0.9" opacity="0.5"/>
            <circle cx="28" cy="28" r="4" fill="#3b82f6" opacity="0.9"/>
            <circle cx="28" cy="28" r="2" fill="#93c5fd"/>
            <rect x="22" y="46" width="12" height="6" rx="1" fill="#1e3a6a"/>
            <rect x="18" y="52" width="20" height="2" rx="1" fill="#60a5fa" opacity="0.7"/>
          </svg>
        </div>
        <h1 class="sede-title">Cattails Granizados</h1>
        <p class="sede-sub">¿En qué sede trabajas hoy?</p>
      </div>

      <div class="sede-list">
        <div v-if="!sedes.length && !error" class="sede-loading">
          <div class="loader"></div>
          <span>Cargando sedes...</span>
        </div>
        <div
          v-for="s in sedes" :key="s.id"
          class="sede-opt"
          :class="{ sel: seleccionada?.id === s.id }"
          @click="seleccionada = s"
        >
          <span class="sede-opt-name">{{ s.nombre }}</span>
          <span v-if="seleccionada?.id === s.id" class="sede-check">✓</span>
        </div>
        <p v-if="error" class="err-txt">{{ error }}</p>
      </div>

      <button class="sede-btn" :disabled="!seleccionada" @click="confirmar">
        <span v-if="seleccionada">Continuar → {{ seleccionada.nombre }}</span>
        <span v-else>Selecciona una sede</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from '@/plugins/axios'

const router = useRouter()
const theme  = computed(() => localStorage.getItem('cattails_theme') || 'dark')

const sedes        = ref([])
const seleccionada = ref(null)
const error        = ref('')

onMounted(async () => {
  try {
    const { data } = await axios.get('/sedes/listar')
    sedes.value = data
  } catch {
    error.value = 'Error al cargar sedes. Verifica tu conexión.'
  }
})

function confirmar() {
  if (!seleccionada.value) return
  localStorage.setItem('sede_id',     seleccionada.value.id)
  localStorage.setItem('sede_nombre', seleccionada.value.nombre)
  const rol = localStorage.getItem('user_role')?.replace('ROLE_', '')
  router.push(rol === 'ADMIN' ? '/admin' : '/empleado')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600&family=Plus+Jakarta+Sans:wght@300;400;500;600&display=swap');

.sede-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Plus Jakarta Sans', sans-serif;
  position: relative;
  overflow: hidden;
  padding: 1rem;
  transition: background 0.3s;
}
.dark.sede-wrap  { background: #040810; }
.light.sede-wrap { background: #f0f6ff; }

.sede-bg { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.blob { position: absolute; border-radius: 50%; filter: blur(90px); }
.dark .b1  { width: 350px; height: 350px; background: #1d4ed8; opacity: 0.1; top: -80px; right: -60px; }
.dark .b2  { width: 300px; height: 300px; background: #3b82f6; opacity: 0.08; bottom: -80px; left: -60px; }
.light .b1 { width: 350px; height: 350px; background: #3b82f6; opacity: 0.06; top: -80px; right: -60px; }
.light .b2 { width: 300px; height: 300px; background: #1d4ed8; opacity: 0.05; bottom: -80px; left: -60px; }

.sede-card {
  position: relative; z-index: 1;
  width: 100%; max-width: 440px;
  border-radius: 20px; padding: 2.5rem 2rem;
  transition: all 0.3s;
}
.dark .sede-card {
  background: rgba(8,13,28,0.92);
  border: 1px solid #1a2a4a;
  box-shadow: 0 8px 48px rgba(96,165,250,0.12);
  backdrop-filter: blur(20px);
}
.light .sede-card {
  background: rgba(255,255,255,0.95);
  border: 1px solid #a0c4f0;
  box-shadow: 0 8px 48px rgba(29,78,216,0.1);
}

.sede-header { text-align: center; margin-bottom: 2rem; }

.sede-icon {
  width: 60px; height: 60px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 14px;
}
.dark .sede-icon  { background: rgba(96,165,250,0.1); border: 1px solid rgba(96,165,250,0.25); }
.light .sede-icon { background: rgba(59,130,246,0.08); border: 1px solid rgba(59,130,246,0.2); }

.sede-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 6px;
  line-height: 1.2;
}
.dark .sede-title  { color: #93c5fd; text-shadow: 0 0 30px rgba(147,197,253,0.25); }
.light .sede-title { color: #1d4ed8; }

.sede-sub {
  font-size: 0.78rem; letter-spacing: 0.1em;
  font-weight: 500;
}
.dark .sede-sub  { color: #3a5a80; }
.light .sede-sub { color: #5a80aa; }

.sede-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 1.5rem; }

.sede-loading {
  display: flex; align-items: center; gap: 10px;
  justify-content: center; padding: 1.5rem;
  font-size: 0.85rem; font-weight: 500;
}
.dark .sede-loading  { color: #3a5a80; }
.light .sede-loading { color: #5a80aa; }

.loader {
  width: 16px; height: 16px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.sede-opt {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.9rem 1.1rem; border-radius: 10px;
  cursor: pointer; font-size: 0.9rem; font-weight: 500;
  transition: all 0.18s; border: 1px solid transparent;
}
.dark .sede-opt        { background: #0d1428; border-color: #1a2a4a; color: #3a5a80; }
.dark .sede-opt:hover  { background: #131a32; border-color: rgba(96,165,250,0.4); color: #93c5fd; }
.dark .sede-opt.sel    { background: rgba(96,165,250,0.1); border-color: #60a5fa; color: #e0f0ff; }
.light .sede-opt       { background: #e0eeff; border-color: #a0c4f0; color: #1e3a6a; }
.light .sede-opt:hover { background: #c8dffe; border-color: rgba(59,130,246,0.5); color: #0a1a3a; }
.light .sede-opt.sel   { background: rgba(59,130,246,0.1); border-color: #3b82f6; color: #0a1a3a; }
.sede-opt.sel { font-weight: 600; }

.sede-opt-name { flex: 1; }
.sede-check { color: #60a5fa; font-weight: 700; font-size: 1.1rem; }
.light .sede-check { color: #1d4ed8; }

.sede-btn {
  width: 100%; padding: 0.9rem; border-radius: 10px;
  cursor: pointer; font-size: 0.85rem; font-weight: 600;
  font-family: 'Plus Jakarta Sans', sans-serif;
  transition: all 0.2s; border: none;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
  box-shadow: 0 4px 20px rgba(59,130,246,0.4);
}
.sede-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 28px rgba(59,130,246,0.55);
}
.sede-btn:disabled { opacity: 0.4; cursor: not-allowed; transform: none; box-shadow: none; }

.err-txt { color: #f87171; font-size: 0.82rem; text-align: center; padding: 0.5rem; font-weight: 500; }
</style>