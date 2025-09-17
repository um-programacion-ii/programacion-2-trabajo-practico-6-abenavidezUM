# 🗄️ Data Service - Microservicio de Datos

> **Autor**: Agustin Benavidez - Legajo: 62344  
> **Versión**: 1.0.0  
> **Puerto**: 8081

## 📋 Descripción

Microservicio independiente encargado de la persistencia y gestión de datos para el sistema de gestión de productos. Maneja las operaciones CRUD para productos, categorías e inventario utilizando Spring Boot y JPA.

## 🏗️ Arquitectura

Este microservicio sigue los principios de arquitectura limpia:

- **Entidades**: Producto, Categoria, Inventario
- **Repositories**: Acceso a datos con Spring Data JPA
- **Services**: Lógica de negocio específica de datos
- **Controllers**: Endpoints REST para comunicación externa

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 3.3.4**
- **Spring Data JPA**
- **Spring Web**
- **Spring Validation**
- **H2 Database** (desarrollo)
- **MySQL 8.4** (producción)
- **PostgreSQL 16** (producción)
- **Maven**

## 📊 Modelo de Datos

### Entidades

#### Categoria
- `id`: Long (PK)
- `nombre`: String (único, 100 chars)
- `descripcion`: String (500 chars)
- `fechaCreacion`: LocalDateTime
- `fechaActualizacion`: LocalDateTime
- `productos`: List<Producto> (OneToMany)

#### Producto
- `id`: Long (PK)
- `nombre`: String (100 chars)
- `descripcion`: String (500 chars)
- `precio`: BigDecimal (12,2)
- `categoria`: Categoria (ManyToOne)
- `inventario`: Inventario (OneToOne)
- `activo`: Boolean
- `fechaCreacion`: LocalDateTime
- `fechaActualizacion`: LocalDateTime

#### Inventario
- `id`: Long (PK)
- `producto`: Producto (OneToOne)
- `cantidad`: Integer
- `stockMinimo`: Integer
- `fechaUltimaActualizacion`: LocalDateTime
- `fechaCreacion`: LocalDateTime
- `version`: Long (optimistic locking)

### Relaciones
- **Categoria ↔ Producto**: One-to-Many bidireccional
- **Producto ↔ Inventario**: One-to-One bidireccional

## 🔧 Configuración

### Profiles Disponibles

#### `dev` (H2 en memoria)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### `mysql` (MySQL con Docker)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

#### `postgres` (PostgreSQL con Docker)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

#### `test` (Para testing)
```bash
./mvnw test
```

### Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Profile activo | `dev` |
| `SERVER_PORT` | Puerto del servidor | `8081` |
| `DB_HOST` | Host de la base de datos | `localhost` |
| `DB_NAME` | Nombre de la base de datos | `microservices_db` |
| `DB_USERNAME` | Usuario de la base de datos | `microservices_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `microservices_pass` |

## 📡 API Endpoints

### Productos (`/data/productos`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/data/productos` | Obtener todos los productos |
| GET | `/data/productos/{id}` | Obtener producto por ID |
| POST | `/data/productos` | Crear nuevo producto |
| PUT | `/data/productos/{id}` | Actualizar producto |
| DELETE | `/data/productos/{id}` | Eliminar producto (soft delete) |
| GET | `/data/productos/categoria/{nombre}` | Productos por categoría |
| GET | `/data/productos/buscar?texto={texto}` | Buscar productos |
| GET | `/data/productos/precio?min={min}&max={max}` | Productos por rango de precio |
| GET | `/data/productos/stock-bajo` | Productos con stock bajo |
| GET | `/data/productos/sin-stock` | Productos sin stock |
| PUT | `/data/productos/{id}/reactivar` | Reactivar producto |

### Categorías (`/data/categorias`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/data/categorias` | Obtener todas las categorías |
| GET | `/data/categorias/{id}` | Obtener categoría por ID |
| GET | `/data/categorias/nombre/{nombre}` | Obtener categoría por nombre |
| POST | `/data/categorias` | Crear nueva categoría |
| PUT | `/data/categorias/{id}` | Actualizar categoría |
| DELETE | `/data/categorias/{id}` | Eliminar categoría |
| GET | `/data/categorias/buscar?texto={texto}` | Buscar categorías |
| GET | `/data/categorias/con-productos` | Categorías con productos |
| GET | `/data/categorias/estadisticas` | Estadísticas de categorías |

