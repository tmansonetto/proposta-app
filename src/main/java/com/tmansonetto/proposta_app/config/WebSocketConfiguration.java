package com.tmansonetto.proposta_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket (STOMP sobre SockJS).
 *
 * <p>O front-end abre uma conexão em {@code http://localhost:8080/ws} para receber
 * atualizações em tempo real (por exemplo, o resultado da análise de uma proposta).
 * Sem este endpoint, o servidor responde 404 e o front fica tentando reconectar.
 *
 * <p>O front se inscreve no destino {@code /propostas}; é para ele que o servidor deve enviar
 * as propostas atualizadas (por exemplo, com {@code SimpMessagingTemplate.convertAndSend("/propostas", dto)}).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /** Registra o endpoint {@code /ws} (com fallback SockJS) e as origens permitidas. */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost", "http://localhost:*")
                .withSockJS();
    }

    /** Habilita o broker simples em memória para os destinos {@code /propostas} e {@code /topic}. */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/propostas", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
