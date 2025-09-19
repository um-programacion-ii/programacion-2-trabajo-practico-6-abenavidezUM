#!/bin/bash

# Script para monitorear el sistema de microservicios en tiempo real
# Autor: Agustin Benavidez - Legajo: 62344

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

print_status() {
    local status=$1
    local message=$2
    
    case $status in
        "UP")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "DOWN")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "WARNING")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
    esac
}

print_header() {
    echo -e "${CYAN}================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}================================${NC}"
}

# Función para verificar salud de servicio
check_service_health() {
    local service_name=$1
    local url=$2
    local port=$3
    
    # Verificar si el puerto está en uso
    if ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_status "DOWN" "$service_name (Puerto $port no en uso)"
        return 1
    fi
    
    # Verificar endpoint de salud
    if curl -s "$url" > /dev/null 2>&1; then
        local response=$(curl -s "$url" 2>/dev/null)
        if echo "$response" | grep -q '"status":"UP"\|"status": "UP"'; then
            print_status "UP" "$service_name (Puerto $port)"
            return 0
        else
            print_status "WARNING" "$service_name (Puerto $port - Responde pero status no UP)"
            return 1
        fi
    else
        print_status "DOWN" "$service_name (Puerto $port - No responde)"
        return 1
    fi
}

# Función para verificar base de datos
check_database() {
    local db_name=$1
    local container_name=$2
    local check_command=$3
    
    if docker ps --format "table {{.Names}}" | grep -q "^$container_name$"; then
        if docker exec "$container_name" $check_command > /dev/null 2>&1; then
            print_status "UP" "$db_name (Container: $container_name)"
            return 0
        else
            print_status "WARNING" "$db_name (Container corriendo pero no responde)"
            return 1
        fi
    else
        print_status "DOWN" "$db_name (Container no corriendo)"
        return 1
    fi
}

# Función para obtener métricas de un servicio
get_service_metrics() {
    local service_name=$1
    local metrics_url=$2
    
    if curl -s "$metrics_url" > /dev/null 2>&1; then
        local metrics=$(curl -s "$metrics_url" 2>/dev/null)
        
        # Extraer información relevante del JSON
        local total_productos=$(echo "$metrics" | grep -o '"totalProductos":[0-9]*' | cut -d':' -f2 || echo "N/A")
        local productos_activos=$(echo "$metrics" | grep -o '"productosActivos":[0-9]*' | cut -d':' -f2 || echo "N/A")
        local stock_bajo=$(echo "$metrics" | grep -o '"productosConStockBajo":[0-9]*' | cut -d':' -f2 || echo "N/A")
        
        echo -e "  ${BLUE}📊 Métricas de $service_name:${NC}"
        echo -e "    • Total productos: $total_productos"
        echo -e "    • Productos activos: $productos_activos"
        echo -e "    • Productos stock bajo: $stock_bajo"
    else
        echo -e "  ${YELLOW}⚠️  No se pudieron obtener métricas de $service_name${NC}"
    fi
}

# Función para verificar comunicación entre servicios
check_inter_service_communication() {
    print_header "🔗 COMUNICACIÓN ENTRE SERVICIOS"
    
    # Test de que business-service puede obtener datos de data-service
    if curl -s "http://localhost:8082/api/productos" > /dev/null 2>&1; then
        local productos=$(curl -s "http://localhost:8082/api/productos" 2>/dev/null)
        local count=$(echo "$productos" | jq length 2>/dev/null || echo "N/A")
        print_status "UP" "Business Service → Data Service (Productos obtenidos: $count)"
    else
        print_status "DOWN" "Business Service → Data Service (Falló comunicación)"
    fi
    
    # Test específico de Feign Client
    if curl -s "http://localhost:8082/api/metricas/resumen" > /dev/null 2>&1; then
        print_status "UP" "Feign Client funcionando correctamente"
    else
        print_status "DOWN" "Feign Client con problemas"
    fi
}

