### WebSocket Exception Handling

Demonstrates that GlassFish 4.0.1-b08-m1/Tyrus x.x successfully handles application exceptions occuring in a @ServerEndpoint. WildFly 8.1.0/Undertow 1.0.15 force-close the endpoint without even calling @OnClose. A bug has been filed here:
[https://issues.jboss.org/browse/UNDERTOW-284](https://issues.jboss.org/browse/UNDERTOW-284)

### How-to setup this project

Just open the project like any other Maven-based Java project. The project uses Arquillian as test runner. The `POM` file include two profiles for GlassFish- and WildFly remote. Thus make sure that either GlassFish or WildFly is running, and have the corresponding maven build profile activated, then just build/test the project and the test application will be deployed and executed live on the server.
