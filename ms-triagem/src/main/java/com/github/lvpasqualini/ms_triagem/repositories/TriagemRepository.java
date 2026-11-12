package com.github.lvpasqualini.ms_triagem.repositories;

import com.github.lvpasqualini.ms_triagem.entities.Triagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    Optional<Triagem> findByProtocolo(String protocolo);

    List<Triagem> findByCpfPaciente(String cpfPaciente);

    boolean existsByProtocolo(String protocolo);
}
