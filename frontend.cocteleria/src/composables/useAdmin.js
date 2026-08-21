import { ref } from 'vue'
import axios from '@/plugins/axios'

export function useAdmin() {
  const loading = ref(false)

  async function api(method, url, body) {
    try {
      const res = await axios({ method, url, data: body })
      return res.data
    } catch (e) {
      throw new Error(e.response?.data || e.message || 'Error del servidor')
    }
  }

  return { loading, api }
}