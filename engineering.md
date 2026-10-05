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

## 2026-10-05 — One password policy for register and reset (`fix/reset-password-policy`)

- **Problem (confirmed by reading both DTOs):** `RegisterRequest.password` enforced the complexity regex, but `ResetPasswordRequest.newPassword` only had `@Size(min = 8)`, so a reset could set a weaker password (e.g. `password1`) than registration allows. These are the only two paths that set a password.
- **Fix:** `User/dto/PasswordRules` holds the policy as constants (min length, regex, messages) and both DTOs reference it. Bean Validation annotation attributes must be compile-time constants, so shared constants are the simplest single source; a custom constraint annotation would add machinery for no benefit here. Regex and messages unchanged, so registration behaves exactly as before. Validation runs before the controller, so weak passwords never reach `UserService`.
- **Testing:** `UserControllerTest`: weak reset password returns 400 with `fieldErrors.newPassword` and the service is not called; the same weak password is still rejected by register (guards the refactor). Live end-to-end without SMTP: inserted a valid `password_reset_tokens` row for a fresh user with psql (the same row `generateResetToken` creates), then exercised the real endpoint.
- **Found, deferred:** the regex only allows `@$!%*?&` as special characters (`Pass#1234` is rejected), and there is no maximum length although BCrypt only uses the first 72 bytes. Both would change what registration accepts, so they need a separate decision.
- **Verified:** backend 66/66; live: weak reset 400 with the policy message, strong reset 200, old password login 401, new password login 200, reusing the token 400.

## 2026-10-05 — Bodyless success responses return 204 (`fix/empty-body-204`)

- **Change:** notification mark-as-read / mark-all-as-read / archive / archive bulk and settings PUT / reset-to-defaults / logout returned `200` with an empty body; now `204 No Content`, consistent with the DELETEs in the same controllers.
- **Not a client bug (confirmed by reading Android):** every one of these calls is `Response<Unit>`. Retrofit (2.6+) has a built-in `Unit` converter, so a 200 empty body already became `Unit`, and `handleApi` treats a 204 `null` body as `Success(Unit)` (unit 5b). So this is a contract cleanup with no Android change.
- **Deliberately excluded: import-data.** `ImportService.importUserData` returns an `ImportSummaryDTO` (imported/skipped counts), but `UserSettingService.importDataForUser` is `void` and drops it; Android already has an unused matching `ImportSummaryDTO`. Returning 204 would cement that loss; the right fix is 200 with the summary, done together with the export/import filename mismatch.
- **Testing:** `NotificationControllerTest` (was an empty placeholder) and new `UserSettingControllerTest`, standalone MockMvc: each endpoint returns 204 with an empty body and calls the service; logout with a Bearer token blacklists exactly that token, without one it saves nothing. Live: a 1000 Food budget plus a 600 Food expense produced a real notification (50% stage) to act on.
- **Found, deferred:** `updateSettings` takes `@RequestBody List<UpdateSettingDTO>` without `@Valid`, so `@NotNull` on `key`/`value` is never enforced.
- **Verified:** backend 74/74; live: all seven endpoints `204` with a 0-byte body; the token is rejected (401) after logout.

## 2026-10-05 — Settings export and import that round-trip (`fix/settings-export-import`)

