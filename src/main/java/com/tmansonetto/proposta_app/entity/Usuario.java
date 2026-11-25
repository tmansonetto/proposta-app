package com.tmansonetto.proposta_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade JPA que representa o usuário (pessoa) que solicita uma proposta.
 *
 * <p>Cada objeto {@code Usuario} corresponde a uma linha da tabela {@code usuario}
 * no banco de dados. Os dados pessoais ficam aqui, separados dos dados financeiros
 * da {@link Proposta}, o que deixa o modelo mais organizado.
 *
 * <p>As anotações do Lombok geram construtores, getters e setters automaticamente.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Usuario {
    /** Identificador único do usuário, gerado automaticamente pelo banco (auto incremento). */
    @Id //chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Nome do usuário. */
    private String nome;
    /** Sobrenome do usuário. */
    private String sobrenome;
    /** CPF do usuário. */
    private String cpf;
    /** Telefone de contato. */
    private String telefone;
    /** Renda mensal do usuário. */
    private Double renda;

    /**
     * Proposta associada a este usuário (lado inverso do relacionamento um-para-um).
     *
     * <p>{@code mappedBy = "usuario"} informa ao JPA que quem controla o relacionamento
     * é o atributo {@code usuario} da classe {@link Proposta}. Assim, a tabela {@code usuario}
     * NÃO ganha uma coluna extra: a chave estrangeira fica somente em {@code proposta.id_usuario}.
     * Este atributo existe apenas para permitir navegar do usuário até a sua proposta.
     *
     * <p>{@code @JsonIgnore} evita a referência circular Proposta → Usuario → Proposta → ...
     * ao serializar a proposta em JSON para o RabbitMQ.
     */
    @JsonIgnore
    @OneToOne(mappedBy = "usuario")
    private Proposta proposta;

}
