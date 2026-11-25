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

/**
 * Testes unitários do {@link PropostaService}.
 *
 * <p>O repositório e o serviço de notificação são <b>mocks</b> (objetos falsos criados pelo
 * Mockito), então os testes rodam sem banco de dados e sem RabbitMQ. O mapper, por outro lado,
 * é o real ({@code PropostaMapper.INSTANCE}), pois ele não depende de nada externo.
 *
 * <p>O objetivo é verificar a regra de resiliência: se o envio ao RabbitMQ falhar, a proposta
 * deve ficar salva com {@code integrada = false}, sem devolver erro ao cliente.
 */
@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    /** Nome de exchange usado em todos os testes. */
    private static final String EXCHANGE = "proposta-pendente.ex";

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private NotificacaoRabbitService notificacaoRabbitService;

    /** Classe sendo testada, criada manualmente com as dependências falsas. */
    private PropostaService propostaService;

    /** {@code @BeforeEach}: executado antes de cada teste, garantindo um serviço "limpo" para cada um. */
    @BeforeEach
    void setUp() {
        propostaService = new PropostaService(propostaRepository, PropostaMapper.INSTANCE,
                notificacaoRabbitService, EXCHANGE);
    }

    /** Monta uma requisição de exemplo com todos os campos preenchidos. */
    private PropostaRequestDTO requisicao() {
        PropostaRequestDTO dto = new PropostaRequestDTO();
        dto.setNome("Maria");
        dto.setSobrenome("Silva");
        dto.setCpf("12345678900");
        dto.setTelefone("11999999999");
        dto.setRenda(5000.0);
        dto.setValorSolicitado(10000.0);
        dto.setPrazoPagamento(12);
        return dto;
    }

    /**
     * Retorna o valor de {@code integrada} de cada proposta passada ao {@code save()}, na ordem.
     *
     * <p>O {@link ArgumentCaptor} "captura" os argumentos recebidos pelo mock, permitindo
     * inspecioná-los depois. Como o serviço chama {@code save()} mais de uma vez, os testes
     * olham o <b>último</b> valor salvo, que é o estado final da proposta.
     */
    private List<Boolean> valoresDeIntegradaSalvos() {
        ArgumentCaptor<Proposta> captor = ArgumentCaptor.forClass(Proposta.class);
        verify(propostaRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream().map(Proposta::isIntegrada).toList();
    }

    /**
     * Cenário: RabbitMQ fora do ar (conexão recusada).
     * Esperado: nenhuma exceção chega ao cliente, a proposta fica com {@code integrada = false}
     * e houve exatamente uma tentativa de envio.
     */
    @Test
    void rabbitOffline_propostaFicaComIntegradaFalse_eNaoLancaErro() {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(notificacaoRabbitService).notificar(any(), eq(EXCHANGE));

        assertThatCode(() -> propostaService.criar(requisicao())).doesNotThrowAnyException();

        List<Boolean> salvos = valoresDeIntegradaSalvos();
        assertThat(salvos.get(salvos.size() - 1)).isFalse();
        verify(notificacaoRabbitService, times(1)).notificar(any(), eq(EXCHANGE));
    }

    /**
     * Cenário: outro erro qualquer ao publicar (ex.: broker recusou a mensagem).
     * Esperado: o mesmo comportamento do RabbitMQ offline.
     */
    @Test
    void erroGenericoNaIntegracao_propostaFicaComIntegradaFalse() {
        doThrow(new AmqpException("erro ao publicar"))
                .when(notificacaoRabbitService).notificar(any(), eq(EXCHANGE));

        assertThatCode(() -> propostaService.criar(requisicao())).doesNotThrowAnyException();

        List<Boolean> salvos = valoresDeIntegradaSalvos();
        assertThat(salvos.get(salvos.size() - 1)).isFalse();
    }

    /**
     * Cenário: RabbitMQ funcionando (o mock não lança exceção).
     * Esperado: a proposta fica com {@code integrada = true} e é enviada uma única vez.
     */
    @Test
    void rabbitOnline_propostaFicaComIntegradaTrue_eEnviaUmaUnicaVez() {
        propostaService.criar(requisicao());

        List<Boolean> salvos = valoresDeIntegradaSalvos();
        assertThat(salvos.get(salvos.size() - 1)).isTrue();
        verify(notificacaoRabbitService, times(1)).notificar(any(), eq(EXCHANGE));
    }
}
