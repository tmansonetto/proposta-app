package com.tmansonetto.proposta_app.listener;

import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.mapper.PropostaMapper;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import com.tmansonetto.proposta_app.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PropostaConcluidaListener {

    private static final Logger logger = LoggerFactory.getLogger(PropostaConcluidaListener.class);

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private WebSocketService webSocketService;

    @RabbitListener(queues = "${rabbitmq.queue.proposta.concluida}")
    public void propostaConcluidaMsPropostaConcluida(Proposta propostaAnalisada) {
        // Busca a proposta no banco e atualiza só o resultado da análise.
        // Se ela não existir mais (ex.: banco recriado), a mensagem é ignorada.
        propostaRepository.findById(propostaAnalisada.getId()).ifPresentOrElse(proposta -> {
            proposta.setAprovado(propostaAnalisada.getAprovado());
            proposta.setObservacao(propostaAnalisada.getObservacao());
            propostaRepository.save(proposta);
            PropostaResponseDTO propostaResponseDTO = PropostaMapper.INSTANCE.convertEntityToDto(proposta);
            webSocketService.notificar(propostaResponseDTO);
        }, () -> logger.warn("Proposta {} não encontrada; mensagem de análise ignorada.", propostaAnalisada.getId()));
    }

    private void atualizarProposta(Proposta proposta) {
        propostaRepository.atualizarProposta(proposta.getId(),proposta.getAprovado(),proposta.getObservacao());
    }
}
