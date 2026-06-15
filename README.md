# NovoBanco Account Service

Microservicio de Gestión de Cuentas y Transacciones para NovoBanco. Permite crear cuentas bancarias, realizar depósitos, retiros y transferencias atómicas entre cuentas, y consultar historial de movimientos paginado.

## Arquitectura

![Arquitectura AWS](img/BI_Diagram.svg)

```
Internet → ALB (public subnets) → ECS Fargate (private subnets) → RDS SQL Server (private subnets)
                                         ↕
                                   Secrets Manager
```

**Componentes AWS:**
- **VPC** con subnets públicas (ALB) y privadas (ECS, RDS)
- **Application Load Balancer** como punto de entrada
- **ECS Fargate** ejecutando el contenedor del microservicio (2 réplicas)
- **RDS SQL Server** como base de datos relacional
- **Secrets Manager** para gestión de credenciales
- **NAT Gateway** para que ECS acceda a ECR desde subnets privadas
- **CloudWatch** para logs centralizados

## Stack Tecnológico

| Componente | Tecnología | Justificación |
|---|---|---|
| Backend | Spring Boot 3 + Java 21 | Framework maduro, ecosistema amplio, soporte empresarial |
| Base de Datos | Microsoft SQL Server | SGBD robusto para operaciones financieras, soporte ACID completo |
| ORM | Hibernate + Spring Data JPA | Mapeo objeto-relacional con repositorios declarativos |
| AWS Compute | ECS Fargate | Serverless containers, sin gestión de EC2 |
| Exposición | Application Load Balancer | Balanceo de carga, health checks, alta disponibilidad |
| Secretos | AWS Secrets Manager | Rotación automática, inyección en ECS sin hardcodear |
| IaC | CloudFormation (nested stacks) | Nativo AWS, sin dependencias externas |
| Testing | JUnit 5 + Mockito | Estándar de la industria para Java |
| Contenedores | Docker (multi-stage build) | Imagen optimizada solo con JRE |

## Instrucciones de Ejecución

### Prerequisitos
- Docker y Docker Compose instalados

### Levantar todo con un solo comando

```bash
docker compose up -d
```

Esto levanta:
1. SQL Server con la base de datos `novobanco` inicializada (schema + datos seed)
2. El microservicio en `http://localhost:8080/api`

### Verificar que está corriendo

```bash
curl http://localhost:8080/api/actuator/health
```

### Detener y limpiar

```bash
docker compose down -v
```

### Ejecución local (desarrollo)

Si prefieres ejecutar el microservicio fuera de Docker:

1. Levantar solo la BD:
```bash
cd database
docker compose up -d
```

2. Ejecutar el microservicio:
```bash
cd account-service
mvnw.cmd spring-boot:run
```

Las credenciales de desarrollo están en `account-service/.env.example`.

## Endpoints

Base URL: `http://localhost:8080/api`

### Cuentas

| Método | Ruta | Descripción |
|---|---|---|
| POST | /accounts | Crear cuenta |
| GET | /accounts/{id} | Obtener cuenta por ID |
| GET | /accounts/{id}/balance | Consultar saldo |
| GET | /accounts/{id}/transactions?page=0&size=20 | Historial paginado |

### Transacciones

| Método | Ruta | Descripción |
|---|---|---|
| POST | /transactions/deposit | Depositar fondos |
| POST | /transactions/withdraw | Retirar fondos |
| POST | /transactions/transfer | Transferir entre cuentas |

### Health Check

| Método | Ruta | Descripción |
|---|---|---|
| GET | /actuator/health | Estado del servicio |

### Códigos de Respuesta

| Código | Significado |
|---|---|
| 200 | Operación exitosa |
| 201 | Recurso creado |
| 400 | Validación fallida o datos inválidos |
| 404 | Recurso no encontrado |
| 422 | Regla de negocio violada (saldo insuficiente, cuenta bloqueada) |
| 500 | Error interno |

