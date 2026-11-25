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
 */
@Service
public class NotificacaoRabbitService {

    private final RabbitTemplate rabbitTemplate;
    private final long timeoutConfirmacaoMs;

    public NotificacaoRabbitService(RabbitTemplate rabbitTemplate,
                                    @Value("${rabbitmq.publisher-confirm.timeout-ms:5000}") long timeoutConfirmacaoMs) {
        this.rabbitTemplate = rabbitTemplate;
        this.timeoutConfirmacaoMs = timeoutConfirmacaoMs;
    }

    public void notificar(Proposta proposta, String exchange) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, "", proposta, correlationData);
        aguardarConfirmacao(proposta, exchange, correlationData);
    }

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
