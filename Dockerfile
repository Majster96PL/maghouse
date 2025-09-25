From postgres
ENV POSTGRES_DB="maghouse"
COPY data.sql /docker-entrypoint-initdb.b/