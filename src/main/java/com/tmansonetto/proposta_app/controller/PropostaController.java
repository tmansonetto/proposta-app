package com.tmansonetto.proposta_app.controller;

import com.tmansonetto.proposta_app.dto.PropostaRequestDTO;
import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.service.PropostaService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/proposta")
public class PropostaController {

    private  PropostaService propostaService ;


    @PostMapping
    public ResponseEntity<PropostaResponseDTO> criarProposta(@RequestBody PropostaRequestDTO propostaRequestDTO) {
        PropostaResponseDTO response = propostaService.criar(propostaRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
