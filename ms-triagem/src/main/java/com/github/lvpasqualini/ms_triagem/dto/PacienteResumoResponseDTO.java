package com.github.lvpasqualini.ms_triagem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PacienteResumoResponseDTO {

    private Long id;
    private String cpf;
    private String nomeCompleto;
    private String comunidade;
    private String telefone;
    private String statusAtendimento;
}
