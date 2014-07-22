package martinandersson.com.websockets;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * @see ClientEndpoint
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class MyClientEndpoint extends Endpoint implements ClientEndpoint, MessageHandler.Whole<String>
{
    private static final Logger LOGGER = Logger.getLogger(MyClientEndpoint.class.getName());
    
    /**
     * Amount of seconds before a blocking client give up waiting for a message
     * from the server.
     */
    public static final int RECEIVE_TIMEOUT = 30;
    
    /**
     * Queue of messages from the server in untouched format.
     */
    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    
    private Session session;
    
    
    
    /*
     *  ----------------------
     * | CLIENT ENDPOINT IMPL |
     *  ----------------------
     */
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(URL appPath, String endpointPath) throws URISyntaxException, DeploymentException
    {
        ClientEndpointConfig plainConfig = ClientEndpointConfig.Builder.create().build();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        
        final URI uri = toServerEndpointURI(appPath, endpointPath);
        
        try
        {
            // Possible source of DeploymentException:
            Session session = container.connectToServer(this, plainConfig, uri); // <-- register life cycle methods.
            session.addMessageHandler(this); // <-- register onMessage method.
            
            this.session = session;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect()
    {
        if (isOpen())
            try { session.close(); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        else
            LOGGER.warning(() -> "Asked to disconnect/close WebSocket but we are already closed.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        return session != null? session.isOpen() : false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendAsync(String text) {
        trace("send", text);
        session.getAsyncRemote().sendText(text);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String receiveMessage()
    {
        try {
            return messages.poll(RECEIVE_TIMEOUT, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            return null;
        }
    }
    
    
    
    /*
     *  ---------------------
     * | ENDPOINT LIFE CYCLE |
     *  ---------------------
     */
    
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        trace("onOpen", session, config);
        assert this.session.equals(session) : "Wrong session object received.";
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        trace("onClose", session, closeReason);
    }
    
    @Override
    public void onMessage(String message)
    {
        trace("onMessage", message);
        
        try {
            messages.put(message);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while putting a chat message into queue.", e);
        }
    }
    
    
    
    /*
     *  --------------
     * | INTERNAL API |
     *  --------------
     */
    
    private void trace(String method, Object... args) {
        LOGGER.entering(MyClientEndpoint.class.getSimpleName(), method, args);
    }
    
    private URI toServerEndpointURI(URL appPath, String endpointPath) throws URISyntaxException
    {
        return new URI(
                "ws",                             // Scheme name
                null,                             // User name and authorization information
                appPath.getHost(),                // Host name
                appPath.getPort(),                // Port number
                appPath.getPath() + endpointPath, // Path
                null,                             // Query
                null);                            // Fragment
    }
}