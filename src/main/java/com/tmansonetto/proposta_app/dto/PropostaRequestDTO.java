package com.tmansonetto.proposta_app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) de <b>entrada</b> de uma proposta.
 *
 * <p>Representa exatamente o JSON que o cliente envia ao criar uma proposta
 * ({@code POST /proposta}). Usar um DTO, em vez de receber a entidade diretamente,
 * traz vantagens:
 * <ul>
 *   <li>o cliente só consegue enviar os campos permitidos (ele não pode, por exemplo,
 *       definir sozinho se a proposta está "aprovada" ou qual é o "id");</li>
 *   <li>o formato da API fica independente da estrutura das tabelas do banco
 *       (aqui os dados do usuário e da proposta chegam "achatados" em um único objeto,
 *       mas no banco ficam em duas tabelas: {@code Usuario} e {@code Proposta}).</li>
 * </ul>
 *
 * <p>Exemplo de JSON aceito:
 * <pre>
 * {
 *   "nome": "Maria", "sobrenome": "Silva", "telefone": "11999999999",
 *   "cpf": "12345678900", "renda": 5000.0,
 *   "valorSolicitado": 10000.0, "prazoPagamento": 12
 * }
 * </pre>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
/*as notações acima (Lombok) geram os getters, setters e construtores em tempo de compilação.
  O construtor sem argumentos (@NoArgsConstructor) e os setters são usados pela biblioteca
  Jackson para montar este objeto a partir do JSON recebido.*/
public class PropostaRequestDTO {
    /** Nome do solicitante. */
    private String nome;
    /** Sobrenome do solicitante. */
    private String sobrenome;
    /** Telefone de contato do solicitante. */
    private String telefone;
    /** CPF do solicitante. */
    private String cpf;
    /** Renda mensal do solicitante, usada na análise de crédito. */
    private Double renda;
    /** Valor de empréstimo que está sendo solicitado. */
    private Double valorSolicitado;
    /** Prazo de pagamento (quantidade de parcelas/meses). */
    private int prazoPagamento;


}
