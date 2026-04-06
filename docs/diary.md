# Diary
## March
### March 16, 2026
- GitHub Dependabot and GitLab Dependency Scanning API research
- Project setup and git repository initiation

### March 18, 2026
- started working on the data model for owner, repository, service and vulnerabilities

### March 27, 2026
- updates on the data model with the addition of sast reports and other small changes
- started the backend development with the implementation of the Create User functionality

## April
### April 3, 2026
- Authentication with Google OAuth2 started

### April 4, 2026
- Updated the `users` table: `password_validation` is now nullable and a new nullable `google_id` column was added. These changes support Google OAuth2 users who authenticate without a password, using the Google ID as their identifier.
- Added `user_repositories` table to associate users with repositories (many-to-many). This avoids duplicating repository data when multiple users monitor the same repository.
- replaced `SERIAL PRIMARY KEY` with `INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` across all tables. `GENERATED ALWAYS AS IDENTITY` is the SQL standard approach (PostgreSQL 10+), more explicit and prevents accidental manual ID inserts.
- Google OAuth2 login endpoint functional: Spring Security handles the OAuth2 flow and exposes the authenticated user as an `OidcUser` principal, giving access to `subject` (Google ID), `email`, and `fullName`. After authentication, the plan is to persist the user in the DB and issue an app token, keeping the authentication flow consistent with local login.

### April 6, 2026
- **Nota para o relatório:** Grande parte do código de autenticação por token (pipeline de interceptors, argument resolvers, token processor, domain logic) foi adaptado do código de DAW e dos exemplos do Professor Félix. Referenciar no relatório.
- Implemented token-based authentication pipeline:
  - `AuthenticationInterceptor`: intercepts requests and enforces authentication on endpoints that declare an `AuthenticatedUser` parameter. Returns 401 if the token is missing or invalid.
  - `AuthenticatedUserArgumentResolver`: resolves the `AuthenticatedUser` parameter in controller methods from the request attribute set by the interceptor.
  - `RequestTokenProcessor`: extracts and validates the token from the `Authorization: Bearer` header or from a cookie. Also provides helpers to create and delete the session cookie.
  - `AuthenticatedUser` and `TokenExternalInfo` model classes added.
- `UserDomain` extended with `canBeToken`, `isTokenTimeValid`, and `maxNumberOfTokensPerUser` to support token lifecycle validation.
- `UserRepository` fully implemented `storeToken`, `getTokenByTokenValidationInfo`, and `updateTokenLastUsed`. The token lookup query was fixed to include `email` in the SELECT and correctly join `users` with `tokens`.
- `TokenValidationMapper` added to map the `token_validation` column to `TokenInfo` via JDBI's column mapper system, registered in `JdbiExtensions`.
- `UserServices.getUserByToken` implemented: validates token format, retrieves user+token from DB, checks TTL and rolling TTL, and updates `last_used_at` on valid access.
- `SecurityConfig` fixed: CSRF disabled (REST API uses token-based auth) and `/users/**` added to `permitAll()` so Spring Security does not intercept routes before the custom interceptor.
- `UserController`: base path changed from `/api/users` to `/users`; `/me` endpoint corrected from `@PostMapping` to `@GetMapping` and wired to receive `AuthenticatedUser`.
- Split of `UserController` and `UserServices` into additional `AuthController` and `AuthServices`