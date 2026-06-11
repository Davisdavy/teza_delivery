# Teza — Architecture

Teza is a **modular monolith**: one deployable Spring Boot application, internally
split into business modules that are as independent as separate services but
without the operational cost of distributing them.

## Guiding rules

1. **Package by business capability, not by technical layer.** The top-level
   packages are domains (`auth`, `rider`, …), and layers live *inside* each
   domain.
2. **One module owns its data.** A module never reaches into another module's
   entities or repositories.
3. **Talk across modules through events.** Modules publish/handle application
   events (see `shared/event`) instead of calling each other's internals,
   satisfying the README's "event-driven internal services" principle.
4. **Dependencies point inward.** `api → application → domain`, with
   `infrastructure` implementing the ports the `domain` defines. The `domain`
   layer depends on nothing framework-specific.

## Top-level layout

```
com.wafula.teza
├── TezaApplication            Spring Boot entry point
├── shared/                    Shared kernel + cross-cutting concerns
│   ├── config/                App-wide Spring config (JPA/auditing, web, OpenAPI)
│   ├── domain/                Base abstractions + the Role enum (shared kernel value)
│   ├── event/                 Inter-module event-publishing infrastructure
│   └── exception/             Base exceptions + global API error handling
├── user/                      User accounts / identity (User entity, UserAccountService)
├── auth/                      Authentication only (JWT, refresh tokens, login/register)
│                              — depends on user for identity; owns the SecurityFilterChain
├── rider/                     Rider profiles, onboarding, availability
├── delivery/                  Delivery order lifecycle
├── dispatch/                  Intelligent rider-to-delivery matching engine
├── merchant/                  Merchant accounts & API/webhook integrations
├── tracking/                  Real-time location & delivery tracking
└── notification/              Outbound push / SMS / email (event consumer)
```

## The four layers inside every business module

| Layer            | Responsibility                                                                 | Depends on        |
|------------------|--------------------------------------------------------------------------------|-------------------|
| `api`            | REST controllers + request/response DTOs — the module's public HTTP contract.  | `application`     |
| `application`    | Use-case orchestration, transaction boundaries, event publish/handle.          | `domain`          |
| `domain`         | Entities, value objects, domain events, repository **ports**. No framework.    | nothing           |
| `infrastructure` | JPA repository **adapters**, persistence mappings, external integrations.      | `domain`          |

Only the `api` and `application` layers of a module are considered its public
surface; other modules must not depend on its `domain` or `infrastructure`.

## Module relationships (event flow)

- `auth` depends on `user` (a direct call to `UserAccountService`) for identity;
  it never touches `user`'s entity or repository. This is the one synchronous
  cross-module dependency — the rest below are event-driven.
- `delivery` emits lifecycle events → `dispatch` matches a rider → assignment
  events flow back to `delivery`.
- `rider` emits availability events → consumed by `dispatch`.
- `tracking` consumes rider-location + delivery events to serve live tracking.
- `notification` consumes events from any module; nothing depends on it.

## Other directories

- `src/main/resources/db/migration/` — Flyway SQL migrations (`V1__*.sql`, …).
- `src/main/resources/application.yaml` — application configuration.

> No business logic is implemented yet. Each package currently contains a
> `package-info.java` documenting its purpose and keeping the (otherwise empty)
> package under version control.
