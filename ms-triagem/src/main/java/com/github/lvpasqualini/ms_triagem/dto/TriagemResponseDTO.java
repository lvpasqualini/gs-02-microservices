package com.github.lvpasqualini.ms_triagem.dto;

import com.github.lvpasqualini.ms_triagem.entities.NivelRisco;
import com.github.lvpasqualini.ms_triagem.entities.StatusTriagem;
import com.github.lvpasqualini.ms_triagem.entities.Triagem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TriagemResponseDTO {

    private Long id;
    private String protocolo;
    private String cpfPaciente;
    private String pressaoArterial;
    private Integer glicemia;
    private Double temperatura;
    private String sintomas;
    private NivelRisco nivelRisco;
    private StatusTriagem statusTriagem;

    public TriagemResponseDTO(Triagem entity) {
        this.id = entity.getId();
        this.protocolo = entity.getProtocolo();
        this.cpfPaciente = entity.getCpfPaciente();
        this.pressaoArterial = entity.getPressaoArterial();
        this.glicemia = entity.getGlicemia();
        this.temperatura = entity.getTemperatura();
        this.sintomas = entity.getSintomas();
        this.nivelRisco = entity.getNivelRisco();
        this.statusTriagem = entity.getStatusTriagem();
    }
}
