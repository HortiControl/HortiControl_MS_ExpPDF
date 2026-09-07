-- Remove duas colunas que ficaram órfãs quando o serviço deixou de armazenar
-- os relatórios gerados.
--
-- arquivo_chave apontava para o arquivo no disco, e arquivo_tipo guardava o
-- MIME type. Hoje o PDF não é armazenado em lugar nenhum: ele espera a
-- retirada em memória e é descartado na entrega. O endereço passou a ser o
-- próprio id da exportação, e o tipo é sempre application/pdf.
--
-- Uma migração nova, e não uma edição da V1: a V1 já foi aplicada, e o Flyway
-- valida o checksum de cada migração aplicada. Alterá-la faria a aplicação
-- recusar-se a subir com "migration checksum mismatch".

ALTER TABLE exportacao_pedidos
    DROP COLUMN arquivo_chave,
    DROP COLUMN arquivo_tipo;
