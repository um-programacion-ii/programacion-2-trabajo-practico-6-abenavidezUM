#!/bin/bash

# Script de deployment automático para el sistema de microservicios
# Autor: Agustin Benavidez - Legajo: 62344

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
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
    echo -e "${CYAN}================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}================================${NC}"
}

print_step() {
    echo -e "${BLUE}🔄 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Variables de configuración
ENVIRONMENT=${1:-dev}
BUILD_MODE=${2:-full}
SKIP_TESTS=${3:-no}

print_header "🚀 DEPLOYMENT DE MICROSERVICIOS"
echo -e "Environment: ${GREEN}$ENVIRONMENT${NC}"
echo -e "Build Mode: ${GREEN}$BUILD_MODE${NC}"
echo -e "Skip Tests: ${GREEN}$SKIP_TESTS${NC}"
echo ""

# Validar argumentos
case $ENVIRONMENT in
    dev|mysql|postgres|production)
        print_message "Environment válido: $ENVIRONMENT"
        ;;
    *)
        print_error "Environment inválido. Usar: dev, mysql, postgres, production"
        exit 1
        ;;
esac

# Configurar variables según el environment
case $ENVIRONMENT in
    dev)
        DATABASE_TYPE="h2"
        DATA_PROFILE="dev"
        BUSINESS_PROFILE="dev"
        ;;
    mysql)
        DATABASE_TYPE="mysql"
        DATA_PROFILE="mysql"
        BUSINESS_PROFILE="mysql"
        ;;
    postgres)
        DATABASE_TYPE="postgres"
        DATA_PROFILE="postgres"
        BUSINESS_PROFILE="postgres"
        ;;
    production)
        DATABASE_TYPE="postgres"  # PostgreSQL por defecto en producción
        DATA_PROFILE="postgres"
        BUSINESS_PROFILE="postgres"
        ;;
esac

# Función para verificar prerequisitos
check_prerequisites() {
    print_step "Verificando prerequisitos..."
    
    # Verificar Java
    if ! command -v java &> /dev/null; then
        print_error "Java no está instalado"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 21 ]; then
        print_warning "Se recomienda Java 21 o superior. Versión actual: $java_version"
    fi
    
    # Verificar Maven
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven no está instalado globalmente, usando wrapper"
    fi
    
    # Verificar Docker si no es dev
    if [ "$DATABASE_TYPE" != "h2" ]; then
        if ! command -v docker &> /dev/null; then
            print_error "Docker es requerido para $DATABASE_TYPE"
            exit 1
        fi
        
        if ! docker info &> /dev/null; then
            print_error "Docker no está corriendo"
            exit 1
        fi
    fi
    
    print_success "Prerequisites verificados"
}

# Función para detener servicios existentes
stop_existing_services() {
    print_step "Deteniendo servicios existentes..."
    
    # Detener servicios Java
    pkill -f "microservices.*spring-boot" || true
    
    # Esperar un momento
    sleep 3
    
    # Verificar que los puertos estén libres
    local data_port_free=true
    local business_port_free=true
    
    if lsof -Pi :8081 -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "Puerto 8081 aún en uso"
        data_port_free=false
    fi
    
    if lsof -Pi :8082 -sTCP:LISTEN -t >/dev/null 2>&1; then
        print_warning "Puerto 8082 aún en uso"
        business_port_free=false
    fi
    
    if [ "$data_port_free" = true ] && [ "$business_port_free" = true ]; then
        print_success "Puertos liberados"
    else
        print_warning "Algunos puertos siguen en uso, continuando..."
    fi
}

# Función para preparar la base de datos
setup_database() {
    if [ "$DATABASE_TYPE" = "h2" ]; then
        print_success "Usando H2 (no requiere setup)"
        return 0
    fi
    
    print_step "Configurando base de datos $DATABASE_TYPE..."
    
    # Iniciar contenedores de base de datos
    if [ "$DATABASE_TYPE" = "mysql" ]; then
        docker-compose up -d mysql
        
        # Esperar que MySQL esté disponible
        local attempts=0
        while [ $attempts -lt 30 ]; do
            if docker exec microservices_mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
                break
            fi
            sleep 2
            ((attempts++))
        done
        
        if [ $attempts -eq 30 ]; then
            print_error "MySQL no está disponible"
            exit 1
        fi
        
    elif [ "$DATABASE_TYPE" = "postgres" ]; then
        docker-compose up -d postgres
        
        # Esperar que PostgreSQL esté disponible
        local attempts=0
        while [ $attempts -lt 30 ]; do
            if docker exec microservices_postgres pg_isready -U microservices_user -d microservices_db 2>/dev/null; then
                break
            fi
            sleep 2
            ((attempts++))
        done
        
        if [ $attempts -eq 30 ]; then
            print_error "PostgreSQL no está disponible"
            exit 1
        fi
    fi
    
    print_success "Base de datos $DATABASE_TYPE lista"
}

# Función para compilar proyectos
build_projects() {
    print_step "Compilando microservicios..."
    
    # Compilar data-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "data" ]; then
        print_message "Compilando data-service..."
        cd data-service
        
        if [ "$SKIP_TESTS" = "yes" ]; then
            ./mvnw clean package -DskipTests -q
        else
            ./mvnw clean package -q
        fi
        
        if [ $? -ne 0 ]; then
            print_error "Error compilando data-service"
            exit 1
        fi
        cd ..
        print_success "Data-service compilado"
    fi
    
    # Compilar business-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "business" ]; then
        print_message "Compilando business-service..."
        cd business-service
        
        if [ "$SKIP_TESTS" = "yes" ]; then
            ./mvnw clean package -DskipTests -q
        else
            ./mvnw clean package -q
        fi
        
        if [ $? -ne 0 ]; then
            print_error "Error compilando business-service"
            exit 1
        fi
        cd ..
        print_success "Business-service compilado"
    fi
}

