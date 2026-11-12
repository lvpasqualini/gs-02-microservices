package com.github.lvpasqualini.ms_paciente.controller;

import com.github.lvpasqualini.ms_paciente.dto.PacienteRequestDTO;
import com.github.lvpasqualini.ms_paciente.dto.PacienteResponseDTO;
import com.github.lvpasqualini.ms_paciente.dto.StatusPacientePatchDTO;
import com.github.lvpasqualini.ms_paciente.services.PacienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @GetMapping("/port")
    public ResponseEntity<String> getPort(@Value("${local.server.port}") String port) {
        return ResponseEntity.ok("Instância ms-paciente respondeu na porta: " + port);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<PacienteResponseDTO> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.findByCpf(cpf));
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> save(@Valid @RequestBody PacienteRequestDTO requestDTO) {
        PacienteResponseDTO responseDTO = service.save(requestDTO);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(responseDTO.getId())
                .toUri();
        return ResponseEntity.created(uri).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody PacienteRequestDTO requestDTO) {
        return ResponseEntity.ok(service.update(id, requestDTO));
    }

    @RequestMapping(value = "/cpf/{cpf}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<PacienteResponseDTO> updateStatusByCpf(@PathVariable String cpf,
                                                                 @Valid @RequestBody StatusPacientePatchDTO patchDTO) {
        return ResponseEntity.ok(service.updateStatusByCpf(cpf, patchDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PacienteResponseDTO> updateStatusById(@PathVariable Long id,
                                                                @Valid @RequestBody StatusPacientePatchDTO patchDTO) {
        return ResponseEntity.ok(service.updateStatusById(id, patchDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
