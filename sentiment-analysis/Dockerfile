ARG PYTHON_VERSION=3.9
FROM python:$PYTHON_VERSION-slim as base

ARG FFF_PYPI_USERNAME
ARG POETRY_VERSION=1.1.8
ARG POETRY_CORE_VERSION=1.0.4

RUN groupadd --system --gid 8080 app
RUN useradd --system --create-home --gid app --uid 8080 app

RUN mkdir /app
WORKDIR /app
RUN apt-get update && apt-get -y install --no-install-recommends make git curl
RUN pip install -U pip>=21.1.3 setuptools>=57.0.0 virtualenv>=20.4.7

# Setup poetry
ENV POETRY_HOME=/tmp/poetry
RUN curl -sSL https://raw.githubusercontent.com/python-poetry/poetry/master/install-poetry.py | python3 - --version $POETRY_VERSION
RUN $POETRY_HOME/venv/bin/pip install poetry-core==$POETRY_CORE_VERSION
ENV PATH=$POETRY_HOME/bin:$PATH
RUN pip list
RUN poetry --version
RUN poetry config virtualenvs.create false
COPY pyproject.toml /app/
COPY poetry.lock /app/

# Format of the mount is: --mount=type=secret,id=<ENV_VAR_NAME>
RUN --mount=type=secret,id=FFF_PYPI_PASSWORD poetry config http-basic.fff-pypi $FFF_PYPI_USERNAME $(cat /run/secrets/FFF_PYPI_PASSWORD)
RUN poetry install --no-dev --no-root

FROM base as test

RUN apt-get update && apt-get -y install --no-install-recommends postgresql redis
RUN poetry install --no-root

COPY sentiment_analysis /app/sentiment_analysis
COPY tests /app/tests
COPY makefiles/python.mk Makefile
USER app
# Force the production build to run after the tests
# If we switch to poetry we can get this dependency by generating the product requirements here and copying in.
USER root
RUN touch /app/test.complete

FROM base as app

RUN apt-get update && apt-get install -y --no-install-recommends awscli jq gettext
COPY sentiment_analysis /app/sentiment_analysis
RUN poetry install --no-dev

USER app
#CMD ["uvicorn", "sentiment_analysis.main:app", "--port 8080"]
CMD ["fff_fastapi", "start", "sentiment_analysis.main:app", "--no-enable-launchdarkly-client"]

FROM app as production
# Ensure we've run the tests (see above)
COPY --from=test /app/test.complete /app/test.complete
