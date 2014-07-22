package martinandersson.com.websockets;

import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.websocket.DeploymentException;

/**
 * Client endpoint declares methods to connect against a server endpoint, send
 * and receive messages to/from the server endpoint.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public interface ClientEndpoint
{
    /**
     * Will connect to the server endpoint.
     * 
     * @param appPath is the URL of the WAR application as provided by {@code
     *        Arquillian}. The HTTP protocol will be swapped to WS.
     * @param endpointPath the server endpoint path, for example {@code test}
     * 
     * @throws URISyntaxException if parsing of {@code appPath} and {@code
     *         endpointPath} failed
     * @throws DeploymentException on WebSocket handshake error
     */
    void connect(URL appPath, String endpointPath) throws URISyntaxException, DeploymentException;
    
    /**
     * Disconnect the client.<p>
     * 
     * Safe to call if the endpoint already has been closed.
     */
    void disconnect();
    
    /**
     * @return {@code true} if this endpoint has been opened and are alive,
     *         otherwise {@code false}
     */
    boolean isOpen();
    
    /**
     * Send the provided text to server endpoint.<p>
     * 
     * This method executes asynchronously.
     * 
     * @param text the text
     */
    void sendAsync(String text) throws UncheckedIOException;
    
    /**
     * Get the next message received from server.<p>
     * 
     * This method blocks while waiting for a new message if an old message
     * hasn't already been received. Timeout is implementation specific. When
     * timeout happens, {@code null} is returned.
     * 
     * @return the message, or {@code null} if none was received
     */
    String receiveMessage();
}