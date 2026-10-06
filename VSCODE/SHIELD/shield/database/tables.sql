-- 1. Usuarios
CREATE TABLE usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  rol ENUM('ADMIN','TECNICO','EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  intentos_fallidos INT NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Activos
CREATE TABLE activos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(30) NOT NULL UNIQUE,
  tipo ENUM('ORDENADOR','PORTATIL','SERVIDOR','MOVIL','IMPRESORA','RED','OTRO') NOT NULL,
  fabricante VARCHAR(50),
  modelo VARCHAR(50),
  numero_serie VARCHAR(50) UNIQUE,
  estado ENUM('DISPONIBLE','EN_USO','REPARACION','BAJA') NOT NULL DEFAULT 'DISPONIBLE',
  fecha_alta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
 
-- 3. Asignaciones (quién tiene qué equipo)
CREATE TABLE asignaciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  activo_id INT NOT NULL,
  usuario_id INT NOT NULL,
  asignado_por INT NOT NULL,
  fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_retirada TIMESTAMP NULL,
  FOREIGN KEY (activo_id) REFERENCES activos(id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  FOREIGN KEY (asignado_por) REFERENCES usuarios(id)
);
 
-- 4. Historial de activos (trazabilidad)
CREATE TABLE historial_activos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  activo_id INT NOT NULL,
  usuario_id INT NULL,
  evento VARCHAR(30) NOT NULL,          -- REGISTRADO, ASIGNADO, RETIRADO, REPARACION, BAJA...
  detalle VARCHAR(255),
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (activo_id) REFERENCES activos(id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
 
-- 5. Incidencias
CREATE TABLE incidencias (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(150) NOT NULL,
  descripcion TEXT,
  tipo ENUM('TECNICA','SEGURIDAD') NOT NULL DEFAULT 'TECNICA',
  prioridad ENUM('BAJA','MEDIA','ALTA','CRITICA') NOT NULL DEFAULT 'MEDIA',
  estado ENUM('NUEVA','ASIGNADA','INVESTIGANDO','RESUELTA','CERRADA') NOT NULL DEFAULT 'NUEVA',
  creador_id INT NOT NULL,
  tecnico_id INT NULL,
  activo_id INT NULL,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_resolucion TIMESTAMP NULL,
  FOREIGN KEY (creador_id) REFERENCES usuarios(id),
  FOREIGN KEY (tecnico_id) REFERENCES usuarios(id),
  FOREIGN KEY (activo_id) REFERENCES activos(id)
);
 
-- 6. Mensajes (chat de cada incidencia)
CREATE TABLE mensajes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  incidencia_id INT NOT NULL,
  autor_id INT NOT NULL,
  contenido TEXT NOT NULL,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (incidencia_id) REFERENCES incidencias(id) ON DELETE CASCADE,
  FOREIGN KEY (autor_id) REFERENCES usuarios(id)
);
 
-- 7. Auditoría
CREATE TABLE auditoria (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NULL,                  -- NULL si el login falla con un usuario inexistente
  username_intento VARCHAR(50),
  accion VARCHAR(50) NOT NULL,          -- LOGIN, LOGIN_FALLIDO, CREATE_INCIDENT...
  detalle VARCHAR(255),
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
 
-- 8. Alertas de seguridad
CREATE TABLE alertas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tipo VARCHAR(50) NOT NULL,            -- LOGIN_FALLIDOS, EQUIPO_CONFLICTIVO...
  descripcion VARCHAR(255) NOT NULL,
  severidad ENUM('BAJA','MEDIA','ALTA','CRITICA') NOT NULL DEFAULT 'MEDIA',
  estado ENUM('ABIERTA','REVISADA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
  usuario_id INT NULL,
  activo_id INT NULL,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  FOREIGN KEY (activo_id) REFERENCES activos(id)
);
 
-- 9. Software instalado en cada activo
CREATE TABLE software (
  id INT AUTO_INCREMENT PRIMARY KEY,
  activo_id INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  version VARCHAR(30),
  FOREIGN KEY (activo_id) REFERENCES activos(id) ON DELETE CASCADE
);
 