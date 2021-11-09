## Introduction

This repository serves as a collection of examples of extending functionality of OpenTelemetry Java instrumentation agent.
It demonstrates how to repackage the aforementioned agent adding custom functionality.
For every extension point provided by OpenTelemetry Java instrumentation, this repository contains an example of
its usage.

此存储库用作扩展 OpenTelemetry Java 检测代理功能的示例集合。它演示了如何重新打包上述代理添加自定义功能。对于 OpenTelemetry Java 检测提供的每个扩展点，此存储库包含其用法示例。

## General structure

This repository has four main submodules:这个存储库有四个主要的子模块：

* `custom` contains all custom functionality, SPI and other extensions   包含所有自定义功能、SPI 和其他扩展
* `agent` contains the main repackaging functionality and, optionally, an entry point to the agent, if one wishes to
customize that  包含主要的重新打包功能，以及可选的代理入口点（如果希望自定义）
* `instrumentation` contains custom instrumentations added by vendor  包含供应商添加的自定义工具
* `smoke-tests` contains simple tests to verify that resulting agent builds and applies correctly  包含简单的测试来验证生成的代理是否正确构建和应用

## Extensions examples

* [DemoIdGenerator](custom/src/main/java/com/example/javaagent/DemoIdGenerator.java) - custom `IdGenerator`
* [DemoPropagator](custom/src/main/java/com/example/javaagent/DemoPropagator.java) - custom `TextMapPropagator`
* [DemoPropertySource](custom/src/main/java/com/example/javaagent/DemoPropertySource.java) - default configuration
* [DemoSampler](custom/src/main/java/com/example/javaagent/DemoSampler.java) - custom `Sampler`
* [DemoSpanProcessor](custom/src/main/java/com/example/javaagent/DemoSpanProcessor.java) - custom `SpanProcessor`
* [DemoSpanExporter](custom/src/main/java/com/example/javaagent/DemoSpanExporter.java) - custom `SpanExporter`
* [DemoServlet3InstrumentationModule](instrumentation/servlet-3/src/main/java/com/example/javaagent/instrumentation/DemoServlet3InstrumentationModule.java) - additional instrumentation

## Instrumentation customisation

There are several options to override or customise instrumentation provided by the upstream agent.
The following description follows one specific use-case:

有几个选项可以覆盖或自定义上游代理提供的检测。以下描述遵循一个特定用例：

> Instrumentation X from Otel distribution creates span that I don't like and I want to change it in my vendor distro.

As an example, let us take some database client instrumentation that creates a span for database call
and extracts data from db connection to provide attributes for that span.

作为一个例子，让我们以一些数据库客户端工具为例，它为数据库调用创建一个span，并从数据库连接中提取数据以提供该span的属性。

### I don't want this span at all
The easiest case. You can just pre-configure your distribution and disable given instrumentation.

最简单的情况。您可以预先配置您的发行版并禁用给定的instrumentation。

### I want to add/modify some attributes and their values does NOT depend on a specific db connection instance. 我想添加/修改一些属性，它们的值不依赖于特定的数据库连接实例
E.g. you want to add some data from call stack as span attribute. 
In this case just provide your custom `SpanProcessor`.
No need for touching instrumentation itself.

例如。您想从调用堆栈中添加一些数据作为 span 属性。在这种情况下，只需提供您的自定义 SpanProcessor。无需接触instrumentation本身。

### I want to add/modify some attributes and their values depend on a specific db connection instance. 。我想添加/修改一些属性，它们的值取决于特定的数据库连接实例。
Write a _new_ instrumentation which injects its own advice into the same method as the original one.
Use `getOrder` method to ensure it is run after the original instrumentation.
Now you can augment current span with new information.

编写一个新的instrumentation，将自己的advice注入与原始方法相同的方法中。使用 getOrder 方法确保它在原始检测之后运行。现在，您可以使用新信息扩大当前span。

See [DemoServlet3Instrumentation](instrumentation/servlet-3/src/main/java/com/example/javaagent/instrumentation/DemoServlet3Instrumentation.java).

### I want to remove some attributes
Write custom exporter or use attribute filtering functionality in Collector.

在收集器中编写自定义导出器或使用属性过滤功能。

### I don't like Otel span at all. I want to significantly modify it and its lifecycle  我根本不喜欢 Otel span。我想显着修改它及其生命周期
Disable existing instrumentation.
Write a new one, which injects `Advice` into the same (or better) method as the original instrumentation.
Write your own `Advice` for this.
Use existing `Tracer` directly or extend it.
As you have your own `Advice`, you can control which `Tracer` you use.

禁用现有检测。编写一个新的，将 Advice 注入与原始检测相同（或更好）的方法中。为此写下您自己的建议。直接使用现有的 Tracer 或扩展它。由于您有自己的建议，因此您可以控制使用哪个 Tracer。
