CREATE TABLE IF NOT EXISTS tb_triagens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    protocolo VARCHAR(40) NOT NULL UNIQUE,
    cpf_paciente VARCHAR(11) NOT NULL,
    pressao_arterial VARCHAR(15) NOT NULL,
    glicemia INT NOT NULL,
    temperatura DOUBLE NOT NULL,
    sintomas VARCHAR(255) NOT NULL,
    nivel_risco VARCHAR(20) NOT NULL,
    status_triagem VARCHAR(40) NOT NULL
);
