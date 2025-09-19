#!/bin/bash

# Script para iniciar el sistema completo de microservicios
# Autor: Agustin Benavidez - Legajo: 62344

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes coloreados
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Verificar argumentos
PROFILE=${1:-dev}
DATABASE=${2:-h2}

print_header "🚀 INICIANDO SISTEMA DE MICROSERVICIOS"
echo -e "Profile: ${GREEN}$PROFILE${NC}"
echo -e "Database: ${GREEN}$DATABASE${NC}"
echo ""

# Validar profiles
case $PROFILE in
    dev|mysql|postgres|test)
        print_message "Profile válido: $PROFILE"
        ;;
    *)
        print_error "Profile inválido. Usar: dev, mysql, postgres, test"
        exit 1
        ;;
esac

# Función para verificar si un puerto está en uso
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        print_warning "$service ya está corriendo en puerto $port"
        return 0
    else
        return 1
    fi
}

# Función para esperar que un servicio esté disponible
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    print_message "Esperando que $service_name esté disponible..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s $url > /dev/null 2>&1; then
            print_message "$service_name está disponible ✅"
            return 0
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_error "$service_name no está disponible después de $max_attempts intentos ❌"
            return 1
        fi
        
        echo -n "."
        sleep 2
        ((attempt++))
    done
}

# 1. Iniciar bases de datos si es necesario
if [ "$DATABASE" != "h2" ]; then
    print_header "📊 INICIANDO BASES DE DATOS"
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        print_error "Docker no está corriendo. Iniciar Docker primero."
        exit 1
    fi
    
    print_message "Iniciando contenedores de base de datos..."
    docker-compose up -d
    
    if [ "$DATABASE" = "mysql" ]; then
        wait_for_service "http://localhost:3306" "MySQL"
    elif [ "$DATABASE" = "postgres" ]; then
        wait_for_service "http://localhost:5432" "PostgreSQL"
    fi
    
    print_message "Esperando que las bases de datos terminen de inicializar..."
    sleep 10
fi

# 2. Compilar proyectos
print_header "🔨 COMPILANDO MICROSERVICIOS"

print_message "Compilando data-service..."
cd data-service
if ! ./mvnw clean package -DskipTests; then
    print_error "Error compilando data-service"
    exit 1
fi
cd ..

print_message "Compilando business-service..."
cd business-service
if ! ./mvnw clean package -DskipTests; then
    print_error "Error compilando business-service"
    exit 1
fi
cd ..

# 3. Iniciar data-service
print_header "🗄️ INICIANDO DATA-SERVICE"

if ! check_port 8081 "Data-Service"; then
    print_message "Iniciando data-service en puerto 8081..."
    cd data-service
    
    # Determinar profile a usar
    case $DATABASE in
        mysql)
            DATA_PROFILE="mysql"
            ;;
        postgres)
            DATA_PROFILE="postgres"
            ;;
        *)
            DATA_PROFILE="dev"
            ;;
    esac
    
    nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=$DATA_PROFILE > ../logs/data-service.log 2>&1 &
    DATA_SERVICE_PID=$!
    echo $DATA_SERVICE_PID > ../logs/data-service.pid
    cd ..
    
    # Esperar que data-service esté disponible
    wait_for_service "http://localhost:8081/data/health" "Data-Service"
else
    print_message "Data-Service ya está corriendo"
fi

# 4. Iniciar business-service
print_header "🏢 INICIANDO BUSINESS-SERVICE"

if ! check_port 8082 "Business-Service"; then
    print_message "Iniciando business-service en puerto 8082..."
    cd business-service
    
    nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=$PROFILE > ../logs/business-service.log 2>&1 &
    BUSINESS_SERVICE_PID=$!
    echo $BUSINESS_SERVICE_PID > ../logs/business-service.pid
    cd ..
    
    # Esperar que business-service esté disponible
    wait_for_service "http://localhost:8082/api/health" "Business-Service"
else
    print_message "Business-Service ya está corriendo"
fi

# 5. Verificar comunicación entre servicios
print_header "🔗 VERIFICANDO COMUNICACIÓN"

print_message "Verificando que business-service puede comunicarse con data-service..."
if curl -s "http://localhost:8082/api/productos" > /dev/null; then
    print_message "Comunicación entre microservicios OK ✅"
else
    print_warning "Problemas de comunicación entre microservicios ⚠️"
fi

# 6. Mostrar información del sistema
print_header "📋 SISTEMA INICIADO EXITOSAMENTE"

echo ""
echo -e "${GREEN}🌐 URLs de los servicios:${NC}"
echo -e "  • Data Service:     http://localhost:8081"
echo -e "  • Business Service: http://localhost:8082"
echo ""

echo -e "${GREEN}🔍 Health Checks:${NC}"
echo -e "  • Data Service:     http://localhost:8081/data/health"
echo -e "  • Business Service: http://localhost:8082/api/health"
echo ""

echo -e "${GREEN}📊 Endpoints principales:${NC}"
echo -e "  • Productos:        http://localhost:8082/api/productos"
echo -e "  • Categorías:       http://localhost:8081/data/categorias"
echo -e "  • Inventario:       http://localhost:8081/data/inventario"
echo -e "  • Reportes:         http://localhost:8082/api/reportes/inventario"
echo ""

echo -e "${GREEN}📝 Logs:${NC}"
echo -e "  • Data Service:     tail -f logs/data-service.log"
echo -e "  • Business Service: tail -f logs/business-service.log"
echo ""

echo -e "${GREEN}🛑 Para detener:${NC}"
echo -e "  • Ejecutar:         ./scripts/stop-system.sh"
echo ""

echo -e "${BLUE}✅ Sistema de microservicios iniciado correctamente${NC}"
echo -e "${BLUE}Profile: $PROFILE | Database: $DATABASE${NC}"
