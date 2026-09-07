-- Estado das solicitações de exportação.
--
-- O PDF em si NÃO fica aqui: vai para o armazenamento de arquivos e a tabela
-- guarda apenas a chave necessária para recuperá-lo. Guardar BLOB inflaria os
-- backups e a memória de cada consulta de status, que é o acesso mais frequente.

CREATE TABLE exportacao_pedidos (
    -- VARCHAR e nao CHAR: o Hibernate roda com ddl-auto=validate e compara
    -- codigos JDBC, onde CHAR e VARCHAR sao tipos distintos. CHAR(36) faria
    -- a validacao falhar na subida.
    id                 VARCHAR(36)   NOT NULL,
    solicitante        VARCHAR(255)  NOT NULL,
    status             VARCHAR(20)   NOT NULL,

    -- Filtro solicitado, desmembrado em colunas para permitir consulta futura.
    escopo             VARCHAR(20)   NOT NULL,
    mercado_id         BIGINT        NULL,
    data_inicio        DATE          NULL,
    data_fim           DATE          NULL,
    pedido_ids         VARCHAR(2000) NULL,

    -- Preenchidos somente quando a exportação conclui.
    quantidade_pedidos INT           NULL,
    arquivo_chave      VARCHAR(255)  NULL,
    arquivo_nome       VARCHAR(255)  NULL,
    arquivo_tipo       VARCHAR(100)  NULL,
    arquivo_tamanho    BIGINT        NULL,

    -- Preenchido somente quando falha. Mensagem segura para exibição.
    motivo_falha       VARCHAR(500)  NULL,

    solicitada_em      DATETIME(6)   NOT NULL,
    atualizada_em      DATETIME(6)   NOT NULL,

    CONSTRAINT pk_exportacao_pedidos PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Consulta "minhas exportações mais recentes".
CREATE INDEX idx_exportacao_solicitante
    ON exportacao_pedidos (solicitante, solicitada_em);

-- Apoia limpeza de antigas e diagnóstico de exportações travadas.
CREATE INDEX idx_exportacao_status
    ON exportacao_pedidos (status, solicitada_em);
