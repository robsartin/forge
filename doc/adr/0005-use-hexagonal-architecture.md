# 5. Use hexagonal architecture

Date: 2025-12-20

## Status

Accepted

## Context

As Forge grows, we need an architecture that separates business logic from infrastructure concerns, enabling testability and flexibility to swap implementations without affecting core domain logic.

## Decision

We will use hexagonal architecture (ports and adapters) to structure the application.

Structure:
- **Domain**: Core business logic with no external dependencies
- **Ports**: Interfaces defining how the domain interacts with the outside world
  - Inbound ports: Use cases / application services
  - Outbound ports: Repository and external service interfaces
- **Adapters**: Implementations connecting ports to infrastructure
  - Inbound: Controllers, message listeners
  - Outbound: Database repositories, HTTP clients, message publishers

## Consequences

**Benefits:**
- Domain logic is isolated and highly testable without infrastructure
- Easy to swap adapters (e.g., change database, add new API protocols)
- Clear dependency direction: adapters depend on ports, never the reverse
- Enforces separation of concerns and prevents business logic leakage

**Constraints:**
- More boilerplate with port interfaces and adapter implementations
- Team must understand and follow the architectural boundaries
- Requires discipline to avoid shortcuts that violate layer boundaries
- Consider using ArchUnit to enforce dependency rules automatically
