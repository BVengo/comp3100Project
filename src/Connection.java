import java.io.*;
import java.net.*;

public abstract class Connection {
    static Socket s;
    static BufferedReader din;
    static DataOutputStream dout;

    /**
     * Connect to a server
     * @param hostname Hostname of the server. Use localhost for testing ds-server
     * @param port Port the server is listening on. 50000 is the default for ds-server
     * @throws IOException On connection failure
     */
    static void connect(String hostname, int port) throws IOException {
        s = new Socket(hostname, port);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));  
        dout = new DataOutputStream(s.getOutputStream());
    }

     /**
     * Close the connection to the server gracefully
     * @throws Exception
     */
    static void disconnect() throws IOException {
        handleMessage("QUIT", "QUIT");
        din.close();
        s.close();
    }

    /**
     * Sends off a message to the server, and returns the reply provided
     * @param message The message to be sent
     * @return The reply from the server
     * @throws IOException On message failure
     */
    public static String handleMessage(String message) throws IOException {
        if(!message.equals("")) {
            dout.write(formatOutput(message));
            dout.flush();
        }
        
        return din.readLine();
    }

    /**
     * Sends off a message to the server, and returns the reply provided
     * Errors if the expected response isn't received
     * @param message The message to be sent
     * @param expectedResponse The response expected from the server
     * @return The reply received from the server
     * @throws IOException On message failure
     */
    public static String handleMessage(String message, String expectedResponse) throws IOException {
        String reply = handleMessage(message);

        if(!reply.equals(expectedResponse)) {
            throw new IOException("Didn't received an expected reply! This implies something incorrect was sent");
        }

        return reply;
    }

    /**
     * Formats string to be sent to ds-server
     * @param s The string that needs formatting
     * @return The string as a byte array, ended with a newline
     */
    public static byte[] formatOutput(String s) {
        return (s + "\n").getBytes();
    }
}
