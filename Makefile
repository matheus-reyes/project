.PHONY: install db db-stop db-reset dev test lint

install:
	cd backend && ./gradlew classes testClasses --console=plain
	cd frontend && npm ci

# Starts Postgres on :5432 and waits until it accepts connections; data lives in a Docker volume.
db:
	docker compose up -d --wait db

db-stop:
	docker compose stop db

# Drops the volume, so the next start runs the migrations and the seed again.
db-reset:
	docker compose down -v

# Runs the API on :8080 and the web app on :5173; Ctrl+C stops both and leaves Postgres running.
dev: db
	@trap 'kill 0' INT TERM; \
	(cd backend && ./gradlew bootRun --console=plain) & \
	(cd frontend && npm run dev) & \
	wait

# Testcontainers starts its own Postgres, so tests only need Docker running.
test:
	cd backend && ./gradlew test --console=plain

lint:
	cd backend && ./gradlew ktlintCheck --console=plain
	cd frontend && npm run lint
