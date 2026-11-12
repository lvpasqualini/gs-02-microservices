package com.github.lvpasqualini.ms_triagem.services;

import com.github.lvpasqualini.ms_triagem.client.PacienteClient;
import com.github.lvpasqualini.ms_triagem.dto.StatusPacientePatchDTO;
import com.github.lvpasqualini.ms_triagem.dto.TriagemRequestDTO;
import com.github.lvpasqualini.ms_triagem.dto.TriagemResponseDTO;
import com.github.lvpasqualini.ms_triagem.entities.NivelRisco;
import com.github.lvpasqualini.ms_triagem.entities.StatusTriagem;
import com.github.lvpasqualini.ms_triagem.entities.Triagem;
import com.github.lvpasqualini.ms_triagem.exceptions.DatabaseException;
import com.github.lvpasqualini.ms_triagem.exceptions.ResourceNotFoundException;
import com.github.lvpasqualini.ms_triagem.repositories.TriagemRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TriagemService {

    private static final List<String> PALAVRAS_CHAVE_URGENTE = List.of(
            "falta de ar", "dispneia", "dor no peito", "dor toracica",
            "desmaio", "convulsao", "hemorragia", "inconsciente", "infarto", "avc"
    );

    private static final List<String> PALAVRAS_CHAVE_MODERADO = List.of(
            "febre", "vomito", "tontura", "diarreia", "dor intensa", "infeccao", "calafrio"
    );

    @Autowired
    private TriagemRepository repository;

    @Autowired
    private PacienteClient pacienteClient;

    @Transactional(readOnly = true)
    public List<TriagemResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(TriagemResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TriagemResponseDTO findById(Long id) {
        Triagem entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Triagem não encontrada com o ID: " + id)
        );
        return new TriagemResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<TriagemResponseDTO> findByCpfPaciente(String cpfPaciente) {
        return repository.findByCpfPaciente(cpfPaciente)
                .stream()
                .map(TriagemResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public TriagemResponseDTO save(TriagemRequestDTO requestDTO) {
        String protocolo = resolverProtocolo(requestDTO.getProtocolo());
        if (repository.existsByProtocolo(protocolo)) {
            throw new DatabaseException("Já existe uma triagem registrada com o protocolo: " + protocolo);
        }

        Triagem entity = new Triagem();
        copyDtoToEntity(requestDTO, entity);
        entity.setProtocolo(protocolo);

        NivelRisco nivelRisco = calcularNivelRisco(
                requestDTO.getPressaoArterial(),
                requestDTO.getGlicemia(),
                requestDTO.getTemperatura(),
                requestDTO.getSintomas()
        );
        entity.setNivelRisco(nivelRisco);
        entity.setStatusTriagem(StatusTriagem.SINCRONIZADA_COM_PACIENTE);
        entity = repository.save(entity);

        sincronizarStatusComPacienteService(entity);

        return new TriagemResponseDTO(entity);
    }

    @Transactional
    public TriagemResponseDTO salvarEmContingencia(TriagemRequestDTO requestDTO) {
        String protocolo = resolverProtocolo(requestDTO.getProtocolo());
        Triagem entity = repository.findByProtocolo(protocolo).orElseGet(Triagem::new);

        copyDtoToEntity(requestDTO, entity);
        entity.setProtocolo(protocolo);
        entity.setNivelRisco(calcularNivelRisco(
                requestDTO.getPressaoArterial(),
                requestDTO.getGlicemia(),
                requestDTO.getTemperatura(),
                requestDTO.getSintomas()
        ));
        entity.setStatusTriagem(StatusTriagem.PENDENTE_SINCRONIZACAO);

        entity = repository.save(entity);
        return new TriagemResponseDTO(entity);
    }

    @Transactional
    public TriagemResponseDTO update(Long id, TriagemRequestDTO requestDTO) {
        Triagem entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Triagem não encontrada com o ID: " + id)
        );

        copyDtoToEntity(requestDTO, entity);
        if (requestDTO.getProtocolo() != null && !requestDTO.getProtocolo().isBlank()) {
            entity.setProtocolo(requestDTO.getProtocolo().trim());
        }

        NivelRisco nivelRisco = calcularNivelRisco(
                requestDTO.getPressaoArterial(),
                requestDTO.getGlicemia(),
                requestDTO.getTemperatura(),
                requestDTO.getSintomas()
        );
        entity.setNivelRisco(nivelRisco);
        entity.setStatusTriagem(StatusTriagem.SINCRONIZADA_COM_PACIENTE);
        entity = repository.save(entity);

        sincronizarStatusComPacienteService(entity);

        return new TriagemResponseDTO(entity);
    }

    @Transactional
    public TriagemResponseDTO sincronizarComPaciente(Long id) {
        Triagem entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Triagem não encontrada com o ID: " + id)
        );

        sincronizarStatusComPacienteService(entity);

        entity.setStatusTriagem(StatusTriagem.SINCRONIZADA_COM_PACIENTE);
        entity = repository.save(entity);
        return new TriagemResponseDTO(entity);
    }

    @Transactional
    public TriagemResponseDTO marcarPendenteSincronizacao(Long id) {
        Triagem entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Triagem não encontrada com o ID: " + id)
        );
        entity.setStatusTriagem(StatusTriagem.PENDENTE_SINCRONIZACAO);
        entity = repository.save(entity);
        return new TriagemResponseDTO(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Triagem não encontrada com o ID: " + id);
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Violação de integridade ao excluir a triagem ID: " + id);
        }
    }

    public NivelRisco calcularNivelRisco(String pressaoArterial, Integer glicemia, Double temperatura, String sintomas) {
        int sistolica = 120;
        int diastolica = 80;

        if (pressaoArterial != null && pressaoArterial.contains("/")) {
            String[] partes = pressaoArterial.trim().split("/");
            try {
                sistolica = Integer.parseInt(partes[0].trim());
                diastolica = Integer.parseInt(partes[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String sintomasNormalizados = normalizarTexto(sintomas);

        boolean riscoUrgentePressao = sistolica >= 180 || sistolica < 90 || diastolica >= 110 || diastolica < 60;
        boolean riscoUrgenteGlicemia = glicemia != null && (glicemia >= 250 || glicemia < 70);
        boolean riscoUrgenteTemperatura = temperatura != null && (temperatura >= 39.0 || temperatura < 35.0);
        boolean riscoUrgenteSintomas = PALAVRAS_CHAVE_URGENTE.stream().anyMatch(sintomasNormalizados::contains);

        if (riscoUrgentePressao || riscoUrgenteGlicemia || riscoUrgenteTemperatura || riscoUrgenteSintomas) {
            return NivelRisco.URGENTE;
        }

        boolean riscoModeradoPressao = sistolica >= 140 || diastolica >= 90;
        boolean riscoModeradoGlicemia = glicemia != null && glicemia >= 140;
        boolean riscoModeradoTemperatura = temperatura != null && temperatura >= 37.8;
        boolean riscoModeradoSintomas = PALAVRAS_CHAVE_MODERADO.stream().anyMatch(sintomasNormalizados::contains);

        if (riscoModeradoPressao || riscoModeradoGlicemia || riscoModeradoTemperatura || riscoModeradoSintomas) {
            return NivelRisco.MODERADO;
        }

        return NivelRisco.BAIXO;
    }

    private void sincronizarStatusComPacienteService(Triagem entity) {
        String statusDestino = mapearStatusPacientePorRisco(entity.getNivelRisco());
        StatusPacientePatchDTO patchDTO = new StatusPacientePatchDTO(statusDestino);

        try {
            pacienteClient.atualizarStatusPaciente(entity.getCpfPaciente(), patchDTO);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Paciente não encontrado com o CPF: " + entity.getCpfPaciente());
        } catch (FeignException e) {
            throw new RuntimeException("Falha ao comunicar com ms-paciente", e);
        }
    }

    private String mapearStatusPacientePorRisco(NivelRisco nivelRisco) {
        if (nivelRisco == NivelRisco.URGENTE) {
            return "ATENDIMENTO_URGENTE";
        }
        return "EM_TRIAGEM";
    }

    private String resolverProtocolo(String protocoloInformado) {
        if (protocoloInformado != null && !protocoloInformado.isBlank()) {
            return protocoloInformado.trim();
        }
        return "TRI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void copyDtoToEntity(TriagemRequestDTO dto, Triagem entity) {
        entity.setCpfPaciente(dto.getCpfPaciente());
        entity.setPressaoArterial(dto.getPressaoArterial());
        entity.setGlicemia(dto.getGlicemia());
        entity.setTemperatura(dto.getTemperatura());
        entity.setSintomas(dto.getSintomas());
    }
}
