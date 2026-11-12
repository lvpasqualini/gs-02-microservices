package com.github.lvpasqualini.ms_paciente.services;

import com.github.lvpasqualini.ms_paciente.dto.PacienteRequestDTO;
import com.github.lvpasqualini.ms_paciente.dto.PacienteResponseDTO;
import com.github.lvpasqualini.ms_paciente.dto.StatusPacientePatchDTO;
import com.github.lvpasqualini.ms_paciente.entities.Paciente;
import com.github.lvpasqualini.ms_paciente.entities.StatusAtendimento;
import com.github.lvpasqualini.ms_paciente.exceptions.DatabaseException;
import com.github.lvpasqualini.ms_paciente.exceptions.ResourceNotFoundException;
import com.github.lvpasqualini.ms_paciente.repositories.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository repository;

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(PacienteResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO findById(Long id) {
        Paciente entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Paciente não encontrado com o ID: " + id)
        );
        return new PacienteResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO findByCpf(String cpf) {
        Paciente entity = repository.findByCpf(cpf).orElseThrow(
                () -> new ResourceNotFoundException("Paciente não encontrado com o CPF: " + cpf)
        );
        return new PacienteResponseDTO(entity);
    }

    @Transactional
    public PacienteResponseDTO save(PacienteRequestDTO requestDTO) {
        if (repository.existsByCpf(requestDTO.getCpf())) {
            throw new DatabaseException("Já existe um paciente cadastrado com o CPF: " + requestDTO.getCpf());
        }

        Paciente entity = new Paciente();
        copyDtoToEntity(requestDTO, entity);
        if (entity.getStatusAtendimento() == null) {
            entity.setStatusAtendimento(StatusAtendimento.CADASTRADO);
        }

        entity = repository.save(entity);
        return new PacienteResponseDTO(entity);
    }

    @Transactional
    public PacienteResponseDTO update(Long id, PacienteRequestDTO requestDTO) {
        Paciente entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Paciente não encontrado com o ID: " + id)
        );

        Optional<Paciente> existingByCpf = repository.findByCpf(requestDTO.getCpf());
        if (existingByCpf.isPresent() && !existingByCpf.get().getId().equals(id)) {
            throw new DatabaseException("Já existe outro paciente cadastrado com o CPF: " + requestDTO.getCpf());
        }

        StatusAtendimento currentStatus = entity.getStatusAtendimento();
        copyDtoToEntity(requestDTO, entity);
        if (requestDTO.getStatusAtendimento() == null) {
            entity.setStatusAtendimento(currentStatus);
        }

        entity = repository.save(entity);
        return new PacienteResponseDTO(entity);
    }

    @Transactional
    public PacienteResponseDTO updateStatusByCpf(String cpf, StatusPacientePatchDTO patchDTO) {
        Paciente entity = repository.findByCpf(cpf).orElseThrow(
                () -> new ResourceNotFoundException("Paciente não encontrado com o CPF: " + cpf)
        );
        entity.setStatusAtendimento(patchDTO.getStatusAtendimento());
        entity = repository.save(entity);
        return new PacienteResponseDTO(entity);
    }

    @Transactional
    public PacienteResponseDTO updateStatusById(Long id, StatusPacientePatchDTO patchDTO) {
        Paciente entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Paciente não encontrado com o ID: " + id)
        );
        entity.setStatusAtendimento(patchDTO.getStatusAtendimento());
        entity = repository.save(entity);
        return new PacienteResponseDTO(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente não encontrado com o ID: " + id);
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Violação de integridade referencial ao excluir o paciente ID: " + id);
        }
    }

    private void copyDtoToEntity(PacienteRequestDTO dto, Paciente entity) {
        entity.setCpf(dto.getCpf());
        entity.setNomeCompleto(dto.getNomeCompleto());
        entity.setComunidade(dto.getComunidade());
        entity.setTelefone(dto.getTelefone());
        if (dto.getStatusAtendimento() != null) {
            entity.setStatusAtendimento(dto.getStatusAtendimento());
        }
    }
}
