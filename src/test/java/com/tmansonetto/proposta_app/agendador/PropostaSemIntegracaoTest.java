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

/**
 * Testes unitários do agendador {@link PropostaSemIntegracao}.
 *
 * <p>Um teste <b>unitário</b> testa uma classe isolada, sem banco de dados e sem RabbitMQ reais.
 * Para isso usamos o <b>Mockito</b>: {@code @Mock} cria objetos "falsos" (mocks) que imitam as
 * dependências. Nós dizemos ao mock como ele deve se comportar ({@code when}/{@code doThrow})
 * e depois conferimos como ele foi usado ({@code verify}).
 *
 * <p>{@code @ExtendWith(MockitoExtension.class)} faz o JUnit inicializar os campos com {@code @Mock}.
 *
 * <p>Os testes seguem a estrutura <i>Arrange / Act / Assert</i>: preparar o cenário,
 * executar o método testado e verificar o resultado.
 */
@ExtendWith(MockitoExtension.class)
class PropostaSemIntegracaoTest {

    /** Repositório falso: nenhum acesso real ao banco acontece. */
    @Mock
    private PropostaRepository propostaRepository;

    /** Serviço de notificação falso: nenhuma mensagem real é enviada ao RabbitMQ. */
    @Mock
    private NotificacaoRabbitService notificacaoRabbitService;

    /** Cria uma proposta de exemplo que ainda não foi integrada. */
    private Proposta pendente() {
        Proposta p = new Proposta();
        p.setId(1L);
        p.setIntegrada(false);
        return p;
    }

    /**
     * Cenário: o RabbitMQ continua fora do ar.
     * Esperado: a proposta continua pendente e nada é salvo no banco.
     */
    @Test
    void rabbitAindaOffline_propostaContinuaComIntegradaFalse() {
        // Arrange: o banco devolve uma proposta pendente e o envio ao RabbitMQ falha
        Proposta proposta = pendente();
        when(propostaRepository.findAllByIntegradaIsFalse()).thenReturn(List.of(proposta));
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(notificacaoRabbitService).notificar(any(), anyString());

        // Act: executa o agendador manualmente (sem esperar os 10 segundos)
        new PropostaSemIntegracao(propostaRepository, notificacaoRabbitService, "proposta-pendente.ex")
                .buscarPropostasSemIntegracao();

        // Assert: a proposta não foi marcada como integrada e o save() nunca foi chamado
        assertThat(proposta.isIntegrada()).isFalse();
        verify(propostaRepository, never()).save(any());
    }

    /**
     * Cenário: o RabbitMQ voltou a funcionar.
     * Esperado: a proposta é marcada como integrada e salva no banco.
     */
    @Test
    void rabbitVoltou_propostaPassaParaIntegradaTrue() {
        // Arrange: por padrão, um mock não lança exceção, então o envio "dá certo"
        Proposta proposta = pendente();
        when(propostaRepository.findAllByIntegradaIsFalse()).thenReturn(List.of(proposta));

        // Act
        new PropostaSemIntegracao(propostaRepository, notificacaoRabbitService, "proposta-pendente.ex")
                .buscarPropostasSemIntegracao();

        // Assert
        assertThat(proposta.isIntegrada()).isTrue();
        verify(propostaRepository).save(proposta);
    }
}
