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

## 2026-10-05 — Money as exact decimals (`refactor/money-bigdecimal`)

- **Problem:** `transactions.amount` was `Double` / `DOUBLE PRECISION` while budgets already used `BigDecimal` / `NUMERIC(19,2)`. Java code wrapped every read in `BigDecimal.valueOf(double)`, which happens to give exact Java-side sums. But any **SQL** aggregation over the float column drifts, and unit 4c computes budget spending with SQL `SUM`.
- **Evidence:** on the float schema, two expenses of 0.10 and 0.20 gave `SELECT SUM(amount)` = `0.30000000000000004`. After `V2` migrated the same rows: `0.30`.
- **Fix:**
  - `V2__transaction_amount_numeric.sql`: `ALTER COLUMN amount TYPE NUMERIC(19,2) USING ROUND(amount::NUMERIC, 2)` + `CHECK (amount > 0)`.
  - `BigDecimal` end-to-end: entity, create/update/filter DTOs, controller params, specification, services, PDF/CSV utilities, sync mapper.
  - `@Digits(integer = 17, fraction = 2)` on create/update DTOs: `10.999` is rejected (400) instead of silently rounded.
- **Gotcha (hashing):** `BigDecimal("12000")` and `BigDecimal("12000.00")` have different `toString()`s. The API may receive the former while Postgres returns the latter, so the content hash (used for import de-duplication) would change without the data changing. `computeHash()` and `HashUtils` now normalize with `setScale(2).toPlainString()`.
- **Gotcha (Flyway):** `V2` converted rows that already existed. `ALTER ... TYPE ... USING` needs an explicit conversion expression for float → numeric; the `ROUND(..., 2)` makes the precision loss explicit rather than implicit.
- **Wire compatibility:** JSON numbers bind to `BigDecimal` on the server and to `Double` in the current Android DTOs, so the app keeps working unchanged; Android-side money types get aligned in the API-contract phase.
- **Verified:** suite 33/33 (incl. `contextLoads` applying V2 on a DB with existing float rows); Flyway history `1:true 2:true`; column `numeric(19,2)`; `10.999` → 400; `10.50` → 201.

## 2026-10-05 — Budgets track real spending (`feat/budget-spending`)

- **Problem:** `budgets.spent_amount` was never written by any code path (only the CSV import set it). Every budget's spent/remaining/percentage, the dashboard's remaining budget and the budget PDF reported ₹0 spent forever. Budgets also had no category, so there was no way to link them to transactions.
- **Fix:**
  - `V3`: optional `budgets.category` (NULL = all expense categories), drop `spent_amount`, `CHECK (end_date >= start_date)`.
  - `BudgetSpendingCalculator.spentFor(budget)`: `SUM(amount)` of the owner's EXPENSE transactions in `[startDate, endDate + 1 day)`, filtered by category (case-insensitive) when the budget has one.
  - `BudgetResponseDTO.fromEntity(budget, spent)`; every consumer (budget API, dashboard, budget PDF) gets spending from the calculator. CSV export/import swaps `spentAmount` for `category`.
  - DTOs: optional `category` (blank → null), `@AssertTrue isDateRangeValid()`.
- **Design choice:** two repository queries (with/without category) instead of one JPQL `(:category IS NULL OR ...)`. Binding a null to an untyped `? IS NULL` parameter can fail on Postgres ("could not determine data type of parameter"), and two explicit queries are clearer anyway.
- **Design choice:** compute on read instead of storing a running total. One indexed `SUM` per budget (index on `(user_id, transaction_date)`), always correct after edits/deletes, no drift. N+1 per budget list is acceptable at this scale; batch later if needed.
- **Gotcha:** same scale issue as transactions: budget content hash now uses `amount.setScale(2).toPlainString()` and includes `category`.
- **Testing:** `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` runs the real JPQL against the Flyway-migrated Postgres, in a rolled-back transaction. It covers date boundaries (first instant/last second in; day before/after out), case-insensitive category, income excluded, and the all-categories budget.
- **Verified:** suite 38/38; Flyway `1,2,3` applied; live: Food budget 650.00 spent / 350.00 remaining / 65%, all-categories budget 2650.00 / 2350.00 / 53%, dashboard remaining 2700.00, end-before-start → 400, budget PDF → 200.

## 2026-10-05 — Budget notifications that actually fire (`feat/budget-alerts`)

