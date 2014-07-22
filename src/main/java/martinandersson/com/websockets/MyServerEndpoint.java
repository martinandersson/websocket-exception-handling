package martinandersson.com.websockets;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * A server endpoint that listen on {@value #PATH}.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
@ServerEndpoint(value = MyServerEndpoint.PATH)
public class MyServerEndpoint
{
    public static final String PATH = "/test";
    
    private static final Logger LOGGER = Logger.getLogger(MyServerEndpoint.class.getName());
    
    static {
        // During testing only:
        LOGGER.setLevel(Level.FINER);
    }
    
    private static final LongAdder ENDPOINTS = new LongAdder();
    
    public static long countEndpointsOpened() {
        return ENDPOINTS.sum();
    }
    
    private Session session;

    
    
    @OnMessage
    public void onMessage(String data)
    {
        trace("onMessage", data);
        
        LOGGER.fine(() -> "WebSocket is open? " + isOpen());
        
        if ("BAD MESSAGE".equals(data)) {
            LOGGER.warning(() -> "Received a bad message: " + data);
            throw new BadMessageException(data);
        }
        else
            LOGGER.info(() -> "Okay! Application is supposed to do something cool now!");
    }
    
    
    
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        trace("onOpen", session, config);
        ENDPOINTS.increment();
        this.session = session;
    }
    
    @OnClose
    public void onClose(CloseReason closeReason) {
        trace("onClose", closeReason);
    }
    
    @OnError
    public void onError(Throwable throwable)
    {
        trace("onError", throwable);
        
        if (!isOpen()) {
            LOGGER.log(Level.WARNING, "Received unhandled throwable: ", throwable);
            return;
        }
        
        if (throwable instanceof BadMessageException)
        {
            // Okay, BadMessageException is expected sometimes.
            final String bad = ((BadMessageException) throwable).getMessage();
            session.getAsyncRemote().sendText("Unacceptable message: " + bad);
        }
        else
        {
            LOGGER.log(Level.WARNING, "Received unknown throwable: ", throwable);
            
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, null));
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
    
    
    
    
    public boolean isOpen() {
        return session != null? session.isOpen() : false;
    }
    
    private void trace(String method, Object... args) {
        LOGGER.entering(MyServerEndpoint.class.getSimpleName(), method, args);
    }
}