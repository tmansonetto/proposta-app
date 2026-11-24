package com.tmansonetto.proposta_app.config;

import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue criarFilaPropostaPendenteMsAnaliseCredito() {
        return (Queue) QueueBuilder.durable("proposta-pendente.ms-analise-credito").build();
    }
    @Bean
    public Queue criarFilaPropostaPendenteMsNotificacao() {
        return (Queue) QueueBuilder.durable("proposta-pendente.ms-notificacao").build();
    }
    @Bean
    public Queue criarFilaPropostaConcluidaMsProposta() {
        return (Queue) QueueBuilder.durable("proposta-concluida.ms-proposta").build();
    }
    @Bean
    public Queue criarFilaPropostaConcluidaMsNotificacao() {
        return (Queue) QueueBuilder.durable("proposta-concluida.ms-notificacao").build();
    }
}
