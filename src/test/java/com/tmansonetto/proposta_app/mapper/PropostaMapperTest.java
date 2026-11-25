package com.tmansonetto.proposta_app.mapper;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropostaMapperTest {

    private final PropostaMapper mapper = PropostaMapper.INSTANCE;

    @Test
    void requisicao_valorSolicitadoEnviadoPeloFrontendChegaNaEntidade() {
        PropostaRequestDTO dto = new PropostaRequestDTO();
        dto.setValorSolicitado(15000.50);

        assertThat(mapper.convertDtoToProposta(dto).getValorSolicitado()).isEqualTo(15000.50);
    }

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
