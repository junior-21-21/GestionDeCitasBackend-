-- PetyZoos - reparacion segura del esquema de lotes para SQL Server.
-- Objetivo: alinear la tabla existente con la entidad JPA Lote.idLote
-- sin borrar datos. Ejecutar con respaldo previo de la base de datos.

USE clinica_veterinaria;
GO

-- 1) Revisar el estado actual antes de aplicar cambios.
EXEC sp_help 'lotes';
GO

-- 2) Si la tabla tiene una columna historica [id] como IDENTITY
-- y la aplicacion actual usa [id_lote], migrar valores conservando datos.
-- Ejecutar SOLO si [id_lote] no existe todavia:
-- EXEC sp_rename 'lotes.id', 'id_lote', 'COLUMN';

-- Alternativa si no se puede renombrar directamente:
-- ALTER TABLE lotes ADD id_lote BIGINT NULL;
-- UPDATE lotes SET id_lote = id WHERE id_lote IS NULL;

-- 3) Quitar el PK anterior antes de promover [id_lote].
-- Buscar el nombre del constraint PK con: EXEC sp_help 'lotes';
-- ALTER TABLE lotes DROP CONSTRAINT PK_lotes; -- reemplazar con nombre real del PK

-- 4) Promover [id_lote] como clave primaria con IDENTITY.
-- Nota: SQL Server no permite agregar IDENTITY a una columna existente.
-- Si se necesita IDENTITY, hay que recrear la tabla con:
--   CREATE TABLE lotes_new (id_lote BIGINT IDENTITY(1,1) PRIMARY KEY, ...);
--   INSERT INTO lotes_new (...) SELECT ... FROM lotes;
--   DROP TABLE lotes;
--   EXEC sp_rename 'lotes_new', 'lotes';

-- 5) Validar la estructura final.
EXEC sp_help 'lotes';
GO