- **Problem 1 (confirmed):** `ExportService` wrote `user_settings.csv`, `ImportService` required `settings.csv`, so importing any export failed with 400 "Missing required file". Fix: export uses `ImportService`'s entry-name constants, so the two cannot drift again.
- **Problem 2:** `UserSettingService.importDataForUser` was `void` and discarded the `ImportSummaryDTO` the import already built. Now `POST /import-data` returns 200 with the summary; Android shows "Imported X budgets, Y transactions, Z settings (N already present)".
- **Problem 3 (Android, same mechanism as 5d):** `exportData()` was `Response<ByteArray>` with only the Gson converter, so Gson tried to read the ZIP as a JSON array and export always failed; even on success the ViewModel dropped the bytes. Fix: `Response<ResponseBody>` (passed through raw by Retrofit), bytes read on `Dispatchers.IO`, then a system Save dialog (`CreateDocument("application/zip")`). `SettingsContractTest` proves the ByteArray failure. The same bug exists in `BudgetApi.exportBudgetsAsPdf` (left for the budgets unit).
- **Security:** `ZipUtil.extractCsvFiles` decompressed entries without limit, so a small upload could expand to gigabytes in memory (zip bomb). Now a bounded read with `MAX_ENTRY_BYTES` (10 MB per entry; only the 3 expected entries are read). File ids are not reused on import and records of another user are rejected (already the case, now covered by tests).
- **Errors:** CSV parse failures threw `RuntimeException` (500); now `IllegalArgumentException("Invalid <file>.csv: ...")` (400). CSV is read explicitly as UTF-8, matching the export.
- **Validation:** settings PUT now takes `List<@Valid UpdateSettingDTO>`. Spring 6.1+ built-in method validation applies to `@Valid` on a container element of `@RequestBody` (confirmed by test and live). It surfaces as `HandlerMethodValidationException`, so the `ApiError` message is the generic "Validation failure" without field names (deferred: map it to `fieldErrors`).
- **Testing:** `ExportImportRoundTripTest` feeds the real `ExportService` output into the real `ImportService` (mocked repositories): re-import counts, ids not reused, other user rejected, malformed CSV, oversized entry. `UserSettingControllerTest`: import returns the summary, null key 400. Android `SettingsContractTest`. The SAF save dialog is compile-checked only (no emulator).
- **Verified:** backend 80/80; Android `testDebugUnitTest` 19/19 and `assembleDebug`; live: export has `budgets.csv`, `transactions.csv`, `settings.csv`, `transaction_report.pdf`; unchanged re-import skips all; after deleting the budget and transaction they import back; another user's export 400; non-zip 400; null key 400.

## 2026-10-05 — Notifications: inbox, one alert per expense, settings honoured (`fix/notifications-inbox-and-settings`)

