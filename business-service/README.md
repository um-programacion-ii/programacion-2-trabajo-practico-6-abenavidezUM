# 🏢 Business Service - Microservicio de Negocio

> **Autor**: Agustin Benavidez - Legajo: 62344  
> **Versión**: 1.0.0  
> **Puerto**: 8082

## 📋 Descripción

Microservicio independiente encargado de la lógica de negocio, validaciones y comunicación con el microservicio de datos a través de **Spring Cloud OpenFeign**. Implementa reglas de negocio, reportes avanzados y métricas empresariales.

## 🏗️ Arquitectura

Este microservicio sigue los principios de arquitectura limpia y se comunica con `data-service`:

- **DTOs**: Transferencia de datos entre microservicios
- **Feign Clients**: Comunicación HTTP con data-service
- **Business Services**: Lógica de negocio y validaciones
- **Controllers**: Endpoints REST para clientes externos
- **Circuit Breakers**: Resiliencia y tolerancia a fallos

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 3.3.4**
- **Spring Cloud OpenFeign** (comunicación entre microservicios)
- **Spring Cloud Circuit Breaker** (Resilience4j)
- **Spring Web**
- **Spring Validation**
- **Maven**

## 🔗 Comunicación con Data Service

### Configuración de Feign
```yaml
feign:
  client:
    config:
      data-service:
        connectTimeout: 3000
        readTimeout: 8000
        loggerLevel: full
```

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    instances:
      data-service:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

## 📊 DTOs para Comunicación

### ProductoDTO
- `id`: Long
- `nombre`: String
- `descripcion`: String  
- `precio`: BigDecimal
- `categoriaNombre`: String
- `stock`: Integer
- `stockBajo`: Boolean
- `valorInventario`: BigDecimal (calculado)
- `estadoStock`: String (calculado)

### CategoriaDTO
- `id`: Long
- `nombre`: String
- `descripcion`: String
- `cantidadProductos`: Integer
- `valorTotalInventario`: BigDecimal

### InventarioDTO
- `id`: Long
- `productoId`: Long
- `cantidad`: Integer
- `stockMinimo`: Integer
- `estadoStock`: String (calculado)
- `valorTotal`: BigDecimal (calculado)

### ReporteDTO
- `tipoReporte`: String
- `titulo`: String
- `datos`: Map<String, Object>
- `productos`: List<ProductoDTO>
- `metricas`: Métricas calculadas

## 🔧 Configuración

### Profiles Disponibles

#### `dev` (Desarrollo)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### `mysql` (Producción con MySQL)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

#### `postgres` (Producción con PostgreSQL)  
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
| `SERVER_PORT` | Puerto del servidor | `8082` |
| `DATA_SERVICE_URL` | URL del data-service | `http://localhost:8081` |

## 📡 API Endpoints

### Productos (`/api/productos`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/productos` | Obtener todos los productos con lógica de negocio |
| GET | `/api/productos/{id}` | Obtener producto por ID con validaciones |
| POST | `/api/productos` | Crear producto con validaciones de negocio |
| PUT | `/api/productos/{id}` | Actualizar producto con validaciones |
| DELETE | `/api/productos/{id}` | Eliminar producto (soft delete) |
| GET | `/api/productos/categoria/{nombre}` | Productos por categoría |
| GET | `/api/productos/buscar?texto={texto}` | Buscar productos (min 2 chars) |
| GET | `/api/productos/precio?min={min}&max={max}` | Productos por rango de precio |
| GET | `/api/productos/stock-bajo` | Productos con stock bajo + prioridad |
| POST | `/api/productos/validar` | Validar datos sin crear |
| GET | `/api/productos/{id}/disponibilidad` | Verificar disponibilidad |

### Reportes (`/api/reportes`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reportes/inventario` | Reporte completo de inventario |
| GET | `/api/reportes/categorias` | Reporte de productos por categoría |
| GET | `/api/reportes/alertas-stock` | Reporte de alertas de stock |
| GET | `/api/reportes/financiero` | Reporte financiero del inventario |

### Métricas (`/api/metricas`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/metricas/valor-total-inventario` | Valor total del inventario |
| GET | `/api/metricas/resumen` | Resumen de métricas principales |

