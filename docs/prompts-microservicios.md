# Prompts de Microservicios

> **Autor**: Agustin Benavidez - Legajo: 62344  
> **Proyecto**: Sistema de Microservicios con Spring Boot y Feign  
> **Etapa**: Configuración de arquitectura de microservicios

## Introducción

Este archivo documenta todos los prompts utilizados para la configuración de la arquitectura de microservicios, separación de responsabilidades y comunicación entre servicios.

---

*[Este archivo se completará durante el desarrollo de los microservicios]*

## Arquitectura Planificada

### Microservicio Data-Service
- Puerto: 8081
- Responsabilidad: Acceso a datos y persistencia
- Tecnologías: Spring Boot, Spring Data JPA, H2/MySQL/PostgreSQL

### Microservicio Business-Service  
- Puerto: 8082
- Responsabilidad: Lógica de negocio y validaciones
- Tecnologías: Spring Boot, Spring Cloud OpenFeign

### Comunicación entre Servicios
- Protocolo: HTTP REST
- Cliente: Feign Client
- Formato: JSON con DTOs
