import java.net.*;
import java.util.HashMap;
import java.io.*;


public class Client {
    static final int NUM_ARGS = 3;

    static Socket s;
    static BufferedReader din;
    static DataOutputStream dout;

    static Algorithm algorithm;

    static HashMap<String, Server> servers = new HashMap<String, Server>();

    public static void main(String[] args) {
        if(args.length < NUM_ARGS) {
            System.out.println("Please provide a host name, port number, and algorithm");
            System.exit(0);
        } else if(args.length > NUM_ARGS) {
            System.out.println("Too many arguments supplied! Only the first " + NUM_ARGS + " will be used.");
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        if(port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1024 and 65535 (inclusive).");
        }

        algorithm = Algorithm.getAlgorithm(args[2]);
               
        try{
            connect(hostname, port);
            authenticateConnection();
            startScheduling();            
            disconnect();

        } catch(Exception e) {
            System.out.println(e);
        }       
    }

    /**
     * Connect to a server
     * @param hostname Hostname of the server. Use localhost for testing ds-server
     * @param port Port the server is listening on. 50000 is the default for ds-server
     * @throws IOException
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
     * Send the authentication messages to the server to confirm connection.
     * @throws Exception
     */
    public static void authenticateConnection() {

        String messages[] = {
            "HELO", 
            "AUTH " + System.getProperty("user.name")
        };

        for(String m : messages) {
            handleMessage(m, "OK");
        }
    }

    /**
     * Tells the server that the client is ready for commands, and calls the appropriate function in response
     * @return TRUE if the server still has jobs to be managed. FALSE if the scheduling has been completed.
     */
    public static void startScheduling() {
        boolean toContinue = true;
        
        while(toContinue) {
            String reply = handleMessage("REDY");
            String[] splitReply = reply.split(" ");

            String command = splitReply[0];

            switch(command) {
                case "JOBN":
                    Job job = getJobFromArray(splitReply);
                    algorithm.scheduleJob(job);
                    break;
                case "JCPL":
                    String serverId = splitReply[3] + "-" + splitReply[4];
                    int runtime = Integer.parseInt(splitReply[1]);
                    int jobId = Integer.parseInt(splitReply[2]);
    
                    servers.get(serverId).completeJob(jobId, runtime);
                    break;
                case "NONE": // intentional fall-through
                default:
                    toContinue = false;
            }
        }
    }

    /**
     * Generates a Job from the split JOBN command sent by ds-server
     * @param jobDetails
     * @return
     */
    public static Job getJobFromArray(String[] jobDetails) {
        int submitTime  = Integer.parseInt(jobDetails[1]);
        int jobId       = Integer.parseInt(jobDetails[2]);
        int estRuntime  = Integer.parseInt(jobDetails[3]);
        int core        = Integer.parseInt(jobDetails[4]);
        int memory      = Integer.parseInt(jobDetails[5]);
        int disk        = Integer.parseInt(jobDetails[6]);

        return(new Job(submitTime, jobId, estRuntime, core, memory, disk));
    }

    /**
     * Update a server with the latest details or create a new one if it doesn't exist. Uses a line from the response
     * provided by the ds-server to a GETS command.
     * @param s The string from ds-server with server details
     * @return The updated or newly created server
     */
    public static Server updateServerFromString(String s) {
        String[] serverDetails = s.split(" ");

        String serverName   = serverDetails[0];
        int serverId        = Integer.parseInt(serverDetails[1]);
        String serverState  = serverDetails[2];
        int curStartTime    = Integer.parseInt(serverDetails[3]);
        int core            = Integer.parseInt(serverDetails[4]);
        int memory          = Integer.parseInt(serverDetails[5]);
        int disk            = Integer.parseInt(serverDetails[6]);
        int wJobs           = Integer.parseInt(serverDetails[7]);
        int rJobs           = Integer.parseInt(serverDetails[8]);

        Server server;
        String serverKey = serverDetails[0] + "-" + serverDetails[1];

        if(servers.containsKey(serverKey)) {
            // Update non-base values of server
            server = servers.get(serverKey);

            server.serverState      = serverState;
            server.curStartTime     = curStartTime;
            server.availableCores   = core;
            server.availableMemory  = memory;
            server.availableDisk    = disk;
            server.wJobs            = wJobs;
            server.rJobs            = rJobs;
        } else {
            // Add new server to the HashMap
            server = new Server(serverName, serverId, serverState, curStartTime, core, memory, disk, wJobs, rJobs);
            servers.put(serverKey, server);
        }
        
        return(server);
    }

    /**
     * Sends off a message to the server, and returns the reply provided
     * @param message The message to be sent
     * @return The reply from the server
     * @throws Exception
     */
    public static String handleMessage(String message) {
        String receivedMessage = "";

        try {
            if(!message.equals("")) {
                dout.write(formatOutput(message));
                dout.flush();
            }
            
            receivedMessage = din.readLine();
        }
        catch(IOException e) {
            terminate("Could not send or read a message! This implies the send message was incorrect.");
        }
        
        return receivedMessage;
    }

    /**
     * Terminates the program due to an error with a known reason.
     * @param e An error message
     */
    public static void terminate(String e) {
        System.err.println(e);
        System.exit(0);
    }

    /**
     * Sends off a message to the server, and returns the reply provided
     * Errors if the expected response isn't received
     * @param message The message to be sent
     * @param expectedResponse The response expected from the server
     * @return The reply received from the server
     * @throws Exception
     */
    public static String handleMessage(String message, String expectedResponse) {
        String reply = handleMessage(message);

        if(!reply.equals(expectedResponse)) {
            terminate("Didn't received an expected reply! This implies something incorrect was sent");
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
