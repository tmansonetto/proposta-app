package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class NotificacaoSerive {

    private RabbitTemplate rabbitTemplate;

    public void notificar(PropostaResponseDTO proposta, String exchange){
        rabbitTemplate.convertAndSend(exchange, "", proposta);
    }


}
