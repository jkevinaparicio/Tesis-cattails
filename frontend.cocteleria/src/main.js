import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import '@/assets/admin.css'
import { useWebSocket } from '@/composables/useWebSocket'

const app = createApp(App)
app.use(createPinia())
app.use(router)

router.afterEach(() => {
  const token = localStorage.getItem('jwt_token')
  if (token) {
    const { iniciar } = useWebSocket()
    iniciar()
  }
})

app.mount('#app')