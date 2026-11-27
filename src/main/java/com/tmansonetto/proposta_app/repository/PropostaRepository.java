package com.tmansonetto.proposta_app.repository;

import com.tmansonetto.proposta_app.entity.Proposta;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    /**
     * Busca todas as propostas que ainda não foram enviadas ao RabbitMQ.
     *
     * <p>Não escrevemos SQL aqui: o Spring Data lê o <b>nome do método</b> e monta a consulta
     * sozinho (são os chamados <i>query methods</i>). Lendo por partes:
     * {@code findAll} (buscar todas) + {@code By} (onde) + {@code Integrada} (o atributo)
     * + {@code IsFalse} (é falso). O resultado equivale a
     * {@code SELECT * FROM proposta WHERE integrada = false}.
     *
     * @return propostas pendentes de integração (lista vazia se não houver)
     */
    List<Proposta> findAllByIntegradaIsFalse();

    /**
     * Atualiza a aprovação e a observação de uma proposta diretamente no banco.
     *
     * @param id         identificador da proposta
     * @param aprovada   resultado da análise de crédito
     * @param observacao texto explicativo do resultado
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE proposta SET aprovada = :aprovada, observacao = :observacao WHERE id = :id", nativeQuery = true)
    void atualizarProposta(@Param("id") Long id, @Param("aprovada") boolean aprovada, @Param("observacao") String observacao);
}
