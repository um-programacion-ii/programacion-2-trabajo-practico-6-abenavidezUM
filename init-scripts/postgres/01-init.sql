-- Script de inicialización para PostgreSQL
-- Autor: Agustin Benavidez - Legajo: 62344
-- Sistema de Microservicios - TP6

-- Configuración de timezone
SET timezone = 'America/Argentina/Buenos_Aires';

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Mensaje de confirmación
SELECT 'Base de datos PostgreSQL inicializada correctamente para microservicios' AS mensaje;
