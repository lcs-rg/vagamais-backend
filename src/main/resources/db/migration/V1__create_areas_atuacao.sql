-- Criação da tabela de áreas de atuação
CREATE TABLE areas_atuacao (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    ordem INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_areas_atuacao_slug ON areas_atuacao(slug);
CREATE INDEX idx_areas_atuacao_ativo ON areas_atuacao(ativo);

-- Inserção de áreas iniciais
INSERT INTO areas_atuacao (nome, slug, descricao, ordem) VALUES
('Tecnologia da Informação', 'tecnologia-informacao', 'Desenvolvimento, infraestrutura, dados, IA', 1),
('Marketing Digital', 'marketing-digital', 'SEO, redes sociais, conteúdo, performance', 2),
('Vendas e Comercial', 'vendas-comercial', 'Vendas B2B/B2C, inside sales, account management', 3),
('Financeiro e Administração', 'financeiro-administracao', 'Contabilidade, controladoria, administrativo', 4),
('Recursos Humanos', 'recursos-humanos', 'Talent acquisition, T&D, DP, cultura', 5),
('Engenharia', 'engenharia', 'Civil, mecânica, elétrica, produção', 6),
('Design e UX', 'design-ux', 'UI/UX, design gráfico, produto', 7),
('Jurídico', 'juridico', 'Direito trabalhista, contratual, compliance', 8),
('Saúde', 'saude', 'Médicos, enfermeiros, farmácia, biomedicina', 9),
('Educação', 'educacao', 'Professores, pedagogia, treinamento', 10),
('Logística e Supply Chain', 'logistica', 'Transporte, armazenagem, cadeia de suprimentos', 11),
('Atendimento ao Cliente', 'atendimento-cliente', 'Suporte, SAC, customer success', 12),
('Outro', 'outro', 'Outras áreas não listadas', 99);
