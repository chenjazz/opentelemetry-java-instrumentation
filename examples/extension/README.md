## Introduction

Extensions add new features and capabilities to the agent without having to create a separate distribution (for examples and ideas, see [Use cases for extensions](#use-cases-for-extensions)). 

扩展向代理添加新特性和功能，而无需创建单独的分发（有关示例和想法，请参阅扩展的用例）。

The contents in this folder demonstrate how to create an extension for the OpenTelemetry Java instrumentation agent, with examples for every extension point. 

此文件夹中的内容演示了如何为 OpenTelemetry Java 检测代理创建扩展，以及每个扩展点的示例。

> Read both the source code and the Gradle build script, as they contain documentation that explains the purpose of all the major components.

阅读源代码和 Gradle 构建脚本，因为它们包含解释所有主要组件用途的文档。

## Build and add extensions

To build this extension project, run `./gradlew build`. You can find the resulting jar file in `build/libs/`. 

To add the extension to the instrumentation agent:要将扩展添加到检测代理：

1. Copy the jar file to a host that is running an application to which you've attached the OpenTelemetry Java instrumentation. 将 jar 文件复制到运行已附加 OpenTelemetry Java 工具的应用程序的主机。
2. Modify the startup command to add the full path to the extension file. For example:修改启动命令，添加扩展文件的完整路径。例如：

     ```bash
     java -javaagent:path/to/opentelemetry-javaagent.jar \
          -Dotel.javaagent.extensions=build/libs/opentelemetry-java-instrumentation-extension-demo-1.0-all.jar
          -jar myapp.jar
     ```
## Embed extensions in the OpenTelemetry Agent  在 OpenTelemetry 代理中嵌入扩展

To simplify deployment, you can embed extensions into the OpenTelemetry Java Agent to produce a single jar file. With an integrated extension, you no longer need the `-Dotel.javaagent.extensions` command line option.

为了简化部署，您可以将扩展嵌入到 OpenTelemetry Java 代理中以生成单个 jar 文件。使用集成扩展，您不再需要 -Dotel.javaagent.extensions 命令行选项。

For more information, see the `extendedAgent` task in [build.gradle](build.gradle).

有关更多信息，请参阅 build.gradle 中的 extendedAgent 任务。

## Extensions examples  


* Custom `IdGenerator`: [DemoIdGenerator](src/main/java/com/example/javaagent/DemoIdGenerator.java)
* Custom `TextMapPropagator`: [DemoPropagator](src/main/java/com/example/javaagent/DemoPropagator.java)
* New default configuration: [DemoPropertySource](src/main/java/com/example/javaagent/DemoPropertySource.java)
* Custom `Sampler`: [DemoSampler](src/main/java/com/example/javaagent/DemoSampler.java)
* Custom `SpanProcessor`: [DemoSpanProcessor](src/main/java/com/example/javaagent/DemoSpanProcessor.java)
* Custom `SpanExporter`: [DemoSpanExporter](src/main/java/com/example/javaagent/DemoSpanExporter.java)
* Additional instrumentation: [DemoServlet3InstrumentationModule](src/main/java/com/example/javaagent/instrumentation/DemoServlet3InstrumentationModule.java)

## Sample use cases 示例用例

Extensions are designed to override or customize the instrumentation provided by the upstream agent without having to create a new OpenTelemetry distribution or alter the agent code in any way.

扩展旨在覆盖或自定义上游代理提供的检测，而无需创建新的 OpenTelemetry 发行版或以任何方式更改代理代码。

Consider an instrumented database client that creates a span per database call and extracts data from the database connection to provide span attributes. The following are sample use cases for that scenario that can be solved by using extensions.

考虑一个检测的数据库客户端，它为每个数据库调用创建一个跨度并从数据库连接中提取数据以提供span属性。以下是该场景的示例用例，可以通过使用扩展来解决。

### "I don't want this span at all"

Create an extension to disable selected instrumentation by providing new default settings.

### "I want to edit some attributes that don't depend on any db connection instance"

Create an extension that provide a custom `SpanProcessor`.

### "I want to edit some attributes and their values depend on a specific db connection instance"

Create an extension with new instrumentation which injects its own advice into the same method as the original one. You can use the `order` method to ensure it runs after the original instrumentation and augment the current span with new information.

For example, see [DemoServlet3InstrumentationModule](src/main/java/com/example/javaagent/instrumentation/DemoServlet3InstrumentationModule.java).

### "I want to remove some attributes"

Create an extension with a custom exporter or use the attribute filtering functionality in the OpenTelemetry Collector.

### "I don't like the OTel spans. I want to modify them and their lifecycle"

Create an extension that disables existing instrumentation and replace it with new one that injects `Advice` into the same (or a better) method as the original instrumentation. You can write your `Advice` for this and use the existing `Tracer` directly or extend it. As you have your own `Advice`, you can control which `Tracer` you use.
