# MoonPark Pricing Engine

Spring Boot web service for calculating parking fees using versioned tariff rules.

| Zone | Default pricing |
|---|---|
| M1 | 60 NOK/hour, charged per started minute |
| M2 | 100 NOK per started hour on weekdays; 200 NOK on weekends |
| M3 | Monday-Saturday 08:00-16:00: first 60 daytime minutes per session free, then 2 NOK per started minute; outside daytime: 3 NOK/minute; Sunday: free |

Controllers expose the API, services coordinate use cases, and domain strategies implement pricing.
PostgreSQL stores tariffs, users, clients, and audit data. Liquibase creates and seeds the database.
Caffeine caches fee results and API-key lookups locally; an in-memory registry holds active tariffs.
Redis provides shared Bucket4j rate limiting in production. New zones can reuse existing rule types through the tariff API.

## 1. Prerequisites

- JDK 25 with `JAVA_HOME` configured for building or running the JAR outside Docker.
- PostgreSQL with an existing database and credentials for JAR execution. The application creates tables, not the database itself.
- Docker with Linux containers for container deployment or integration tests (optional for local JAR execution).
  The Docker Compose setup in section 6 supplies Java, Gradle, and PostgreSQL; none needs to be installed on the host.
- Redis reachable from the application for shared production rate limiting.

Commands below use PowerShell from the project root. Copy the complete project to another build machine, including the Gradle wrapper; a separate Gradle installation is unnecessary.
On Linux/macOS, use `./gradlew` instead of `.\gradlew.bat` and your shell's environment-variable syntax.

## 2. Run locally

1. Start PostgreSQL and create a database named `moonpark_dev`, with a user allowed to create schemas and tables.
   The development defaults are username/password `moonpark`/`moonpark`.
   Alternatively, connect to an existing development database:

   ```powershell
   $env:DB_URL = "jdbc:postgresql://localhost:5432/moonpark_dev"
   $env:DB_USERNAME = "your-db-user"
   $env:DB_PASSWORD = "your-db-password"
   ```

2. Start the application:

   ```powershell
   .\gradlew.bat bootRun
   ```

3. Check readiness:

   ```powershell
   curl.exe http://localhost:8080/actuator/health/readiness
   ```

   Wait for HTTP 200 and `"status":"UP"`. Local rate limiting uses memory; Redis is not required.

4. Try a public calculation:

   ```powershell
   curl.exe "http://localhost:8080/takst?zone=M1&start=2026-10-22T08:00&end=2026-10-22T09:01"
   ```

   The seeded M1 tariff returns 61 NOK for this period. Later tariff changes can affect results.
   Times without an offset use `PRICING_TIME_ZONE` (default `Europe/Oslo`).

If port 8080 is occupied, set `$env:PORT = "8081"` before starting and adjust the URLs.

## 3. Swagger and authentication

| Resource | Local URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Public calculation | http://localhost:8080/takst |
| Versioned public calculation | http://localhost:8080/api/v1/takst |

Both calculation GET endpoints are public and rate-limited. Batch calculation and administration require permissions.

In Postman, send `POST http://localhost:8080/api/v1/auth/login` with **Body > raw > JSON**:

```json
{
  "username": "superadmin",
  "password": "ChangeMe-Local-123"
}
```

These are **development-only bootstrap credentials**, created if no super admin exists and not overridden.
Use the returned `accessToken` in Postman's **Authorization > Bearer Token** field, or Swagger's **Authorize > bearerAuth** field.
JWTs default to 30 minutes. For an external client, send its full key in `X-API-Key` instead; do not send both credentials.
Client creation/key rotation returns the full API key once.

## 4. Tests and JaCoCo

| Command | Purpose |
|---|---|
| `.\gradlew.bat test` | Unit and MVC slice tests; no Docker required |
| `.\gradlew.bat integrationTest --rerun-tasks` | Full-context API tests against disposable PostgreSQL when Docker is available |
| `.\gradlew.bat test jacocoTestReport` | Generate regular-test coverage |
| `.\gradlew.bat check` | Tests, integration tests, and configured coverage verification |
| `.\gradlew.bat build` | Verification plus application JAR |

Integration tests are tagged `integration` and use `@Testcontainers(disabledWithoutDocker = true)`.
Without Docker they are **skipped**, not failed. They do not run automatically at application startup and never use the production database.
Docker availability alone does not prove test success: image pulls, migrations, and assertions must also succeed.
CI should provide Docker and check that integration tests were actually executed.

Open these HTML reports in a browser:

