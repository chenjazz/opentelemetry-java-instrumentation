# Understanding the javaagent components

The javaagent jar can logically be divided into 3 parts:

* Modules that live in the system class loader  存在于系统类加载器中的模块
* Modules that live in the bootstrap class loader  存在于引导类加载器中的模块
* Modules that live in the agent class loader  存在于代理类加载器中的模块

## Modules that live in the system class loader

### `javaagent` module

This module consists of single class
`io.opentelemetry.javaagent.OpenTelemetryAgent` which implements [Java
instrumentation
agent](https://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html).
This class is loaded during application startup by application classloader.
Its sole responsibility is to push agent's classes into JVM's bootstrap
classloader and immediately delegate to
`io.opentelemetry.javaagent.bootstrap.AgentInitializer` (now in the bootstrap class loader)
class from there.

该模块由实现 Java 检测代理的单个类 io.opentelemetry.javaagent.OpenTelemetryAgent 组成。此类在应用程序启动期间由应用程序类加载器加载。它的唯一职责是将代理的类推入 JVM 的引导类加载器，并立即从那里委托给 io.opentelemetry.javaagent.bootstrap.AgentInitializer（现在在引导类加载器中）类。

## Modules that live in the bootstrap class loader

### `javaagent-bootstrap` module

`io.opentelemetry.javaagent.bootstrap.AgentInitializer` and a few other classes that live in the bootstrap class
loader but are not used directly by auto-instrumentation

io.opentelemetry.javaagent.bootstrap.AgentInitializer 和其他一些存在于引导类加载器中但不直接被auto-instrumentation使用的类

### `instrumentation-api` and `javaagent-instrumentation-api` modules

These modules contain support classes for actual instrumentations to be loaded
later and separately. These classes should be available from all possible
classloaders in the running application. For this reason the `javaagent` module puts
all these classes into JVM's bootstrap classloader. For the same reason this
module should be as small as possible and have as few dependencies as
possible. Otherwise, there is a risk of accidentally exposing these classes to
the actual application.

这些模块包含用于稍后单独加载的实际instrumentations的支持类。这些类应该可以从正在运行的应用程序中的所有可能的类加载器中获得。出于这个原因，javaagent 模块将所有这些类放入 JVM 的引导类加载器中。出于同样的原因，这个模块应该尽可能小并且依赖尽可能少。否则，存在将这些类意外暴露给实际应用程序的风险。

`instrumentation-api` contains classes that are needed for both library and auto-instrumentation,
while `javaagent-instrumentation-api` contains classes that are only needed for auto-instrumentation.

instrumentation-api 包含库和auto-instrumentation都需要的类，而 javaagent-instrumentation-api 包含只需要自动检测的类。

## Modules that live in the agent class loader

### `javaagent-tooling`, `javaagent-extension-api` modules and `instrumentation` submodules

Contains everything necessary to make instrumentation machinery work,
including integration with [ByteBuddy](https://bytebuddy.net/) and actual
library-specific instrumentations. As these classes depend on many classes
from different libraries, it is paramount to hide all these classes from the
host application. This is achieved in the following way:

包含使instrumentation machinery工作所需的一切，包括与 ByteBuddy 和实际库特定instrumentations的集成。由于这些类依赖于来自不同库的许多类，因此对宿主应用程序隐藏所有这些类至关重要。这是通过以下方式实现的：

- When `javaagent` module builds the final agent, it moves all classes from
`instrumentation` submodules, `javaagent-tooling` and `javaagent-extension-api` modules
into a separate folder inside final jar file, called`inst`.
In addition, the extension of all class files is changed from `class` to `classdata`.
This ensures that general classloaders cannot find nor load these classes.
- When `io.opentelemetry.javaagent.bootstrap.AgentInitializer` is invoked, it creates an
instance of `io.opentelemetry.javaagent.bootstrap.AgentClassLoader`, loads an
`io.opentelemetry.javaagent.tooling.AgentInstaller` from that `AgentClassLoader`
and then passes control on to the `AgentInstaller` (now in the
`AgentClassLoader`). The `AgentInstaller` then installs all of the
instrumentations with the help of ByteBuddy. Instead of using agent classloader all agent classes
could be shaded and used from the bootstrap classloader. However, this opens de-serialization
security vulnerability and in addition to that the shaded classes are harder to debug.

- 当 javaagent 模块构建最终代理时，它将所有类从检测子模块、javaagent-tooling 和 javaagent-extension-api 模块移动到最终 jar 文件中的一个单独文件夹中，称为 inst。此外，所有类文件的扩展名都从 class 更改为 classdata。这确保了一般的类加载器无法找到或加载这些类。

- 当 io.opentelemetry.javaagent.bootstrap.AgentInitializer 被调用时，它会创建一个 io.opentelemetry.javaagent.bootstrap.AgentClassLoader 的实例，从该 AgentClassLoader 加载一个 io.opentelemetry.javaagent.tooling.AgentInstaller，然后将控制权传递给 AgentInstaller （现在在 AgentClassLoader 中）。然后，AgentInstaller 在 ByteBuddy 的帮助下安装所有工具。所有代理类都可以从引导类加载器中着色和使用，而不是使用代理类加载器。然而，这会打开反序列化安全漏洞，此外阴影类更难调试。

The complicated process above ensures that the majority of
auto-instrumentation agent's classes are totally isolated from application
classes, and an instrumented class from arbitrary classloader in JVM can
still access helper classes from bootstrap classloader.

上面复杂的过程确保了大多数自动检测代理的类与应用程序类完全隔离，并且来自 JVM 中任意类加载器的检测类仍然可以从引导类加载器访问辅助类。

### Agent jar structure

If you now look inside
`javaagent/build/libs/opentelemetry-javaagent-<version>.jar`, you will see the
following "clusters" of classes:

如果您现在查看 javaagent/build/libs/opentelemetry-javaagent-.jar，您将看到以下类的“集群”：

Available in the system class loader:在系统类加载器中可用：

- `io/opentelemetry/javaagent/bootstrap/AgentBootstrap` - the one class from `javaagent`
module

Available in the bootstrap class loader:在引导类加载器中可用：

- `io/opentelemetry/javaagent/bootstrap/` - contains the `javaagent-bootstrap` module
- `io/opentelemetry/javaagent/instrumentation/api/` - contains the `javaagent-instrumentation-api` module
- `io/opentelemetry/javaagent/shaded/instrumentation/api/` - contains the `instrumentation-api` module,
 shaded during creation of `javaagent` jar file by Shadow Gradle plugin
- `io/opentelemetry/javaagent/shaded/io/` - contains the OpenTelemetry API and its dependency gRPC
Context, both shaded during creation of `javaagent` jar file by Shadow Gradle plugin
- `io/opentelemetry/javaagent/slf4j/` - contains SLF4J and its simple logger implementation, shaded
during creation of `javaagent` jar file by Shadow Gradle plugin

Available in the agent class loader:在代理类加载器中可用：
- `inst/` - contains `javaagent-tooling` and `javaagent-extension-api` modules and
  `instrumentation` submodules, loaded and isolated inside `AgentClassLoader`.
  Includes the OpenTelemetry SDK.

![Agent initialization sequence](initialization-sequence.svg)
[Image source](https://docs.google.com/drawings/d/1GHAcJ8AOaf_v2Ip82cQD9dN0mtvSk2C1B11KfwV2U8o)
![Agent classloader state](classloader-state.svg)
[Image source](https://docs.google.com/drawings/d/1x_eiGRodZ715ai6gDMTkyPYU4_wQnEkS4LQKSasEJAk)
