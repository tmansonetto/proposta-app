package com.tmansonetto.proposta_app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) de <b>saída</b> de uma proposta.
 *
 * <p>Representa o JSON que a API devolve ao cliente. Além dos dados enviados na criação
 * (ver {@link PropostaRequestDTO}), contém informações geradas pelo sistema:
 * o {@code id}, o resultado da análise ({@code aprovada}) e uma {@code observacao}.
 *
 * <p>Repare que o campo interno {@code integrada} da entidade {@code Proposta} não aparece aqui:
 * o DTO de saída permite escolher exatamente o que é exposto para fora da aplicação.
 *
 * <p>As anotações do Lombok geram, em tempo de compilação, construtores, getters e setters.
 * Os getters são usados pela biblioteca Jackson para transformar o objeto em JSON.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PropostaResponseDTO {

    /** Identificador único da proposta, gerado pelo banco de dados. */
    private Long id;
    /** Nome do solicitante. */
    private String nome;
    /** Sobrenome do solicitante. */
    private String sobrenome;
    /** Telefone de contato do solicitante. */
    private String telefone;
    /** CPF do solicitante. */
    private String cpf;
    /** Renda mensal do solicitante. */
    private Double renda;
    /**
     * Valor de empréstimo solicitado, já <b>formatado</b> como moeda brasileira (ex.: {@code "R$ 10.000,00"}).
     * É um {@code String} (e não um número) porque serve apenas para exibição no front-end.
     * A formatação é feita no {@code PropostaMapper}.
     */
    private String valorSolicitadoFmt;
    /** Prazo de pagamento (quantidade de parcelas/meses). */
    private int prazoPagamento;
    /** Resultado da análise: {@code true} aprovada, {@code false} reprovada, {@code null} ainda não analisada. */
    private Boolean aprovada;
    /** Observação/justificativa sobre o resultado da análise. */
    private String observacao;
}
