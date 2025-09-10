# Prompts de Desarrollo

> **Autor**: Agustin Benavidez - Legajo: 62344  
> **Proyecto**: Sistema de Microservicios con Spring Boot y Feign  
> **Etapa**: Desarrollo de código (entidades, services, controllers)

## Introducción

Este archivo documenta todos los prompts utilizados durante el desarrollo del código del sistema de microservicios. Cada prompt incluye el contexto, la respuesta recibida, las modificaciones realizadas y los aprendizajes obtenidos.

---

## Prompt 1: Análisis inicial del proyecto

### Prompt Utilizado:
```
quiero que clones este repositorio, leas atentamente el readme y me digas si podemos resolver todo el proyecto. todavia no quiero que programes nada, solo quiero saber si es viable que lo hagamos
```

### Respuesta Recibida:
```
[Se analizó completamente el README del proyecto y se confirmó la viabilidad del desarrollo]
```

### Modificaciones Realizadas:
- No se realizaron modificaciones de código en este prompt
- Se evaluó la estructura y requerimientos del proyecto

### Explicación del Prompt:
Este prompt se utilizó para hacer una evaluación completa de la viabilidad del proyecto antes de comenzar el desarrollo. Era importante entender todos los requerimientos, tecnologías y complejidad antes de comprometerse con la implementación.

### Aprendizajes Obtenidos:
- Importancia de analizar completamente los requerimientos antes de comenzar
- Comprensión de la arquitectura de microservicios independientes
- Evaluación de tecnologías: Spring Boot, Feign, Docker, múltiples bases de datos
- Estimación realista de tiempo y complejidad

---

## Prompt 2: Creación del plan de desarrollo por etapas

### Prompt Utilizado:
```
este fue otro comentario que hizo el profesor sobre el desarrollo:
compartiendo mi experiencia personal, he visto varios tutoriales donde hacen "cosas" multimódulo y en mi caso siempre terminó complicándome la vida, por ese motivo prefiero que cada microservicio sea independiente del resto, lo que además lo hace intercambiable por otras tecnologías de ser necesario, por otro lado, en referencia a los DTO, siempre deberían usarse para el intercambio de información entre servicios.

Podrias crear un plan por etapas para que lo desarrollemos y sigamos la estructura de terminar una etapa, crear una rama en el repo, pushear a esa rama, de ahi arrancar la siguiente etapa y asi sucesivamente.
```

### Respuesta Recibida:
```
[Se creó un plan detallado de 5 etapas con enfoque en microservicios independientes y uso de DTOs]
```

### Modificaciones Realizadas:
- No se realizaron modificaciones de código
- Se estructuró un plan de desarrollo basado en buenas prácticas

### Explicación del Prompt:
Este prompt incorporó la experiencia del profesor para evitar problemas comunes con proyectos multimódulo. Se enfocó en crear microservicios completamente independientes y establecer un flujo de trabajo ordenado con ramas por etapa.

### Aprendizajes Obtenidos:
- Microservicios independientes son más mantenibles que proyectos multimódulo
- DTOs son esenciales para la comunicación entre servicios
- Importancia de un flujo de trabajo estructurado con Git
- Separación clara de responsabilidades entre servicios

---

*[Este archivo se continuará actualizando con cada prompt utilizado durante el desarrollo]*
