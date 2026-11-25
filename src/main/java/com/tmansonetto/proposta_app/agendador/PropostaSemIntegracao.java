package com.tmansonetto.proposta_app.agendador;

import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import com.tmansonetto.proposta_app.service.NotificacaoRabbitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class PropostaSemIntegracao {

    private PropostaRepository propostaRepository;
    private NotificacaoRabbitService notificacaoRabbitService;
    private String exchage;
    private final Logger logger = LoggerFactory.getLogger(PropostaSemIntegracao.class);

    public PropostaSemIntegracao(PropostaRepository propostaRepository,
                                 NotificacaoRabbitService notificacaoRabbitService,
                                 @Value("${rabbitmq.propostapendente.exchange}") String exchage) {
        this.propostaRepository = propostaRepository;
        this.notificacaoRabbitService = notificacaoRabbitService;
        this.exchage = exchage;
    }

    @Scheduled(fixedDelay = 10,timeUnit = SECONDS)
    public void buscarPropostasSemIntegracao() {
        propostaRepository.findAllByIntegradaIsFalse().forEach(proposta -> {
       try {
           notificacaoRabbitService.notificar(proposta,exchage);
            atualizarProposta(proposta);
       } catch (RuntimeException e) {
           logger.error(e.getMessage());
       }

        });
    }
    private void atualizarProposta(Proposta proposta) {
        proposta.setIntegrada(true);
        propostaRepository.save(proposta);
    }
}
