import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { API_URL } from '@/config'

let client = null

export function useWebSocket() {
  function conectar(onVentaNueva = () => {}) {
    const correo = localStorage.getItem('user_correo') || ''

    client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`, null, {
        transports: ['websocket']
      }),
      reconnectDelay: 3000,
      connectHeaders: { correo },
      onConnect: () => {
        console.log('✅ Conectado como:', correo)
        client.subscribe('/topic/ventas', (mensaje) => {
          const venta = JSON.parse(mensaje.body)
          onVentaNueva(venta)
        }, { correo })
      },
      onDisconnect: () => console.log('❌ Desconectado:', correo),
      onStompError: (frame) => console.error('❌ STOMP error:', frame)
    })
    client.activate()
  }

  function desconectar() {
    client?.deactivate()
  }

  return { conectar, desconectar }
}