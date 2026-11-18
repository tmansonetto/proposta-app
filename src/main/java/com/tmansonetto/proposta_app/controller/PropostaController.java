package com.tmansonetto.proposta_app.controller;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/proposta")
public class PropostaController {

    @PostMapping
    public ResponseEntity <PropostaResponseDTO> criarProposta(@RequestBody PropostaRequestDTO propostaRequestDTO) {
        return null;
    }
}
