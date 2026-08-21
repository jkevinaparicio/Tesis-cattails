<template>
  <div class="cb-wrap">
    <div class="login-bg">
      <div class="blob b1"></div>
      <div class="blob b2"></div>
      <div class="blob b3"></div>
    </div>

    <div class="cb-card">
      <div class="logo-icon">
        <svg width="38" height="38" viewBox="0 0 56 56">
          <polygon points="28,6 52,46 4,46" fill="none" stroke="#60a5fa" stroke-width="1.8"/>
          <polygon points="28,14 45,43 11,43" fill="none" stroke="#93c5fd" stroke-width="0.9" opacity="0.5"/>
          <circle cx="28" cy="28" r="4" fill="#3b82f6" opacity="0.9"/>
          <circle cx="28" cy="28" r="2" fill="#93c5fd"/>
          <rect x="22" y="46" width="12" height="6" rx="1" fill="#1e3a6a"/>
          <rect x="18" y="52" width="20" height="2" rx="1" fill="#60a5fa" opacity="0.7"/>
        </svg>
      </div>
      <div class="spinner"></div>
      <p>Verificando sesión con Google...</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router    = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const token  = params.get('token')
  const rol    = params.get('rol')
  const error  = params.get('error')

  if (error || !token) {
    router.push({ path: '/login', query: { error } })
    return
  }

  authStore.setAuth({ token, rol })

  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    localStorage.setItem('user_correo', payload.sub || '')
  } catch { /* no crítico */ }

  router.push('/sede')
})
</script>