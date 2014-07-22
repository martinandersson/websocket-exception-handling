package martinandersson.com.websockets;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import javax.websocket.DeploymentException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
@RunWith(Arquillian.class)
public class MyServerEndpointTest
{
    private static final Logger LOGGER = Logger.getLogger(MyServerEndpointTest.class.getName());
    
    static MyClientEndpoint client;
    
    @Deployment
    public static WebArchive buildDeployment()
    {
        WebArchive war = ShrinkWrap.create(WebArchive.class, MyServerEndpointTest.class.getSimpleName() + ".war");
        war.addClasses(MyServerEndpoint.class, BadMessageException.class);
        return war;
    }
    
    @BeforeClass
    public static void setupClientEndpoint() { // <-- is executed on the client-side
        client = new MyClientEndpoint();
    }
    
    @Test
    @RunAsClient
    @InSequence(1)
    public void client_connect(@ArquillianResource URL url) throws URISyntaxException, DeploymentException
    {
        final String withoutSlash = MyServerEndpoint.PATH.substring(1);
        client.connect(url, withoutSlash);
    }
    
    @Test
    @InSequence(2)
    public void server_assertClientConnected() {
        assertSame(1L, MyServerEndpoint.countEndpointsOpened());
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void client_sendBadMessage() {
        // This message is hardcoded on the server test endpoint to not be accepted:
        client.sendAsync("BAD MESSAGE");
    }
    
    @Test
    @RunAsClient
    @InSequence(4)
    public void client_assertResponse()
    {
        final String response = client.receiveMessage();
        assertNotNull("Server didn't send us anything", response);
        
        // This is the hardcoded response we expect if client sent a "bad message":
        assertEquals("Unacceptable message: BAD MESSAGE", response);
    }
    
    @Test
    @RunAsClient
    @InSequence(99)
    public void client_disconnect() {
        client.disconnect();
    }
}