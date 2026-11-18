package com.tmansonetto.proposta_app.entity;

import jakarta.persistence.*;


@Entity //representa uma tabela no banco
public class Proposta {
    @Id //chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private Double valorSolicitado;
    private int prazoPagamento;
    private Boolean aprovado;
    private boolean integrada;
    private String observacao;

    @ManyToOne
    @JoinColumn(name="id_usuario")
    private Usuario usuario;
}
