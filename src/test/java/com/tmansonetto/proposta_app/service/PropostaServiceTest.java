package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.mapper.PropostaMapper;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;

import java.net.ConnectException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    private static final String EXCHANGE = "proposta-pendente.ex";

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private NotificacaoRabbitService notificacaoRabbitService;

    private PropostaService propostaService;

    @BeforeEach
    void setUp() {
        propostaService = new PropostaService(propostaRepository, PropostaMapper.INSTANCE,
                notificacaoRabbitService, EXCHANGE);
    }

    private PropostaRequestDTO requisicao() {
        PropostaRequestDTO dto = new PropostaRequestDTO();
        dto.setNome("Maria");
        dto.setSobrenome("Silva");
        dto.setCpf("12345678900");
        dto.setTelefone("11999999999");
        dto.setRenda(5000.0);
        dto.setValorSolicitadoFmt(10000.0);
        dto.setPrazoPagamento(12);
        return dto;
    }

    private List<Boolean> valoresDeIntegradaSalvos() {
        ArgumentCaptor<Proposta> captor = ArgumentCaptor.forClass(Proposta.class);
        verify(propostaRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream().map(Proposta::isIntegrada).toList();
    }

    @Test
    void rabbitOffline_propostaFicaComIntegradaFalse_eNaoLancaErro() {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(notificacaoRabbitService).notificar(any(), eq(EXCHANGE));

        assertThatCode(() -> propostaService.criar(requisicao())).doesNotThrowAnyException();

        List<Boolean> salvos = valoresDeIntegradaSalvos();
        assertThat(salvos.get(salvos.size() - 1)).isFalse();
        verify(notificacaoRabbitService, times(1)).notificar(any(), eq(EXCHANGE));
    }

    @Test
    void erroGenericoNaIntegracao_propostaFicaComIntegradaFalse() {
        doThrow(new AmqpException("erro ao publicar"))
                .when(notificacaoRabbitService).notificar(any(), eq(EXCHANGE));

        assertThatCode(() -> propostaService.criar(requisicao())).doesNotThrowAnyException();

        List<Boolean> salvos = valoresDeIntegradaSalvos();
        assertThat(salvos.get(salvos.size() - 1)).isFalse();
    }

    @Test
    void rabbitOnline_propostaFicaComIntegradaTrue_eEnviaUmaUnicaVez() {
        propostaService.criar(requisicao());

        List<Boolean> salvos = valoresDeIntegradaSalvos();
        assertThat(salvos.get(salvos.size() - 1)).isTrue();
        verify(notificacaoRabbitService, times(1)).notificar(any(), eq(EXCHANGE));
    }
}
