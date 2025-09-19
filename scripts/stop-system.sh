#!/bin/bash

# Script para detener el sistema completo de microservicios
# Autor: Agustin Benavidez - Legajo: 62344

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

print_header "🛑 DETENIENDO SISTEMA DE MICROSERVICIOS"

# Función para detener un servicio por PID
stop_service() {
    local service_name=$1
    local pid_file=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            print_message "Deteniendo $service_name (PID: $pid)..."
            kill $pid
            
            # Esperar a que termine
            local attempts=0
            while ps -p $pid > /dev/null 2>&1 && [ $attempts -lt 30 ]; do
                sleep 1
                ((attempts++))
            done
            
            if ps -p $pid > /dev/null 2>&1; then
                print_warning "$service_name no terminó gracefully, forzando..."
                kill -9 $pid
            fi
            
            print_message "$service_name detenido ✅"
        else
            print_warning "$service_name ya estaba detenido"
        fi
        rm -f "$pid_file"
    else
        print_warning "No se encontró archivo PID para $service_name"
    fi
}

# Función para detener procesos por puerto
stop_by_port() {
    local port=$1
    local service_name=$2
    
    local pid=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        print_message "Deteniendo $service_name en puerto $port (PID: $pid)..."
        kill $pid 2>/dev/null || true
        sleep 2
        
        # Verificar si sigue corriendo
        local still_running=$(lsof -ti :$port 2>/dev/null || true)
        if [ -n "$still_running" ]; then
            print_warning "Forzando detención de $service_name..."
            kill -9 $still_running 2>/dev/null || true
        fi
        print_message "$service_name detenido ✅"
    else
        print_message "$service_name no estaba corriendo en puerto $port"
    fi
}

# Crear directorio de logs si no existe
mkdir -p logs

# 1. Detener Business Service
print_header "🏢 DETENIENDO BUSINESS-SERVICE"
stop_service "Business-Service" "logs/business-service.pid"
stop_by_port 8082 "Business-Service"

# 2. Detener Data Service
print_header "🗄️ DETENIENDO DATA-SERVICE"
stop_service "Data-Service" "logs/data-service.pid"
stop_by_port 8081 "Data-Service"

# 3. Detener bases de datos (opcional)
STOP_DATABASES=${1:-no}

if [ "$STOP_DATABASES" = "yes" ] || [ "$STOP_DATABASES" = "y" ]; then
    print_header "📊 DETENIENDO BASES DE DATOS"
    
    if command -v docker &> /dev/null; then
        if docker info &> /dev/null; then
            print_message "Deteniendo contenedores de base de datos..."
            docker-compose down
            print_message "Contenedores detenidos ✅"
        else
            print_warning "Docker no está corriendo"
        fi
    else
        print_warning "Docker no está instalado"
    fi
fi

# 4. Limpiar procesos Maven que puedan haber quedado
print_header "🧹 LIMPIANDO PROCESOS"

# Buscar y terminar procesos Maven relacionados
maven_pids=$(pgrep -f "mvn.*spring-boot:run" 2>/dev/null || true)
if [ -n "$maven_pids" ]; then
    print_message "Terminando procesos Maven restantes..."
    echo "$maven_pids" | xargs kill 2>/dev/null || true
    sleep 2
    
    # Verificar si siguen corriendo
    still_running=$(pgrep -f "mvn.*spring-boot:run" 2>/dev/null || true)
    if [ -n "$still_running" ]; then
        print_warning "Forzando terminación de procesos Maven..."
        echo "$still_running" | xargs kill -9 2>/dev/null || true
    fi
fi

# Buscar procesos Java que puedan ser nuestros microservicios
java_pids=$(pgrep -f "java.*microservices" 2>/dev/null || true)
if [ -n "$java_pids" ]; then
    print_message "Terminando procesos Java de microservicios..."
    echo "$java_pids" | xargs kill 2>/dev/null || true
    sleep 2
    
    still_running=$(pgrep -f "java.*microservices" 2>/dev/null || true)
    if [ -n "$still_running" ]; then
        print_warning "Forzando terminación de procesos Java..."
        echo "$still_running" | xargs kill -9 2>/dev/null || true
    fi
fi

# 5. Verificar que los puertos estén libres
print_header "🔍 VERIFICANDO PUERTOS"

check_port_free() {
    local port=$1
    local service=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "Puerto $port ($service) aún en uso"
        return 1
    else
        print_message "Puerto $port ($service) libre ✅"
        return 0
    fi
}

check_port_free 8081 "Data-Service"
check_port_free 8082 "Business-Service"

# 6. Mostrar información final
print_header "📋 SISTEMA DETENIDO"

echo ""
echo -e "${GREEN}📁 Archivos de logs conservados:${NC}"
if [ -f "logs/data-service.log" ]; then
    echo -e "  • Data Service:     logs/data-service.log"
fi
if [ -f "logs/business-service.log" ]; then
    echo -e "  • Business Service: logs/business-service.log"
fi
echo ""

echo -e "${GREEN}🔄 Para reiniciar el sistema:${NC}"
echo -e "  • Desarrollo:       ./scripts/start-system.sh dev h2"
echo -e "  • Con MySQL:        ./scripts/start-system.sh mysql mysql"
echo -e "  • Con PostgreSQL:   ./scripts/start-system.sh postgres postgres"
echo ""

if [ "$STOP_DATABASES" != "yes" ] && [ "$STOP_DATABASES" != "y" ]; then
    echo -e "${YELLOW}💡 Para detener también las bases de datos:${NC}"
    echo -e "  • Ejecutar:         ./scripts/stop-system.sh yes"
    echo ""
fi

echo -e "${BLUE}✅ Sistema de microservicios detenido correctamente${NC}"
