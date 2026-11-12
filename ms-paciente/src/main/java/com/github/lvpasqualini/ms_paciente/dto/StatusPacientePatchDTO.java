package com.github.lvpasqualini.ms_paciente.dto;

import com.github.lvpasqualini.ms_paciente.entities.StatusAtendimento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatusPacientePatchDTO {

    @NotNull(message = "O status de atendimento é obrigatório")
    private StatusAtendimento statusAtendimento;
}
