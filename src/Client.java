import java.net.*;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.io.*;

// TODO: Remove abundant exception usage, and have proper error handling.
// TODO: Comment functions

public class Client {
    static final int NUM_ARGS = 3;
    static final String[] algorithms = {"lrr"};

    Socket s;
    BufferedReader din;
    DataOutputStream dout;
    BufferedReader br;

    String algorithm;
    boolean canSetAlgorithm = true;

    HashMap<String, Server> servers = new HashMap<String, Server>();

    public static void main(String[] args) {
        if(args.length < NUM_ARGS) {
            System.out.println("Please provide a host name, port number, and algorithm");
            System.exit(0);
        } else if(args.length > NUM_ARGS) {
            System.out.println("Too many arguments supplied! Only the first " + NUM_ARGS + " will be used.");
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String algorithm = args[2];

        // Port checking
        if(port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1024 and 65535 (inclusive).");
        }

        // Algorithm checking
        boolean validAlgorithm = false;
        for(String a : algorithms) {
            if(algorithm.equals(a)) {
                validAlgorithm = true;
                break;
            }
        }

        if(!validAlgorithm) {
            System.out.println("Please provide a valid algorithm!");
            System.exit(0);
        }

        // Connect and start scheduling
        Client client = new Client();
        
        try{
            client.connect(hostname, port);
            client.authenticateConnection();
            client.setAlgorithm(algorithm);

            while(client.sendReady());
            
            client.disconnect();

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
    public void connect(String hostname, int port) throws IOException {
        s = new Socket(hostname, port);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));  
        dout = new DataOutputStream(s.getOutputStream());
    }

    /**
     * Close the connection to the server gracefully
     * @throws Exception
     */
    public void disconnect() throws Exception {
        handleMessage("QUIT", "QUIT");
        din.close();
        s.close();
    }

    /**
     * Send the authentication messages to the server to confirm connection.
     * @throws Exception
     */
    public void authenticateConnection() throws Exception {

        String messages[] = {
            "HELO", 
            "AUTH " + System.getProperty("user.name")
        };

        for(String m : messages) {
            handleMessage(m, "OK");
        }
    }

    /**
     * Set the scheduling algorithm that the client will use. Stage 1 requires lrr be used
     * @param a The name of the algorithm to be used
     */
    public void setAlgorithm(String a) {
        // Only let the algorithm be set once. There should be no reason for this to be called twice
        if(canSetAlgorithm) {
            algorithm = a;
            canSetAlgorithm = false;
        } else {
            System.out.println("Algorithm has already been set! It cannot be changed.");
        }
    }

    /**
     * Tells the server that the client is ready for commands, and calls the appropriate function in response
     * @return TRUE if the server still has jobs to be managed. FALSE if the scheduling has been completed.
     * @throws Exception
     */
    public boolean sendReady() throws Exception {
        String reply = handleMessage("REDY");
        String[] splitReply = reply.split(" ");

        String command = splitReply[0];
        boolean toContinue = true;

        switch(command) {
            case "JOBN":
                Job job = getJobFromArray(splitReply);
                scheduleJob(job);
                break;
            case "JCPL":
                String serverId = splitReply[3] + "-" + splitReply[4];
                int runtime = Integer.parseInt(splitReply[1]);
                int jobId = Integer.parseInt(splitReply[2]);

                servers.get(serverId).completeJob(jobId, runtime);
                break;
            case "NONE":
                toContinue = false;
            default:
                toContinue = false;
        }

        return toContinue;
    }

    /**
     * Generates a Job from the split JOBN command sent by ds-server
     * @param jobDetails
     * @return
     */
    public Job getJobFromArray(String[] jobDetails) {
        int submitTime  = Integer.parseInt(jobDetails[1]);
        int jobId       = Integer.parseInt(jobDetails[2]);
        int estRuntime  = Integer.parseInt(jobDetails[3]);
        int core        = Integer.parseInt(jobDetails[4]);
        int memory      = Integer.parseInt(jobDetails[5]);
        int disk        = Integer.parseInt(jobDetails[6]);

        return(new Job(submitTime, jobId, estRuntime, core, memory, disk));
    }

    /**
     * Sends a job off to be scheduled using the appropriate algorithm
     * @param job The job to be scheduled
     * @throws Exception
     */
    public void scheduleJob(Job job) throws Exception {
        switch(algorithm) {
            case "lrr":
                scheduleJobLRR(job);
                break;
        }
    }

    /**
     * Schedule a job using the Largest Round Robin (LRR) algorithm
     * @param job The job to be scheduled
     * @throws Exception
     */
    public void scheduleJobLRR(Job job) throws Exception {
        String reply = handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);

        reply = handleMessage("OK");

        // Empty server has cores, wJobs, and rJobs initialised to 0
        Server lServer = new Server();

        for(int i = 0; i < numServers; i++) {
            Server cServer = updateServerFromString(reply);
            
            // Picks the server with the highest number of total cores. If there is more than one, pick the first 
            // server with the most cores and the least total jobs.
            int sJobs = cServer.wJobs + cServer.rJobs + cServer.cJobs;
            int lsJobs = lServer.wJobs + lServer.rJobs + lServer.cJobs;

            if(cServer.totalCores > lServer.totalCores || (cServer.totalCores == lServer.totalCores && sJobs < lsJobs)) {
                lServer = cServer;
            }
            
            if(i != numServers - 1) {
                reply = handleMessage("");
            }
        }

        handleMessage("OK", ".");

        lServer.addJob(job);
        handleMessage("SCHD " + job.jobId + " " + lServer.serverName + " " + lServer.serverId, "OK");
    }

    /**
     * Update a server with the latest details or create a new one if it doesn't exist. Uses a line from the response
     * provided by the ds-server to a GETS command.
     * @param s The string from ds-server with server details
     * @return The updated or newly created server
     */
    public Server updateServerFromString(String s) {
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
    public String handleMessage(String message) throws Exception {
        if(!message.equals("")) {
            dout.write(formatOutput(message));
            dout.flush();
        }
        
        String receivedMessage = din.readLine();
        return receivedMessage;
    }

    /**
     * Sends off a message to the server, and returns the reply provided
     * Errors if the expected response isn't received
     * @param message The message to be sent
     * @param expectedResponse The response expected from the server
     * @return The reply received from the server
     * @throws Exception
     */
    public String handleMessage(String message, String expectedResponse) throws Exception {
        String reply = handleMessage(message);

        if(!reply.equals(expectedResponse)) {
            throw new UnexpectedException("Didn't received an expected reply!");
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
