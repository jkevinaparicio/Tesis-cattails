<template>
  <div class="login-wrap" :class="theme">
    <div class="login-bg">
      <div class="blob b1"></div>
      <div class="blob b2"></div>
      <div class="blob b3"></div>
    </div>

    <div class="login-card">
      <div class="logo">
        <div class="logo-icon">
          <img :src="logo" alt="Cattails" class="logo-img" />
        </div>
        <h1>Cattails</h1>
        <p>Granizados</p>
      </div>

      <!-- Error general -->
      <transition name="fade">
        <div v-if="error" class="msg-err">
          <span>⚠</span> {{ error }}
        </div>
      </transition>

      <!-- Campo correo -->
      <div class="field">
        <label>Correo</label>
        <input
          v-model="form.correo"
          type="email"
          placeholder="usuario@gmail.com"
          @keyup.enter="login"
          autocomplete="email"
        />
      </div>

      <!-- Campo contraseña -->
      <div class="field">
        <label>Contraseña</label>
        <div class="pass-wrap">
          <input
            v-model="form.contraseña"
            :type="showPass ? 'text' : 'password'"
            placeholder="••••••••"
            @keyup.enter="login"
            autocomplete="current-password"
          />
          <button class="eye" @click="showPass = !showPass" type="button">
            <svg v-if="!showPass" width="16" height="16" viewBox="0 0 16 16"
              fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
              <ellipse cx="8" cy="8" rx="6" ry="4"/>
              <circle cx="8" cy="8" r="2" fill="currentColor" stroke="none"/>
            </svg>
            <svg v-else width="16" height="16" viewBox="0 0 16 16"
              fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
              <path d="M2 8s2-4 6-4 6 4 6 4-2 4-6 4-6-4-6-4z"/>
              <line x1="2" y1="2" x2="14" y2="14"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Botón ingresar -->
      <button class="btn-login" :disabled="loading" @click="login">
        <span v-if="loading" class="spinner"></span>
        {{ loading ? 'Verificando...' : 'Ingresar' }}
      </button>

      <!-- Separador -->
      <div class="divider">
        <span class="divider-line"></span>
        <span class="divider-text">o continúa con</span>
        <span class="divider-line"></span>
      </div>

      <!-- Botón Google -->
      <button class="btn-google" @click="loginGoogle">
        <svg width="18" height="18" viewBox="0 0 48 48">
          <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
          <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
          <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
          <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.36-8.16 2.36-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
        </svg>
        Entrar con Google
      </button>

      <p class="hint">Acceso solo para personal autorizado</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/services/api'
import logo from '@/assets/logo.png'

// URL base del backend (misma variable que usa el resto de la app para las llamadas API)
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

const router    = useRouter()
const route     = useRoute()
const authStore = useAuthStore()
const loading   = ref(false)
const showPass  = ref(false)
const form      = ref({ correo: '', contraseña: '' })
const theme     = computed(() => localStorage.getItem('cattails_theme') || 'dark')

// Mostrar error que llegue por query param desde el callback de Google
const error = ref(route.query.msg || route.query.error
  ? mensajeError(route.query.error) || route.query.msg
  : '')

function mensajeError(code) {
  const map = {
    no_registrado: 'Tu cuenta de Google no está registrada. Contacta al administrador.',
    inactivo:      'Tu cuenta está desactivada. Contacta al administrador.',
    google_fallido:'No se pudo autenticar con Google. Intenta de nuevo.'
  }
  return map[code] || ''
}

async function login() {
  error.value = ''
  if (!form.value.correo || !form.value.contraseña) {
    error.value = 'Ingresa tu correo y contraseña.'
    return
  }
  loading.value = true
  try {
    const { data } = await api.post('/auth/login', form.value)

    authStore.setAuth({ token: data.token, rol: data.roles })
    localStorage.setItem('user_correo', form.value.correo)
    localStorage.setItem('sede_id',     data.sedeId     || '')
    localStorage.setItem('sede_nombre', data.sedeNombre || '')

    const role = data.roles?.replace('ROLE_', '')
    router.push(role === 'ADMIN' ? '/admin/estadisticas' : '/empleado/ventas')
  } catch (e) {
    error.value = e.response?.data || 'Credenciales incorrectas.'
  } finally {
    loading.value = false
  }
}

function loginGoogle() {
  window.location.href = `${API_URL}/oauth2/authorization/google`
}

