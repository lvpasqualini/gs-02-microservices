package com.github.lvpasqualini.ms_triagem.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TriagemRequestDTO {

    private String protocolo;

    @NotBlank(message = "O CPF do paciente é obrigatório")
    @Size(min = 11, max = 11, message = "O CPF do paciente deve conter 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "O CPF do paciente deve conter apenas 11 dígitos numéricos")
    private String cpfPaciente;

    @NotBlank(message = "A pressão arterial é obrigatória")
    @Pattern(regexp = "\\d{2,3}/\\d{2,3}", message = "A pressão arterial deve estar no formato SISTOLICA/DIASTOLICA (ex: 120/80 ou 180/110)")
    private String pressaoArterial;

    @NotNull(message = "A glicemia é obrigatória")
    @Positive(message = "A glicemia deve ser um valor positivo em mg/dL")
    private Integer glicemia;

    @NotNull(message = "A temperatura é obrigatória")
    @DecimalMin(value = "30.0", message = "A temperatura deve ser maior ou igual a 30.0 °C")
    @DecimalMax(value = "45.0", message = "A temperatura deve ser menor ou igual a 45.0 °C")
    private Double temperatura;

    @NotBlank(message = "Os sintomas são obrigatórios")
    @Size(min = 3, max = 255, message = "A descrição dos sintomas deve ter entre 3 e 255 caracteres")
    private String sintomas;
}
