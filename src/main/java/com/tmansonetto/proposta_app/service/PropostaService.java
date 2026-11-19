package com.tmansonetto.proposta_app.service;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import com.tmansonetto.proposta_app.repository.PropostaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PropostaService {

    private PropostaRepository propostaRepository;

    public PropostaResponseDTO criar(PropostaRequestDTO propostaRequestDTO) {
        propostaRepository.save(new Proposta());
        return null;
    }
}