# Función para ejecutar tests
run_tests() {
    if [ "$SKIP_TESTS" = "yes" ]; then
        print_warning "Tests omitidos por parámetro"
        return 0
    fi
    
    print_step "Ejecutando tests..."
    
    # Tests unitarios de data-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "data" ]; then
        print_message "Tests de data-service..."
        cd data-service
        ./mvnw test -Dspring.profiles.active=test -q
        if [ $? -ne 0 ]; then
            print_error "Tests de data-service fallaron"
            exit 1
        fi
        cd ..
    fi
    
    # Tests unitarios de business-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "business" ]; then
        print_message "Tests de business-service..."
        cd business-service
        ./mvnw test -Dspring.profiles.active=test -q
        if [ $? -ne 0 ]; then
            print_error "Tests de business-service fallaron"
            exit 1
        fi
        cd ..
    fi
    
    print_success "Todos los tests pasaron"
}

# Función para deployar servicios
deploy_services() {
    print_step "Deploying servicios..."
    
    # Crear directorio de logs
    mkdir -p logs
    
    # Deployar data-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "data" ]; then
        print_message "Deploying data-service..."
        cd data-service
        
        # Ejecutar con el profile correcto
        nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=$DATA_PROFILE > ../logs/data-service-deploy.log 2>&1 &
        DATA_SERVICE_PID=$!
        echo $DATA_SERVICE_PID > ../logs/data-service.pid
        
        cd ..
        
        # Esperar que esté disponible
        local attempts=0
        while [ $attempts -lt 30 ]; do
            if curl -s http://localhost:8081/data/health > /dev/null 2>&1; then
                break
            fi
            sleep 2
            ((attempts++))
        done
        
        if [ $attempts -eq 30 ]; then
            print_error "Data-service no está disponible después del deployment"
            exit 1
        fi
        
        print_success "Data-service deployed en puerto 8081"
    fi
    
    # Deployar business-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "business" ]; then
        print_message "Deploying business-service..."
        cd business-service
        
        # Ejecutar con el profile correcto
        nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=$BUSINESS_PROFILE > ../logs/business-service-deploy.log 2>&1 &
        BUSINESS_SERVICE_PID=$!
        echo $BUSINESS_SERVICE_PID > ../logs/business-service.pid
        
        cd ..
        
        # Esperar que esté disponible
        local attempts=0
        while [ $attempts -lt 30 ]; do
            if curl -s http://localhost:8082/api/health > /dev/null 2>&1; then
                break
            fi
            sleep 2
            ((attempts++))
        done
        
        if [ $attempts -eq 30 ]; then
            print_error "Business-service no está disponible después del deployment"
            exit 1
        fi
        
        print_success "Business-service deployed en puerto 8082"
    fi
}

# Función para verificar deployment
verify_deployment() {
    print_step "Verificando deployment..."
    
    local all_services_ok=true
    
    # Verificar data-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "data" ]; then
        if curl -s http://localhost:8081/data/health | grep -q "UP"; then
            print_success "Data-service: OK"
        else
            print_error "Data-service: FAIL"
            all_services_ok=false
        fi
    fi
    
    # Verificar business-service
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "business" ]; then
        if curl -s http://localhost:8082/api/health | grep -q "UP"; then
            print_success "Business-service: OK"
        else
            print_error "Business-service: FAIL"
            all_services_ok=false
        fi
    fi
    
    # Verificar comunicación entre servicios
    if [ "$BUILD_MODE" = "full" ]; then
        if curl -s http://localhost:8082/api/productos > /dev/null 2>&1; then
            print_success "Comunicación entre servicios: OK"
        else
            print_warning "Comunicación entre servicios: PROBLEMAS"
            all_services_ok=false
        fi
    fi
    
    if [ "$all_services_ok" = true ]; then
        print_success "Deployment verificado exitosamente"
    else
        print_error "Problemas en el deployment"
        exit 1
    fi
}

# Función principal
main() {
    print_header "INICIANDO DEPLOYMENT"
    
    check_prerequisites
    stop_existing_services
    setup_database
    build_projects
    run_tests
    deploy_services
    verify_deployment
    
    print_header "🎉 DEPLOYMENT COMPLETADO"
    
    echo ""
    echo -e "${GREEN}🌐 URLs de los servicios:${NC}"
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "data" ]; then
        echo -e "  • Data Service:     http://localhost:8081"
        echo -e "  • Health Check:     http://localhost:8081/data/health"
    fi
    if [ "$BUILD_MODE" = "full" ] || [ "$BUILD_MODE" = "business" ]; then
        echo -e "  • Business Service: http://localhost:8082"
        echo -e "  • Health Check:     http://localhost:8082/api/health"
    fi
    
    echo ""
    echo -e "${GREEN}📊 Environment:${NC} $ENVIRONMENT"
    echo -e "${GREEN}🗄️  Database:${NC} $DATABASE_TYPE"
    echo -e "${GREEN}📝 Logs:${NC} logs/"
    
    echo ""
    echo -e "${YELLOW}🛑 Para detener:${NC} ./scripts/stop-system.sh"
    echo -e "${YELLOW}📊 Para monitorear:${NC} ./scripts/monitor-system.sh"
}

# Ejecutar función principal
main "$@"
