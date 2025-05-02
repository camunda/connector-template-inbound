# https://github.com/camunda/connectors/blob/main/connector-runtime/connector-runtime-application/example/Dockerfile
FROM camunda/connectors-bundle:8.7.0 AS runtime

COPY ./target/connector-template-inbound-0.1.0-SNAPSHOT-with-dependencies.jar /opt/app/connector-template-inbound.jar
