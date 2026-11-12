package com.github.lvpasqualini.ms_paciente.dto;

import com.github.lvpasqualini.ms_paciente.entities.Paciente;
import com.github.lvpasqualini.ms_paciente.entities.StatusAtendimento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacienteResponseDTO {

    private Long id;
    private String cpf;
    private String nomeCompleto;
    private String comunidade;
    private String telefone;
    private StatusAtendimento statusAtendimento;

    public PacienteResponseDTO(Paciente entity) {
        this.id = entity.getId();
        this.cpf = entity.getCpf();
        this.nomeCompleto = entity.getNomeCompleto();
        this.comunidade = entity.getComunidade();
        this.telefone = entity.getTelefone();
        this.statusAtendimento = entity.getStatusAtendimento();
    }
}
