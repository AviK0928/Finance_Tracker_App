# Engineering Log

## 2026-10-05 — Build & boot baseline (`fix/build-boot`)

**Context:** Builds can't run on the dev machine. All verification runs in GitHub Codespaces: JDK 17, Postgres 16 in Docker, Android SDK 35, Gradle 8.9.

### Backend failed at startup
- **Error:** `QueryCreationException ... Attribute 'BlacklistedToken#expiry(BASIC)' is not joinable`.
- **Root cause:** Spring Data derives queries from method names. `deleteByExpiryDateBefore` was parsed as the path `expiry.date`, so Hibernate tried to join into a basic column.
- **Fix:** method names must match entity fields → `deleteByExpiryBefore`.
- **Learning:** Spring Data validates *all* derived queries at startup but fails on the *first* bad repository, which hides the rest. `findByUserIdAndIsReadFalse` / `countByUserIdAndIsReadFalse` had the same bug (field is `read`, not `isRead`) → `...ReadFalse`.
- **Verified:** `Started FinanceTrackerApplication in ~12s`.

### Android kapt failed
- **Hilt plugin missing:** `Expected @HiltAndroidApp to have a value. Did you forget to apply the Gradle Plugin?` The short form `@HiltAndroidApp class X : Application()` relies on the Hilt Gradle plugin's bytecode transform. Fix: apply `com.google.dagger.hilt.android` (Dagger bumped 2.48 → 2.51.1 as a Kotlin 2.0 / AGP 8.7 compatibility precaution).
- **`[Dagger/MissingBinding] TokenManager`:** no `@Inject` constructor or `@Provides`. Fix: `@Singleton class TokenManager @Inject constructor(@ApplicationContext context)`.
- **`[Dagger/MissingBinding] java.lang.Long`:** `NotificationViewModel` injected a bare `userId: Long`. Primitives need a qualifier + provider, and the client has no source for its user id. Removed; real-time notifications will be redesigned.
- **Verified:** `assembleDebug` BUILD SUCCESSFUL (7m 49s first run). AGP auto-installed Build-Tools 34; kapt warns it falls back to Kotlin 1.9 (move to KSP later).

### Toolchain / environment
- **JDK 25 breaks both builds.** The Codespaces default JDK is 25.
  - Android: Gradle 8.9 / Kotlin 2.0 can't parse its version string (`What went wrong: 25.0.4.1`).
  - Backend: Lombok 1.18.30 doesn't run on it, so no getters/setters/builders were generated (~200 `cannot find symbol` errors). The `sun.misc.Unsafe ... terminally deprecated` warning was the giveaway that Maven was on JDK 24+.
  - **Fix:** use JDK 17 (`pom.xml` targets 17). Planned: pin the JDK in `.devcontainer`, and add a Maven enforcer rule + explicit Lombok annotation-processor config.
- **Maven wrapper:** `bash mvnw` fails (`mvnw/.mvn/wrapper/...: Not a directory`) because the script derives its base dir from `$0`. Use `bash ./mvnw`; executable bits on `mvnw`/`gradlew` are missing in git.
- **Gradle wrapper:** `gradle-wrapper.jar` is not committed (root `.gitignore` `*.jar` overrides the `!gradle/wrapper/gradle-wrapper.jar` exception), so `./gradlew` can't run. A downloaded Gradle 8.9 is used until this is fixed in the repo-hygiene unit.

## 2026-10-05 — Reproducible dev environment & repo hygiene (`chore/dev-env`)

### Config & secrets
- **Change:** DB settings now come from env vars (`DB_URL`, `DB_USERNAME` with local defaults; `DB_PASSWORD` required). Mail credentials default to empty, so the app boots without SMTP config. `.env.example` documents every variable; the real `.env` stays gitignored.
- **Learning:** the old `qwerty` password remains in git history. Acceptable because it was only ever a local dev password; rewriting history isn't worth the risk.
- **Verified:** backend boots with only `.env` loaded (`Started FinanceTrackerApplication in 12.6s`).