```text
build\reports\tests\test\index.html
build\reports\tests\integrationTest\index.html
build\reports\jacoco\test\html\index.html
```

The JaCoCo report covers the regular `test` task, not combined integration coverage.

## 5. Build once and deploy a JAR

On the build machine:

```powershell
.\gradlew.bat test bootJar
```

Copy `build\libs\app.jar` to the target machine. That machine needs Java 25 and database connectivity, but not Gradle or source code.
Configure the environment as described below, then run:

```powershell
java -jar .\app.jar
```

## 6. Build and run with Docker

### Recommended: application and PostgreSQL together

Install and start Docker Desktop with **Linux containers** and Docker Compose v2 or newer.
On Linux, use Docker Engine with the Compose plugin instead. Run all commands from the project root.
On Windows, complete Docker Desktop's first-run setup and ensure its WSL 2 or Hyper-V backend is
working; enable hardware virtualization and required Windows features if Docker Desktop requests them.
Installing the Docker CLI alone is not sufficient.
Check that the Docker server is reachable:

```powershell
docker version
docker compose version
```

Start the complete local stack:

```powershell
docker compose up --build --wait --wait-timeout 240
```

This command runs the containers in the background and waits for both services to be healthy.
The first build needs internet access to download Java/PostgreSQL images, Gradle, and Maven dependencies.
You do **not** need host Java, Gradle, PostgreSQL, a prebuilt JAR, or manually created Docker networks.

The included files provide the following:

| File | Purpose |
|---|---|
| `Dockerfile` | Builds the JAR from source with Java 25, then runs it in a separate Java 25 runtime image as a non-root user; checks application readiness |
| `.dockerignore` | Includes only the build configuration, Gradle wrapper, and source files in the Docker build context |
| `compose.yaml` | Starts PostgreSQL 17 with persistent storage, waits for database health before starting the application, and publishes the application only on host loopback |

Liquibase creates and seeds tables automatically. The application connects to `postgres:5432` on the
Compose network; the database port is not exposed on the host. Rate limiting uses local memory,
so this development setup does not require Redis.

Verify readiness and try a calculation:

```powershell
curl.exe --fail http://localhost:8080/actuator/health/readiness
curl.exe --fail "http://localhost:8080/takst?zone=M1&start=2026-10-22T08:00&end=2026-10-22T09:01"
```

Readiness should return HTTP 200 and `"status":"UP"`; the seeded calculation returns 61 NOK.
Open http://localhost:8080/swagger-ui.html for Swagger UI. The development login from section 3 applies.

**Local development only:** this Compose stack uses the application's development JWT/bootstrap defaults
and database credentials `moonpark`/`moonpark`. Do not expose it publicly or use it for production.
For production, build the same application image but deploy it with the settings and safeguards in section 7.

### Logs, shutdown, restart, and rebuilding

```powershell
docker compose ps
docker compose logs --tail 100 app postgres
docker compose logs -f app
```

Press Ctrl+C to stop following logs; containers keep running.

```powershell
# Stop and remove containers and their network; keep database data.
docker compose down

# Start again, keeping existing database data.
docker compose up --wait --wait-timeout 240

# After source or Dockerfile changes, rebuild and recreate as needed.
docker compose up --build --wait --wait-timeout 240
```

Database data persists in the Compose-managed `postgres-data` volume across container removal,
rebuilds, and Docker restarts. **Do not run `docker compose down --volumes` unless you intend to
permanently erase this stack's database.** Existing data and accounts are not re-seeded or reset on each startup.
The image build compiles the application; it does not run tests. Use section 4 for test commands.

### Troubleshooting

| Problem | Action |
|---|---|
| Cannot connect to the Docker daemon / missing `dockerDesktopLinuxEngine` pipe | Start Docker Desktop, select Linux containers, and wait until `docker version` shows a Server section |
| Docker Server API returns HTTP 500 or Desktop stays stopped | Open Docker Desktop and complete setup or resolve its backend error; check Settings > General for the selected backend and Troubleshoot for diagnostics. Do not retry application startup until `docker version` succeeds |
| Host port 8080 is already in use | Set `$env:MOONPARK_PORT = "8081"`, rerun the Compose startup command, and use `http://localhost:8081` |
| Build cannot download images, Gradle, or dependencies | Check internet connectivity and Docker Desktop proxy/DNS settings, then rerun the build command |
| Startup times out or a service is unhealthy | Run `docker compose ps` and `docker compose logs --tail 200 app postgres`; fix the reported error before retrying |
| Database authentication fails after configuration changes | Ensure application and PostgreSQL credentials match; PostgreSQL initialization variables do not change credentials already stored in the volume |
| Application health check fails | Inspect application logs for migration/startup errors; check database readiness with `docker compose exec postgres pg_isready -U moonpark -d moonpark_dev` |

