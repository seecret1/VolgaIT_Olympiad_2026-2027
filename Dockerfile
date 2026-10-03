FROM maven:3.9.16-eclipse-temurin-17-noble AS maven-runtime

FROM selenium/standalone-chrome:4.49.0-20260909

USER root

COPY --from=maven-runtime /opt/java/openjdk /opt/java/openjdk17
COPY --from=maven-runtime /usr/share/maven /usr/share/maven

ENV JAVA_HOME=/opt/java/openjdk17
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"

RUN ln -s /usr/share/maven/bin/mvn /usr/local/bin/mvn

WORKDIR /workspace

COPY --chown=seluser:seluser pom.xml ./

USER seluser

RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

COPY --chown=seluser:seluser src ./src

ENTRYPOINT []
CMD ["mvn", "--batch-mode", "--no-transfer-progress", "clean", "test", "-Dheadless=true", "-Dbrowser=chrome"]
