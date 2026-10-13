.PHONY: install dev test lint

install:
	cd backend && ./gradlew classes testClasses --console=plain
	cd frontend && npm ci

# Runs the API on :8080 and the web app on :5173; Ctrl+C stops both.
dev:
	@trap 'kill 0' INT TERM; \
	(cd backend && ./gradlew bootRun --console=plain) & \
	(cd frontend && npm run dev) & \
	wait

test:
	cd backend && ./gradlew test --console=plain

lint:
	cd backend && ./gradlew ktlintCheck --console=plain
	cd frontend && npm run lint
