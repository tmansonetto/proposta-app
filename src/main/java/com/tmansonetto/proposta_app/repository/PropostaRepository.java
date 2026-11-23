package com.tmansonetto.proposta_app.repository;

import com.tmansonetto.proposta_app.entity.Proposta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório de acesso ao banco de dados para a entidade {@link Proposta}.
 *
 * <p>É apenas uma interface, sem nenhuma implementação escrita por nós: ao estender
 * {@link CrudRepository}, o <b>Spring Data JPA</b> cria em tempo de execução uma classe
 * que já oferece as operações básicas de CRUD (Create, Read, Update, Delete), como
 * {@code save()}, {@code findById()}, {@code findAll()}, {@code deleteById()} e {@code count()}.
 *
 * <p>Os tipos genéricos {@code <Proposta, Long>} indicam, respectivamente,
 * a entidade gerenciada e o tipo da sua chave primária ({@code id}).
 *
 * <p>{@code @Repository} marca a interface como componente da camada de persistência,
 * tornando-a injetável em outras classes (ex.: no {@code PropostaService}).
 */
@Repository
public interface PropostaRepository extends CrudRepository<Proposta, Long > {
}
