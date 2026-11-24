package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.mapper.PropostaMapper;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
 * <p>{@code @Service} registra a classe como bean do Spring. {@code @AllArgsConstructor}
 * (Lombok) gera um construtor com todos os atributos, que o Spring usa para injetar
 * automaticamente o repositório e o mapper (injeção de dependência via construtor).
 */

@Service
public class PropostaService {

    /** Acesso ao banco de dados para salvar e consultar propostas. */
    private PropostaRepository propostaRepository;

    /** Conversor entre DTOs e entidades (implementação gerada pelo MapStruct). */
    private PropostaMapper propostaMapper;

    private NotificacaoSerive notificacaoSerive;
    private RabbitTemplate rabbitTemplate;
    

    private String exchage;

    public PropostaService(PropostaRepository propostaRepository, PropostaMapper propostaMapper,
                           NotificacaoSerive notificacaoSerive, RabbitTemplate rabbitTemplate,
                           @Value("${rabbitmq.propostapendente.exchange}") String exchage) {
        this.propostaRepository = propostaRepository;
        this.propostaMapper = propostaMapper;
        this.notificacaoSerive = notificacaoSerive;
        this.rabbitTemplate = rabbitTemplate;
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
     *   <li>converte a entidade (agora com {@code id}) no DTO de resposta.</li>
     * </ol>
     *
     * @param propostaRequestDTO dados da proposta enviados pelo cliente
     * @return a proposta criada, já com o {@code id} gerado
     */
    public PropostaResponseDTO criar(PropostaRequestDTO propostaRequestDTO) {

        Proposta proposta = propostaMapper.convertDtoToProposta(propostaRequestDTO);
        propostaRepository.save(proposta);

        PropostaResponseDTO propostaResponseDTO = PropostaMapper.INSTANCE.convertEntityToDto(proposta);
        notificacaoSerive.notificar(propostaResponseDTO,exchage);

        return propostaResponseDTO;
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
