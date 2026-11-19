package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.mapper.PropostaMapper;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PropostaService {

    private PropostaRepository propostaRepository;

    private PropostaMapper propostaMapper;

    public PropostaResponseDTO criar(PropostaRequestDTO propostaRequestDTO) {

        Proposta proposta = propostaMapper.convertDtoToProposta(propostaRequestDTO);
        propostaRepository.save(proposta);
        return propostaMapper.convertEntityToDto(proposta);
    }
}
