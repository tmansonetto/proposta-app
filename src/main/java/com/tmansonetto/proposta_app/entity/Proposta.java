package com.tmansonetto.proposta_app.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Entidade JPA que representa uma proposta de crédito.
 *
 * <p>Uma entidade é uma classe Java "espelhada" em uma tabela do banco de dados:
 * cada objeto {@code Proposta} corresponde a uma linha da tabela {@code proposta},
 * e cada atributo corresponde a uma coluna. Quem faz essa tradução (ORM) é o
 * JPA/Hibernate, sem que precisemos escrever SQL.
 *
 * <p>Cada proposta pertence a um {@link Usuario} (relacionamento um-para-um).
 *
 * <p>As anotações do Lombok geram construtores, getters e setters. O construtor sem
 * argumentos ({@code @NoArgsConstructor}) é obrigatório para o JPA, que o usa para
 * instanciar o objeto ao ler os dados do banco.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity //representa uma tabela no banco
public class Proposta {
    /**
     * Identificador único da proposta.
     * {@code @Id} marca a chave primária e {@code GenerationType.IDENTITY}
     * delega ao banco a geração automática do valor (auto incremento).
     */
    @Id //chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Valor de empréstimo solicitado. */
    private Double valorSolicitado;
    /** Prazo de pagamento (quantidade de parcelas/meses). */
    private int prazoPagamento;
    /** Resultado da análise: {@code true} aprovada, {@code false} reprovada, {@code null} ainda não analisada. */
    private Boolean aprovado;
    /**
     * Indica se a proposta já foi enviada com sucesso ao RabbitMQ (controle interno).
     * Quando fica {@code false} (ex.: RabbitMQ fora do ar), o agendador
     * {@code PropostaSemIntegracao} tenta enviá-la novamente mais tarde.
     * Por ser um {@code boolean} primitivo, o Lombok gera o getter como {@code isIntegrada()}.
     */
    private boolean integrada;
    /** Observação/justificativa sobre o resultado da análise. */
    private String observacao;

    /**
     * Usuário (solicitante) dono desta proposta.
     *
     * <ul>
     *   <li>{@code @OneToOne}: uma proposta está ligada a exatamente um usuário;</li>
     *   <li>{@code cascade = CascadeType.PERSIST}: ao salvar uma proposta nova, o usuário
     *       associado também é salvo automaticamente (não é preciso salvá-lo antes);</li>
     *   <li>{@code @JoinColumn(name="id_usuario")}: cria na tabela {@code proposta} a coluna
     *       {@code id_usuario}, que é a chave estrangeira apontando para a tabela {@code usuario}.
     *       Por ter essa coluna, a {@code Proposta} é o lado "dono" do relacionamento.</li>
     * </ul>
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="id_usuario")
    private Usuario usuario;
}
