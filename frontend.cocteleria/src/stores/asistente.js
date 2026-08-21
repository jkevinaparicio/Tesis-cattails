import { defineStore } from 'pinia'

export const useAsistenteStore = defineStore('asistente', {
  state: () => ({
    mensajes: []
  }),
  actions: {
    agregar(msg) {
      this.mensajes.push(msg)
    },
    limpiar() {
      this.mensajes = []
    }
  }
})