# Función principal de monitoreo
monitor_system() {
    clear
    
    print_header "🖥️  MONITOR DEL SISTEMA DE MICROSERVICIOS"
    echo -e "${MAGENTA}Timestamp: $(date)${NC}"
    echo -e "${MAGENTA}Autor: Agustin Benavidez - Legajo: 62344${NC}"
    echo ""
    
    # 1. Verificar microservicios
    print_header "🚀 ESTADO DE MICROSERVICIOS"
    
    local data_service_up=false
    local business_service_up=false
    
    if check_service_health "Data Service" "http://localhost:8081/data/health" 8081; then
        data_service_up=true
    fi
    
    if check_service_health "Business Service" "http://localhost:8082/api/health" 8082; then
        business_service_up=true
    fi
    
    echo ""
    
    # 2. Verificar bases de datos
    print_header "🗄️  ESTADO DE BASES DE DATOS"
    
    check_database "MySQL" "microservices_mysql" "mysqladmin ping -h localhost --silent"
    check_database "PostgreSQL" "microservices_postgres" "pg_isready -U microservices_user -d microservices_db"
    
    # Verificar H2 (solo si data-service está corriendo con profile dev)
    if [ "$data_service_up" = true ]; then
        # Intentar determinar si está usando H2
        if curl -s "http://localhost:8081/data/health" | grep -q "dev\|h2" 2>/dev/null; then
            print_status "UP" "H2 Database (Embebida en data-service)"
        fi
    fi
    
    echo ""
    
    # 3. Verificar comunicación entre servicios
    if [ "$data_service_up" = true ] && [ "$business_service_up" = true ]; then
        check_inter_service_communication
        echo ""
    fi
    
    # 4. Obtener métricas
    if [ "$business_service_up" = true ]; then
        print_header "📈 MÉTRICAS DEL SISTEMA"
        get_service_metrics "Business Service" "http://localhost:8082/api/metricas/resumen"
        echo ""
    fi
    
    # 5. Información de recursos
    print_header "💻 RECURSOS DEL SISTEMA"
    
    # CPU y memoria de los contenedores Docker
    if command -v docker &> /dev/null && docker info &> /dev/null; then
        echo -e "${BLUE}🐳 Recursos de contenedores Docker:${NC}"
        
        if docker ps --format "table {{.Names}}" | grep -q microservices; then
            docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker ps --filter "name=microservices" --format "{{.Names}}") 2>/dev/null || echo "No se pudieron obtener estadísticas"
        else
            echo "  No hay contenedores de microservicios corriendo"
        fi
    fi
    
    echo ""
    
    # 6. Procesos Java
    echo -e "${BLUE}☕ Procesos Java relacionados:${NC}"
    local java_processes=$(ps aux | grep -E "(data-service|business-service|microservices)" | grep -v grep | wc -l)
    echo -e "  Procesos Java encontrados: $java_processes"
    
    if [ $java_processes -gt 0 ]; then
        ps aux | grep -E "(data-service|business-service|microservices)" | grep -v grep | awk '{print "  • PID: " $2 " - " $11 " " $12 " " $13}'
    fi
    
    echo ""
    
    # 7. Logs recientes (últimas 3 líneas de cada servicio)
    print_header "📝 LOGS RECIENTES"
    
    if [ -f "logs/data-service.log" ]; then
        echo -e "${BLUE}Data Service (últimas 3 líneas):${NC}"
        tail -3 logs/data-service.log 2>/dev/null | sed 's/^/  /' || echo "  No se pudieron leer los logs"
    fi
    
    if [ -f "logs/business-service.log" ]; then
        echo -e "${BLUE}Business Service (últimas 3 líneas):${NC}"
        tail -3 logs/business-service.log 2>/dev/null | sed 's/^/  /' || echo "  No se pudieron leer los logs"
    fi
    
    echo ""
    
    # 8. Comandos útiles
    print_header "🛠️  COMANDOS ÚTILES"
    echo -e "${CYAN}• Reiniciar sistema:${NC}    ./scripts/stop-system.sh && ./scripts/start-system.sh"
    echo -e "${CYAN}• Ver logs completos:${NC}   tail -f logs/data-service.log"
    echo -e "${CYAN}• Tests del sistema:${NC}    ./scripts/test-system.sh"
    echo -e "${CYAN}• Detener sistema:${NC}      ./scripts/stop-system.sh"
    
    echo ""
    echo -e "${MAGENTA}Presionar Ctrl+C para salir del monitor${NC}"
}

# Verificar si se quiere modo continuo
CONTINUOUS_MODE=${1:-no}

if [ "$CONTINUOUS_MODE" = "continuous" ] || [ "$CONTINUOUS_MODE" = "c" ]; then
    echo "Iniciando monitoreo continuo (actualización cada 10 segundos)..."
    echo "Presionar Ctrl+C para salir"
    echo ""
    
    while true; do
        monitor_system
        sleep 10
    done
else
    monitor_system
fi
