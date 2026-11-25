package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.entity.Proposta;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publica propostas no RabbitMQ e só retorna com sucesso quando o broker
 * confirma o recebimento (publisher confirms).
 *
 * <p>Sem a confirmação, o {@code convertAndSend} não lança exceção em casos como
 * exchange inexistente ou mensagem sem fila de destino: a mensagem se perderia e a
 * proposta seria marcada como integrada. Aqui, qualquer um desses casos lança
 * {@link AmqpException}, e quem chama mantém a proposta com {@code integrada = false}.
 *
 * <p>Analogia: é como enviar uma carta registrada. Não basta colocá-la na caixa do
 * correio; esperamos o "aviso de recebimento" antes de considerar o envio concluído.
 */
@Service
public class NotificacaoRabbitService {

    /** Componente do Spring usado para enviar mensagens ao RabbitMQ. */
    private final RabbitTemplate rabbitTemplate;
    /** Tempo máximo (em milissegundos) que esperamos pela confirmação do broker. */
    private final long timeoutConfirmacaoMs;

    /**
     * Construtor usado pelo Spring para injetar as dependências.
     *
     * <p>Em {@code ${rabbitmq.publisher-confirm.timeout-ms:5000}}, o trecho depois dos dois-pontos
     * é o <b>valor padrão</b>: se a chave não existir no {@code application.properties},
     * serão usados 5000 ms (5 segundos).
     *
     * @param rabbitTemplate       template configurado em {@code RabbitMQConfiguration}
     * @param timeoutConfirmacaoMs tempo máximo de espera pela confirmação
     */
    public NotificacaoRabbitService(RabbitTemplate rabbitTemplate,
                                    @Value("${rabbitmq.publisher-confirm.timeout-ms:5000}") long timeoutConfirmacaoMs) {
        this.rabbitTemplate = rabbitTemplate;
        this.timeoutConfirmacaoMs = timeoutConfirmacaoMs;
    }

    /**
     * Envia a proposta para a exchange informada e espera a confirmação do RabbitMQ.
     *
     * <ol>
     *   <li>cria um {@link CorrelationData} com um identificador único (UUID). Ele funciona como
     *       um "número de protocolo" que liga a mensagem enviada à confirmação que o broker
     *       devolverá depois;</li>
     *   <li>{@code convertAndSend} converte a proposta em JSON e a publica. O segundo argumento
     *       ({@code ""}) é a <i>routing key</i>, vazia porque a exchange fanout não a utiliza;</li>
     *   <li>aguarda a confirmação; se algo der errado, uma exceção é lançada.</li>
     * </ol>
     *
     * @param proposta proposta a ser publicada
     * @param exchange nome da exchange de destino
     * @throws AmqpException se o broker não confirmar, recusar ou não encontrar fila para a mensagem
     */
    public void notificar(Proposta proposta, String exchange) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, "", proposta, correlationData);
        aguardarConfirmacao(proposta, exchange, correlationData);
    }

    /**
     * Espera a resposta do RabbitMQ e verifica se a mensagem foi realmente entregue.
     *
     * <p>O envio é <b>assíncrono</b>: a confirmação chega depois, por meio de um
     * {@code Future} (uma "promessa" de resultado futuro). O método {@code get(timeout)}
     * bloqueia a execução até a resposta chegar ou o tempo limite estourar.
     *
     * <p>São verificados três cenários de falha:
     * <ul>
     *   <li><b>sem resposta</b> (tempo esgotado ou erro): o broker pode estar fora do ar;</li>
     *   <li><b>nack</b> ({@code confirm.ack() == false}): o broker recusou a mensagem,
     *       por exemplo porque a exchange não existe;</li>
     *   <li><b>mensagem devolvida</b> ({@code getReturned() != null}): a exchange existe,
     *       mas nenhuma fila estava ligada a ela, então a mensagem não teria destino.</li>
     * </ul>
     *
     * <p>Sobre o {@code InterruptedException}: quando uma thread é interrompida enquanto espera,
     * a boa prática é restaurar o "sinal de interrupção" com
     * {@code Thread.currentThread().interrupt()}, para que o código acima dela saiba do ocorrido.
     *
     * @param proposta        proposta enviada (usada nas mensagens de erro)
     * @param exchange        exchange de destino (usada nas mensagens de erro)
     * @param correlationData "protocolo" da mensagem, por onde chega a confirmação
     * @throws AmqpException em qualquer um dos cenários de falha descritos acima
     */
    private void aguardarConfirmacao(Proposta proposta, String exchange, CorrelationData correlationData) {
        CorrelationData.Confirm confirm;
        try {
            confirm = correlationData.getFuture().get(timeoutConfirmacaoMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AmqpException("Interrompido aguardando confirmação do RabbitMQ", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new AmqpException("Sem confirmação do RabbitMQ para a proposta " + proposta.getId(), e);
        }

        if (!confirm.ack()) {
            throw new AmqpException("RabbitMQ recusou a proposta " + proposta.getId()
                    + " na exchange '" + exchange + "': " + confirm.reason());
        }

        ReturnedMessage devolvida = correlationData.getReturned();
        if (devolvida != null) {
            throw new AmqpException("Proposta " + proposta.getId() + " não chegou a nenhuma fila da exchange '"
                    + exchange + "': " + devolvida.getReplyText());
        }
    }
}
