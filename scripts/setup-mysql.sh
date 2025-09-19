#!/bin/bash

# Script para configurar y verificar MySQL para el sistema de microservicios
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

print_header "🐬 CONFIGURACIÓN MYSQL"

# Verificar Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker no está instalado"
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker no está corriendo"
    exit 1
fi

# 1. Iniciar MySQL con Docker Compose
print_message "Iniciando MySQL con Docker Compose..."
docker-compose up -d mysql

# 2. Esperar que MySQL esté disponible
print_message "Esperando que MySQL esté disponible..."
max_attempts=60
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker exec microservices_mysql mysqladmin ping -h localhost --silent; then
        print_message "MySQL está disponible ✅"
        break
    fi
    
    if [ $attempt -eq $max_attempts ]; then
        print_error "MySQL no está disponible después de $max_attempts intentos"
        exit 1
    fi
    
    echo -n "."
    sleep 2
    ((attempt++))
done

# 3. Verificar la base de datos
print_message "Verificando configuración de la base de datos..."

# Verificar que la base de datos existe
if docker exec microservices_mysql mysql -u microservices_user -pmicroservices_pass -e "USE microservices_db; SHOW TABLES;" > /dev/null 2>&1; then
    print_message "Base de datos 'microservices_db' accesible ✅"
else
    print_error "No se puede acceder a la base de datos"
    exit 1
fi

# 4. Ejecutar scripts de inicialización adicionales si existen
if [ -f "init-scripts/mysql/01-init.sql" ]; then
    print_message "Ejecutando scripts de inicialización adicionales..."
    docker exec -i microservices_mysql mysql -u microservices_user -pmicroservices_pass microservices_db < init-scripts/mysql/01-init.sql
    print_message "Scripts de inicialización ejecutados ✅"
fi

# 5. Verificar la conexión desde Java
print_message "Verificando conexión desde data-service..."
cd data-service

# Test de conexión con Spring Boot
if ./mvnw test -Dtest=*ApplicationTests -Dspring.profiles.active=mysql -q; then
    print_message "Conexión desde Spring Boot exitosa ✅"
else
    print_warning "Problemas de conexión desde Spring Boot ⚠️"
fi

cd ..

# 6. Mostrar información de conexión
print_header "📋 INFORMACIÓN DE MYSQL"

echo ""
echo -e "${GREEN}🔗 Información de conexión:${NC}"
echo -e "  • Host:     localhost"
echo -e "  • Puerto:   3306"
echo -e "  • Database: microservices_db"
echo -e "  • Usuario:  microservices_user"
echo -e "  • Password: microservices_pass"
echo ""

echo -e "${GREEN}🐳 Comandos útiles:${NC}"
echo -e "  • Logs:     docker logs microservices_mysql"
echo -e "  • CLI:      docker exec -it microservices_mysql mysql -u microservices_user -pmicroservices_pass microservices_db"
echo -e "  • Stop:     docker stop microservices_mysql"
echo -e "  • Restart:  docker restart microservices_mysql"
echo ""

echo -e "${GREEN}🚀 Para usar MySQL:${NC}"
echo -e "  • Data Service:     ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql"
echo -e "  • Business Service: ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql"
echo -e "  • Sistema completo: ./scripts/start-system.sh mysql mysql"
echo ""

# 7. Verificar tablas creadas
print_message "Verificando estructura de tablas..."

TABLES=$(docker exec microservices_mysql mysql -u microservices_user -pmicroservices_pass microservices_db -e "SHOW TABLES;" -s 2>/dev/null || echo "")

if [ -n "$TABLES" ]; then
    echo -e "${GREEN}📊 Tablas encontradas:${NC}"
    echo "$TABLES" | while read table; do
        echo -e "  • $table"
    done
else
    print_warning "No se encontraron tablas (se crearán automáticamente al iniciar data-service)"
fi

echo ""
echo -e "${BLUE}✅ MySQL configurado y listo para usar${NC}"
