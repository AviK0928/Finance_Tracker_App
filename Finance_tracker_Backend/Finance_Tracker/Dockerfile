# Container image for the backend (used by Render; also runs locally with docker build/run).
# Build stage: the Maven wrapper downloads Maven and the dependencies, then packages the jar.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN ./mvnw -B -q -DskipTests package

# Runtime stage: JRE only, non-root user.
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 app
COPY --from=build /app/target/Finance_Tracker-0.0.1-SNAPSHOT.jar app.jar
USER app
# Sized for a 512 MB instance: heap at 60% of the container memory leaves room for metaspace and threads;
# serial GC and C1-only JIT cut memory use and startup time on a small CPU share.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
# The server listens on $PORT when it is set (Render sets it), otherwise 8080.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
