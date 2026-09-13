FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y postgresql-client \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "echo '=== TESTANDO CONEXAO DIRETA COM SUPABASE ===' && PGPASSWORD=\"$DB_PASSWORD\" psql -h aws-0-ca-central-1.pooler.supabase.com -p 5432 -U \"$DB_USERNAME\" -d postgres \"sslmode=require\" -c 'SELECT 1;' && echo '=== CONEXAO PSQL FUNCIONOU ===' && java -jar app.jar"]