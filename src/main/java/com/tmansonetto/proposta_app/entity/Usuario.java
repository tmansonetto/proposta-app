package com.tmansonetto.proposta_app.entity;

import jakarta.persistence.*;

@Entity
public class Usuario {
    @Id //chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String nome;
    private String sobrenome;
    private String cpf;
    private String telefone;
    private Double renda;
    @OneToOne(mappedBy = "usuario")
    private Proposta proposta;

}
