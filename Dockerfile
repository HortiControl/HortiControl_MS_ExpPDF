# Estágio 1: build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia primeiro os arquivos do Maven: enquanto o pom.xml não mudar, o
# download das dependências é reaproveitado do cache entre builds.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Estágio 2: execução (imagem final sem JDK nem Maven)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Não roda como root: um processo comprometido não deve ter poder de root
# dentro do container.
RUN addgroup -S horti && adduser -S horti -G horti

COPY --from=build /app/target/*.jar app.jar
RUN chown -R horti:horti /app

# Sem VOLUME: o serviço não guarda os relatórios gerados. O PDF fica em
# memória apenas entre a geração e o download, então o container é
# efetivamente stateless — o estado que importa está no banco.

USER horti

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
