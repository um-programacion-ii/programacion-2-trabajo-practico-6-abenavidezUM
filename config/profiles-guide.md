# 🔧 Guía de Profiles y Configuración Avanzada

> **Autor**: Agustin Benavidez - Legajo: 62344  
> **Sistema**: Microservicios con Spring Boot  
> **Fecha**: Septiembre 2024

## 📋 Índice

1. [Profiles Disponibles](#profiles-disponibles)
2. [Configuración por Environment](#configuración-por-environment)
3. [Variables de Entorno](#variables-de-entorno)
4. [Configuración de Bases de Datos](#configuración-de-bases-de-datos)
5. [Configuración de Feign](#configuración-de-feign)
6. [Configuración de Logging](#configuración-de-logging)
7. [Scripts de Automatización](#scripts-de-automatización)
8. [Docker y Contenedores](#docker-y-contenedores)

## 🎯 Profiles Disponibles

### Profile: `dev` (Desarrollo)
```yaml
spring:
  profiles:
    active: dev
```

**Características:**
- ✅ Base de datos H2 en memoria
- ✅ Logs detallados (DEBUG)
- ✅ DevTools habilitado
- ✅ Hot reload activado
- ✅ Timeouts relajados para debugging
- ✅ Console H2 disponible

**Uso:**
```bash
# Data Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Business Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Sistema completo
./scripts/start-system.sh dev h2
make start
```

### Profile: `mysql` (Producción con MySQL)
```yaml
spring:
  profiles:
    active: mysql
```

**Características:**
- ✅ MySQL 8.4 con Docker
- ✅ Logs optimizados (INFO)
- ✅ Connection pooling configurado
- ✅ Timeouts de producción
- ✅ Circuit breaker estricto
- ✅ Métricas de performance

**Uso:**
```bash
# Iniciar MySQL primero
./scripts/setup-mysql.sh

# Data Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# Business Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# Sistema completo
./scripts/start-system.sh mysql mysql
make start-mysql
```

### Profile: `postgres` (Producción con PostgreSQL)
```yaml
spring:
  profiles:
    active: postgres
```

**Características:**
- ✅ PostgreSQL 16 con Docker
- ✅ Logs optimizados (INFO)
- ✅ Connection pooling avanzado
- ✅ Timeouts de producción
- ✅ Circuit breaker estricto
- ✅ Performance tuning

**Uso:**
```bash
# Iniciar PostgreSQL primero
./scripts/setup-postgres.sh

# Data Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres

# Business Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres

# Sistema completo
./scripts/start-system.sh postgres postgres
make start-postgres
```

### Profile: `test` (Testing)
```yaml
spring:
  profiles:
    active: test
```

**Características:**
- ✅ H2 en memoria para tests
- ✅ Puerto aleatorio
- ✅ Logs mínimos (ERROR)
- ✅ Timeouts rápidos
- ✅ Mocks automáticos
- ✅ Transacciones rollback

**Uso:**
```bash
# Tests unitarios
./mvnw test -Dspring.profiles.active=test

# Tests del sistema
./scripts/test-system.sh
make test
```

## 🌍 Configuración por Environment

### Development Environment
```bash
export SPRING_PROFILES_ACTIVE=dev
export LOG_LEVEL=DEBUG
export ENABLE_DEV_TOOLS=true
export DATA_SERVICE_URL=http://localhost:8081
```

### Staging Environment
```bash
export SPRING_PROFILES_ACTIVE=mysql
export LOG_LEVEL=INFO
export DATA_SERVICE_URL=http://staging-data-service:8081
export MYSQL_HOST=staging-mysql-server
```

### Production Environment
```bash
export SPRING_PROFILES_ACTIVE=postgres
export LOG_LEVEL=WARN
export DATA_SERVICE_URL=http://data-service:8081
export POSTGRES_HOST=prod-postgres-cluster
export ENABLE_ACTUATOR_SECURITY=true
```

## 📊 Variables de Entorno

### Variables Generales
```bash
# Profiles
SPRING_PROFILES_ACTIVE=dev|mysql|postgres|test

# Puertos
DATA_SERVICE_PORT=8081
BUSINESS_SERVICE_PORT=8082

# URLs
DATA_SERVICE_URL=http://localhost:8081
```

### Variables de Base de Datos

#### MySQL
```bash
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=microservices_db
MYSQL_USER=microservices_user
MYSQL_PASSWORD=microservices_pass
MYSQL_ROOT_PASSWORD=root_password
```

#### PostgreSQL
```bash
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=microservices_db
POSTGRES_USER=microservices_user
POSTGRES_PASSWORD=microservices_pass
```

### Variables de Feign Client
```bash
# Timeouts (milisegundos)
FEIGN_CONNECT_TIMEOUT=3000
FEIGN_READ_TIMEOUT=8000

# Circuit Breaker
CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=50
CIRCUIT_BREAKER_WAIT_DURATION=10s
CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE=10
```

## 🗄️ Configuración de Bases de Datos

### H2 Database (Development)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:microservices_db
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

**Acceso:**
- URL: http://localhost:8081/h2-console
- JDBC URL: jdbc:h2:mem:microservices_db
- Usuario: sa
- Password: (vacío)

### MySQL Database (Production)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/microservices_db
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: microservices_user
    password: microservices_pass
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

**Comando de conexión:**
```bash
# CLI
docker exec -it microservices_mysql mysql -u microservices_user -pmicroservices_pass microservices_db

# Web (phpMyAdmin)
http://localhost:8090
```

### PostgreSQL Database (Production)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/microservices_db
    driver-class-name: org.postgresql.Driver
    username: microservices_user
    password: microservices_pass
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

**Comando de conexión:**
```bash
# CLI
docker exec -it microservices_postgres psql -U microservices_user -d microservices_db

# Web (pgAdmin)
http://localhost:8091
```

## 🔄 Configuración de Feign Client

### Development (Timeouts relajados)
```yaml
feign:
  client:
    config:
      data-service:
        connectTimeout: 5000
        readTimeout: 15000
        loggerLevel: full
  hystrix:
    enabled: true

resilience4j:
  circuitbreaker:
    instances:
      data-service:
        waitDurationInOpenState: 5s
        failureRateThreshold: 70
```

### Production (Timeouts estrictos)
```yaml
feign:
  client:
    config:
      data-service:
        connectTimeout: 2000
        readTimeout: 5000
        loggerLevel: basic

resilience4j:
  circuitbreaker:
    instances:
      data-service:
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        waitDurationInOpenState: 30s
        failureRateThreshold: 40
```

## 📝 Configuración de Logging

### Development Logging
```yaml
logging:
  level:
    root: INFO
    com.microservices: DEBUG
    feign: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Production Logging
```yaml
logging:
  level:
    root: WARN
    com.microservices: INFO
    feign: INFO
  file:
    name: logs/microservice.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
```

## 🚀 Scripts de Automatización

### Comandos Disponibles

```bash
# Iniciar sistema
./scripts/start-system.sh [profile] [database]
./scripts/start-system.sh dev h2
./scripts/start-system.sh mysql mysql
./scripts/start-system.sh postgres postgres

# Detener sistema
./scripts/stop-system.sh [detener_bd]
./scripts/stop-system.sh         # Solo microservicios
./scripts/stop-system.sh yes     # Incluye bases de datos

# Tests completos
./scripts/test-system.sh [profile] [integration]
./scripts/test-system.sh test yes

# Monitoreo
./scripts/monitor-system.sh           # Snapshot
./scripts/monitor-system.sh continuous # Continuo

# Setup de BD
./scripts/setup-mysql.sh
./scripts/setup-postgres.sh

# Deployment
./scripts/deploy.sh [env] [build_mode] [skip_tests]
./scripts/deploy.sh dev full no
./scripts/deploy.sh mysql full yes
```

### Makefile Commands

```bash
# Básicos
make help                    # Mostrar ayuda
make start                   # Iniciar (dev + H2)
make start-mysql            # Iniciar con MySQL
make start-postgres         # Iniciar con PostgreSQL
make stop                   # Detener servicios
make stop-all              # Detener todo

# Testing
make test                   # Todos los tests
make test-unit             # Solo unitarios
make test-integration      # Solo integración

# Build
make build                 # Compilar todo
make build-data           # Solo data-service
make build-business       # Solo business-service

# Utilidades
make monitor              # Monitorear sistema
make logs                # Ver logs
make clean               # Limpiar builds
make setup               # Setup inicial
```

## 🐳 Docker y Contenedores

### Comandos de Docker Compose

```bash
# Iniciar todas las BD
docker-compose up -d

# Solo MySQL
docker-compose up -d mysql

# Solo PostgreSQL
docker-compose up -d postgres

# Con herramientas de admin
docker-compose --profile admin up -d

# Detener todo
docker-compose down

# Detener y limpiar volúmenes
docker-compose down -v
```

### Verificación de Contenedores

```bash
# Ver contenedores corriendo
docker ps --filter "name=microservices"

# Logs de MySQL
docker logs microservices_mysql

# Logs de PostgreSQL
docker logs microservices_postgres

# Acceso directo a MySQL
docker exec -it microservices_mysql bash

# Acceso directo a PostgreSQL
docker exec -it microservices_postgres bash
```

## 🔍 Troubleshooting

### Problemas Comunes

#### Puerto en uso
```bash
# Verificar qué está usando el puerto
lsof -i :8081
lsof -i :8082

# Matar proceso
kill -9 <PID>
```

#### Base de datos no conecta
```bash
# Verificar contenedor
docker ps | grep microservices

# Reiniciar contenedor
docker restart microservices_mysql
docker restart microservices_postgres

# Ver logs de la BD
docker logs microservices_mysql
```

#### Feign Client falla
```bash
# Verificar que data-service responde
curl http://localhost:8081/data/health

# Ver logs de business-service
tail -f logs/business-service.log
```

### Logs Importantes

```bash
# Logs en tiempo real
tail -f logs/data-service.log
tail -f logs/business-service.log

# Buscar errores
grep -i error logs/*.log
grep -i exception logs/*.log

# Buscar problemas de Feign
grep -i feign logs/business-service.log
```

## 📈 Performance Tuning

### JVM Options
```bash
# Para desarrollo
export JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"

# Para producción
export JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseStringDeduplication"
```

### Database Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Máximo conexiones
      minimum-idle: 5            # Mínimo idle
      connection-timeout: 30000  # Timeout conexión
      idle-timeout: 600000       # Timeout idle
      max-lifetime: 1800000      # Vida máxima conexión
```

---

**Esta guía cubre la configuración completa del sistema de microservicios desarrollado para el TP6 de Programación II**
