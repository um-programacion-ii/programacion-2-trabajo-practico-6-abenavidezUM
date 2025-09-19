#!/bin/bash

# Script para ejecutar tests completos del sistema de microservicios
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

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_failure() {
    echo -e "${RED}❌ $1${NC}"
}

# Variables de control
PROFILE=${1:-test}
RUN_INTEGRATION_TESTS=${2:-yes}
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

print_header "🧪 EJECUTANDO TESTS DEL SISTEMA"
echo -e "Profile: ${GREEN}$PROFILE${NC}"
echo -e "Tests de integración: ${GREEN}$RUN_INTEGRATION_TESTS${NC}"
echo ""

# Función para ejecutar un test y trackear resultados
run_test() {
    local test_name="$1"
    local test_command="$2"
    local working_dir="$3"
    
    ((TOTAL_TESTS++))
    print_message "Ejecutando: $test_name"
    
    if [ -n "$working_dir" ]; then
        cd "$working_dir"
    fi
    
    if eval "$test_command" > /dev/null 2>&1; then
        print_success "$test_name"
        ((PASSED_TESTS++))
    else
        print_failure "$test_name"
        ((FAILED_TESTS++))
    fi
    
    if [ -n "$working_dir" ]; then
        cd - > /dev/null
    fi
}

# Función para verificar que un servicio responde
test_service_health() {
    local service_name="$1"
    local url="$2"
    local max_attempts=5
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            return 0
        fi
        sleep 2
        ((attempt++))
    done
    return 1
}

# 1. Tests unitarios
print_header "🔬 TESTS UNITARIOS"

print_message "Ejecutando tests unitarios de data-service..."
cd data-service
if ./mvnw test -Dspring.profiles.active=test; then
    print_success "Tests unitarios data-service"
    ((PASSED_TESTS++))
else
    print_failure "Tests unitarios data-service"
    ((FAILED_TESTS++))
fi
((TOTAL_TESTS++))
cd ..

print_message "Ejecutando tests unitarios de business-service..."
cd business-service
if ./mvnw test -Dspring.profiles.active=test; then
    print_success "Tests unitarios business-service"
    ((PASSED_TESTS++))
else
    print_failure "Tests unitarios business-service"
    ((FAILED_TESTS++))
fi
((TOTAL_TESTS++))
cd ..

# 2. Tests de integración (si están habilitados)
if [ "$RUN_INTEGRATION_TESTS" = "yes" ]; then
    print_header "🔗 TESTS DE INTEGRACIÓN"
    
    # Verificar si el sistema está corriendo
    print_message "Verificando que los servicios estén disponibles..."
    
    if ! test_service_health "Data Service" "http://localhost:8081/data/health"; then
        print_warning "Data Service no disponible, iniciando para tests..."
        cd data-service
        nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=test > ../logs/test-data-service.log 2>&1 &
        TEST_DATA_PID=$!
        cd ..
        
        # Esperar que inicie
        sleep 15
        
        if ! test_service_health "Data Service" "http://localhost:8081/data/health"; then
            print_error "No se pudo iniciar Data Service para tests"
            kill $TEST_DATA_PID 2>/dev/null || true
            exit 1
        fi
        CLEANUP_DATA_SERVICE=true
    fi
    
    if ! test_service_health "Business Service" "http://localhost:8082/api/health"; then
        print_warning "Business Service no disponible, iniciando para tests..."
        cd business-service
        nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=test > ../logs/test-business-service.log 2>&1 &
        TEST_BUSINESS_PID=$!
        cd ..
        
        # Esperar que inicie
        sleep 15
        
        if ! test_service_health "Business Service" "http://localhost:8082/api/health"; then
            print_error "No se pudo iniciar Business Service para tests"
            kill $TEST_BUSINESS_PID 2>/dev/null || true
            [ "$CLEANUP_DATA_SERVICE" = true ] && kill $TEST_DATA_PID 2>/dev/null || true
            exit 1
        fi
        CLEANUP_BUSINESS_SERVICE=true
    fi
    
    # Tests de endpoints de data-service
    print_message "Probando endpoints de data-service..."
    
    run_test "GET /data/health" "curl -s http://localhost:8081/data/health"
    run_test "GET /data/categorias" "curl -s http://localhost:8081/data/categorias"
    run_test "GET /data/productos" "curl -s http://localhost:8081/data/productos"
    run_test "GET /data/inventario" "curl -s http://localhost:8081/data/inventario"
    
    # Tests de endpoints de business-service
    print_message "Probando endpoints de business-service..."
    
    run_test "GET /api/health" "curl -s http://localhost:8082/api/health"
    run_test "GET /api/productos" "curl -s http://localhost:8082/api/productos"
    run_test "GET /api/reportes/inventario" "curl -s http://localhost:8082/api/reportes/inventario"
    run_test "GET /api/metricas/resumen" "curl -s http://localhost:8082/api/metricas/resumen"
    
    # Test de comunicación entre microservicios
    print_message "Probando comunicación entre microservicios..."
    
    # Crear una categoría en data-service y verificar que aparece en business-service
    CATEGORIA_JSON='{"nombre":"Test Integration","descripcion":"Categoria para test de integración"}'
    
    run_test "POST /data/categorias (crear)" "curl -s -X POST http://localhost:8081/data/categorias -H 'Content-Type: application/json' -d '$CATEGORIA_JSON'"
    run_test "GET categorías via business-service" "curl -s 'http://localhost:8082/api/productos/categoria/Test Integration'"
    
    # Cleanup de servicios de test si los iniciamos
    if [ "$CLEANUP_BUSINESS_SERVICE" = true ]; then
        print_message "Deteniendo Business Service de test..."
        kill $TEST_BUSINESS_PID 2>/dev/null || true
    fi
    
    if [ "$CLEANUP_DATA_SERVICE" = true ]; then
        print_message "Deteniendo Data Service de test..."
        kill $TEST_DATA_PID 2>/dev/null || true
    fi
