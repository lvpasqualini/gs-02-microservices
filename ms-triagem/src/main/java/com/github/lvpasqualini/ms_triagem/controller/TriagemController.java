package com.github.lvpasqualini.ms_triagem.controller;

import com.github.lvpasqualini.ms_triagem.dto.TriagemRequestDTO;
import com.github.lvpasqualini.ms_triagem.dto.TriagemResponseDTO;
import com.github.lvpasqualini.ms_triagem.services.TriagemService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/triagens")
public class TriagemController {

    @Autowired
    private TriagemService service;

    @GetMapping("/port")
    public ResponseEntity<String> getPort(@Value("${local.server.port}") String port) {
        return ResponseEntity.ok("Instância ms-triagem respondeu na porta: " + port);
    }

    @GetMapping
    public ResponseEntity<List<TriagemResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TriagemResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/paciente/{cpf}")
    public ResponseEntity<List<TriagemResponseDTO>> findByCpfPaciente(@PathVariable String cpf) {
        return ResponseEntity.ok(service.findByCpfPaciente(cpf));
    }

    @PostMapping
    @CircuitBreaker(name = "atualizarStatusPaciente", fallbackMethod = "fallbackCriarTriagemPendente")
    public ResponseEntity<TriagemResponseDTO> save(@Valid @RequestBody TriagemRequestDTO requestDTO) {
        TriagemResponseDTO responseDTO = service.save(requestDTO);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(responseDTO.getId())
                .toUri();
        return ResponseEntity.created(uri).body(responseDTO);
    }

    public ResponseEntity<TriagemResponseDTO> fallbackCriarTriagemPendente(TriagemRequestDTO requestDTO, Throwable e) {
        if (e instanceof com.github.lvpasqualini.ms_triagem.exceptions.ResourceNotFoundException ex) {
            throw ex;
        }
        if (e instanceof com.github.lvpasqualini.ms_triagem.exceptions.DatabaseException ex) {
            throw ex;
        }
        TriagemResponseDTO dto = service.salvarEmContingencia(requestDTO);
        return ResponseEntity.status(503).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TriagemResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody TriagemRequestDTO requestDTO) {
        return ResponseEntity.ok(service.update(id, requestDTO));
    }

    @PatchMapping("/{id}/sincronizar")
    @CircuitBreaker(name = "atualizarStatusPaciente", fallbackMethod = "fallbackSincronizarPaciente")
    public ResponseEntity<TriagemResponseDTO> sincronizarComPaciente(@PathVariable Long id) {
        TriagemResponseDTO dto = service.sincronizarComPaciente(id);
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<TriagemResponseDTO> fallbackSincronizarPaciente(Long id, Throwable e) {
        if (e instanceof com.github.lvpasqualini.ms_triagem.exceptions.ResourceNotFoundException ex) {
            throw ex;
        }
        TriagemResponseDTO dto = service.marcarPendenteSincronizacao(id);
        return ResponseEntity.status(503).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
