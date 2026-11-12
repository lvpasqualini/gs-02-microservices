package com.github.lvpasqualini.ms_triagem.client;

import com.github.lvpasqualini.ms_triagem.dto.PacienteResumoResponseDTO;
import com.github.lvpasqualini.ms_triagem.dto.StatusPacientePatchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("ms-paciente")
public interface PacienteClient {

    @PatchMapping("/api/v1/pacientes/cpf/{cpf}/status")
    PacienteResumoResponseDTO atualizarStatusPaciente(@PathVariable("cpf") String cpf,
                                                      @RequestBody StatusPacientePatchDTO patchDTO);
}
