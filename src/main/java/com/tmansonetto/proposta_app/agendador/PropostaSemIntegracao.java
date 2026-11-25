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

/**
 * Agendador (tarefa automática) que reenvia ao RabbitMQ as propostas que ainda não
 * foram integradas.
 *
 * <p><b>Por que ele existe?</b> Quando uma proposta é criada, o {@code PropostaService}
 * tenta publicá-la no RabbitMQ. Se o RabbitMQ estiver fora do ar nesse momento, a proposta
 * é salva no banco com {@code integrada = false}. Esta classe funciona como uma "rede de
 * segurança": de tempos em tempos ela procura essas propostas pendentes e tenta enviá-las
 * de novo. Esse padrão é uma forma simples de <b>resiliência</b>: uma falha temporária
 * não faz a proposta se perder.
 *
 * <p>{@code @Component} registra a classe como um bean do Spring. Só assim o Spring
 * consegue encontrar o método anotado com {@code @Scheduled} e executá-lo sozinho.
 * (O agendamento só funciona porque a classe principal tem {@code @EnableScheduling}.)
 */
@Component
public class PropostaSemIntegracao {

    /** Acesso ao banco: usado para buscar as propostas pendentes e salvar a atualização. */
    private PropostaRepository propostaRepository;
    /** Serviço que publica a proposta no RabbitMQ e aguarda a confirmação do broker. */
    private NotificacaoRabbitService notificacaoRabbitService;
    /** Nome da exchange do RabbitMQ para onde as propostas pendentes são enviadas. */
    private String exchage;
    /**
     * Logger: registra mensagens no console/arquivo de log. É preferível a {@code System.out.println}
     * porque permite níveis (info, warn, error) e usa marcadores {@code {}} que são substituídos
     * pelos argumentos, na ordem em que aparecem.
     */
    private final Logger logger = LoggerFactory.getLogger(PropostaSemIntegracao.class);

    /**
     * Construtor usado pelo Spring para injetar as dependências.
     *
     * <p>{@code @Value("${rabbitmq.propostapendente.exchange}")} lê o valor dessa chave no
     * arquivo {@code application.properties} (aqui, {@code proposta-pendente.ex}). Assim o nome
     * da exchange não fica "fixo" no código e pode mudar sem recompilar.
     *
     * @param propostaRepository       repositório de propostas
     * @param notificacaoRabbitService serviço de publicação no RabbitMQ
     * @param exchage                  nome da exchange lido da configuração
     */
    public PropostaSemIntegracao(PropostaRepository propostaRepository,
                                 NotificacaoRabbitService notificacaoRabbitService,
                                 @Value("${rabbitmq.propostapendente.exchange}") String exchage) {
        this.propostaRepository = propostaRepository;
        this.notificacaoRabbitService = notificacaoRabbitService;
        this.exchage = exchage;
    }

    /**
     * Busca as propostas com {@code integrada = false} e tenta publicá-las no RabbitMQ.
     *
     * <p>{@code @Scheduled(fixedDelay = 10, timeUnit = SECONDS)} faz o Spring chamar este método
     * automaticamente, esperando <b>10 segundos após o término</b> da execução anterior
     * (diferente de {@code fixedRate}, que conta a partir do início).
     *
     * <p>Para cada proposta pendente:
     * <ol>
     *   <li>tenta enviar ao RabbitMQ;</li>
     *   <li>se der certo, marca como integrada e salva no banco;</li>
     *   <li>se der errado, apenas registra o erro no log. A proposta continua com
     *       {@code integrada = false} e será tentada de novo na próxima execução.</li>
     * </ol>
     * O {@code try/catch} fica <b>dentro</b> do laço de propósito: a falha de uma proposta
     * não impede que as próximas sejam processadas.
     */
    @Scheduled(fixedDelay = 10,timeUnit = SECONDS)
    public void buscarPropostasSemIntegracao() {
        propostaRepository.findAllByIntegradaIsFalse().forEach(proposta -> {
       try {
           notificacaoRabbitService.notificar(proposta,exchage);
            atualizarProposta(proposta);
       } catch (RuntimeException e) {
           logger.error("Falha ao integrar a proposta {}: {}", proposta.getId(), descreverErro(e));
       }

        });
    }

    /**
     * Monta uma descrição legível do erro: a mensagem da exceção seguida da causa raiz,
     * quando houver (ex.: erro de conversão para JSON).
     *
     * <p>Exceções costumam vir "embrulhadas" umas nas outras: uma exceção pode ter uma
     * causa ({@code getCause()}), que pode ter outra causa, e assim por diante. O laço
     * {@code while} desce por essa cadeia até a última, que normalmente explica o problema real.
     * A condição {@code causa.getCause() != causa} evita um laço infinito caso uma exceção
     * aponte para ela mesma.
     *
     * @param e exceção capturada
     * @return texto com a mensagem e, se existir, a causa raiz
     */
    private String descreverErro(RuntimeException e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa == e ? e.getMessage() : e.getMessage() + " (causa: " + causa + ")";
    }

    /**
     * Marca a proposta como integrada e salva a alteração no banco.
     *
     * <p>Como a proposta já possui {@code id}, o {@code save()} faz um UPDATE
     * (e não um INSERT) na tabela.
     *
     * @param proposta proposta que acabou de ser enviada com sucesso
     */
    private void atualizarProposta(Proposta proposta) {
        proposta.setIntegrada(true);
        propostaRepository.save(proposta);
    }
}
