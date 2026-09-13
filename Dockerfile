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

CMD ["sh", "-c", "echo '=== TESTE PSQL COM SSL OBRIGATORIO ===' && PGPASSWORD=\"$DB_PASSWORD\" PGSSLMODE=require psql -h aws-0-ca-central-1.pooler.supabase.com -p 5432 -U \"$DB_USERNAME\" -d postgres -c 'SELECT 1;' && echo '=== PSQL SSL FUNCIONOU ===' && java -jar app.jar"]