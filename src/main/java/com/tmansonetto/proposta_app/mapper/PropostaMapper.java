package com.tmansonetto.proposta_app.mapper;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Mapper responsável por converter objetos entre DTOs e entidades.
 *
 * <p>Usa a biblioteca <b>MapStruct</b>: nós apenas declaramos a interface e as regras
 * de conversão; durante a compilação o MapStruct gera automaticamente uma classe
 * ({@code PropostaMapperImpl}) com todo o código de "copiar campo a campo".
 * Isso evita escrever manualmente dezenas de {@code getX()/setX()}.
 *
 * <p>Por padrão, campos com o mesmo nome nos dois lados são copiados sozinhos
 * (ex.: {@code valorSolicitado}, {@code prazoPagamento}). As anotações {@code @Mapping}
 * servem para os casos diferentes: nomes/caminhos distintos ou campos a ignorar.
 *
 * <p>{@code componentModel = "spring"} faz a classe gerada ser um bean do Spring,
 * permitindo que ela seja injetada em outras classes (como no {@code PropostaService}).
 */
@Mapper(componentModel = "spring")
public interface PropostaMapper {

    /**
     * Instância estática do mapper, obtida sem a ajuda do Spring.
     * Permite usar o mapper em qualquer lugar com {@code PropostaMapper.INSTANCE.metodo(...)}.
     * Observação: quando o mapper já é injetado pelo Spring, basta usar a instância injetada.
     */
    PropostaMapper INSTANCE = Mappers.getMapper(PropostaMapper.class);

    /**
     * Converte o DTO de entrada (dados "achatados") em uma entidade {@link Proposta}.
     *
     * <p>Os dados pessoais do DTO ({@code nome}, {@code sobrenome}, {@code cpf},
     * {@code telefone}, {@code renda}) são colocados dentro do objeto {@code usuario}
     * da proposta — o {@code target = "usuario.nome"} indica esse caminho aninhado.
     * O MapStruct cria o {@code Usuario} automaticamente.
     *
     * <p>Os campos marcados com {@code ignore = true} não existem no DTO de entrada e
     * devem ser definidos pelo sistema, não pelo cliente: os {@code id} (gerados pelo banco),
     * {@code aprovado}, {@code integrada}, {@code observacao} e o vínculo inverso
     * {@code usuario.proposta}. Ignorá-los explicitamente evita avisos de "campo não mapeado".
     *
     * @param propostaRequestDTO dados recebidos na requisição
     * @return nova entidade {@code Proposta} (ainda não salva no banco) com seu {@code Usuario}
     */
    @Mapping(target = "usuario.nome", source = "nome")
    @Mapping(target = "usuario.sobrenome", source = "sobrenome")
    @Mapping(target = "usuario.cpf", source = "cpf")
    @Mapping(target = "usuario.telefone", source = "telefone")
    @Mapping(target = "usuario.renda", source = "renda")
    @Mapping(target = "valorSolicitado", source = "valorSolicitadoFmt")
    @Mapping(target = "usuario.id", ignore = true)
    @Mapping(target = "usuario.proposta", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aprovado", ignore = true)
    @Mapping(target = "integrada", constant = "true")
    @Mapping(target = "observacao", ignore = true)
    Proposta convertDtoToProposta(PropostaRequestDTO propostaRequestDTO);

    /**
     * Converte a entidade {@link Proposta} no DTO de saída.
     *
     * <p>É o caminho inverso do método anterior: os dados que estão dentro de
     * {@code proposta.usuario} são "achatados" para o nível principal do DTO
     * ({@code source = "usuario.nome"} → {@code target = "nome"}). Os demais campos
     * de mesmo nome ({@code id}, {@code valorSolicitado}, {@code aprovado}...) são copiados
     * automaticamente.
     *
     * @param proposta entidade vinda do banco de dados
     * @return DTO pronto para ser devolvido ao cliente em JSON
     */
    @Mapping(target = "nome", source = "usuario.nome")
    @Mapping(target = "sobrenome", source = "usuario.sobrenome")
    @Mapping(target = "cpf", source = "usuario.cpf")
    @Mapping(target = "telefone", source = "usuario.telefone")
    @Mapping(target = "renda", source = "usuario.renda")
    @Mapping(target = "valorSolicitado", expression ="java(formatarValorSolicitado(proposta))")
    PropostaResponseDTO convertEntityToDto(Proposta proposta);

    /**
     * Converte uma coleção de entidades em uma lista de DTOs de saída.
     *
     * <p>O MapStruct percorre a coleção e, para cada item, reaproveita
     * o método {@link #convertEntityToDto(Proposta)}. Recebe {@code Iterable}
     * porque é esse o tipo retornado por {@code findAll()} do {@code CrudRepository}.
     *
     * @param propostas propostas vindas do banco
     * @return lista de DTOs (vazia se não houver propostas)
     */
    List<PropostaResponseDTO> convertListEntityToListDto(Iterable<Proposta> propostas);

    default String formatarValorSolicitado(Proposta proposta) {
        if (proposta.getValorSolicitado() == null) {
            return null;
        }
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(proposta.getValorSolicitado());
    }

}
