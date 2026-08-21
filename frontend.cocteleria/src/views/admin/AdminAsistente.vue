<template>
  <div class="chat-wrap">
    <div class="chat-header">
      <div class="chat-avatar">✨</div>
      <div>
        <h2>Asistente Cattails</h2>
        <p>Pregúntame sobre ventas, productos, empleados o predicciones</p>
      </div>
      <button v-if="mensajes.length" class="btn-clear" @click="limpiarChat" title="Limpiar conversación">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6l-1 14H6L5 6M10 11v6M14 11v6M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
        </svg>
      </button>
    </div>

    <div class="chat-body" ref="chatBody">
      <div v-if="!mensajes.length" class="sugerencias">
        <p class="sug-titulo">¿Qué quieres saber hoy?</p>
        <div class="sug-grid">
          <button
            v-for="s in sugerencias" :key="s"
            class="sug-btn"
            @click="enviarSugerencia(s)"
          >{{ s }}</button>
        </div>
      </div>

      <div
        v-for="(msg, i) in mensajes" :key="i"
        class="msg-row"
        :class="msg.rol"
      >
        <div class="msg-avatar">{{ msg.rol === 'user' ? '👤' : '✨' }}</div>
        <div class="msg-bubble" v-html="formatear(msg.texto)"></div>
      </div>

      <div v-if="cargando" class="msg-row asistente">
        <div class="msg-avatar">✨</div>
        <div class="msg-bubble typing">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>

    <div class="chat-input-container">
      <div class="chat-input">
        <input
          v-model="pregunta"
          placeholder="Escribe tu pregunta..."
          @keyup.enter="enviar"
          :disabled="cargando"
        />
        <button @click="enviar" :disabled="cargando || !pregunta.trim()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="22" y1="2" x2="11" y2="13"/>
            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'
import { useAsistenteStore } from '@/stores/asistente'

const { api } = useAdmin()
const asistenteStore = useAsistenteStore()

const pregunta  = ref('')
const mensajes  = asistenteStore.mensajes // ✅ referencia directa al store: persiste entre navegación
const cargando  = ref(false)
const chatBody  = ref(null)

const sugerencias = [
  '¿Cuánto se vendió en total?',
  '¿Cuáles son los productos más vendidos?',
  '¿Qué empleado tiene más ventas?',
  '¿Cómo va a ser la venta del próximo mes?',
  '¿Qué empleado va a vender más el próximo mes?',
  '¿Cuáles son las últimas ventas registradas?'
]

function formatear(texto) {
  if (!texto) return ''
  return texto
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '$1')
    .replace(/#{1,6}\s?/g, '')
    .replace(/\n/g, '<br>')
}

async function enviar() {
  const texto = pregunta.value.trim()
  if (!texto || cargando.value) return

  asistenteStore.agregar({ rol: 'user', texto })
  pregunta.value = ''
  cargando.value = true
  await scrollAbajo()

  try {
    const res = await api('POST', '/asistente/preguntar', { pregunta: texto })
    asistenteStore.agregar({ rol: 'asistente', texto: res.respuesta })
  } catch (e) {
    asistenteStore.agregar({
      rol: 'asistente',
      texto: 'Ocurrió un error al consultar el asistente. Intenta de nuevo.'
    })
  } finally {
    cargando.value = false
    await scrollAbajo()
  }
}

function enviarSugerencia(s) {
  pregunta.value = s
  enviar()
}

function limpiarChat() {
  if (confirm('¿Borrar toda la conversación?')) {
    asistenteStore.limpiar()
  }
}

async function scrollAbajo() {
  await nextTick()
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight
  }
}

onMounted(scrollAbajo)
</script>

<style scoped>
.chat-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: var(--bg1);
  border-radius: 12px;
  border: 1px solid var(--border);
  overflow: hidden; /* el chat-wrap no scrollea, solo chat-body */
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border);
  background: var(--bg2);
  flex-shrink: 0;
}
.chat-avatar {
  width: 40px; height: 40px; border-radius: 12px;
  background: color-mix(in srgb, var(--accent) 15%, transparent);
  border: 1px solid color-mix(in srgb, var(--accent) 30%, transparent);
  display: flex; align-items: center; justify-content: center;
  font-size: 1.2rem;
}
.chat-header h2 { margin: 0; font-size: 1.1rem; font-weight: 700; color: var(--text1); }
.chat-header p  { margin: 2px 0 0; font-size: 0.8rem; color: var(--text3); }
.chat-header > div { flex: 1; }
.btn-clear {
  width: 34px; height: 34px; border-radius: 8px;
  border: 1px solid var(--border); background: var(--bg1);
  color: var(--text3); cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s; flex-shrink: 0;
}
.btn-clear:hover { color: var(--err); border-color: var(--err); }

/* ✅ único scroll del componente, contenido y limitado al espacio disponible */
.chat-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.sugerencias {
  margin: auto;
  text-align: center;
  width: 100%;
  max-width: 800px;
  padding: 1rem;
}
.sug-titulo  { font-size: 0.95rem; color: var(--text3); margin-bottom: 1.5rem; }
.sug-grid    {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}
.sug-btn {
  padding: 0.75rem 1rem;
  border-radius: 12px;
  font-size: 0.85rem;
  border: 1px solid var(--border);
  background: var(--bg2);
  color: var(--text2);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}
.sug-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 100%;
  width: 100%;
}
.msg-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px; height: 32px; border-radius: 10px;
  background: var(--bg2); border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: 0.9rem; flex-shrink: 0;
}

.msg-bubble {
  max-width: 75%;
  padding: 0.75rem 1.1rem;
  border-radius: 14px;
  font-size: 0.9rem;
  line-height: 1.6;
  word-wrap: break-word;
}

/* ✅ FIX DEFINITIVO: colores fijos, NO dependen de --accent ni del tema */
.msg-row.user .msg-bubble {
  background: #2563eb;
  color: #ffffff;
  border-bottom-right-radius: 4px;
}
.msg-row.asistente .msg-bubble {
  background: var(--bg2);
  border: 1px solid var(--border);
  color: var(--text1);
  border-bottom-left-radius: 4px;
}

.typing {
  display: flex; align-items: center; gap: 5px;
  padding: 0.85rem 1.1rem;
}
.typing span {
  width: 7px; height: 7px; border-radius: 50%;
  background: var(--text3);
  animation: bounce 1.2s infinite;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.chat-input-container {
  padding: 1rem 1.5rem;
  background: var(--bg1);
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.chat-input {
  display: flex; gap: 10px;
  background: var(--bg2);
  padding: 6px 6px 6px 16px;
  border-radius: 14px;
  border: 1px solid var(--border);
  align-items: center;
}
.chat-input input {
  flex: 1; padding: 0.5rem 0; font-size: 0.92rem;
  border: none; background: transparent;
  color: var(--text1); outline: none;
}
.chat-input input:disabled { opacity: 0.5; }
.chat-input:focus-within {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--accent) 20%, transparent);
}
.chat-input button {
  width: 38px; height: 38px; border-radius: 10px;
  border: none; cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; justify-content: center;
  background: #2563eb; color: #fff; flex-shrink: 0;
}
.chat-input button:hover:not(:disabled) { opacity: 0.9; }
.chat-input button:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 640px) {
  .chat-body { padding: 1rem; }
  .chat-input-container { padding: 1rem; }
  .sug-grid { grid-template-columns: 1fr; }
  .msg-bubble { max-width: 85%; }
}
</style>