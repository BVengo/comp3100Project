import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.HashMap;

public class Client {
    static final int NUM_ARGS = 3;
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

        algorithm = AlgorithmFactory.getAlgorithm(args[2]);        
        System.out.println("Beginning scheduling with " + algorithm.toString());

        try{
            Connection.connect(hostname, port);
            authenticateConnection();
            startScheduling();            
            Connection.disconnect();

        } catch(Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Send the authentication messages to the server to confirm connection.
     * @throws IOException On message failure
     */
    private static void authenticateConnection() throws IOException {

        String messages[] = {
            "HELO", 
            "AUTH " + System.getProperty("user.name")
        };

        for(String m : messages) {
            Connection.handleMessage(m, "OK");
        }
    }

    /**
     * Tells the server that the client is ready for commands, and calls the appropriate function in response
     * @return TRUE if the server still has jobs to be managed. FALSE if the scheduling has been completed.
     * @throws IOException On message failure
     * @throws UnexpectedException On job identification failure
     */
    private static void startScheduling() throws IOException, UnexpectedException {
        boolean toContinue = true;
        
        while(toContinue) {
            String reply = Connection.handleMessage("REDY");
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
     * @return a Job object
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
}
