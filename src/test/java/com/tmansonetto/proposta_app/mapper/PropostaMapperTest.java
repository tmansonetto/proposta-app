package com.tmansonetto.proposta_app.mapper;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários do {@link PropostaMapper}.
 *
 * <p>Não precisamos do Spring aqui: {@code PropostaMapper.INSTANCE} já entrega a classe
 * gerada pelo MapStruct, então o teste roda rápido e sem banco de dados.
 */
class PropostaMapperTest {

    /** Instância do mapper gerada pelo MapStruct (obtida sem o Spring). */
    private final PropostaMapper mapper = PropostaMapper.INSTANCE;

    /** Garante que o valor enviado pelo front-end é copiado do DTO para a entidade. */
    @Test
    void requisicao_valorSolicitadoEnviadoPeloFrontendChegaNaEntidade() {
        PropostaRequestDTO dto = new PropostaRequestDTO();
        dto.setValorSolicitado(15000.50);

        assertThat(mapper.convertDtoToProposta(dto).getValorSolicitado()).isEqualTo(15000.50);
    }

    /**
     * Garante que a resposta traz o valor formatado em reais e que {@code aprovado}
     * (entidade) é copiado para {@code aprovada} (DTO) nos três casos possíveis:
     * {@code true}, {@code false} e {@code null}.
     *
     * <p>O {@code replace} na comparação existe porque o Java formata moeda com um
     * <i>espaço não separável</i> (caractere invisível diferente do espaço comum) entre
     * {@code R$} e o número.
     */
    @Test
    void resposta_trazValorFormatadoEAprovadaNoFormatoDoFrontend() {
        Proposta proposta = new Proposta();
        proposta.setValorSolicitado(15000.50);
        proposta.setUsuario(new Usuario());

        proposta.setAprovado(true);
        PropostaResponseDTO dto = mapper.convertEntityToDto(proposta);
        assertThat(dto.getValorSolicitadoFmt()).isEqualTo("R$ 15.000,50".replace(' ', ' '));
        assertThat(dto.getAprovada()).isTrue();

        proposta.setAprovado(false);
        assertThat(mapper.convertEntityToDto(proposta).getAprovada()).isFalse();

        proposta.setAprovado(null);
        assertThat(mapper.convertEntityToDto(proposta).getAprovada()).isNull();
    }
}