</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600&family=Plus+Jakarta+Sans:wght@300;400;500;600&display=swap');

.login-wrap {
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
.dark.login-wrap, .light.login-wrap { background: #f0f4ff; }

.login-bg { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.blob { position: absolute; border-radius: 50%; filter: blur(100px); }
.b1 { width: 420px; height: 420px; background: #bfdbfe; opacity: 0.5; top: -140px; left: -100px; }
.b2 { width: 320px; height: 320px; background: #dbeafe; opacity: 0.45; bottom: -100px; right: -80px; }
.b3 { width: 220px; height: 220px; background: #eff6ff; opacity: 0.6; top: 40%; left: 55%; }

.login-card {
  position: relative;
  width: 100%; max-width: 400px;
  border-radius: 20px;
  padding: 2.75rem 2.25rem;
  background: #ffffff;
  border: 1px solid #e0eaff;
  box-shadow:
    0 4px 6px rgba(59,130,246,0.04),
    0 12px 40px rgba(59,130,246,0.08),
    0 0 0 1px rgba(59,130,246,0.04);
}

.logo { text-align: center; margin-bottom: 2rem; }
.logo-icon {
  width: 90px; height: 90px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 14px;
}
.logo-img {
  width: 100%; height: 100%; object-fit: contain;
}
.logo h1 {
  font-family: 'Playfair Display', serif;
  font-size: 2.1rem; font-weight: 600;
  letter-spacing: 0.18em; text-transform: uppercase;
  margin: 0; color: #1e3a8a;
}
.logo p {
  font-size: 0.72rem; letter-spacing: 0.3em;
  text-transform: uppercase; margin: 6px 0 0;
  font-weight: 500; color: #60a5fa;
}

.field { margin-bottom: 1.1rem; }
.field label {
  display: block; font-size: 0.7rem;
  letter-spacing: 0.1em; text-transform: uppercase;
  margin-bottom: 7px; font-weight: 600; color: #6b8fc7;
}
.field input {
  width: 100%; border-radius: 10px;
  padding: 0.78rem 1rem; font-size: 0.92rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  outline: none; transition: all 0.2s;
  border: 1.5px solid #dbeafe;
  background: #f8fbff; color: #0f2b6e;
  box-sizing: border-box;
}
.field input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
  background: #ffffff;
}
.field input::placeholder { color: #94b4d6; }

.pass-wrap { position: relative; }
.pass-wrap input { padding-right: 3rem; }
.eye {
  position: absolute; right: 12px; top: 50%;
  transform: translateY(-50%);
  background: none; border: none; cursor: pointer;
  padding: 4px; color: #94b4d6; border-radius: 4px;
  transition: color 0.2s;
}
.eye:hover { color: #3b82f6; }

.btn-login {
  width: 100%; border-radius: 10px; padding: 0.88rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 0.85rem; font-weight: 600;
  letter-spacing: 0.08em; text-transform: uppercase;
  cursor: pointer; margin-top: 0.75rem;
  transition: all 0.2s; border: none;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
  box-shadow: 0 4px 20px rgba(59,130,246,0.35);
}
.btn-login:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 28px rgba(59,130,246,0.5);
}
.btn-login:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

/* ── Separador ── */
.divider {
  display: flex; align-items: center; gap: 10px;
  margin: 1.25rem 0 1rem;
}
.divider-line { flex: 1; height: 1px; background: #dbeafe; }
.divider-text { font-size: 0.7rem; color: #94b4d6; font-weight: 500; white-space: nowrap; }

/* ── Botón Google ── */
.btn-google {
  width: 100%; border-radius: 10px; padding: 0.78rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 0.85rem; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  background: #ffffff;
  border: 1.5px solid #dbeafe;
  color: #1e3a8a;
  box-shadow: 0 2px 8px rgba(59,130,246,0.08);
  text-decoration: none;
  box-sizing: border-box;
}
.btn-google:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 16px rgba(59,130,246,0.15);
  transform: translateY(-1px);
}

.spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.msg-err {
  border-radius: 8px; padding: 0.75rem 1rem;
  font-size: 0.82rem; margin-bottom: 1.25rem;
  display: flex; align-items: center; gap: 8px; font-weight: 500;
  background: #fff0f0;
  border: 1px solid rgba(220,38,38,0.2);
  color: #dc2626;
}

.hint {
  text-align: center; font-size: 0.68rem;
  margin-top: 1.5rem; font-weight: 400; color: #94b4d6;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
