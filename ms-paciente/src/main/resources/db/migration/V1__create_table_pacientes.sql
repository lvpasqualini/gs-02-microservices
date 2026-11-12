CREATE TABLE IF NOT EXISTS tb_pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome_completo VARCHAR(100) NOT NULL,
    comunidade VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    status_atendimento VARCHAR(30) NOT NULL
);
