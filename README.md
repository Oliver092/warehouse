# Warehouse Management System

A Spring Boot REST API for managing warehouse inventory with role-based access control, JWT authentication, and Elasticsearch-powered product search.

## Tech Stack

- **Java 21** — modern language features
- **Spring Boot 3.x** — REST API, Security, Data JPA
- **PostgreSQL** — primary data store (via Spring Data JPA)
- **Elasticsearch 8.12** — full-text and fuzzy product search, aggregations
- **Redis 7.2** — persistent JWT token blacklist for logout
- **Docker / Docker Compose** — local infrastructure

## Features

### Warehouse Structure
The warehouse is modelled as a four-level hierarchy:

```
Hall → Aisle → Shelf → Product
```

CRUD operations are available for each level with appropriate role restrictions.

### Product Management
- Create products on a specific shelf with capacity and SKU conflict validation
- Move products between shelves with capacity and SKU checks
- Update stock quantities with low-stock threshold alerts
- View all low-stock products across the warehouse

### Elasticsearch Search
Products are indexed in Elasticsearch alongside their denormalised location data (shelf, aisle, hall), enabling:

- **Full-text search** — search by product name or SKU: `GET /api/products/search?q=bolt`
- **Fuzzy search** — typo-tolerant search: `GET /api/products/search/fuzzy?q=sniker` finds "snickers"
- **Hall statistics** — aggregated product count per hall: `GET /api/products/statistics`
- **Manual reindex** — sync all Postgres products into Elasticsearch: `POST /api/products/reindex`

PostgreSQL is the source of truth. Elasticsearch is updated on every product create, update, move, and delete. 
The reindex endpoint rebuilds the entire index from Postgres on demand.

### Authentication & Security
- JWT-based stateless authentication
- Token blacklisting on logout via **Redis** with automatic TTL expiry — blacklisted tokens persist across application restarts
- Role-based access control on every endpoint

### Roles

| Role | Access                                                        |
|------|---------------------------------------------------------------|
| `ROLE_ADMIN` | Full access including hall/aisle/shelf management and reindex |
| `ROLE_MANAGER` | Product management, low-stock view, statistics                |
| `ROLE_WORKER` | View products, update quantities, move products               |
| `ROLE_READONLY` | View and search only (e.g. office staff)                      |

## Getting Started
cp .env.example .env
# Fill in your values in .env

### Prerequisites
- Java 21
- Docker Desktop

### Run infrastructure

```bash
docker-compose up -d
```

This starts:
- Elasticsearch on `localhost:9200`
- Kibana on `localhost:5601`
- Redis on `localhost:6379`

### Run the application

```bash
./mvnw spring-boot:run
```

### Seed the index

After starting, call the reindex endpoint with an admin token to populate Elasticsearch from existing Postgres data:

```
POST /api/products/reindex
Authorization: Bearer <admin_token>
```

## API Overview

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/auth/login` | Public |
| POST | `/auth/register` | Public |
| POST | `/auth/logout` | Authenticated |

### Products
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/products` | All staff |
| GET | `/api/products/{id}` | All staff |
| GET | `/api/products/search?q=` | All staff |
| GET | `/api/products/search/fuzzy?q=` | All staff |
| GET | `/api/products/low-stock` | Admin, Manager |
| GET | `/api/products/statistics` | Admin, Manager |
| POST | `/api/products/shelf/{shelfId}` | Admin, Manager |
| POST | `/api/products/reindex` | Admin |
| PATCH | `/api/products/{id}/quantity?change=` | Admin, Manager, Worker |
| PATCH | `/api/products/{id}/move/{shelfId}` | Admin, Manager, Worker |
| DELETE | `/api/products/{id}` | Admin, Manager |

### Halls / Aisles / Shelves
Structure management is restricted to `ROLE_ADMIN`.

## Configuration

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/warehouse_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

# Elasticsearch
spring.elasticsearch.uris=http://localhost:9200

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## Notable Design Decisions

**Dual-write sync pattern** — every write to Postgres is followed by an Elasticsearch index update in the same service method. Postgres is always the source of truth; ES is rebuilt on demand via `/reindex` if they drift out of sync.

**Denormalised ES documents** — the `ProductDocument` flattens the Hall → Aisle → Shelf → Product chain into a single flat document. This avoids joins at search time and makes location-aware queries instant.

**Redis TTL matching JWT expiry** — when a token is blacklisted on logout, the Redis entry TTL is set to the token's remaining lifetime. Expired tokens clean themselves up automatically with no manual maintenance.

**Mapping change strategy** — Elasticsearch field type changes require deleting and recreating the index (unlike `ALTER TABLE` in SQL). The `/reindex` endpoint exists specifically to handle this safely.

## Kibana

Access Kibana at `http://localhost:5601` → Dev Tools → Console to run queries directly against the index:

```
GET /products/_search
GET /products/_mapping
DELETE /products
```
Testing
The project includes unit tests for service-layer business logic using:
- JUnit 5
- Mockito
- AssertJ

Test coverage includes:
- CRUD operations
- validation rules
- capacity and SKU conflict handling
- quantity updates
- exception scenarios
- Elasticsearch indexing interactions
