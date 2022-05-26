[![License](https://img.shields.io/badge/License-Apache%20License%202.0-brightgreen.svg)][1]
![Java CI with Gradle](https://github.com/andifalk/microservices-auth-authz-spring-security/workflows/Java%20CI%20with%20Gradle/badge.svg)
# Microservices Authentication & Authorization With Spring Security [Workshop]

This contains both, theory parts on all important concepts, and hands-on practice labs.

__Table of Contents__

* [Workshop Tutorial](https://andifalk.gitbook.io/microservices-authentication-and-authorization)
* [Requirements and Setup](setup)
* [Hands-On Workshop](#hands-on-workshop)
    * [Intro Labs](#intro-labs)
        * [Demo: Auth Code Flow in Action](intro-labs/auth-code-demo)
    * [Hands-On Labs](#hands-on-labs)
        * [Lab 1: Resource Server](lab1)
        * [Lab 2: Testing the Resource Server](lab2)
        * [Lab 3: Call another Microservice](lab3)
* [Feedback](#feedback)
* [License](#license)

## Workshop Tutorial

To follow the hands-on workshop please open the [workshop tutorial](https://andifalk.gitbook.io/microservices-authentication-and-authorization/).

## Requirements and Setup

For the hands-on workshop you will extend a provided sample application along with guided tutorials.

You will need a customized version of the [Spring Authorization Server](https://github.com/spring-projects/spring-authorization-server) that you can get from [https://github.com/andifalk/custom-spring-authorization-server](https://github.com/andifalk/custom-spring-authorization-server)

The components you will build (and use) look like this:

![Architecture](docs/images/demo-architecture.png)

__Please check out the [complete documentation](application-architecture) for the sample application before
starting with the first hands-on lab__.

All the code currently is build using

* [Spring Boot 2.7.x Release](https://spring.io/blog/2022/05/19/spring-boot-2-7-0-available-now)
* [Spring Framework 5.3.x Release](https://spring.io/blog/2020/10/27/spring-framework-5-3-goes-ga)
* [Spring Security 5.7.x Release](https://spring.io/blog/2022/05/15/spring-security-5-7-0-5-6-4-5-5-7-released-fixes-cve-2022-22978-cve-2022-22976)

All code is verified against the currently supported long-term versions 11 and 17 of Java.

To check system requirements and setup for this workshop please follow the [setup guide](setup).

## Hands-On Workshop

### Intro Labs

* [Demo: Authorization Code Grant Flow in Action](intro-labs/auth-code-demo)

### Hands-On Labs

* [Lab 1: Resource Server](lab1)
* [Lab 2: Testing the Resource Server](lab2)
* [Lab 3: Call another Microservice](lab3)

## Feedback

Any feedback on this hands-on workshop is highly appreciated.

Just send an email to _andreas.falk(at)novatec-gmbh.de_ or contact me via Twitter (_@andifalk_).

## License

Apache 2.0 licensed

[1]:http://www.apache.org/licenses/LICENSE-2.0.txt