### Utilidades

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/health` | Health check del servicio |
| GET | `/api/info` | Información del servicio |

## 🧠 Lógica de Negocio Implementada

### Validaciones
- **Productos**: Nombre mínimo 2 chars, precio > 0, categoría obligatoria
- **Búsquedas**: Texto mínimo 2 caracteres
- **Precios**: Rango válido (min ≤ max), valores positivos
- **IDs**: Valores positivos obligatorios

### Enriquecimiento de Datos
- **Cálculo de valor de inventario**: precio × stock
- **Estado de stock**: NORMAL, BAJO, CRITICO, SIN_STOCK
- **Prioridad de reabastecimiento**: Basada en % de stock mínimo

### Reportes de Negocio
- **Estado de Inventario**: Métricas completas + recomendaciones
- **Por Categoría**: Distribución y estadísticas
- **Alertas de Stock**: Clasificación por urgencia
- **Financiero**: Análisis de valor y distribución

### Reglas de Negocio
- Stock crítico: ≤ 50% del stock mínimo
- Stock bajo: ≤ stock mínimo
- Alertas automáticas para productos sin stock
- Cálculo de impacto económico por categoría

## 🛡️ Resiliencia y Tolerancia a Fallos

### Circuit Breaker
- **Ventana deslizante**: 10 llamadas
- **Umbral de falla**: 50%
- **Tiempo de espera**: 10 segundos
- **Reintentos**: 3 intentos con 1s de espera

### Fallbacks
- Implementados para todos los métodos del DataServiceClient
- Retorna datos seguros (listas vacías, valores por defecto)
- Logs detallados de errores de comunicación

### Timeouts
- **Conexión**: 3 segundos
- **Lectura**: 8 segundos
- **Circuit Breaker**: 8 segundos

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest=ProductoBusinessServiceTest

# Tests de integración
./mvnw test -Dtest=BusinessControllerIntegrationTest
```

### Estructura de Tests
```
src/test/java/
├── BusinessServiceApplicationTests.java       # Test básico de contexto
├── service/
│   ├── ProductoBusinessServiceTest.java       # Tests unitarios con mocks
│   └── ReporteBusinessServiceTest.java        # Tests de lógica de reportes
├── controller/
│   └── BusinessControllerIntegrationTest.java # Tests de integración REST
└── client/
    └── DataServiceClientTest.java             # Tests de Feign Client
```

### Mocking de Data Service
```java
@MockBean
private DataServiceClient dataServiceClient;

@Test
void cuandoDataServiceNoDisponible_entoncesUsaFallback() {
    when(dataServiceClient.obtenerTodosLosProductos())
        .thenThrow(FeignException.InternalServerError.class);
    
    // Verificar que el fallback se ejecuta correctamente
}
```

## 🔄 Flujo de Comunicación

### Ejemplo: Crear Producto
```
Cliente → BusinessController 
       → ProductoBusinessService (validaciones)
       → DataServiceClient (Feign)
       → Data Service HTTP REST
       ← ProductoDTO enriquecido
       ← Respuesta con lógica de negocio
```

### Manejo de Errores
1. **Validación**: Errores 400 con detalles de campos
2. **Comunicación**: Errores 503 con fallback
3. **Not Found**: Errores 404 desde data-service
4. **Circuit Breaker**: Fallback automático

## 🔧 Configuración de Feign

### Headers y Encoders
```java
@FeignClient(
    name = "data-service",
    url = "${data.service.url}",
    fallback = DataServiceClientFallback.class
)
```

### Logging
- **Nivel FULL** en desarrollo
- **Nivel BASIC** en producción
- Logs de request/response para debugging

## 📊 Monitoreo

### Health Checks
```bash
# Business service
curl http://localhost:8082/api/health

# Actuator health (incluye circuit breakers)
curl http://localhost:8082/actuator/health
```

### Métricas de Circuit Breaker
```bash
curl http://localhost:8082/actuator/circuitbreakers
```

### Métricas de Feign
```bash
curl http://localhost:8082/actuator/metrics/feign.client
```

## 🚀 Deployment

### Desarrollo Local
```bash
# Asegurar que data-service esté corriendo en 8081
curl http://localhost:8081/data/health

# Ejecutar business-service
./mvnw spring-boot:run

# Verificar comunicación
curl http://localhost:8082/api/productos
```

### Producción
```bash
./mvnw clean package -DskipTests
java -jar target/business-service-1.0.0.jar \
  --spring.profiles.active=mysql \
  --data.service.url=http://data-service:8081
```

## 🔒 Seguridad y Mejores Prácticas

### Validación de Entrada
- Validaciones Jakarta Bean Validation
- Sanitización de parámetros de búsqueda
- Validación de rangos numéricos

### Manejo de Errores
- Logs detallados sin exposición de datos sensibles
- Mensajes de error informativos para el cliente
- Códigos de estado HTTP apropiados

### Performance
- Circuit breakers para evitar cascading failures
- Timeouts agresivos para responsividad
- Fallbacks que no comprometen la funcionalidad

## 🤝 Independencia del Microservicio

### Características de Independencia
- ✅ **Sin dependencias directas** hacia data-service (solo HTTP)
- ✅ **Puede ejecutarse standalone** (con fallbacks)
- ✅ **Intercambiable** por otras tecnologías
- ✅ **API REST estándar** para comunicación
- ✅ **Lógica de negocio encapsulada**

### Intercambiabilidad
El business-service puede comunicarse con cualquier servicio que implemente la misma API REST que data-service, incluso servicios escritos en otros lenguajes o frameworks.

---

**Microservicio desarrollado como parte del TP6 - Sistema de Microservicios**  
**Universidad de Mendoza - Programación II**