## Pruebas

### Pruebas unitarias (16 tests)

Se testea la lógica de negocio de los servicios usando **JUnit 5 + Mockito**, sin conexión a base de datos:

**AccountServiceTest (6 tests):**
- Crear cuenta con cliente válido
- Crear cuenta con cliente inexistente → `ClientNotFoundException`
- Obtener cuenta existente
- Obtener cuenta inexistente → `AccountNotFoundException`
- Obtener balance exitoso
- Obtener balance de cuenta inexistente → `AccountNotFoundException`

**TransactionServiceTest (10 tests):**
- Depósito exitoso → saldo aumenta
- Depósito a cuenta bloqueada → `AccountNotActiveException`
- Depósito a cuenta inexistente → `AccountNotFoundException`
- Retiro exitoso → saldo disminuye
- Retiro con saldo insuficiente → `InsufficientFundsException`
- Retiro de cuenta bloqueada → `AccountNotActiveException`
- Transferencia exitosa → saldos se mueven correctamente
- Transferencia a misma cuenta → `IllegalArgumentException`
- Transferencia con saldo insuficiente → `InsufficientFundsException`
- Historial paginado devuelve resultados correctos

Ejecutar:
```bash
cd account-service
mvnw.cmd test -Dtest="!ConcurrencyIntegrationTest"
```

### Prueba de integración: Concurrencia

Se valida que el **locking pesimista** previene race conditions en operaciones financieras concurrentes.

**Escenario:** Una cuenta con saldo de $1,000. Se lanzan 10 hilos simultáneos que intentan retirar $200 cada uno.

**Resultado esperado:**
- 5 retiros exitosos (5 × $200 = $1,000)
- 5 retiros rechazados por saldo insuficiente
- Saldo final = $0 (nunca negativo)

**Resultado obtenido:** El test pasa consistentemente, demostrando que el locking pesimista serializa los accesos y previene inconsistencias.

Este test requiere SQL Server corriendo localmente (Docker):
```bash
cd account-service
mvnw.cmd test -Dtest=ConcurrencyIntegrationTest
```

### Pipeline CI (GitHub Actions)

Ante cada push o pull request a `main`, se ejecuta automáticamente:
1. Compilación del proyecto
2. Ejecución de los 16 tests unitarios
3. Build de la imagen Docker

El test de integración se excluye del CI porque requiere una instancia de SQL Server.

## Escenarios de Negocio

### Saldo nunca negativo
Garantizado en dos niveles:
- **Aplicación:** Validación en el servicio antes de debitar (`balance.compareTo(amount) < 0`)
- **Base de datos:** Constraint CHECK (`balance >= 0`) que rechaza cualquier UPDATE que viole la regla

### Cuenta bloqueada o cerrada no puede operar
Cada operación valida el estado de la cuenta antes de ejecutarse. Si no es `ACTIVE`, se lanza `AccountNotActiveException` → HTTP 422.

### Transferencia atómica
La transferencia se ejecuta dentro de una sola transacción SQL (`@Transactional`). Si alguna parte falla (saldo insuficiente, cuenta inactiva, error de BD), se revierte todo automáticamente. No existe estado intermedio donde una cuenta está debitada y la otra no está acreditada.

### Concurrencia (race conditions)
Se utiliza **locking pesimista** (`SELECT ... FOR UPDATE`) para evitar que dos operaciones simultáneas sobre la misma cuenta generen inconsistencias. Adicionalmente, los locks se adquieren en orden determinístico (por UUID menor primero) para prevenir deadlocks en transferencias.

### Idempotencia
Cada transacción tiene una referencia única (constraint UNIQUE en BD). Si se intenta registrar una transacción duplicada, la BD la rechaza.

### Resiliencia ante fallo del contenedor
Si el contenedor ECS muere a mitad de una operación, la transacción SQL nunca hace commit → rollback automático. ECS Fargate detecta el contenedor caído y lanza uno nuevo. El estado de la BD permanece consistente.

