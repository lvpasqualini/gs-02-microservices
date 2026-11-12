package com.github.lvpasqualini.ms_paciente.dto;

import com.github.lvpasqualini.ms_paciente.entities.Paciente;
import com.github.lvpasqualini.ms_paciente.entities.StatusAtendimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PacienteRequestDTO {

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 11, message = "O CPF deve conter exatamente 11 dígitos numéricos")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter apenas 11 dígitos numéricos")
    private String cpf;

    @NotBlank(message = "O nome completo é obrigatório")
    @Size(min = 3, max = 100, message = "O nome completo deve ter entre 3 e 100 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "A comunidade é obrigatória")
    @Size(min = 2, max = 100, message = "A comunidade deve ter entre 2 e 100 caracteres")
    private String comunidade;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(min = 8, max = 20, message = "O telefone deve ter entre 8 e 20 caracteres")
    private String telefone;

    private StatusAtendimento statusAtendimento;

    public PacienteRequestDTO(Paciente entity) {
        this.cpf = entity.getCpf();
        this.nomeCompleto = entity.getNomeCompleto();
        this.comunidade = entity.getComunidade();
        this.telefone = entity.getTelefone();
        this.statusAtendimento = entity.getStatusAtendimento();
    }
}
