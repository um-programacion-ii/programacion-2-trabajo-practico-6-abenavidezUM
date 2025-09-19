# Makefile para el sistema de microservicios
# Autor: Agustin Benavidez - Legajo: 62344

.PHONY: help start stop test clean build setup-mysql setup-postgres monitor logs

# Variables
PROFILE ?= dev
DATABASE ?= h2

# Colores para output
GREEN = \033[0;32m
YELLOW = \033[1;33m
BLUE = \033[0;34m
NC = \033[0m # No Color

help: ## Mostrar esta ayuda
	@echo "$(BLUE)Sistema de Microservicios - Comandos Disponibles$(NC)"
	@echo "$(BLUE)================================================$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(YELLOW)Ejemplos de uso:$(NC)"
	@echo "  make start                    # Iniciar con H2 (desarrollo)"
	@echo "  make start-mysql              # Iniciar con MySQL"
	@echo "  make start-postgres           # Iniciar con PostgreSQL"
	@echo "  make test                     # Ejecutar todos los tests"
	@echo "  make monitor                  # Monitorear sistema en tiempo real"
	@echo ""

# Comandos principales
start: ## Iniciar el sistema completo (Profile: dev, Database: h2)
	@echo "$(GREEN)🚀 Iniciando sistema de microservicios...$(NC)"
	@chmod +x scripts/start-system.sh
	@./scripts/start-system.sh $(PROFILE) $(DATABASE)

start-mysql: ## Iniciar el sistema con MySQL
	@echo "$(GREEN)🐬 Iniciando sistema con MySQL...$(NC)"
	@chmod +x scripts/start-system.sh
	@./scripts/start-system.sh mysql mysql

start-postgres: ## Iniciar el sistema con PostgreSQL
	@echo "$(GREEN)🐘 Iniciando sistema con PostgreSQL...$(NC)"
	@chmod +x scripts/start-system.sh
	@./scripts/start-system.sh postgres postgres

stop: ## Detener el sistema completo
	@echo "$(YELLOW)🛑 Deteniendo sistema de microservicios...$(NC)"
	@chmod +x scripts/stop-system.sh
	@./scripts/stop-system.sh

stop-all: ## Detener sistema y bases de datos
	@echo "$(YELLOW)🛑 Deteniendo sistema completo incluyendo bases de datos...$(NC)"
	@chmod +x scripts/stop-system.sh
	@./scripts/stop-system.sh yes

# Comandos de testing
test: ## Ejecutar todos los tests
	@echo "$(BLUE)🧪 Ejecutando tests del sistema...$(NC)"
	@chmod +x scripts/test-system.sh
	@./scripts/test-system.sh

test-unit: ## Ejecutar solo tests unitarios
	@echo "$(BLUE)🔬 Ejecutando tests unitarios...$(NC)"
	@chmod +x scripts/test-system.sh
	@./scripts/test-system.sh test no

test-integration: ## Ejecutar tests de integración
	@echo "$(BLUE)🔗 Ejecutando tests de integración...$(NC)"
	@chmod +x scripts/test-system.sh
	@./scripts/test-system.sh test yes

# Comandos de base de datos
setup-mysql: ## Configurar y verificar MySQL
	@echo "$(GREEN)🐬 Configurando MySQL...$(NC)"
	@chmod +x scripts/setup-mysql.sh
	@./scripts/setup-mysql.sh

setup-postgres: ## Configurar y verificar PostgreSQL
	@echo "$(GREEN)🐘 Configurando PostgreSQL...$(NC)"
	@chmod +x scripts/setup-postgres.sh
	@./scripts/setup-postgres.sh

# Comandos de monitoreo
monitor: ## Monitorear el sistema (snapshot)
	@echo "$(BLUE)🖥️  Monitor del sistema...$(NC)"
	@chmod +x scripts/monitor-system.sh
	@./scripts/monitor-system.sh

monitor-continuous: ## Monitorear el sistema continuamente
	@echo "$(BLUE)🖥️  Monitor continuo del sistema...$(NC)"
	@chmod +x scripts/monitor-system.sh
	@./scripts/monitor-system.sh continuous

# Comandos de logs
logs: ## Ver logs de ambos servicios
	@echo "$(BLUE)📝 Logs del sistema...$(NC)"
	@mkdir -p logs
	@echo "$(GREEN)=== Data Service Logs ===$(NC)"
	@tail -20 logs/data-service.log 2>/dev/null || echo "No hay logs de data-service"
	@echo ""
	@echo "$(GREEN)=== Business Service Logs ===$(NC)"
	@tail -20 logs/business-service.log 2>/dev/null || echo "No hay logs de business-service"

logs-data: ## Ver logs del data-service
	@tail -f logs/data-service.log

logs-business: ## Ver logs del business-service
	@tail -f logs/business-service.log

# Comandos de build
build: ## Compilar ambos microservicios
	@echo "$(GREEN)🔨 Compilando microservicios...$(NC)"
	@echo "Compilando data-service..."
	@cd data-service && ./mvnw clean package -DskipTests
	@echo "Compilando business-service..."
	@cd business-service && ./mvnw clean package -DskipTests
	@echo "$(GREEN)✅ Compilación completada$(NC)"

build-data: ## Compilar solo data-service
	@echo "$(GREEN)🔨 Compilando data-service...$(NC)"
	@cd data-service && ./mvnw clean package -DskipTests

build-business: ## Compilar solo business-service
	@echo "$(GREEN)🔨 Compilando business-service...$(NC)"
	@cd business-service && ./mvnw clean package -DskipTests

# Comandos de limpieza
clean: ## Limpiar archivos de build y logs
	@echo "$(YELLOW)🧹 Limpiando archivos...$(NC)"
	@cd data-service && ./mvnw clean || true
	@cd business-service && ./mvnw clean || true
	@rm -f logs/*.log
	@rm -f logs/*.pid
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

clean-logs: ## Limpiar solo logs
	@echo "$(YELLOW)🧹 Limpiando logs...$(NC)"
	@rm -f logs/*.log
	@rm -f logs/*.pid

clean-docker: ## Limpiar contenedores y volúmenes Docker
	@echo "$(YELLOW)🐳 Limpiando Docker...$(NC)"
	@docker-compose down -v || true
	@docker system prune -f || true

# Comandos de desarrollo
dev-data: ## Ejecutar data-service en modo desarrollo
	@echo "$(GREEN)🗄️  Iniciando data-service en modo desarrollo...$(NC)"
	@cd data-service && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

dev-business: ## Ejecutar business-service en modo desarrollo
	@echo "$(GREEN)🏢 Iniciando business-service en modo desarrollo...$(NC)"
	@cd business-service && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Comandos de verificación
check-ports: ## Verificar qué puertos están en uso
	@echo "$(BLUE)🔍 Verificando puertos...$(NC)"
	@echo "Puerto 8081 (data-service):"
	@lsof -i :8081 || echo "  Puerto libre"
	@echo "Puerto 8082 (business-service):"
	@lsof -i :8082 || echo "  Puerto libre"
	@echo "Puerto 3306 (MySQL):"
	@lsof -i :3306 || echo "  Puerto libre"
	@echo "Puerto 5432 (PostgreSQL):"
	@lsof -i :5432 || echo "  Puerto libre"

check-health: ## Verificar salud de los servicios
	@echo "$(BLUE)🩺 Verificando salud de servicios...$(NC)"
	@echo "Data Service:"
	@curl -s http://localhost:8081/data/health || echo "  No disponible"
	@echo ""
	@echo "Business Service:"
	@curl -s http://localhost:8082/api/health || echo "  No disponible"

# Comandos de información
endpoints: ## Mostrar endpoints disponibles
	@echo "$(BLUE)📡 Endpoints del sistema:$(NC)"
	@echo ""
	@echo "$(GREEN)Data Service (http://localhost:8081):$(NC)"
	@echo "  GET  /data/health"
	@echo "  GET  /data/productos"
	@echo "  GET  /data/categorias"
	@echo "  GET  /data/inventario"
	@echo ""
	@echo "$(GREEN)Business Service (http://localhost:8082):$(NC)"
	@echo "  GET  /api/health"
	@echo "  GET  /api/productos"
	@echo "  GET  /api/reportes/inventario"
	@echo "  GET  /api/metricas/resumen"

info: ## Mostrar información del sistema
	@echo "$(BLUE)ℹ️  Información del Sistema de Microservicios$(NC)"
	@echo ""
	@echo "$(GREEN)Autor:$(NC) Agustin Benavidez"
	@echo "$(GREEN)Legajo:$(NC) 62344"
	@echo "$(GREEN)Versión:$(NC) 1.0.0"
	@echo "$(GREEN)Tecnologías:$(NC) Spring Boot, Spring Cloud OpenFeign, Docker"
	@echo ""
	@echo "$(GREEN)Microservicios:$(NC)"
	@echo "  • data-service (Puerto 8081)"
	@echo "  • business-service (Puerto 8082)"
	@echo ""
	@echo "$(GREEN)Bases de datos soportadas:$(NC)"
	@echo "  • H2 (desarrollo)"
	@echo "  • MySQL (producción)"
	@echo "  • PostgreSQL (producción)"

# Comandos avanzados
restart: stop start ## Reiniciar el sistema completo

restart-mysql: stop start-mysql ## Reiniciar con MySQL

restart-postgres: stop start-postgres ## Reiniciar con PostgreSQL

full-test: build test ## Compilar y ejecutar todos los tests

quick-start: build start ## Compilar e iniciar rápidamente

# Comandos de utilidad
permission-fix: ## Corregir permisos de scripts
	@echo "$(YELLOW)🔧 Corrigiendo permisos de scripts...$(NC)"
	@chmod +x scripts/*.sh
	@echo "$(GREEN)✅ Permisos corregidos$(NC)"

setup: permission-fix ## Configuración inicial del proyecto
	@echo "$(GREEN)🛠️  Configuración inicial...$(NC)"
	@mkdir -p logs config
	@cp config/environment.example config/.env 2>/dev/null || echo "Archivo de environment ya existe"
	@echo "$(GREEN)✅ Configuración inicial completada$(NC)"
	@echo ""
	@echo "$(YELLOW)Próximos pasos:$(NC)"
	@echo "1. make build          # Compilar microservicios"
	@echo "2. make start          # Iniciar sistema"
	@echo "3. make test           # Ejecutar tests"