- **Problem 1:** the list used `findByUserId`, so archived notifications stayed visible (Android's DTO has no `archived` field, only the server can filter) and counted as unread; the list had no order. Now `findByUserIdAndArchivedFalseOrderByCreatedAtDesc` and `countByUserIdAndReadFalseAndArchivedFalse`.
- **Problem 2:** `TransactionService.createTransaction` checked "> 10,000 -> High Value" and "> 5,000 -> Heavy Spending" as two independent `if`s, so a 12,000 expense produced both. Now tiered (`else if`).
- **Problem 3 (found while reading):** the settings `NOTIFICATIONS_ENABLED`, `NOTIFY_SPENDING_ALERTS`, `NOTIFY_BUDGET_EXPIRY`, `NOTIFY_SYNC_EVENTS` were stored and shown in the app but never read anywhere; every alert was always sent.
- **Design:** one decision point. `CreateNotificationDTO` (internal only) carries an optional `preference` key; `NotificationService.createNotificationForUser` checks the master switch, then the preference, and returns `null` without saving or pushing when muted. `createNotification` now delegates to it (removed a duplicated save/publish block). Callers continue as if sent, so budget `lastNotifiedStage` and expiry flags still advance: re-enabling does not flood the user with stale alerts.
- **Settings lookup:** `UserSettingService.getBooleanForUser(userId, key)` uses the same `DEFAULTS`; needed because scheduler threads have no SecurityContext.
- **Testing:** `NotificationServiceTest` (inbox and unread exclude archived, enabled saves and pushes, muted by preference saves nothing, master switch off mutes without a preference). `TransactionServiceTest` (was an empty placeholder): 12,000 sends exactly one alert, 6,000 sends Heavy Spending, 3,000 sends none.
- **Live-check mistake:** the first script's transaction helper omitted `transactionDate` (`@NotNull`), so every create got 400 and no notification appeared; the helper also discarded the status code, hiding it. Rule: check scripts print the HTTP status of every write.
- **Verified:** backend 88/88; live: 12,000 then 6,000 give "Heavy Spending in Food", "High Value Expense" (newest first, no duplicate); unread 2; archiving removes it from list and count (1); spending alerts off and master switch off mute new alerts; master back on, "Large Income Received" appears.

## 2026-10-05 — Budget alerts re-arm; categories the app can use (`fix/budget-alerts-and-categories`)

- **Problem 1:** `BudgetAlertService.evaluate` only ever raised `lastNotifiedStage`, so after the amount was raised or an expense deleted, crossing 50%/90%/100% again was never announced. Now the stage follows spending in both directions; only an increase notifies, a decrease lowers the stage silently.
- **Problem 2:** `deleteTransaction` never re-evaluated budgets, and `updateTransaction` only checked budgets for the new values (a category or date move left the old budgets stale). Update now re-evaluates the old type/category/date as well; delete re-evaluates after deleting. Hibernate auto-flushes the pending delete before the spending SUM query in the same transaction, so the sum is already correct.
- **Problem 3:** expiry flags stayed `true` after a budget's end date was extended, so its reminders never came back. `updateBudget` resets both when the end date changes.
- **Problem 4 (Android, critical):** a transaction could not be created at all. The category field was read-only and `TransactionViewModel` never filled its list, while category is required on both sides. Fix: `GET /api/transactions/categories` (distinct categories of the user; literal path wins over `/{id}`), `DefaultCategories` merged with them (case-insensitive, trimmed, sorted), and an editable `CategoryDropdown` (categories are free text on the backend). Budget form uses the same suggestions; empty means all categories.
- **Problem 5 (Android):** budget PDF export used `Response<ByteArray>` (same Gson failure as settings export in 6a) and discarded the result. Now `ResponseBody` plus a system Save dialog.
- **Testing:** `BudgetAlertServiceTest` (+2: stage lowered without a notification, back to NONE). `BudgetServiceTest` (was an empty placeholder): new end date re-arms reminders, same end date keeps them. `TransactionServiceTest` (+2: delete and category move re-evaluate old and new budgets); its shared create stubs moved into a helper because Mockito strict stubs fail tests that do not use them. `TransactionControllerTest` (+1: categories). Android `DefaultCategoriesTest`. Save dialogs compile-checked only (no emulator).
- **Deferred:** dashboard totals over overlapping budgets (needs a design decision, 6d); N+1 spending query per budget (perf phase, needs native SQL and a repository test); transaction screen fake local fallback and transaction PDF export (6d).
- **Verified:** backend 95/95; Android 21/21 and `assembleDebug`; live on a 1000 Food budget: 600 spent gives FIFTY_PERCENT and one alert; deleting it gives NONE; 600 again gives a second 50% alert; raising the amount to 5000 gives NONE; extending the end date resets both expiry flags; categories returns `["Food"]`.

## 2026-10-05 — Reports, dashboard and transaction exports (`fix/reports-dashboard-exports`)

- **Problem 1:** the transaction PDF's single "Total" added income and expense together (1000 income + 350.50 expense printed 1350.50). A near-identical copy (`Settings/util/PDFExportUtil`) used by the data export had the same bug. Fix: one generator (`Transaction/util/PDFGenerator`, copy deleted) with `totalsOf(...)` and three rows: Total income, Total expense, Net.
- **Problem 2 (Android):** the monthly report could never load. `ReportScreen` sent `month=07` (and a hard-coded 2025) but the backend parameter is the `java.time.Month` enum, so every request was 400; the model also read `income`/`expenses`/`savings` while the backend sends `totalIncome`/`totalExpense`/`netSavings`. Fix: current month by enum name; model mirrors the backend.
- **Problem 3 (Android):** the trend model read `label` (never sent: null in a non-null `String`), `expenses` and `savings` (never sent: 0). The backend sends `date`/`income`/`expense`; savings is now computed. The documented "12m" period does not exist on the backend (it is "1y"; unknown values fall back to 6 months).
- **Problem 4 (design decision):** dashboard `totalBudget`/`remainingBudget` summed overlapping budgets (an all-categories budget and a Food budget both count a Food expense). Decision: remove the aggregate; the dashboard lists each budget with its own spent of amount and percentage, which is always correct.
- **Problem 5 (Android):** on any load error the transaction screen showed Room rows as fake transactions that all had id 99999999 (duplicate list keys crash the list). Removed: online-first shows the error.
- **Problem 6 (Android):** transaction PDF export used `Response<ByteArray>` (Gson failure, as in 6a/6c), discarded the result, and no screen called it. Now `ResponseBody`, Save dialog, and an export button on the transaction screen.
- **Testing:** `PDFGeneratorTest`: totals kept apart, and the generated PDF read back with OpenPDF `PdfTextExtractor` contains the three rows and not the old mixed total. Android `ReportContractTest`: monthly, trend and dashboard `BudgetInfo` shapes. Screens compile-checked only (no emulator).
- **Verified:** backend 97/97; Android 24/24 and `assembleDebug`; live: dashboard has no `totalBudget`/`remainingBudget` and lists both budgets; monthly report OCTOBER 2026 returns 20000.00 / 350.50 / 19649.50 with a category breakdown, `month=07` returns 400; trend returns `date`/`income`/`expense`; transaction PDF 200 `application/pdf`; data export still contains `transaction_report.pdf`.

## 2026-10-05 — Network stack as Hilt singletons (`refactor/android-network-di`)

- **Problem:** `RetrofitInstance.provideXApi(...)` called `provideRetrofit`, which built a new `OkHttpClient` on every call, and each repository called it in a field initializer. The app ran 8 OkHttp clients, each with its own connection pool and dispatcher threads, outside the DI graph. Repositories took `TokenManager` only to build their API.
- **Fix:** `core/di_config/NetworkModule` provides one `OkHttpClient` (same `AuthInterceptor` and timeouts), one `Retrofit` (same base URL and Gson converter) and each API as a `@Singleton`. Repositories receive their API through the constructor; `AppModule` passes it. Only `AuthRepoImpl` and `SettingsRepoImpl` still take `TokenManager`, because they save or clear tokens. `RetrofitInstance` was deleted.
- **Learning:** with Hilt, `assembleDebug` is the DI test. Dagger fails the build on a missing binding, so a green build proves every API and repository is wired.
- **Test drift:** `AuthContractTest` and `SettingsContractTest` each held a copy of the Retrofit builder ("same converter setup as RetrofitInstance"). They now call `NetworkModule.provideRetrofit(OkHttpClient())`, so a converter change in the app is tested automatically.
- **Found for 7b (not changed here):** the backend returns 401 both for an invalid or expired JWT and for a wrong login password, so session-expiry handling must ignore `/api/auth/**`. `SettingsViewModel.logout()` clears tokens only when the server call succeeds, so with an expired token the user cannot log out. The nav graph always starts at `auth` and ignores a stored token. Tokens are saved twice on login (`AuthRepoImpl` and `AuthViewModel`).
- **Testing:** `NetworkModuleTest`: the Retrofit uses the given client and `Constants.BASE_URL`.
- **Verified:** Android 25/25 and `assembleDebug`; no `RetrofitInstance` references remain.

## 2026-10-05 — Session follows the stored token (`fix/android-session`)

- **Problem 1:** the nav graph always started at `auth`, so a stored token was never used and every app start asked for a login.
- **Problem 2:** an expired or blacklisted token left the user on screens that only showed errors; nothing reacted to 401.
- **Problem 3 (trap):** `SettingsViewModel.logout()` cleared tokens only when `POST /api/settings/logout` succeeded. With an expired token that call is 401, so the user could not log out at all. Fix: `SettingsRepoImpl.logout()` clears locally whatever the server answers (the server blacklist is best effort).
- **Discovery:** the backend answers 401 for two different things: a rejected token (blacklist in `JWTAuthenticationFilter`; expired, garbage or deleted user via `HttpStatusEntryPoint`) and a wrong password on `/api/auth/login` (`InvalidCredentialsException`). Rule in `AuthInterceptor.endsSession`: 401 ends the session only outside `/api/auth/`; 403 never does.
- **Design decision:** state instead of events. The stored token is the single source of truth: `SessionViewModel.isLoggedIn` maps `TokenManager.authTokens`; `AppNavGraph` picks the start destination from it (fixed with `remember`, a NavHost start destination must not change) and goes to login whenever it turns false. Login, logout, account deletion and a 401 all just change the token, so one code path handles them; a state value cannot be missed the way an event can when nobody is collecting. The `onLogoutOrDelete` callback that `SettingsScreen` injected into its ViewModel was removed.
- **Race:** a 401 from a request sent before a re-login must not log the new session out. `TokenManager.clearTokensIfCurrent(token)` compares and removes inside one `dataStore.edit`, which is atomic, so parallel 401s also clear only once.
- **Cleanup:** the login token was saved twice (`AuthRepoImpl` and `AuthViewModel`); the ViewModel copy was removed.
- **Testing:** `AuthInterceptorTest` (401 on an API path ends the session, 401 on `/api/auth/login` does not, 403 does not). Navigation and the DataStore compare-and-clear are compile-checked only (no emulator).
- **Verified:** Android 28/28 and `assembleDebug`; live: token 200, wrong password 401, garbage token 401, logout 204, same token after logout 401.

## 2026-10-05 — Session expired message (`feat/android-session-expired-message`)

- **Problem:** after 7b, a session ended by the server (401) dropped the user on the login screen with no explanation.
- **Design:** the forced and voluntary paths were already separate: only `AuthInterceptor` calls `TokenManager.clearTokensIfCurrent`, while logout and account deletion call `clearTokens`. The forced path now sets a `SESSION_EXPIRED` DataStore flag inside the same atomic `edit`; `clearTokens` and `saveTokens` (login) remove it, and the user can dismiss it. It is stored, not held in memory, so it survives the app being killed between the forced logout and the next launch. The flag reaches the screen through `AuthRepo` (`sessionExpired` flow, `dismissSessionExpired`), keeping the ViewModel off `TokenManager` (the rule from 7b).
- **Testing setup:** `viewModelScope` runs on `Dispatchers.Main`, which does not exist in JVM unit tests, so no ViewModel was unit-testable. Added `kotlinx-coroutines-test` (test-only, same coroutines version 1.7.3) and `Dispatchers.setMain(UnconfinedTestDispatcher())`. `AuthViewModelTest` with a fake `AuthRepo`: the flag shows the message; dismiss calls the repo and hides it. The DataStore flag rules are compile-checked only (no emulator).
- **Bump (build, not code):** the first run printed old test results and no BUILD line. Cause: `Gradle build daemon disappeared unexpectedly`, so no task ran and the XMLs in `app/build/test-results/` were left over from the previous run. VS Code's Java and Gradle extensions held about 1.5 GB in three JVMs on the 8 GB Codespace (3.3 GB available); after `./gradlew --stop` and killing the Kotlin daemon the same build passed. Memory pressure is the likely cause, not proven (no readable OOM log). Rules: delete the test-result folder before a run, and only trust a run that prints `BUILD SUCCESSFUL`. The check grep also matched `DOCKER_BUILDKIT` on `BUILD`; use `BUILD (SUCCESSFUL|FAILED)`.
- **Verified:** Android 30/30 and `assembleDebug`.

## 2026-10-05 — Annotation processing on KSP (`build/android-ksp`)

- **Change:** Hilt and Room ran through kapt, which first generates Java stubs for every Kotlin class and is in maintenance mode. Both now run through KSP `2.0.0-1.0.24` (KSP versions are `<kotlin>-<ksp>`, the app is on Kotlin 2.0.0). Only build files changed: `ksp` plugin and version in the catalog, `ksp(...)` instead of `kapt(...)`, catalog alias `kapt` renamed to `hilt-compiler`, unused `kotlin-kapt` alias removed.
- **Decision:** `ksp.useKSP2=false` in `gradle.properties`. Hilt 2.51.1 supports KSP1 only (KSP2 needs Hilt 2.52+); the pin stops a later KSP upgrade from switching silently.
- **Wrong expectation (mine):** I expected `Hilt_FinanceTrackerApp` under `build/generated/ksp/`. The Hilt Gradle plugin's aggregating task writes it to `build/generated/hilt/component_sources/debug/`; only per-class code such as Room's `FinanceTrackerDatabase_Impl` lands in `generated/ksp/`.
- **Verification technique:** without an emulator, prove DI codegen by looking inside the APK: `$ANDROID_HOME/build-tools/<v>/dexdump app-debug.apk | grep "Class descriptor"` showed `Hilt_FinanceTrackerApp`, `DaggerFinanceTrackerApp_HiltComponents_SingletonC` and `NetworkModule_ProvideRetrofitFactory`.
- **Daemon crashes, evidence (corrects the 7b-2 entry):** three builds today failed with "Gradle build daemon disappeared unexpectedly". The daemon logs show each crashed daemon lived about one second (`Starting build in new daemon`, then the JVM's orderly "Daemon vm is shutting down" line), so it was not memory exhaustion during compilation (lowest available memory measured during a full build: 1.2 GB; a kernel OOM kill could not log a shutdown line). The 7b-2 entry's "memory pressure is the likely cause" is therefore wrong. Not proven: the failures coincided with VS Code's Gradle tooling being active around build-file changes (its daemon on the extension's JRE 21, a new daemon started right after the 7c build files changed); every failure passed on a plain retry. Rule: read the daemon log (`~/.gradle/daemon/<ver>/daemon-<pid>.out.log`) before blaming memory.
- **Verified:** `BUILD SUCCESSFUL`, Android 30/30; Room code generated by KSP, no kapt output directory; Hilt classes present in the APK dex.
