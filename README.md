<h1 align="center"><a href="https://doc.itlym.cn/shoulder" target="_blank"><img src="doc/img/shoulder_with_world_logo.svg" /></a></h1>

> Based on `Spring Boot` dedicated to simplify development, improve efficiency. Hope to become the best partner
> for `Spring Boot`.

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](https://github.com/ChinaLym/shoulder-framework/blob/master/LICENSE) [![](https://img.shields.io/badge/Author-lym-yellow.svg)](https://github.com/ChinaLym)

[![](https://img.shields.io/badge/Release%20Version-0.8.1-blue.svg)](https://github.com/ChinaLym/shoulder-framework/releases/tag/v0.8.1) [![](https://img.shields.io/badge/Spring%20Boot%20Version-3.2.x-blue.svg)](https://spring.io/projects/spring-boot) [![](https://img.shields.io/badge/Spring%20Cloud%20Version-2023.0.x-blue.svg)](https://spring.io/projects/spring-cloud/)

# 📖Introduction [中文 language](README_zh.md)

`Shoulder Framework` is a plug-in for `Spring Boot` and provides some useful functions to speed up development! Some examples of functions:

- `AOP logging/exception handling`
- `One config switch to cluster mode`
- `Log tracking and collecting`
- `Multi-language / I18n`
- `Fully automatic dynamic dictionary`
- `Global ID Generator`
- `Unified message format`
- `Operation Audit`
- `Multiple verification codes`
- `Asynchronous upload and download`
- `Top Secret Communications`
- `Embedded DB / Redis`

### 🔥 Sample project: [GitHub](https://github.com/ChinaLym/shoulder-framework-demo), [Gitee](https://gitee.com/ChinaLym/shoulder-framework-demo)

### 🆚Compare with other web frameworks

- **More Secure**
- **Easy to expand**
- **High readability of user code**
- **Low maintenance cost**
- **Low start-up cost**
- **With specification**：**[Software Elegance Design and development best practices](https://doc.itlym.cn)**
- Not only **improve development efficiency**, but also **lower maintenance costs**!

---

# 🚀 Quick Start

## [Start with a Demo](https://github.com/ChinaLym/shoulder-framework-demo/tree/main/demo1)

Through the simple in 👉 **[Demo](https://github.com/ChinaLym/shoulder-framework-demo/tree/main/demo1)** 👈
([github](https://github.com/ChinaLym/shoulder-framework-demo/tree/main/demo1)/ [gitee](https://gitee.com/ChinaLym/shoulder-framework-demo/tree/main/demo1)),
feel the elegant coding experience brought by `Shoulder`.

## Using in an existing `Spring-Boot` project

Just add the module you want to use, for instance, if you want to use `shoulder-web's dynamic dictionary ability`, simply include the
corresponding starter:

```xml

<dependency>
    <groupId>cn.itlym</groupId>
    <artifactId>shoulder-starter-web</artifactId>
    <version>0.8.1</version><!-- shoulder-version -->
</dependency>
```

## Create a Shoulder project via `maven-archetype`

`Shoulder` provides a [maven archetype](https://github.com/ChinaLym/shoulder-framework/tree/master/shoulder-archetype-simple) to quickly
create projects. Just ensure you have `JDK17+` and `Maven` installed locally.

1. Open terminal.
2. Run the following command to generate a new Shoulder project:

```shell
mvn archetype:generate \
  -DarchetypeGroupId=cn.itlym \
  -DarchetypeArtifactId=shoulder-archetype-simple \
  -DarchetypeVersion=0.8.1 \
  -DgroupId=com.yourcompany \
  -DartifactId=yourappName \
  -Dversion=1.0-SNAPSHOT
```

## Manually creating a new project

Just use the following `pom.xml` directly, the only difference from a Spring Boot project is the difference `<parent>` of `pom.xml.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- for global version control. include spring-boot-parent -->
    <parent>
        <groupId>cn.itlym</groupId>
        <artifactId>shoulder-parent</artifactId>
        <version>0.8.1</version><!-- shoulder-version -->
    </parent>

    <groupId>com.demo</groupId><!-- your groupId -->
    <artifactId>hello-shoulder</artifactId><!-- your artifactId -->
    <version>1.0.0-SNAPSHOT</version><!-- your version -->

    <dependencies>
        <!-- add module with version tag like using spring-boot-starter -->
        <dependency>
            <groupId>cn.itlym</groupId>
            <artifactId>shoulder-starter-web</artifactId>
        </dependency>
    </dependencies>

</project>

```

---

# ❓FAQ

See [FAQ Document](doc/faq.md)

More: [Ability.md](doc/ability-intro.md)、[Roadmap](ROADMAP.MD)

# ✨Applicable scenarios

- `Graduation Project`, `Outsourcing Project`: Rapid development based on provided capabilities.
- `Microservice base framework`: `Shoulder` follows coding standards when designing, and has unified coding interfaces and products, making
  it easy for development/operation and maintenance teams to maintain.
- `Internal framework development`: `Shoulder` is designed to fully absorb the ideas of `Spring Boot` and focus on scalability, which allows
  some companies/organizations who want to build internal frameworks to quickly develop internal frameworks.

# ✈ Planning & Development Roadmap

`Shoulder` aims to be a complete re-usable platform (PaaS), where users only need to focus on their business logic. Here is the overall
outlook:

- `Shoulder iPaaS` iPaaS Basic middleware environment: Shoulder offers dependency middleware Docker images or deployment guides (e.g.,
  databases, message queues, service registration centers, task scheduling centers, search engines, alarm and monitoring systems, etc.).
- `Shoulder Specific` manuel of development, see *
  *[the best practices of elegant software design and development](https://doc.itlym.cn)**
- **Shoulder Framework**  This project, aims to encapsulation of common capabilities, reducing code redundancy and lowering system
  development and maintenance costs.
- `Shoulder Platform` General business platform, provides user platform, payment platform, notification center, business gateway, data
  dictionary, global ID generator, and other basic, common business capabilities.
- `Shoulder Platform SDK` Provides SDKs to facilitate business layer integration.

## Relevant Project
| Project Name                | Open Source URLs                                                                                                          | Description                                                                                        |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Shoulder Framework          | [github](https://github.com/ChinaLym/shoulder-framework)、[gitee](https://gitee.com/ChinaLym/shoulder-framework)           | a `Java WEB framework` based on `Spring Boot` while provides a set of common capabilities.         |
| Shoulder Platform           | [github](https://github.com/ChinaLym/shoulder-platform)、[gitee](https://gitee.com/ChinaLym/shoulder-platform)             | `SaaS platform`, provides user, payment, notification gateway and other common services...         |
| shoulder-framework-demo     | [github](https://github.com/ChinaLym/shoulder-framework-demo)、[gitee](https://gitee.com/ChinaLym/shoulder-framework-demo) | `Shoulder Framework` Demos                                                                         |
| shoulder-plugins            | [github](https://github.com/ChinaLym/shoulder-plugins)、[gitee](https://gitee.com/ChinaLym/shoulder-plugins)               | a `maven plugin` for `shoulder-framework` aiming reduce code.                                      |
| shoulder-lombok             | [github](https://github.com/ChinaLym/shoulder-lombok)、[gitee](https://gitee.com/ChinaLym/shoulder-lombok)                 | a `library` based on `lombok`，provide an annotation `@SLog` to simplify code.                      |
| shoulder-lombok-idea-plugin | [github](https://github.com/ChinaLym/lombok-intellij-plugin)、[gitee](https://gitee.com/ChinaLym/lombok-intellij-plugin)   | a `IDEA plugin` based on `lombok-idea-plugin`, to support `@SLog` in `IDEA`.                       |
| Shoulder iPaaS              | [github](https://github.com/ChinaLym/shoulder-ipaas)、[gitee](https://gitee.com/ChinaLym/shoulder-iPaaS)                   | `iPaaS platform`，introduces how to deploy common middlewares, monitoring systems, basic platforms. |

![releated_projects.png](doc/img/releated_projects.png)

# 📒 Version & Change log

See more in [CHANGELOG.adoc](CHANGELOG.adoc).

# 💗 Contribution

It is appreciated that you can contribute to this project, such as issues, code formatting, comments, bugfix, new features, etc.

See more in [How to contribute](CONTRIBUTING.MD)

# 📩 Feedback & Contact

Thanks for your **[🌟Star](https://gitee.com/ChinaLym/shoulder-framework/star)** 、 **🍴Fork** 、 **🏁PR**.

Please feel free to contact with use in `issues`or [email to cn_lym@foxmail.com](mailto:cn_lym@foxmail.com) . For example: your ideas,
expectations~

`Shoulder` does not seek to be the most widely used, but is committed to becoming the development framework with the best user experience.
Any of your usage needs, suggestions, and ideas can be left to communicate with us, `Shoulder`

Let's help developers around the world use technology better and more securely to help their business take off together!

Wish you overcome any difficulties in your business.

### 👨‍💼 About author

Participated in Alibaba core system reconstruction and design many times, and guarantees D11 level promotion. Technical exchanges and resume
submissions are welcome~

- This project is a personal project independently developed and maintained by the author in his spare time and is not an official Alibaba
  product.
