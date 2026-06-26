Teza is a Delivery-as-a-Service backend built with Spring Boot.

It provides APIs for merchants, rider applications, and admin dashboards.

Core capabilities:
- Rider management
- Delivery management
- Intelligent rider matching
- Real-time tracking
- Notifications
- Merchant integrations
- Analytics

Architecture principles:
- Modular monolith
- REST APIs
- JWT authentication
- PostgreSQL database
- Docker support
- Event-driven internal services where appropriate

---

## 🌿 Git Workflow & Branching Strategy

This project follows a structured branch management workflow to ensure code stability:
- **`main`**: Represents the stable production-ready code. Direct commits or feature branch merges are not allowed on `main`. It only accepts merges directly from the `develop` branch.
- **`develop`**: The primary integration branch for ongoing development. All feature branches must target and merge into `develop` first.
- **Feature Branches**: Created for writing new features or fixes (e.g., `feat/some-feature` or `fix/some-bug`). Developers push these branches to remote and create Pull Requests targeting the `develop` branch for peer review.