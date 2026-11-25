package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.mapper.PropostaMapper;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Camada de serviço das propostas.
 *
 * <p>Fica entre o controller (HTTP) e o repositório (banco de dados) e é o lugar
 * da <b>regra de negócio</b>. O fluxo típico é:
 * <pre>
 * Controller --(DTO)--&gt; Service --(Entidade)--&gt; Repository --&gt; Banco
 * Controller &lt;--(DTO)-- Service &lt;--(Entidade)-- Repository &lt;-- Banco
 * </pre>
 * O serviço usa o {@link PropostaMapper} para converter entre DTOs e entidades,
 * de modo que o controller nunca manipula entidades diretamente.
 *
 * <p>Depois de salvar, o serviço também publica a proposta no RabbitMQ, para que outros
 * microsserviços (análise de crédito e notificação) sejam avisados.
 *
 * <p>{@code @Service} registra a classe como bean do Spring. O construtor recebe todas as
 * dependências, e o Spring as injeta automaticamente (injeção de dependência via construtor).
 */

@Service
public class PropostaService {

    /** Acesso ao banco de dados para salvar e consultar propostas. */
    private PropostaRepository propostaRepository;

    /** Conversor entre DTOs e entidades (implementação gerada pelo MapStruct). */
    private PropostaMapper propostaMapper;

    /** Publica as propostas no RabbitMQ. */
    private NotificacaoRabbitService notificacaoRabbitService;

    /** Nome da exchange do RabbitMQ para onde as propostas são enviadas. */
    private String exchage;

    /**
     * Construtor usado pelo Spring para injetar as dependências.
     *
     * <p>Não foi usado o {@code @AllArgsConstructor} do Lombok porque o parâmetro {@code exchage}
     * precisa da anotação {@code @Value}, que lê o nome da exchange do {@code application.properties}.
     *
     * @param propostaRepository       repositório de propostas
     * @param propostaMapper           conversor entre DTOs e entidades
     * @param notificacaoRabbitService serviço de publicação no RabbitMQ
     * @param exchage                  nome da exchange lido da configuração
     */
    public PropostaService(PropostaRepository propostaRepository, PropostaMapper propostaMapper,
                           NotificacaoRabbitService notificacaoRabbitService,
                           @Value("${rabbitmq.propostapendente.exchange}") String exchage) {
        this.propostaRepository = propostaRepository;
        this.propostaMapper = propostaMapper;
        this.notificacaoRabbitService = notificacaoRabbitService;
        this.exchage = exchage;
    }

    /**
     * Cria e salva uma nova proposta.
     *
     * <p>Passo a passo:
     * <ol>
     *   <li>converte o DTO recebido em uma entidade {@link Proposta} (com seu {@code Usuario});</li>
     *   <li>salva no banco com {@code save()}. Graças ao {@code CascadeType.PERSIST},
     *       o usuário é salvo junto. Após salvar, o JPA preenche o {@code id} gerado
     *       no próprio objeto {@code proposta};</li>
     *   <li>envia a proposta ao RabbitMQ (ver o método {@code notificarRabbitMq});</li>
     *   <li>converte a entidade (agora com {@code id}) no DTO de resposta.</li>
     * </ol>
     *
     * @param propostaRequestDTO dados da proposta enviados pelo cliente
     * @return a proposta criada, já com o {@code id} gerado
     */
    public PropostaResponseDTO criar(PropostaRequestDTO propostaRequestDTO) {

        Proposta proposta = propostaMapper.convertDtoToProposta(propostaRequestDTO);
        propostaRepository.save(proposta);

        notificarRabbitMq(proposta);

        return propostaMapper.convertEntityToDto(proposta);
    }

    /**
     * Envia a proposta ao RabbitMQ e registra o resultado no campo {@code integrada}.
     *
     * <p>Se o RabbitMQ estiver fora do ar (ou ocorrer qualquer erro no envio), a proposta
     * fica com {@code integrada = false} e o agendador {@code PropostaSemIntegracao}
     * tenta reenviá-la depois. O erro não é repassado ao cliente.
     *
     * <p>O {@code save()} no final faz um UPDATE, gravando no banco o valor de {@code integrada}.
     *
     * @param proposta proposta já salva (com {@code id}) que será publicada
     */
    private void notificarRabbitMq(Proposta proposta) {
        try {
            notificacaoRabbitService.notificar(proposta, exchage);
            proposta.setIntegrada(true);
        } catch (RuntimeException e) {
            proposta.setIntegrada(false);
        }
        propostaRepository.save(proposta);
    }

    /**
     * Busca todas as propostas cadastradas.
     *
     * <p>{@code findAll()} retorna todas as linhas da tabela como entidades, e o mapper
     * converte essa coleção em uma lista de DTOs de resposta.
     *
     * @return lista com todas as propostas (vazia se não houver nenhuma)
     */
    public List<PropostaResponseDTO> obterProposta() {
        return propostaMapper.convertListEntityToListDto(propostaRepository.findAll());

    }



}
