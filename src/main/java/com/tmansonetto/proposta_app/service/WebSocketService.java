package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WebSocketService {

    private SimpMessagingTemplate simpMessagingTemplate;

    public void notificar(PropostaResponseDTO propostaResponseDTO) {
     simpMessagingTemplate.convertAndSend("/propostas",propostaResponseDTO);
    }
}
