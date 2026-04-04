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