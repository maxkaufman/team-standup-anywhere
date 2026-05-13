# Team Standup Anywhere

A full-stack async standup platform for remote teams. Team members submit daily standups — what they did yesterday, what they're doing today, and any blockers — along with a mood check-in. Managers get a live dashboard with mood trends, completion rates, and team analytics.

**Stack:** React · TypeScript · Java 21 · Spring Boot · GraphQL · PostgreSQL

---

## Features

- **Daily standup submission** — structured yesterday/today/blockers form with a 1–5 mood rating
- **Team dashboard** — real-time stats including average mood, today's completion rate, and active blockers
- **30-day mood trend chart** — visualizes team sentiment over time
- **Team management** — create a team or join one via invite code; role-based access (Member / Lead)
- **Standup history** — paginated personal standup log
- **JWT authentication** — access + refresh token pair, stateless backend

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, TypeScript, Apollo Client 4, Tailwind CSS 4, React Router 6, Vite 8 |
| Backend | Java 21, Spring Boot 3.4, Spring for GraphQL, Spring Security, JPA/Hibernate, Flyway |
| Database | PostgreSQL 16 (H2 in-memory for local dev) |
| Auth | JWT (JJWT 0.12) — access token (24h) + refresh token (7d) |
| Containers | Docker, docker-compose |
| AI | $20 Claude Code Membership - dev workhorse |

---

## Local Development

### Option A — Docker (recommended)

**Prerequisites:** Docker

```bash
docker-compose up -d
```

Starts PostgreSQL, the Spring Boot backend (port 8080), and the Vite dev server (port 5173) with hot reload. Flyway migrations run automatically on startup. Maven is bundled in the build image — no local Java or Maven install needed.

> **Note:** Although this is the recommended path, I do not keep the docker running 24/7! If you would like me to run the docker, shoot me a message :)!

### Option B — Run services individually

**Prerequisites:** Node 20+, Java 21, Maven 3.9+

**Database**
```bash
docker-compose up -d postgres
```

**Backend** (uses H2 in-memory DB by default — no Postgres needed)
```bash
cd backend
mvn spring-boot:run
# GraphQL API: http://localhost:8080/graphql
# GraphiQL explorer: http://localhost:8080/graphiql
```

**Frontend**
```bash
cd frontend
npm install
npm run dev
# App: http://localhost:5173
```

---

## Project Structure

```
├── frontend/               # React SPA
│   └── src/
│       ├── apollo/         # Apollo Client setup (auth link + HTTP link)
│       ├── components/     # StandupCard, MoodChart, StatsCard, ProtectedRoute
│       ├── graphql/        # All GQL queries and mutations
│       ├── hooks/          # useAuth (AuthContext)
│       └── pages/          # Dashboard, SubmitStandup, StandupHistory,
│                           #   TeamManagement, Profile, Login, SignUp
│
├── backend/                # Spring Boot API
│   └── src/main/java/com/teampulse/
│       ├── config/         # Security config
│       ├── entity/         # User, Team, Standup, Role
│       ├── graphql/
│       │   ├── input/      # GraphQL input types
│       │   └── resolver/   # Query, Mutation, Team, Standup, User resolvers
│       ├── repository/     # Spring Data JPA repositories
│       ├── security/       # JWT filter, token provider, UserPrincipal
│       └── service/        # Auth, Team, Standup, User, Analytics
│   └── src/main/resources/
│       ├── graphql/schema.graphqls
│       └── db/migration/V1__initial_schema.sql
│
└── docker-compose.yml
```

---

## GraphQL API

The entire API is a single GraphQL endpoint at `/graphql`. Key operations:

```graphql
# Auth
mutation Login($email: String!, $password: String!)
mutation SignUp($input: SignUpInput!)
mutation RefreshToken($token: String!)

# Standups
mutation SubmitStandup($input: StandupInput!)
query TodayStandup
query MyStandups($limit: Int, $offset: Int)

# Team & analytics
query DashboardData($teamId: ID!, $date: String!)
mutation CreateTeam($name: String!)
mutation JoinTeam($inviteCode: String!)

# Profile
mutation UpdateProfile($input: ProfileInput!)
```

---

## Database Schema

```sql
users      id, email, password (bcrypt), name, avatar_url, role, team_id, created_at
teams      id, name, invite_code (unique), created_by, created_at
standups   id, author_id, team_id, yesterday, today, blockers, mood (1-5), created_at
```

Indexes on `standups(team_id, created_at)` for fast dashboard queries.

---

## Configuration

| Variable | Description |
|---|---|
| `JWT_SECRET` | HS256 signing key (min 256-bit) |
| `DB_HOST` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL connection |

Locally these default to H2 and dev values defined in `application.yml`. For a Postgres-backed local run, set them in your environment or use docker-compose.