### Build toolchain guardrails
- **Change:** Lombok's version is now managed by `spring-boot-starter-parent` (was a hardcoded 1.18.30) and registered under `maven-compiler-plugin` → `annotationProcessorPaths`. Newer JDKs stop auto-discovering annotation processors on the classpath, which silently breaks Lombok.
- **Change:** `maven-enforcer-plugin` requires JDK `[17,25)`, so a wrong JDK fails with one clear message instead of ~200 `cannot find symbol` errors. (The JDK 25 failure path hasn't been exercised yet.)

### Git hygiene
- **`.gitignore` exception order:** in `.gitignore` the last matching rule wins. `!gradle/wrapper/gradle-wrapper.jar` came *before* two later `*.jar` lines, and it was also anchored to the repo root, so it never matched `Finance_tracker_Frontend/Finance_Tracker/gradle/wrapper/`. Fix: an `!**/gradle/wrapper/gradle-wrapper.jar` exception at the end of the file.
- **`.env.*` swallowed `.env.example`:** added an `!.env.example` exception.
- **Executable bits:** the repo has `core.filemode=false` (created on Windows), so `chmod +x` isn't recorded. Use `git update-index --chmod=+x` on `gradlew`, `mvnw` and the devcontainer scripts.
- **Wrapper jar:** generated with `gradle wrapper --gradle-version 8.9` in a throwaway folder and only the jar copied, so the existing `gradlew` / `gradle-wrapper.properties` weren't rewritten. `./gradlew --version` → Gradle 8.9.
- **Line endings:** `.gitattributes` keeps `*.sh`/`gradlew`/`mvnw` as LF and `*.bat`/`*.cmd` as CRLF; all were already stored as LF, so no renormalisation diff.
- Removed committed Gradle debug logs (`deps.txt`, `annotations-deps.txt`). Rewrote the README (the old one said `./gradlew bootRun` for a Maven project).

### Dev environment
- **Change:** `.devcontainer/` (JDK 17 image + docker-in-docker; `post-create.sh` installs Android SDK 35 with build-tools 34/35, writes `local.properties`, creates `.env` with a generated `JWT_SECRET`; `post-start.sh` waits for Docker, then runs `docker compose up -d db`). `docker-compose.yml` runs Postgres 16 with a health check and a named volume.
- **Verified:** Compose Postgres healthy on 5432; `./gradlew :app:assembleDebug` succeeds through the committed wrapper.
- **First devcontainer build failed (codespace in recovery mode).**
  - **Log:** `GPG error: https://dl.yarnpkg.com/debian ... NO_PUBKEY 62D54FD4003F6525`, then `Feature "Docker (Docker-in-Docker)" failed to install`, exit code 100.
  - **Root cause:** the `devcontainers/java:1-17-bookworm` image ships a Yarn apt source with a missing signing key. The docker-in-docker feature runs `apt-get update`, which fails on that source, aborting the image build.
  - **Fix:** switch to `devcontainers/base:bookworm` and install the JDK via the `ghcr.io/devcontainers/features/java:1` feature, pinned to version 17. This also makes the JDK version explicit in config instead of implied by an image tag.
  - **Learning:** recovery mode means the *container build* failed (image/feature), not a lifecycle script. The cause is in `/workspaces/.codespaces/.persistedshare/creation.log`.
- **The terminal is usable before setup finishes.** Codespaces opens the terminal while `postCreateCommand` (Android SDK install) is still running, and `postStartCommand` (Postgres) only runs after it. Checks run too early showed no SDK, no `.env`, no DB. Setup is done when `docker compose ps` shows `db` as healthy.
- **Verified (fresh codespace from `chore/dev-env`):** JDK 17; `$ANDROID_HOME/platforms` → `android-35`; Compose `db` healthy; `.env` created with `JWT_SECRET`; backend `Started FinanceTrackerApplication` (23.7s cold); `./gradlew :app:assembleDebug` succeeds.

## 2026-10-05 — Authentication hardening (`fix/auth-hardening`)

### Fixed
- **Auth backdoor removed.** `JWTAuthenticationFilter` authenticated any request carrying `Bearer demo-token`, and the Android `TokenManager` sent `demo-token` whenever no real token was stored. Server bypass deleted; client now returns `null` and `AuthInterceptor` omits the header. **Verified:** `demo-token` → 401.
- **Secrets out of logs.** `UserService.login` printed the raw password via `System.out.println`; the filter logged the full `Authorization` header and the JWT at DEBUG. **Verified:** after a register/login run, `grep` for the password in the app log → 0.
- **Account enumeration closed.**
  - Login: unknown email threw `UserNotFoundException` (404) while a wrong password threw `InvalidCredentialsException` (401). Both now take one path, `findByEmail(...).filter(passwordMatches).orElseThrow(InvalidCredentials)`. **Verified:** byte-identical responses apart from the timestamp.
  - Forgot password: an unknown email returned 404, contradicting the controller's "If the email exists…" message. Now returns silently. **Verified:** 200 with the generic message.
- **401 vs 403.** With no httpBasic/formLogin configured, Spring Security's default entry point answers unauthenticated requests with 403. Set `HttpStatusEntryPoint(UNAUTHORIZED)` so clients can tell "log in again" from "forbidden". `/error` added to `permitAll` so unhandled errors on public endpoints aren't masked as 401. **Verified:** no token → 401.

### Known gap
- Login still has a timing side channel: BCrypt only runs when the email exists, so response time can hint at account existence. Mitigation (compare against a dummy hash) deferred.

### Testing
- First real tests: `UserServiceTest` (4) and `JWTAuthenticationFilterTest` (4). The filter test uses a **real** `JWTService` (key injected via `ReflectionTestUtils`, then `init()`), so token parsing, generation and expiry are exercised rather than mocked.
- Run with `./mvnw test -Dtest='UserServiceTest,JWTAuthenticationFilterTest'`: `contextLoads` needs a DB and env vars, so it's excluded from the fast loop.

### Gotchas
- **Bash history expansion:** a password like `Wrong1!x` inside double quotes triggers `event not found`, and the request is never sent. Use `set +H` or single quotes.
- **Shell variables don't survive between terminals/runs.** An empty `$EMAIL` made the "wrong password" check hit bean validation (`Email can not be blank`) instead. Verification blocks should be self-contained.

## 2026-10-05 — Global error handling & notification ownership (`fix/error-handling`)

### Error handling
- **Problem:** three copy-pasted, package-scoped `@RestControllerAdvice` classes (Budget, Transaction, User); the other modules had none.
  - Each had a catch-all `@ExceptionHandler(Exception.class)` that swallowed Spring's own MVC exceptions (malformed JSON → 500) and returned raw `ex.getMessage()` to clients.
  - `AccessDeniedException` from `TransactionService` became 500 instead of 403.
  - `Map.of(...)` throws a `NullPointerException` when a message is `null`, so the error handler itself could crash.
- **Fix:** one `GlobalExceptionHandler extends ResponseEntityExceptionHandler`. Spring's standard MVC exceptions (bad JSON, type mismatch, missing params, 405, unknown path) keep correct status codes and are reshaped via `handleExceptionInternal`. Domain exceptions are mapped explicitly: 401/403/404/409/400. Everything else → 500 with a generic message, full stack trace logged server-side only.
- **Design choice:** a custom `ApiError {timestamp, status, error, message, path, fieldErrors?}` instead of Spring's `ProblemDetail`. The Android client's `ErrorUtils` reads `message`; `ProblemDetail` calls it `detail`, and switching would silently break the app's error display. Validation errors now carry a readable `message` (the client previously showed "Unknown error").
- **Gotcha:** some Spring exceptions (e.g. `HttpRequestMethodNotSupportedException` → 405) reach `handleExceptionInternal` with a `null` body; their `ProblemDetail` lives on the exception (`ErrorResponse.getBody()`). Without falling back to it, the 405 message degraded to a generic text.
- **Verified (live, curl):**

  | Case | Before | After |
  |---|---|---|
  | Another user's transaction | 500 | 403 |
  | Bad sort field | 500 | 400 |
  | Invalid reset token | 500 | 400 |
  | Malformed JSON | 500 | 400 |
  | Registration validation | bare field map | 400 with `message` + `fieldErrors` |

### Notification ownership
- **Problem:**
  - `markAsRead` loaded by id with **no owner check**: any user could mark any user's notifications.
  - `delete`/`archive` signalled "not found"/"not yours" with `IllegalArgumentException`/`IllegalStateException`, which surfaced as 500.
  - `POST /api/notifications` let clients create arbitrary notifications.
- **Fix:** private `getOwnedNotification(id)` (missing → `ResourceNotFoundException` 404; other owner → `AccessDeniedException` 403) used by all single-notification operations; create endpoint removed (the server creates notifications internally; the Android app never called it).
- **Verified (live):** Bob marking Alice's notification → 403 (was 200 and succeeded); missing id → 404; `POST /api/notifications` → 405; Alice on her own → 200.

### Tests
- `GlobalExceptionHandlerTest` (9): standalone MockMvc with a throwaway controller. Asserts status codes, body shape, and that internal messages (`secret_table`, ownership detail) never reach the client.
- `NotificationServiceTest` (5): ownership rules, including that `save`/`delete` are never called for a foreign notification.
- Unit suite: 22 tests.

### Known gaps (later units)
- Archived notifications are still returned by `GET /api/notifications`.
- `BudgetNotificationScheduler` calls `createNotification`, which needs a logged-in user, so it fails on the scheduler thread.

## 2026-10-05 — Flyway baseline (`chore/flyway-baseline`)

### Decisions for Phase 2 (domain)
- Budgets get an **optional category** (none = all expenses); spent amount is **computed on read** with a SQL `SUM` instead of a stored column; **online-first** Android app (offline sync deferred to its own phase); the dev DB may be wiped once for a clean baseline.

### Schema under version control
- **Problem:** `ddl-auto=update` let Hibernate create/alter tables silently. No reviewable schema, no indexes, and `user_id` columns were plain numbers with no foreign keys. Deleting an account removed only the `users` row, leaving transactions/budgets/notifications/settings orphaned. A `password_reset_tokens` row (Hibernate-generated FK without cascade) would block the delete entirely.
- **Fix:** Flyway (`flyway-core` + `flyway-database-postgresql`, which Flyway 10+ requires for Postgres) with `V1__baseline_schema.sql`, and `ddl-auto=validate` so Hibernate refuses to start if entities and schema drift apart.
- **V1 contents:** same columns/types as the entities (money stays `DOUBLE PRECISION` for transactions until V2), plus `ON DELETE CASCADE` FKs to `users`, indexes on `(user_id, transaction_date)`, `budgets(user_id)`, `notifications(user_id, created_at)`, `blacklisted_tokens(expiry)`, and `CHECK` constraints mirroring each Java enum.
- **Learning:** with `validate`, the `contextLoads` test becomes a real migration test: it boots the app against Postgres, applies the migrations, and checks every entity mapping.
- **Learning:** `UID` is a read-only bash variable; use another name in scripts.
- **Verified:** full suite 23/23 (incl. `contextLoads`); log shows `Successfully applied 1 migration ... now at version v1`; delete-account removes the user's transactions and notifications (`1|2` → `0|0|0`).
