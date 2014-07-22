### WebSocket Exception Handling

Demonstrates that GlassFish 4.0.1-b08-m1/Tyrus x.x successfully handles application exceptions occuring in a @ServerEndpoint. WildFly 8.1.0/Undertow 1.0.15 force-close the endpoint without even calling @OnClose. A bug (with a finer description) has been filed here:
[https://issues.jboss.org/browse/UNDERTOW-284](https://issues.jboss.org/browse/UNDERTOW-284)

### How-to setup this project

Just open the project like any other Maven-based Java project. The project uses Arquillian as test runner. The `POM` file include two profiles for GlassFish- and WildFly remote. Thus make sure that either GlassFish or WildFly is running, and have the corresponding maven build profile activated, then just build/test the project and the test application will be deployed and executed live on the server.

### Workaround

A workaround for Undertow is to write a decorator method that intercept the message processing and wrap the real message handler in a try-catch statement. If a `RuntimeException` is caught, then programmatically call the `@OnError` annotated method. Here's a code sketch:

    private boolean serverIsUsingUndertow() {
        return ContainerProvider.getWebSocketContainer()
                .getClass().getName()
                .equals("io.undertow.websockets.jsr.ServerWebSocketContainer");
    }
    
    /** Is decorator. */
    @OnMessage
    public void onMessage(String data)
    {
        if (serverIsUsingUndertow()) // <-- should be cached
            try { __onMessage(data); }
            catch (RuntimeException e) { onError(e); } // <-- the Undertow hack
        else
            __onMessage(data);
    }
    
    /** Is the real message handler. */
    private void __onMessage(String data) {
        // ... process as usual
    }
    
    @OnError
    public void onError(Throwable throwable) {
        // ... process as usual
    }
