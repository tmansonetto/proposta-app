package com.tmansonetto.proposta_app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ (filas, exchange, ligações e envio de mensagens).
 *
 * <p><b>Conceitos básicos do RabbitMQ:</b>
 * <ul>
 *   <li><b>Fila (Queue)</b>: "caixa de correio" onde as mensagens ficam guardadas até que
 *       um consumidor (outro microsserviço) as leia;</li>
 *   <li><b>Exchange</b>: "agência dos correios". O produtor (esta aplicação) nunca envia
 *       direto para a fila: envia para a exchange, que decide para quais filas a mensagem vai;</li>
 *   <li><b>Binding</b>: a "ligação" que diz à exchange para quais filas ela deve entregar.</li>
 * </ul>
 *
 * <p>Fluxo desta aplicação:
 * <pre>
 *                                   ┌─&gt; proposta-pendente.ms-analise-credito
 * proposta-app ─&gt; proposta-pendente.ex (fanout)
 *                                   └─&gt; proposta-pendente.ms-notificacao
 * </pre>
 *
 * <p>Cada método anotado com {@code @Bean} cria um objeto que passa a ser gerenciado pelo
 * Spring. {@code @Configuration} indica que esta classe é uma fonte desses beans.
 */
@Configuration
public class RabbitMQConfiguration {

    /** Nome da exchange de propostas pendentes, lido do {@code application.properties}. */
    @Value("${rabbitmq.propostapendente.exchange}")
    private String exchagePropostaPendente;

    @Value("${rabbitmq.propostaconcluida.exchange}")
    private String exchagePropostaConcluida;


    /**
     * Fila de propostas pendentes consumida pelo microsserviço de análise de crédito.
     * {@code durable} significa que a fila sobrevive a uma reinicialização do RabbitMQ.
     */
    @Bean
    public Queue criarFilaPropostaPendenteMsAnaliseCredito() {
        return QueueBuilder.durable("proposta-pendente.ms-analise-credito").build();
    }

    /** Fila de propostas pendentes consumida pelo microsserviço de notificação. */
    @Bean
    public Queue criarFilaPropostaPendenteMsNotificacao() {
        return QueueBuilder.durable("proposta-pendente.ms-notificacao").build();
    }

    /**
     * Fila de propostas já analisadas (concluídas), que esta aplicação poderá consumir
     * para atualizar o resultado da análise.
     */
    @Bean
    public Queue criarFilaPropostaConcluidaMsProposta() {
        return QueueBuilder.durable("proposta-concluida.ms-proposta").build();
    }

    /** Fila de propostas concluídas consumida pelo microsserviço de notificação. */
    @Bean
    public Queue criarFilaPropostaConcluidaMsNotificacao() {
        return QueueBuilder.durable("proposta-concluida.ms-notificacao").build();
    }

    /**
     * Cria o {@link RabbitAdmin}, componente que declara no RabbitMQ as filas, exchanges e
     * bindings definidos como beans nesta classe.
     *
     * @param connectionFactory conexão com o RabbitMQ, criada automaticamente pelo Spring Boot
     *                          a partir das propriedades {@code spring.rabbitmq.*}
     */
    // Cria as filas acima no RabbitMQ
    @Bean
    public RabbitAdmin criarRabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Força a criação das filas assim que a aplicação termina de subir.
     *
     * <p>Por padrão o {@code RabbitAdmin} só declara as filas na primeira vez que uma conexão
     * é aberta. Este listener "escuta" o evento {@link ApplicationReadyEvent} (aplicação pronta)
     * e chama {@code initialize()}, garantindo que as filas existam desde o início.
     * A expressão {@code event -> ...} é uma <i>lambda</i>: uma forma curta de implementar
     * uma interface que tem um único método.
     */
    // Força a criação das filas assim que a aplicação sobe
    @Bean
    public ApplicationListener<ApplicationReadyEvent> inicializarAdmin(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

    /**
     * Cria a exchange do tipo <b>fanout</b> para propostas pendentes.
     *
     * <p>Uma exchange fanout é como um "megafone": entrega uma cópia de cada mensagem para
     * <b>todas</b> as filas ligadas a ela, ignorando a routing key. Assim, uma única publicação
     * chega tanto à análise de crédito quanto à notificação.
     */
    @Bean
    public FanoutExchange criarFanoutExchangePropostaPendente() {
        return ExchangeBuilder.fanoutExchange("proposta-pendente.ex").build();
    }

    @Bean
    public FanoutExchange criarFanoutExchangePropostaConcluida() {
        return ExchangeBuilder.fanoutExchange("proposta-concluida.ex").build();
    }

    /** Liga a fila da análise de crédito à exchange de propostas pendentes. */
    @Bean
    public Binding criarBindingPropostaPendenteMsAnaliseCredito(){
        return BindingBuilder.bind(criarFilaPropostaPendenteMsAnaliseCredito()).to(criarFanoutExchangePropostaPendente());
    }

    /** Liga a fila de notificação à exchange de propostas pendentes. */
    @Bean
    public Binding criarBindingPropostaPendenteMsNotificacao(){
        return BindingBuilder.bind(criarFilaPropostaPendenteMsNotificacao()).to(criarFanoutExchangePropostaPendente());
    }

    @Bean
    public Binding criarBindingPropostaConcluidaMsPropostaApp(){
        return BindingBuilder.bind(criarFilaPropostaConcluidaMsProposta()).to(criarFanoutExchangePropostaConcluida());
    }

    @Bean
    public Binding criarBindingPropostaConcluidaMsNotificacao(){
        return BindingBuilder.bind(criarFilaPropostaConcluidaMsNotificacao()).to(criarFanoutExchangePropostaConcluida());
    }

    /**
     * Conversor que transforma objetos Java em JSON (e vice-versa) usando a biblioteca Jackson.
     * Sem ele, o RabbitMQ receberia o objeto serializado no formato binário do Java,
     * que outros sistemas (em outras linguagens) não conseguiriam ler.
     */
    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Cria o {@link RabbitTemplate}, a classe usada para <b>enviar</b> mensagens ao RabbitMQ
     * (parecido com o que o {@code JdbcTemplate} faz para o banco de dados).
     *
     * @param connectionFactory conexão com o RabbitMQ
     * @param messageConverter  conversor para JSON definido acima
     * @return template configurado para enviar mensagens em JSON
     */
    @Bean
    public RabbitTemplate criarRabbitTemplate(ConnectionFactory connectionFactory,
                                              JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        // Mensagens que não chegam a nenhuma fila são devolvidas pelo broker (publisher returns)
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

}
