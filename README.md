> A Connector template for new C8 inbound connector
>
> To use this template update the following resources to match the name of your connector:
>
> * [README](./README.md) (title, description)
> * [Element Template](./element-templates/template-connector-message-start-event.json)
> * [POM](./pom.xml) (artifact name, id, description)
> * [Connector Executable](src/main/java/io/camunda/connector/inbound/MyConnectorExecutable.java) (rename, implement,
    update
    `InboundConnector` annotation)
> * [Service Provider Interface (SPI)](./src/main/resources/META-INF/services/io.camunda.connector.api.inbound.InboundConnectorExecutable) (
    rename)
>
>
> About [creating Connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/#creating-a-custom-connector)
>
> Check out the [Connectors SDK](https://github.com/camunda/connectors)

# Important concepts

When building an inbound connector, there are several important concepts to understand:

- **Activation:** When a process definition with this connector is deployed. This is a synchronous operation, so long-running tasks should be started asynchronously.
- **[Activation condition](https://docs.camunda.io/docs/components/connectors/use-connectors/#activation-condition):** A BPMN expression that must evaluate to true for the connector to be activated.
- **Deactivation:** When the process definition is deleted or a new version is deployed. Use this to clean up resources.
- **[Correlation](https://docs.camunda.io/docs/components/connectors/use-connectors/#correlation):** Matches incoming events to waiting process instances using correlation keys.
- **[Deduplication](https://docs.camunda.io/docs/components/connectors/use-connectors/inbound/#connector-deduplication):** Sometimes you might want to have multiple BPMN elements listening to the same event source. For example, you might want to link multiple connector events to the same message queue consumer and activate only one of them based on the message content.
- **[Message ID](https://docs.camunda.io/docs/components/connectors/use-connectors/#message-id-expression):** Used for deduplication - events with the same message ID are processed only once.

For more details, see the [Inbound Connector SDK documentation](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk).

We strongly recommend reading through the [Messages documentation](https://docs.camunda.io/docs/components/concepts/messages/) as Inbound connectors rely heavily on the concepts explained there.

# Connector Template

Camunda Inbound Connector Template

Emulates a simple inbound connector function that start process X times per minutes(to be specified in the element
template)

## Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

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

For example, without shading, you might encounter errors like:
```
java.lang.NoSuchMethodError: com.fasterxml.jackson.databind.ObjectMapper.setserializationInclusion(Lcom/fasterxml/jackson/annotation/JsonInclude$Include;)Lcom/fasterxml/jackson/databind/ObjectMapper;
```
This occurs when your connector and the runtime use different versions of the same library (e.g., Jackson).

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

## Testing
### Unit and Integration Tests

You can run the unit and integration tests by executing the following Maven command:
```bash
mvn clean verify
```

### Local environment

#### Prerequisites
You will need the following tools installed on your machine:
1. Camunda Modeler, which is available in two variants:
    - [Desktop Modeler](https://camunda.com/download/modeler/) for a local installation.
    - [Web Modeler](https://modeler.camunda.io/) for an online experience.

2. [Docker](https://www.docker.com/products/docker-desktop), which is required to run the Camunda platform.

#### Setting Up the Camunda platform

The Connectors Runtime requires a running Camunda platform to interact with. To set up a local Camunda environment, follow these steps:

1. Clone the [Camunda distributions repository](https://github.com/camunda/camunda-distributions) from GitHub and navigate to the Camunda 8.8 docker-compose directory:

```shell
git clone git@github.com:camunda/camunda-distributions.git
cd cd docker-compose/versions/camunda-8.8
```

**Note:** This template is compatible with Camunda 8.8. Using other versions may lead to compatibility issues.

Either comment out the connectors service, or use the `--scale` flag to exclude it:

```shell
docker compose -f docker-compose-core.yaml up --scale connectors=0
```

#### Configure the Desktop Modeler and Use Your Connector

Add the `element-templates/template-connector-message-start-event.json` to your Modeler configuration as per
   the [Element Templates documentation](https://docs.camunda.io/docs/components/modeler/desktop-modeler/element-templates/configuring-templates/).

#### Using Your Connector
Then, to use your connector in a local Camunda environment, follow these steps:

1. Run `io.camunda.connector.inbound.LocalConnectorRuntime` to start your connector.
2. Open the Camunda Desktop Modeler and create a new BPMN diagram.
3. Design a process that incorporates your newly created connector.
4. Deploy the process to your local Camunda platform.
3. Verify that the process is running smoothly by accessing Camunda Operate at [localhost:8088/operate](http://localhost:8088/operate). Username and password are both `demo`.

### SaaS environment

#### Creating an API Client

The Connectors Runtime (LocalConnectorRuntime) requires connection details to interact with your Camunda SaaS cluster. To set this up, follow these steps:

1. Navigate to Camunda [SaaS](https://console.camunda.io).
2. Create a cluster using the latest version available.
3. Select your cluster, then go to the `API` section and click `Create new Client`.
4. Ensure the `zeebe` checkbox is selected, then click `Create`.
5. Copy the configuration details displayed under the `Spring Boot` tab.
6. Paste the copied configuration into your `application.properties` file within your project.

#### Using Your Connector

1. Start your connector by executing `io.camunda.connector.inbound.LocalConnectorRuntime` in your development
   environment.
2. Access the Web Modeler and create a new project.
3. Click on `Create new`, then select `Upload files`. Upload the connector template from the repository you have.
4. After uploading, **publish the connector template** by clicking the Publish button.
5. In the same folder, create a new BPMN diagram.
6. Design and start a process that incorporates your new connector.

## Element Template

The element template for this sample connector is generated automatically based on the connector
input class using
the [Element Template Generator](https://github.com/camunda/connectors/tree/main/element-template-generator/core).

The generation is embedded in the Maven build and can be triggered by running `mvn clean package`.

The generated element template can be found
in [element-templates/template-connector-message-start-event.json](./element-templates/template-connector-message-start-event.json).
