-- Criação da tabela de usuários
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(20),
    linkedin VARCHAR(255),
    area_atuacao_id INT REFERENCES areas_atuacao(id),
    
    -- Auth
    email_verificado BOOLEAN DEFAULT FALSE,
    token_confirmacao VARCHAR(255),
    token_expira_em TIMESTAMP,
    
    -- OAuth
    provider VARCHAR(20) DEFAULT 'local',
    provider_id VARCHAR(255),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_provider CHECK (provider IN ('local', 'google', 'github', 'linkedin')),
    CONSTRAINT chk_email_ou_oauth CHECK (
        (provider = 'local' AND senha_hash IS NOT NULL) OR
        (provider != 'local' AND provider_id IS NOT NULL)
    )
);

-- Índices para performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider);
CREATE INDEX idx_users_provider_id ON users(provider_id);
CREATE INDEX idx_users_area_atuacao_id ON users(area_atuacao_id);

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_areas_atuacao_updated_at BEFORE UPDATE ON areas_atuacao
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
