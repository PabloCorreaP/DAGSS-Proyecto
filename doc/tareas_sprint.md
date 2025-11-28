# 1. SPRINT (trabajo a realizar)
- **Objetivo**: Desarrollar los componentes JPA y Spring responsables de la implementación del _backend_ REST del proyecto "Receta electrónica"
- **Inicio**: Semana del 17/11/2025
- **Duración**: Hasta entrega 21/01/2026 (9 semanas)
- **Proyecto de partida:** [GitHub: recetas-24](https://github.com/esei-si-dagss/recetas-24) (Proyecto Spring Boot de partida, con la jerarquía de entidades incompletas encargada de almacenar los diferentes tipos de usuario de la aplicación) 

## TAREAS DEL SPRINT

A continuación se propone una organización orientativa, no obligatoria, si bien es cierto que deben cumplirse los tres hitos descritos.

### Fase 1: Modelado y Entidades JPA (17-30/11/2025) [2 semanas]
- Estudio de la descripción del proyecto y las [historias de usuario](historias.md) incluidas en el _Sprint Backlog_.
- Creación del modelo E-R con las entidades y relaciones necesarias para dar soporte a las historias del Sprint.
- Implementar las entidades JPA correspondientes, junto con sus relaciones.

> **Hito de evaluación 1** (semana 01-07/12/2025): Modelo E-R completado para revisar.

### Fase 2: Capa de Persistencia y Servicios (01-14/12/2025) [2 semanas]
- Implementar los DAOs (_data access object_)/Repositorios de Spring Data JPA necesarios, definiendo las consultas (mediante JPQL en los casos que corresponda) necesarias para dar soporte a las funcionalidades a usar en las historias de usuario incluidas en el Sprint.
- Implementación de la capa de servicios (`@Service`) para dar soporte a las historias que los necesiten.

### Fase 3: Diseño de API REST (08-21/12/2025) [2 semanas, solapamiento 1 semana]
- Listado de los _endpoints_ REST a implementar.
- Esbozar implementación con Spring MVC.

> **Hito de evaluación 2** (semana 15-21/12/2025): Diseño de API REST completado para revisar.

### Fase 4: Implementación y Entrega (22/12/2025-21/01/2026) [4 semanas, incluyendo vacaciones de Navidad]
- Implementación completa del API REST.
- (Opcional) Inclusión de tests automáticos (`@SpringBootTest`, `@DataJpaTest`, `@WebMvcTest`).
- Redacción del documento entregable.

> **Hito de evaluación 3** (21/01/2026): Proyecto entregable, tal y como se describe a continuación.

### Resumen

| FASE   | Semana 1<br>(17-23/11) | Semana 2<br>(24-30/11) | Semana 3<br>(01-07/12) | Semana 4<br>(08-14/12) | Semana 5<br>(15-21/12) | Semanas 6-9<br>(22/12-21/01) |
|--------|------------------------|------------------------|------------------------|------------------------|------------------------|-----------------------------|
| Fase 1 | Modelado y Entidades   | Modelado y Entidades   | **Hito 1**             |                        |                        |                             |
| Fase 2 |                        |                        | Persistencia y Servicios| Persistencia y Servicios|                        |                             |
| Fase 3 |                        |                        |                        | Diseño API REST        | Diseño API REST<br/>**Hito 2**   |                             |
| Fase 4 |                        |                        |                        |                        |                        | Implementación y Entrega final    |

# 2. ENTREGA

* Proyecto en grupos de **3 personas**
* Entrega en Moovi (**hasta el 21/01/2026**, fecha del examen) 
* Requisitos mínimos:
   * Entrega de una clase auxiliar o consultas SQL para la inicialización de la base de datos (datos de prueba de la aplicación).
   * Soporte para las siguientes historias de usuario:
      * HU-M4. [Médico] Creación de _prescripciones_
      * HU-M5. [Médico] Generación de "planes de _recetas_" para una _prescripción_
      * HU-P3. [Paciente] Creación de _cita_
      * HU-P4. [Paciente] Visualización de "planes de _recetas_"

## Contenidos de los documentos entregables

### Informe del proyecto

#### Material a entregar (en un único ZIP)

* Carpeta con código fuente del proyecto (haced antes `mvn clean` para que sólo se incluya el código fuente)
* Memoria del proyecto (ver estructura propuesta)

#### Estructura propuesta para la memoria del proyecto (extensión orientativa: hasta 7-8 páginas)

**Parte 1: Descripción técnica**
1. Breve descripción general del proyecto.
2. Descripción de las funcionalidades cubiertas.
3. Descripción de los elementos que conforman la implementación realizada:
    - Entidades JPA (acompañadas de modelo E-R).
    - Repositorios/DAOs y servicios.
    - _Controllers_ REST de Spring MVC.
      - **Importante:** detallar los _endpoints_ REST y las operaciones implementadas sobre ellos.
    - Otros detalles de configuración relevantes.
4. Instrucciones de compilación y uso (si fueran necesarias).
5. Detalle del reparto de tareas entre los miembros del grupo.

**Parte 2: Revisión crítica de la arquitectura del proyecto Spring**
* Se trata de realizar una revisión de la arquitectura de la aplicación y de las cuestiones de diseño que pudieran ser incorrectas o mejorables en la solución propuesta (duplicidad de código, mejor uso de herencia, _sobreingeniería_ provocada por el uso de Spring, etc.), así como las impresiones personales sobre las soluciones adoptadas.
   * **Importante:** detallar los patrones de diseño empleados o aplicados. 
* El formato es libre, como orientación se pueden abordar los siguientes puntos:
   * Conclusiones, problemas y/o mejoras respecto a la arquitectura general del proyecto y de la idoneidad del empleo de las tecnologías de Spring.
   * Conclusiones, problemas y/o mejoras respecto al diseño del modelo de datos (entidades JPA) propuesto [aspectos poco claros, mejoras en la modelización, relaciones superfluas, diseños alternativos, etc.].
   * Conclusiones, problemas y/o mejoras respecto al diseño de la capa de lógica [limitaciones y mejoras de la arquitectura empleada, ideas para la mejora de la organización y reutilización del código, dificultades de las tecnologías empleadas, etc.].
   * Conclusiones, problemas y/o mejoras respecto al diseño de la capa REST [limitaciones y mejoras de la arquitectura empleada, ideas para la mejora de la organización y reutilización del código, dificultades de las tecnologías empleadas, etc.].
   * Conclusiones generales de la práctica y de las tecnologías empleadas.