## ADRs (Architecture Decision Records)

### ADR-1: Spring Boot sobre Quarkus

**Contexto:** Se necesita un framework Java para construir el microservicio. Las opciones principales son Spring Boot y Quarkus.

**Opciones consideradas:**
- Spring Boot 3: ecosistema maduro, documentación extensa, comunidad grande
- Quarkus 3: arranque rápido, menor consumo de memoria, compilación nativa

**Decisión:** Spring Boot 3.

**Consecuencias:**
- (+) Familiaridad del equipo y evaluadores con el framework
- (+) Más documentación y respuestas disponibles ante problemas
- (+) Spring Data JPA simplifica repositorios
- (-) Mayor tiempo de arranque (~3-5s vs ~0.5s de Quarkus)
- (-) Mayor consumo de RAM (~200MB vs ~80MB)
- Mitigación: el tiempo de arranque no es crítico en ECS Fargate (no es serverless con cold starts)

### ADR-2: Locking pesimista para concurrencia

**Contexto:** Operaciones financieras concurrentes sobre la misma cuenta pueden generar race conditions y saldos incorrectos.

**Opciones consideradas:**
- Locking optimista (`@Version`): sin bloqueo, reintento ante conflicto
- Locking pesimista (`FOR UPDATE`): bloquea la fila durante la transacción
- UPDATE atómico en una sentencia: `UPDATE SET balance = balance - X WHERE balance >= X`
- Isolation level SERIALIZABLE: la BD gestiona todo

**Decisión:** Locking pesimista con `@Lock(PESSIMISTIC_WRITE)`.

**Consecuencias:**
- (+) Garantiza consistencia sin lógica de reintento
- (+) Simple de implementar y entender
- (+) La contención es baja (conflictos solo en la misma cuenta al mismo tiempo)
- (-) Bloquea la fila, reduciendo throughput si hay mucha contención
- Mitigación: las transacciones son cortas (milisegundos), el lock se libera rápido
- Prevención de deadlocks: locks adquiridos en orden determinístico por UUID

### ADR-3: ECS Fargate sobre EC2 y Lambda

**Contexto:** Se necesita un servicio de cómputo en AWS para ejecutar el contenedor del microservicio.

**Opciones consideradas:**
- EC2 + ECS: control total, pero requiere gestión de instancias
- ECS Fargate: serverless containers, sin gestión de infraestructura
- Lambda: serverless functions, pago por invocación

**Decisión:** ECS Fargate.

**Consecuencias:**
- (+) Sin gestión de EC2 (patching, scaling de instancias)
- (+) Conexiones persistentes a SQL Server (connection pool estable)
- (+) Health checks del ALB garantizan disponibilidad
- (+) Escalado horizontal simple (aumentar DesiredCount)
- (-) Más costoso que Lambda en tráfico muy bajo
- (-) No escala a cero (siempre hay al menos 1 task corriendo)
- Lambda fue descartada porque: requiere RDS Proxy para connection pooling, cold starts en operaciones financieras son inaceptables, y el locking pesimista necesita conexiones estables

## Supuestos

1. Los saldos insertados en `data.sql` representan el estado actual de las cuentas (post-transferencias). Las transferencias registradas son historial de movimientos previos.
2. No se implementa autenticación/autorización. En producción se agregaría un API Gateway con JWT o Cognito.
3. La moneda es siempre USD. No se implementa soporte multi-moneda.
4. El número de cuenta se genera aleatoriamente (10 dígitos). En producción se usaría un servicio centralizado de generación secuencial.
5. No se implementa CRUD de clientes. Los clientes existen como data seed para que las cuentas tengan propietario.
6. El `docker-compose.yml` raíz es para evaluación local. La infraestructura de producción se define en CloudFormation (`IaC/`).
