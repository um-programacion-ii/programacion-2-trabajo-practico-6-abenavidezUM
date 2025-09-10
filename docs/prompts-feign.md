# Prompts de Feign

> **Autor**: Agustin Benavidez - Legajo: 62344  
> **Proyecto**: Sistema de Microservicios con Spring Boot y Feign  
> **Etapa**: Configuración de Feign Client para comunicación entre servicios

## Introducción

Este archivo documenta todos los prompts utilizados para la configuración de Spring Cloud OpenFeign, manejo de errores de comunicación y optimización de la comunicación entre microservicios.

---

*[Este archivo se completará durante la etapa 3 cuando se implemente Feign Client]*

## Configuración Planificada de Feign

### Feign Client Interface
- Cliente para data-service desde business-service
- Endpoints para productos, categorías e inventario
- Configuración de timeouts y reintentos

### Manejo de Errores
- FeignException para errores de comunicación
- Custom exceptions para errores de negocio
- Fallbacks para resiliencia

### Configuración Avanzada
- Encoders y decoders personalizados
- Interceptors para logging
- Load balancing y service discovery
