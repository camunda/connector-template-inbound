### A Connector template for new C8 inbound connector

## Pre-requisites

* Linux
* sdkman[https://sdkman.io/]
  - Install [sdkman](https://sdkman.io/install)
    ```bash
    curl -s "https://get.sdkman.io" | bash
    ```
* JDK 21
  - Install and use JDK
    ```bash
    sdk install java 21.0.7-tem
    sdk use java 21.0.7-tem
    ```
* [Apache Maven](https://maven.apache.org/install.html)
  - Install Apache Maven 
    ```bash
    sdk install maven 3.9.9
    sdk use maven 3.9.9
    ```    
* [Docker](https://docs.docker.com/engine/install/)
* [git](https://git-scm.com/downloads)

## TDLR: Run a custom connector with Self-Managed Camunda 8.7

### Clone this repo

```bash
cd ~ && mkdir projects && cd projects
git clone git@github.com:AndriyKalashnykov/connector-template-inbound.git
cd ~/projects/connector-template-inbound
```

### Make Connector's templates available to a localy installed [Camunda Desktop Modeler](https://camunda.com/download/modeler/)

Linux
```bash
cp ./element-templates/*.json ~/.config/camunda-modeler/resources/element-templates/
```

### Deploy and run BPMN with [Camunda Desktop Modeler](https://camunda.com/download/modeler/)

- Start local [Camunda Desktop Modeler](https://camunda.com/download/modeler/)
- Open file [`connector-template-inbound.bpmn`](./connector-template-inbound.bpmn)
- Deploy diagram to "Camunda 8 Self-Managed": use a`spaceship` pictogram in status bar), set `Cluster endpoint` to `http://localhost:26500` to `Authentication` to `None`
- Start BPMN: `Start Current Diagram`- use an `arrow` pictogram in status bar

## Build Connector JAR

```bash
make build
docker logs --since=1h 'connectors' | tee connectors.log
docker logs 'connectors' --follow
```

## Build Connector Docker Image 

```bash
make image-build
```

## Start Docker Compose (uses previously build Connector Docker image)

```bash
make cmpose-up
```

## Observe container `connectors` logs

Tail logs
```bash
make container-logs
```

or save as a file

```bash
docker logs --since=1h 'connectors' | tee connectors.log
```

## Stop Docker Compose

```bash
make cmpose-down
```


> To use this template update the following resources to match the name of your connector:
>
> * [README](./README.md) (title, description)
> * [Element Template](./element-templates/template-connector-message-start-event.json)
> * [POM](./pom.xml) (artifact name, id, description)
> * [Connector Executable](src/main/java/io/camunda/connector/inbound/MyConnectorExecutable.java) (rename, implement, update `InboundConnector` annotation)
> * [Service Provider Interface (SPI)](./src/main/resources/META-INF/services/io.camunda.connector.api.inbound.InboundConnectorExecutable) (rename)
>
> About [creating Connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/#creating-a-custom-connector)
>
> Check out the [Connectors SDK](https://github.com/camunda/connectors)

# Connector Template

Camunda Inbound Connector Template

Emulates a simple inbound connector function that start process X times per minutes(to be specified in the element
template)

Attempts run run shaded JAR locally - doe not work yet

```bash
mvn clean dependency:copy-dependencies package install shade:shade -DskipTests
java -cp target/*:target/dependency/* io.camunda.connector.inbound.MyConnectorExecutable 
java -cp target/*.jar:target/dependency/*.jar io.camunda.connector.inbound.LocalConnectorRuntime 
java  -cp "./target/connector-template-inbound-0.1.0-SNAPSHOT-with-dependencies.jar:/target/dependency/*" "io.camunda.connector.runtime.app.ConnectorRuntimeApplication"
```

What we're looking for: Inbound connector io.camunda:my-inbound-connector:1 activated with deduplication ID

This will create the following artifacts:

- A thin JAR without dependencies.
- A fat JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK
  artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing
  the Connector.
- All element templates

### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and
the [Connector Runtime](https://github.com/camunda/connectors).
This helps to avoid classpath conflicts when the Connector is executed.

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html)
provides more details on relocations.

## API

### Input

| Name               | Description                          | Example |
|--------------------|--------------------------------------|---------|
| Sender             | Message sender                       | `alice` |
| Message per minute | Number of message created per minute | 6       |

### Output

```json
{
  "event":{
    "sender":"test",
    "code":4,
    "message":"56b020ef-e51a-4a4b-b9a4-8df521a2e78f"
  }
}
```

## Test locally

Run unit tests

```bash
mvn clean verify
```

### Test with local runtime

To ensure the seamless functionality of your custom Camunda connector, please follow the steps below:

#### Prerequisites:

1. Camunda Modeler, which is available in two variants:
    - [Desktop Modeler](https://camunda.com/download/modeler/) for a local installation.
    - [Web Modeler](https://camunda.com/download/modeler/) for an online experience.

2. [Docker](https://www.docker.com/products/docker-desktop), which is required to run the Camunda platform.

#### Setting Up the Environment:

1. Clone the Camunda Platform repository from GitHub:

```shell
git clone https://github.com/camunda/camunda-platform.git
```

Navigate to the cloned directory and open docker-compose-core.yaml with your preferred text editor.

Locate the connector image section and comment it out using the # symbol, as you will be executing your connector
locally.

Initiate the Camunda suite with the following Docker command:

```shell
docker compose -f docker-compose-core.yaml up
```

### Configuring Camunda Modeler

1. Install the Camunda Modeler if not already done.
2. Add the `element-templates/template-connector-message-start-event.json` to your Modeler configuration as per
   the [Element Templates documentation](https://docs.camunda.io/docs/components/modeler/desktop-modeler/element-templates/configuring-templates/).



### Launching Your Connector

1. Run `io.camunda.example.LocalConnectorRuntime` to start your connector.
2. Create and initiate a process that utilizes your newly created connector within the Camunda Modeler. ![Connector in Camunda Modeler](img/img.png)
3. Verify that the process is running smoothly by accessing Camunda Operate at [localhost:8081](http://localhost:8081).

Follow these instructions to test and use your custom Camunda connector effectively.

### Test with SaaS

#### Prerequisites:

None required.

#### Setting Up the Environment:

1. Navigate to Camunda [SaaS](https://console.camunda.io).
2. Create a cluster using the latest version available.
3. Select your cluster, then go to the `API` section and click `Create new Client`.
4. Ensure the `zeebe` checkbox is selected, then click `Create`.
5. Copy the configuration details displayed under the `Spring Boot` tab.
6. Paste the copied configuration into your `application.properties` file within your project.

### Launching Your Connector

1. Start your connector by executing `io.camunda.connector.inbound.LocalConnectorRuntime` in your development
   environment.
2. Access the Web Modeler and create a new project.
3. Click on `Create new`, then select `Upload files`. Upload the connector template from the repository you have.
4. In the same folder, create a new BPMN diagram.
5. Design and start a process that incorporates your new connector.

By adhering to these steps, you can validate the integration of your custom Camunda connector with the SaaS environment.

## Element Template

The element template for this sample connector is generated automatically based on the connector
input class using
the [Element Template Generator](https://github.com/camunda/connectors/tree/main/element-template-generator/core).

The generation is embedded in the Maven build and can be triggered by running `mvn clean package`.

The generated element template can be found
in [element-templates/template-connector.json](./element-templates/template-connector-message-start-event.json).
