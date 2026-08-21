package backendWebProyectoApp.WebBackendApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class PresenciaWebSocket {

    // sessionId → correo
    private final ConcurrentHashMap<String, String> sesiones = new ConcurrentHashMap<>();

    @EventListener
    public void onSuscripcion(SessionSubscribeEvent event) {
        try {
            StompHeaderAccessor accessor =
                    StompHeaderAccessor.wrap(event.getMessage());
            String correo    = accessor.getFirstNativeHeader("correo");
            String sessionId = accessor.getSessionId();
            if (correo != null && !correo.isBlank() && sessionId != null) {
                sesiones.put(sessionId, correo);
            }
        } catch (Exception ignored) {}
    }

    @EventListener
    public void onDesconexion(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor accessor =
                    StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = accessor.getSessionId();
            if (sessionId != null) {
                sesiones.remove(sessionId);
            }
        } catch (Exception ignored) {}
    }

    public Set<String> getConectados() {
        return Collections.unmodifiableSet(
                ConcurrentHashMap.newKeySet()
        );
    }

    public Set<String> getCorreosConectados() {
        return Collections.unmodifiableSet(
                new java.util.HashSet<>(sesiones.values())
        );
    }
}