### Inventario (`/data/inventario`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/data/inventario` | Obtener todo el inventario |
| GET | `/data/inventario/{id}` | Obtener inventario por ID |
| GET | `/data/inventario/producto/{productoId}` | Inventario por producto |
| POST | `/data/inventario` | Crear inventario |
| PUT | `/data/inventario/{id}` | Actualizar inventario |
| PUT | `/data/inventario/producto/{id}/stock?cantidad={n}` | Actualizar stock |
| PUT | `/data/inventario/producto/{id}/incrementar?incremento={n}` | Incrementar stock |
| PUT | `/data/inventario/producto/{id}/decrementar?decremento={n}` | Decrementar stock |
| GET | `/data/inventario/stock-bajo` | Inventarios con stock bajo |
| GET | `/data/inventario/stock-critico` | Inventarios con stock crítico |
| GET | `/data/inventario/sin-stock` | Inventarios sin stock |
| GET | `/data/inventario/estadisticas` | Estadísticas de inventario |
| GET | `/data/inventario/valor-total` | Valor total del inventario |

### Utilidades

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/data/health` | Health check del servicio |

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest=DataControllerIntegrationTest

# Tests con coverage
./mvnw test jacoco:report
```

### Estructura de Tests
```
src/test/java/
├── DataServiceApplicationTests.java          # Test básico de contexto
├── controller/
│   └── DataControllerIntegrationTest.java    # Tests de integración REST
├── service/
│   ├── ProductoServiceTest.java              # Tests unitarios de productos
│   ├── CategoriaServiceTest.java             # Tests unitarios de categorías
│   └── InventarioServiceTest.java            # Tests unitarios de inventario
└── repository/
    ├── ProductoRepositoryTest.java           # Tests de repositorio productos
    ├── CategoriaRepositoryTest.java          # Tests de repositorio categorías
    └── InventarioRepositoryTest.java         # Tests de repositorio inventario
```

## 🔄 Datos de Prueba

En el profile `dev`, se cargan automáticamente datos de prueba:

- **5 categorías**: Electrónicos, Ropa, Hogar, Deportes, Libros
- **20 productos** distribuidos en las categorías
- **20 inventarios** con diferentes niveles de stock

### Ejemplos de Datos
- Productos con stock normal, bajo y crítico
- Productos sin stock para testing de alertas
- Variedad de precios y categorías

## 🚨 Manejo de Errores

El microservicio maneja los siguientes tipos de errores:

### Errores Específicos
- **404 Not Found**: Recurso no encontrado
- **409 Conflict**: Recurso duplicado o violación de integridad
- **400 Bad Request**: Validación fallida o stock insuficiente

### Formato de Respuesta de Error
```json
{
  "timestamp": "2024-03-15T10:30:00",
  "status": 404,
  "error": "Recurso no encontrado",
  "message": "Producto no encontrado con id: '123'",
  "path": "/data/productos/123",
  "resourceName": "Producto",
  "fieldName": "id",
  "fieldValue": "123"
}
```

## 📊 Monitoreo

### Health Check
```bash
curl http://localhost:8081/data/health
```

### Actuator Endpoints
- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información de la aplicación
- `/actuator/metrics` - Métricas del sistema

## 🔧 Configuración de Base de Datos

### MySQL
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/microservices_db
    username: microservices_user
    password: microservices_pass
```

### PostgreSQL
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/microservices_db
    username: microservices_user
    password: microservices_pass
```

## 🚀 Deployment

### Desarrollo Local
```bash
# Con H2
./mvnw spring-boot:run

# Con MySQL (requiere Docker)
docker compose up -d mysql
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# Con PostgreSQL (requiere Docker)
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Build para Producción
```bash
./mvnw clean package -DskipTests
java -jar target/data-service-1.0.0.jar --spring.profiles.active=mysql
```

## 📝 Logs

### Configuración de Logging
- **Desarrollo**: Level DEBUG para el package del proyecto
- **Producción**: Level INFO
- **SQL**: Habilitado en desarrollo, deshabilitado en producción

### Archivos de Log
Los logs se escriben en la consola y pueden ser redirigidos a archivos según la configuración del deployment.

## 🤝 Comunicación con Otros Servicios

Este microservicio es **independiente** y **autocontenido**. La comunicación con otros microservicios (como business-service) se realiza únicamente a través de los endpoints REST expuestos.

### Características de Independencia
- ✅ **Sin dependencias** hacia otros microservicios
- ✅ **Base de datos propia** para cada profile
- ✅ **Puede ejecutarse standalone**
- ✅ **Intercambiable** por otras tecnologías
- ✅ **API REST estándar** para comunicación

---

**Microservicio desarrollado como parte del TP6 - Sistema de Microservicios**  
**Universidad de Mendoza - Programación II**