- **Problem 1 (confirmed):** 50/90/100% alerts lived in `BudgetService.updateBudget`, so they only ran when a *budget* was edited, never when an expense was recorded, which is the event that actually crosses a threshold.
- **Problem 2:** three independent `if`s meant one large expense going from 0% to over budget produced three notifications ("50%", "90%", "Exceeded") at once.
- **Problem 3 (confirmed):** `BudgetNotificationScheduler` called `NotificationService.createNotification(dto)`, which resolves the user from `SecurityContext`. Scheduled jobs run on a scheduler thread with no authenticated user, so both daily jobs threw and no expiry notification was ever sent. They also loaded every budget in the DB and filtered in Java.
- **Fix:**
  - `BudgetAlertService`: `stageFor(spent, amount)` gives the highest stage reached. Notify only if it is above `lastNotifiedStage` (one notification per jump, each stage at most once). Called from `TransactionService` create/update for EXPENSE transactions (via `BudgetRepository.findBudgetsCovering`: same user, ACTIVE, date inside range, same category case-insensitive or all-categories budget) and from `BudgetService` create/update.
  - Scheduler addresses notifications explicitly with `createNotificationForUser(budget.getUserId(), ...)` and uses derived queries (`...ExpiryNotificationSentFalseAndEndDateBefore`, `...NearingExpiryNotificationSentFalseAndEndDate`) instead of `findAll()`.
  - Cron expressions moved to `budget.notifications.*` properties so they can be overridden from the command line for testing.
- **Lesson:** anything that runs outside an HTTP request (schedulers, async jobs, event listeners) must not depend on `SecurityContextHolder`; pass the owner id explicitly.
- **Gotcha:** `Budget`'s `@PrePersist` resets the notification flags on insert, so the repository test sets `expiryNotificationSent = true` via a second `save` (update).
- **Gotcha:** the VS Code Java extension in Codespaces writes `.vscode/settings.json`, which `git add -A` silently staged. Now in `.gitignore`. Always check `git status` before committing.
- **Known gap:** `lastNotifiedStage` is not reset when a budget's amount is raised or an expense is deleted, so a stage is never re-announced. Acceptable for now.
- **Verified:** suite 54/54; live: Food 1000 + expense 1200 → only "Budget Exceeded"; Travel 600 → 50%, +300 → 90%, +50 → nothing; scheduler with a 10 s cron sent "Budget Expired" / "Budget Nearing Expiry" exactly once each (job log 1 then 0), flags set, no exceptions in the log.
- **Follow-up fix:** the `budget.notifications.*` block was appended to `application.properties` twice, because the `>>` step was re-run during a long paste. It was harmless, since duplicate keys in `.properties` silently resolve to the last value, but that same behaviour would hide a real conflicting override. Lesson: appending with `>>` is not idempotent, so check `git diff` on config files before committing, not just the test result.

## 2026-10-05 — API contract, backend side (`feat/api-contract-backend`)

