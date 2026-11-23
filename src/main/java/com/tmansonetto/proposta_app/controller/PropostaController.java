package com.tmansonetto.proposta_app.controller;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.service.PropostaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Controller REST responsável pelas propostas.
 *
 * <p>É a "porta de entrada" HTTP da aplicação: recebe as requisições dos clientes
 * (front-end, Postman, outro sistema...), repassa o trabalho para a camada de serviço
 * e devolve a resposta HTTP adequada. O controller NÃO deve conter regra de negócio;
 * ele apenas traduz HTTP &lt;-&gt; Java.
 *
 * <p>Anotações usadas:
 * <ul>
 *   <li>{@code @RestController}: registra a classe como controller e faz com que os retornos
 *       dos métodos sejam convertidos automaticamente em JSON no corpo da resposta;</li>
 *   <li>{@code @RequestMapping("/proposta")}: define o caminho base, ou seja, todos os
 *       endpoints desta classe começam com {@code /proposta};</li>
 *   <li>{@code @AllArgsConstructor} (Lombok): gera um construtor recebendo todos os atributos.
 *       Como a classe tem um único construtor, o Spring o usa para injetar o
 *       {@link PropostaService} automaticamente (injeção de dependência via construtor,
 *       sem precisar de {@code @Autowired}).</li>
 * </ul>
 */
@AllArgsConstructor
@RestController
@RequestMapping("/proposta")
public class PropostaController {

    /** Serviço com a lógica das propostas; injetado pelo Spring através do construtor. */
    private  PropostaService propostaService ;


    /**
     * Cria uma nova proposta.
     *
     * <p>Endpoint: {@code POST /proposta}
     *
     * <p>Funcionamento passo a passo:
     * <ol>
     *   <li>{@code @RequestBody} faz o Spring ler o JSON enviado no corpo da requisição
     *       e convertê-lo em um objeto {@link PropostaRequestDTO};</li>
     *   <li>o DTO é enviado ao serviço, que salva a proposta e devolve um
     *       {@link PropostaResponseDTO} já com o {@code id} gerado pelo banco;</li>
     *   <li>a resposta é montada com status <b>201 Created</b> (o padrão REST para criação) e com
     *       o cabeçalho {@code Location}, que informa a URL do novo recurso.
     *       O {@link ServletUriComponentsBuilder} pega a URL da requisição atual
     *       ({@code http://.../proposta}), acrescenta {@code /{id}} e substitui o
     *       {@code {id}} pelo id gerado, resultando em algo como {@code http://.../proposta/1};</li>
     *   <li>o corpo da resposta contém a proposta criada, em JSON.</li>
     * </ol>
     *
     * @param propostaRequestDTO dados da proposta enviados pelo cliente
     * @return resposta HTTP 201 com a proposta criada e o cabeçalho Location
     */
    @PostMapping
    public ResponseEntity<PropostaResponseDTO> criarProposta(@RequestBody PropostaRequestDTO propostaRequestDTO) {
        PropostaResponseDTO response = propostaService.criar(propostaRequestDTO);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.getId())
                        .toUri()).body(response);

    }

    /**
     * Lista todas as propostas cadastradas.
     *
     * <p>Endpoint: {@code GET /proposta}
     *
     * <p>Pede ao serviço a lista de propostas (já convertidas em DTOs) e a devolve
     * com status <b>200 OK</b>. O Spring converte a {@code List} em um array JSON.
     *
     * @return resposta HTTP 200 com a lista de propostas (pode ser vazia)
     */
    @GetMapping
    public ResponseEntity<List<PropostaResponseDTO>> obterProposta() {
        return ResponseEntity.ok(propostaService.obterProposta());
    }
}
