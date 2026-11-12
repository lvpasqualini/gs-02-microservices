package com.github.lvpasqualini.ms_triagem.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tb_triagens")
public class Triagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "protocolo", nullable = false, unique = true, length = 40)
    private String protocolo;

    @Column(name = "cpf_paciente", nullable = false, length = 11)
    private String cpfPaciente;

    @Column(name = "pressao_arterial", nullable = false, length = 15)
    private String pressaoArterial;

    @Column(name = "glicemia", nullable = false)
    private Integer glicemia;

    @Column(name = "temperatura", nullable = false)
    private Double temperatura;

    @Column(name = "sintomas", nullable = false, length = 255)
    private String sintomas;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false, length = 20)
    private NivelRisco nivelRisco;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_triagem", nullable = false, length = 40)
    private StatusTriagem statusTriagem;
}
