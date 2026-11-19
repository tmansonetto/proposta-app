package com.tmansonetto.proposta_app.repository;

import com.tmansonetto.proposta_app.dto.PropostaResponseDTO;
import com.tmansonetto.proposta_app.entity.Proposta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaRepository extends CrudRepository<Proposta, Long > {
}
