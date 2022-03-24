import java.net.*;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.io.*;

// TODO: Remove abundant exception usage, and have proper error handling.

public class Client {
    static final int NUM_ARGS = 2;

    Socket s;
    BufferedReader din;
    DataOutputStream dout;
    BufferedReader br;

    HashMap<String, Server> servers = new HashMap<String, Server>();

    public static void main(String[] args) {
        if(args.length < NUM_ARGS) {
            System.out.println("Please provide a host name and port number");
            System.exit(0);
        } else if(args.length > NUM_ARGS) {
            System.out.println("Too many arguments supplied! Only the first " + NUM_ARGS + " will be used.");
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        if(port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535 (inclusive).");
        }

        if(port < 1024) {
            System.out.println("Using system ports. Make sure this is what you want!");
        }

        Client client = new Client();
        
        try{
            client.connect(hostname, port);
            client.authenticateConnection();
            client.storeServerDetails();
            client.sendReady();
            client.disconnect();

        } catch(Exception e) {
            System.out.println(e);
        }       
    }

    public void connect(String hostname, int port) throws IOException {
        s = new Socket(hostname, port);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));  
        dout = new DataOutputStream(s.getOutputStream());
    }

    public void disconnect() throws Exception {
        handleMessage("QUIT", "QUIT");
        din.close();
        s.close();
    }

    public void authenticateConnection() throws Exception {

        String messages[] = {
            "HELO", 
            "AUTH " + System.getProperty("user.name")
        };

        for(String m : messages) {
            handleMessage(m, "OK");
        }
    }

    public void storeServerDetails() throws Exception {
        handleMessage("REDY");

        String reply = handleMessage("GETS All");
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);
        
        for(int i = 0; i < numServers; i++) {
            reply = handleMessage(i == 0 ? "OK" : "");

            Server s = getServerFromString(reply); 
            servers.put(s.getKey(), s);
        }

        handleMessage("OK", ".");
    }

    public void sendReady() throws Exception {
        String reply = handleMessage("REDY");
        String[] splitReply = reply.split(" ");

        switch(splitReply[0]) {
            case "JOBN":
                Job job = getJobFromArray(splitReply);
                distributeJob(job);
                return;
            case "JCPL":
                sendReady();
                return;
            case "NONE":
                return;
            default:
                return;
        }
    }

    public void distributeJob(Job job) throws Exception {
        String reply = handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);

        reply = handleMessage("OK");
        Server largestServer = new Server();
        int maxTotalCores = -1;

        for(int i = 0; i < numServers; i++) {
            Server s = getServerFromString(reply);
            String key = s.getKey();

            int totalCores = servers.get(key).core;
            
            if(i == 0 || totalCores > maxTotalCores || (totalCores == maxTotalCores && s.compareTo(largestServer) > 0)) {
                largestServer = s;
                maxTotalCores = totalCores;
            }
            
            if(i != numServers - 1) {
                reply = handleMessage("");
            }
        }

        handleMessage("OK", ".");
        handleMessage("SCHD " + job.jobId + " " + largestServer.serverName + " " + largestServer.serverId, "OK");

        sendReady();
    }

    public Job getJobFromArray(String[] jobDetails) {
        int submitTime  = Integer.parseInt(jobDetails[1]);
        int jobId       = Integer.parseInt(jobDetails[2]);
        int estRuntime  = Integer.parseInt(jobDetails[3]);
        int core        = Integer.parseInt(jobDetails[4]);
        int memory      = Integer.parseInt(jobDetails[5]);
        int disk        = Integer.parseInt(jobDetails[6]);

        return(new Job(submitTime, jobId, estRuntime, core, memory, disk));
    }

    public Server getServerFromString(String s) {
        String[] serverDetails = s.split(" ");
        
        String serverName   = serverDetails[0];
        int serverId        = Integer.parseInt(serverDetails[1]);
        String serverState  = serverDetails[2];
        int curStartTime    = Integer.parseInt(serverDetails[3]);
        int core                = Integer.parseInt(serverDetails[4]);
        int memory              = Integer.parseInt(serverDetails[5]);
        int disk                = Integer.parseInt(serverDetails[6]);
        int wJobs           = Integer.parseInt(serverDetails[7]);
        int rJobs           = Integer.parseInt(serverDetails[8]);

        return(new Server(serverName, serverId, serverState, curStartTime, core, memory, disk, wJobs, rJobs));
    }

    public String handleMessage(String message) throws Exception {
        if(!message.equals("")) {
            dout.write(formatOutput(message));
            dout.flush();
        }
        
        String receivedMessage = din.readLine();
        return receivedMessage;
    }

    public String handleMessage(String message, String expectedResponse) throws Exception {
        String reply = handleMessage(message);

        if(!reply.equals(expectedResponse)) {
            throw new UnexpectedException("Didn't received an expected reply!");
        }

        return reply;
    }

    public static byte[] formatOutput(String s) {
        return (s + "\n").getBytes();
    }
}
