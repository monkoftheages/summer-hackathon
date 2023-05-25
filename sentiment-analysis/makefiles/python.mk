.PHONY: py-start
py-start:
	@export $(xargs < .env) > /dev/null
	poetry run uvicorn --port 8080 --reload sentiment_analysis.main:app

.PHONY: py-format
py-format:
	poetry run isort --diff --check .
	poetry run black --check .

.PHONY: py-lint
py-lint:
	poetry run pylint sentiment_analysis tests

.PHONY: py-typecheck
py-typecheck:
	poetry run mypy --ignore-missing-imports .

.PHONY: py-test
py-test:
	DD_TRACE_ENABLED=false poetry run pytest -vv --tb=short --log-level=INFO

.PHONY: py-all
py-all: py-format py-lint py-typecheck py-test
