package com.tmansonetto.proposta_app.agendador;

import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import com.tmansonetto.proposta_app.service.NotificacaoRabbitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;

import java.net.ConnectException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropostaSemIntegracaoTest {

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private NotificacaoRabbitService notificacaoRabbitService;

    private Proposta pendente() {
        Proposta p = new Proposta();
        p.setId(1L);
        p.setIntegrada(false);
        return p;
    }

    @Test
    void rabbitAindaOffline_propostaContinuaComIntegradaFalse() {
        Proposta proposta = pendente();
        when(propostaRepository.findAllByIntegradaIsFalse()).thenReturn(List.of(proposta));
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(notificacaoRabbitService).notificar(any(), anyString());

        new PropostaSemIntegracao(propostaRepository, notificacaoRabbitService, "proposta-pendente.ex")
                .buscarPropostasSemIntegracao();

        assertThat(proposta.isIntegrada()).isFalse();
        verify(propostaRepository, never()).save(any());
    }

    @Test
    void rabbitVoltou_propostaPassaParaIntegradaTrue() {
        Proposta proposta = pendente();
        when(propostaRepository.findAllByIntegradaIsFalse()).thenReturn(List.of(proposta));

        new PropostaSemIntegracao(propostaRepository, notificacaoRabbitService, "proposta-pendente.ex")
                .buscarPropostasSemIntegracao();

        assertThat(proposta.isIntegrada()).isTrue();
        verify(propostaRepository).save(proposta);
    }
}