- **Problem:** every `/api/transactions` endpoint returned the JPA entity `Transaction`, leaking internals (`contentHash`) and coupling the API to the DB schema. A `TransactionResponseDTO` already existed but lacked `transactionDate`, which the Android model requires; swapping it in as-is would have silently dropped the date. Budget create returned `200` while transaction create returned `201`. There was no machine-readable API spec.
- **Fix:**
  - `TransactionResponseDTO` gains `transactionDate`; every transaction endpoint returns it (paginated via `Page.map`, which keeps the `content/totalPages/number/first/last/empty` shape Android's `PaginatedTransactionResponse` reads).
  - Both creates return `201 Created` + `Location` (`ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")`).
  - springdoc-openapi `2.8.9` (the line targeting Spring Boot 3.5): spec at `/v3/api-docs`, Swagger UI at `/swagger-ui.html`, global JWT `bearerAuth` scheme. Docs paths are `permitAll`; `/api/**` still requires a token.
- **Bug found by the live check (pre-existing):** `POST /api/transactions/filter/paginated` returned `400 "No property 'desc' found"` for the default request AND for what Android sends (`sort=transactionDate,desc`), so pagination never worked. Root cause: `@RequestParam String[] sort` — Spring comma-splits a single value when converting to an array, giving `["transactionDate", "desc"]`, and the hand-written loop sorted by a property named `desc`. Fix: Spring Data `Pageable` with `@PageableDefault(size = 10, sort = "transactionDate", direction = DESC)`; same query parameter names, so no client change.
- **Checked before changing status codes:** Android's `ApiResponseHandler` uses `response.isSuccessful` (any 2xx), so `200 -> 201` is safe. `204` is NOT: Retrofit gives a `null` body for 204 and `handleApi` turns it into "Empty response body" (the existing DELETEs already hit this). The 200-with-empty-body endpoints stay unchanged until the Android handler is fixed (5b).
- **Testing lessons (standalone MockMvc):**
  - It does not apply Spring Boot's Jackson config, so `LocalDateTime` serialized as `[2026,10,5,10,0]` instead of an ISO string. The test now sets a converter with `WRITE_DATES_AS_TIMESTAMPS` disabled, matching the app.
  - It does not register the `Pageable` resolver; added `PageableHandlerMethodArgumentResolver` explicitly.
  - A mocked `new PageImpl<>(List.of())` is unpaged; serializing it calls `Unpaged.getPageNumber()`, which throws, and that surfaced as a 500. Write failures are wrapped in `HttpMessageNotWritableException`, which `ResponseEntityExceptionHandler` answers with 500 **without logging**, so the exception was invisible. Mocks now build the page from the resolved `Pageable`, as the real repository does.
  - Mocking the service with `any(Pageable.class)` hid the sort bug; the new tests capture the `Pageable` and assert its sort.
- **Cleanup:** removed emoji characters from code comments, the welcome email subject and an Android comment (8 files). Several `*ControllerTest`/`*ServiceTest` files are empty placeholders (known gap; the two touched here are now real).
- **Known gap:** Spring Data warns that serializing `PageImpl` directly isn't a stable JSON contract. Kept because Android depends on that shape; move to a stable page DTO together with the Android change.
- **Verified:** suite 61/61; live: `/v3/api-docs` 200 without a token while `/api/**` is 401, Swagger UI 200, transaction create 201 + Location with ISO `transactionDate` and no `contentHash`, paginated default and Android sort 200, unknown sort field 400, budget create 201 + Location, no errors in the log.

## 2026-10-05 — API contract, Android side (`feat/api-contract-android`)

- **Problem 1 (confirmed by reading both sides):** login could never work end-to-end. The backend `AuthResponse` returned the JWT in a field named `message`; Android's `AuthResponseDTO` reads `token`, so Gson left it `null` and `AuthRepoImpl` stored `null` as the token. Fix: backend field renamed to `token` (the honest name; Android unchanged).
- **Problem 2:** Retrofit used plain `GsonConverterFactory.create()`. Gson has no java.time support, so every DTO with `LocalDateTime` (transactions, notifications) failed to parse the backend's ISO strings, and requests would have serialized dates as objects. Fix: `GsonProvider` with null-safe ISO-8601 `TypeAdapter`s for `LocalDateTime`/`LocalDate`, used by `RetrofitInstance`.
- **Problem 3:** `ApiResponseHandler.handleApi` treated any `null` body as "Empty response body". Retrofit returns a `null` body for 204, so every successful DELETE showed an error. Fix: `handleApi` is now `inline` with `reified T : Any`; a `null` body is `Success(Unit)` only when `T` is `Unit`, and remains an error for calls that expect data (no unchecked cast). The 37 call sites are unchanged.
- **Problem 4:** `TransactionResponseDTO.description` was non-null `String` but optional on the backend; Gson writes `null` into it anyway (it bypasses Kotlin null-safety via reflection). Now `String?`; the one consumer that needed a value uses `?: ""`.
- **Learning:** Gson + Kotlin non-null types is a silent trap: Gson never calls the constructor, so a missing/null field becomes a `null` in a non-null property and crashes later, far from the parse.
- **Testing:** JVM unit tests (no emulator available in Codespaces): `ApiResponseHandlerTest` (204 Unit success, 204 for data is an error, body success, backend error message + code, IOException) and `GsonProviderTest` (parses a body captured from the live backend, null description, ISO serialization of request dates, `LocalDate` round trip).
- **Deferred (5c):** budget DTO alignment (backend `name`/`budgetFrequency`/`budgetStatus`/`LocalDate` and different enum values vs Android `title`/`frequency`/`status`/`String`); `forgot-password`/`reset-password` return plain text and need a scalars converter; WebSocket client is a placeholder.
- **Verified:** Android `testDebugUnitTest` 10/10 and `assembleDebug` successful; backend suite 61/61; live: register/login return `token` + `email`, the token authenticates (200), DELETE returns 204 with an empty body.

## 2026-10-05 — Budget contract, Android side (`fix/budget-contract-android`)

- **Problem (confirmed by reading both sides):** the budget screen and the dashboard's active-budget list could not work. Android's `BudgetResponseDTO` read `title`, `frequency`, `status`; the backend sends `name`, `budgetFrequency`, `budgetStatus`, so Gson left non-null properties `null` and the list would crash on `.name`. Requests sent `title`, so the backend rejected every create/update with "Name cannot be blank". Enum values differed (`EXCEEDED`/`INACTIVE` vs `PAUSED`/`COMPLETED`/`CANCELLED`, no `NONE`), category was required on Android but optional on the backend, and the computed `spentAmount`/`remainingAmount`/`percentageSpent` were never read. `dashboard/BudgetInfo` reuses the same DTO, so the dashboard was broken by the same mismatch.
- **Fix:** Kotlin models renamed to the backend field names (no `@SerializedName`, matching the transaction DTOs), `LocalDate` dates, nullable `category`/`notes`, enums identical to the backend. ViewModel builds DTOs from parsed dates, sends a blank category as `null` (all categories), checks end >= start like the backend `@AssertTrue`, and stores today's date when the form opens because the pickers already display it.
- **Removed:** the Room fallback in `loadBudgets()` rendered local rows as fake budgets that all had `id = -1`; `LazyColumn` keys must be unique, so more than one row would crash. The table is only filled by the unusable sync path, and the app is online-first.
- **Wrong assumption:** we believed the old `"2025-06-19T00:00:00"` start date was rejected by the backend `LocalDate`. The live check showed Jackson accepts it and keeps the date part (201). The type change is still right (match the contract instead of relying on server leniency), but it was not the failure; `title`/`name` was.
- **UI side effect of new enum values:** five frequency chips and `COMPLETED`/`CANCELLED` overflow a phone-width `Row` and Compose clips them silently, making some filters untappable; rows now use `horizontalScroll`. Status chips are shown only when editing, since the create request has no status.
- **Testing:** `BudgetContractTest` (parse the backend response shape incl. null category and `NONE`/`PAUSED`, create request field names and plain dates, update carries status). Live-check script bug on the way: login takes `email`, not `username`; the script passed an error body as the Bearer token and every call was 401. The script now aborts when no token is extracted.
- **Found, deferred:** dashboard `totalBudget`/`remainingBudget` sum over overlapping budgets, so one expense is subtracted once per matching budget. Budget category options come only from existing budgets (empty for a new user).
- **Verified:** Android `testDebugUnitTest` 14/14 and `assembleDebug`; live: budgets created with and without category (201), list and dashboard bodies match the new DTO field-for-field, spending computed (1250.50 against both budgets).

## 2026-10-05 — Password reset responses as JSON (`fix/auth-message-json`)

- **Problem (confirmed):** `forgot-password` and `reset-password` returned `ResponseEntity<String>`, which Spring sends as `text/plain`. Android declared `Response<String>` with only `GsonConverterFactory`, so Retrofit fed the sentence to Gson as JSON; Gson rejects unquoted text, `handleApi` caught the exception, and a successful reset showed "Unexpected error". `AuthContractTest` reproduces it with the app's own converter setup.
- **Learning:** `Response<String>` does not mean "raw body" in Retrofit. Every return type, including `String`, goes through the registered converters; raw text needs `converter-scalars` registered before Gson.
- **Decision:** return JSON `{"message": "..."}` (`MessageResponse`) from the backend instead of adding the scalars converter. Keeps the API JSON-only (documented in Swagger, same `message` field as `ApiError`), adds no Android dependency, and does not change how any future `Response<String>` is parsed. The message wording, including the no-enumeration "If the email exists..." text, is unchanged.
- **Testing:** `UserControllerTest` was an empty placeholder; now standalone MockMvc tests for both endpoints (JSON content type and `message`) and an invalid reset token (400 `ApiError`). Android `AuthContractTest`: plain text fails the Gson converter, JSON message parses.
- **Found, deferred:** `ResetPasswordRequest.newPassword` only has `@Size(min = 8)`, while registration requires lower, upper, digit, and special characters, so a reset can set a weaker password than registration allows. The reset link still uses the placeholder domain `https://yourfrontend.com`.
- **Verified:** backend 64/64; Android `testDebugUnitTest` 16/16 and `assembleDebug`; live: forgot-password (unknown email) 200 `application/json` with `message`, reset-password with a bogus token 400 `application/json` `ApiError`.
