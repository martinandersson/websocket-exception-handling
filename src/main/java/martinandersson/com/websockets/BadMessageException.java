package martinandersson.com.websockets;

import java.util.Objects;

/**
 * Simple container for a message that does not fit the communication protocol.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class BadMessageException extends RuntimeException
{
    /**
     * @param message rejected message
     */
    public BadMessageException(String message) {
        super(Objects.requireNonNull(message, "message is null"));
    }
    
    /**
     * @return the rejected, bad, message
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}