fi

# 3. Tests específicos de bases de datos
print_header "🗄️ TESTS DE BASES DE DATOS"

# Test con H2
run_test "H2 Database Connection" "./mvnw test -Dtest=*ApplicationTests -Dspring.profiles.active=dev" "data-service"

# Test con MySQL (si está disponible)
if docker ps | grep -q microservices_mysql; then
    run_test "MySQL Database Connection" "./mvnw test -Dtest=*ApplicationTests -Dspring.profiles.active=mysql" "data-service"
else
    print_warning "MySQL no disponible, saltando test"
fi

# Test con PostgreSQL (si está disponible)
if docker ps | grep -q microservices_postgres; then
    run_test "PostgreSQL Database Connection" "./mvnw test -Dtest=*ApplicationTests -Dspring.profiles.active=postgres" "data-service"
else
    print_warning "PostgreSQL no disponible, saltando test"
fi

# 4. Tests de performance básicos
print_header "⚡ TESTS DE PERFORMANCE BÁSICOS"

if [ "$RUN_INTEGRATION_TESTS" = "yes" ]; then
    # Test de tiempo de respuesta
    print_message "Midiendo tiempos de respuesta..."
    
    # Función para medir tiempo de respuesta
    measure_response_time() {
        local url="$1"
        local service_name="$2"
        
        local time_ms=$(curl -o /dev/null -s -w "%{time_total}" "$url" | awk '{print int($1 * 1000)}')
        
        if [ "$time_ms" -lt 1000 ]; then
            print_success "$service_name responde en ${time_ms}ms"
            ((PASSED_TESTS++))
        else
            print_warning "$service_name responde en ${time_ms}ms (>1s)"
            ((FAILED_TESTS++))
        fi
        ((TOTAL_TESTS++))
    }
    
    if test_service_health "Data Service" "http://localhost:8081/data/health"; then
        measure_response_time "http://localhost:8081/data/productos" "Data Service /productos"
        measure_response_time "http://localhost:8081/data/categorias" "Data Service /categorias"
    fi
    
    if test_service_health "Business Service" "http://localhost:8082/api/health"; then
        measure_response_time "http://localhost:8082/api/productos" "Business Service /productos"
        measure_response_time "http://localhost:8082/api/reportes/inventario" "Business Service /reportes"
    fi
fi

# 5. Resumen final
print_header "📊 RESUMEN DE TESTS"

echo ""
echo -e "${BLUE}Total de tests ejecutados: $TOTAL_TESTS${NC}"
echo -e "${GREEN}Tests exitosos: $PASSED_TESTS${NC}"

if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${RED}Tests fallidos: $FAILED_TESTS${NC}"
    echo ""
    echo -e "${RED}❌ ALGUNOS TESTS FALLARON${NC}"
    exit 1
else
    echo -e "${GREEN}Tests fallidos: 0${NC}"
    echo ""
    echo -e "${GREEN}✅ TODOS LOS TESTS PASARON EXITOSAMENTE${NC}"
fi

# Información adicional
echo ""
echo -e "${BLUE}📁 Logs de tests disponibles en:${NC}"
if [ -f "logs/test-data-service.log" ]; then
    echo -e "  • Data Service:     logs/test-data-service.log"
fi
if [ -f "logs/test-business-service.log" ]; then
    echo -e "  • Business Service: logs/test-business-service.log"
fi
echo ""

# Calcular porcentaje de éxito
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$(( PASSED_TESTS * 100 / TOTAL_TESTS ))
    echo -e "${BLUE}Tasa de éxito: ${SUCCESS_RATE}%${NC}"
fi