`--wait-timeout` controls the health wait, not image download/build time. On a slow machine you can
increase it, but a successful command requires both services to become healthy.
Docker health checks report failures; the restart policy restarts exited containers, not containers
that are merely unhealthy.

### Application image with an existing database

If you already have PostgreSQL, build and run only the application image:

```powershell
docker build -t moonpark-pricing:local .
docker run -d --name moonpark-pricing `
  -p 127.0.0.1:8080:8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/moonpark_dev" `
  -e DB_USERNAME="moonpark" `
  -e DB_PASSWORD="moonpark" `
  -e RATELIMIT_STORE="local" `
  moonpark-pricing:local
docker logs -f moonpark-pricing
```

This alternative uses Docker Desktop and an existing `moonpark_dev` database on the host.
Allow database connections from Docker's network in PostgreSQL's listener, access rules, and the host firewall.
Inside a container, `localhost` refers to that container.
For a remote database, use its hostname; for another container, use its service/container DNS name on a shared Docker network.
On Linux Docker Engine, add `--add-host host.docker.internal:host-gateway` to the run command
when using the host database. Do not start this example on the same host port as the Compose stack.

To transfer the built image to another Docker machine without a registry:

```powershell
docker save -o moonpark-pricing.tar moonpark-pricing:local
# Copy the archive to the target machine, then:
docker load -i .\moonpark-pricing.tar
```

Use the appropriate database settings when running it there. Lifecycle commands:

```powershell
docker stop moonpark-pricing
docker start moonpark-pricing
```

## 7. Production configuration

Use the `prod` profile and inject settings through your deployment's secret/configuration mechanism.
Do not commit credentials, bake them into images, or use development defaults.

| Environment variable | Production value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | PostgreSQL JDBC URL reachable from the application |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | Strong random secret of at least 32 characters; identical across instances |
| `REDIS_HOST`, `REDIS_PORT` | Standalone Redis endpoint; port defaults to 6379 |
| `REDIS_PASSWORD` | Redis credential, where required |
| `RATELIMIT_STORE` | `redis` (production default) |
| `PRICING_TIME_ZONE` | `Europe/Oslo` by default |
| `SUPER_ADMIN_USERNAME`, `SUPER_ADMIN_EMAIL`, `SUPER_ADMIN_PASSWORD` | First deployment only; password at least 12 characters |

After injecting those variables, run `java -jar .\app.jar`, or pass the deployment environment to the Docker container.
For an environment file supplied securely outside source control:

```powershell
docker run -d --name moonpark-pricing --restart unless-stopped `
  --env-file .\production.env `
  -p 127.0.0.1:8080:8080 `
  moonpark-pricing:local
```

This binds to the host loopback interface for a reverse proxy; change networking to match your platform.
Remove bootstrap credentials from deployment secrets after the initial account is created.

Before exposing the service:

- Put HTTPS at a trusted reverse proxy/ingress; block direct untrusted traffic and sanitize forwarded headers.
- Configure persistent PostgreSQL storage, backups, and suitable migration privileges.
- Keep Redis private and authenticated. The current custom rate-limit Redis client supports standalone host/password settings; Redis TLS/cluster support needs additional implementation before using endpoints that require it.
- Restrict monitoring endpoints and supply the OTLP collector endpoint if using production tracing.
- Verify readiness, execute integration tests with Docker, and load-test rate limiting.

Redis failures temporarily fall back to per-instance limits, so global limits become less strict.
Tariffs remain in memory if refresh fails, but database failure makes readiness unhealthy and can stop platform traffic routing.

## 8. Actuator checks

These health endpoints are public:

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe http://localhost:8080/actuator/health/liveness
curl.exe http://localhost:8080/actuator/health/readiness
```

- **Liveness:** whether the application should be restarted.
- **Readiness:** whether it should receive traffic; checks application state, database, and tariff loading.
- Healthy checks return HTTP 200; unhealthy readiness normally returns HTTP 503. Production hides health details.

Metrics require a SUPER_ADMIN bearer token. In Postman, select **Authorization > Bearer Token** and call:

```text
GET http://localhost:8080/actuator/metrics
GET http://localhost:8080/actuator/metrics/http.server.requests
GET http://localhost:8080/actuator/prometheus
```

Prometheus/Grafana dashboards are not bundled. Replace `localhost:8080` with your deployed HTTPS host when testing another environment.
