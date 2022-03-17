import java.net.*;
import java.rmi.UnexpectedException;
import java.io.*;

public class Client {
    static final int NUM_ARGS = 2;

    Socket s;
    BufferedReader din;
    DataOutputStream dout;
    BufferedReader br;

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

            if(!client.confirmConnection()) {
                throw new UnexpectedException("Server was unable to handle authentication!");
            }

            client.runJobs();
            client.disconnect();

        } catch(Exception e) {
            System.out.println(e);
        }       
    }

    public void connect(String hostname, int port) throws IOException {
        s = new Socket(hostname, port);
        din = new BufferedReader(new InputStreamReader(s.getInputStream()));  
        dout = new DataOutputStream(s.getOutputStream());

        System.out.println("Connection with server established.");
    }

    public void disconnect() throws IOException {
        din.close();
        s.close();
    }

    public boolean confirmConnection() throws IOException {

        String messages[] = {
            "HELO", 
            "AUTH " + System.getProperty("user.name")
        };

        String reply = "";

        for(String m : messages) {
            reply = handleMessage(m);

            if(!reply.equals("OK")) {
                return false;
            }
        }

        return true;
    }

    public void runJobs() throws IOException {
        // This is where the client will be sent jobs to distribute
        String reply = handleMessage("REDY");
        String[] splitReply = reply.split(" ");

        if(!splitReply[0].equals("JOBN")) {
            // TODO: Handle other commands at this point, or branch for different commands
            return;
        }
        // See Job.java for descriptions
        int submitTime  = Integer.parseInt(splitReply[1]);
        int jobId       = Integer.parseInt(splitReply[2]);
        int estRuntime  = Integer.parseInt(splitReply[3]);
        int core        = Integer.parseInt(splitReply[4]);
        int memory      = Integer.parseInt(splitReply[5]);
        int disk        = Integer.parseInt(splitReply[6]);

        Job job = new Job(submitTime, jobId, estRuntime, core, memory, disk);

        System.out.println(job);

        // TODO: Example log only has capable, but I need to explore further
        reply = handleMessage("GETS Capable " + core + " " + memory + " " + disk);
        splitReply = reply.split(" ");

        if(!splitReply[0].equals("DATA")) {
            // TODO: Handle other commands at this point, or branch for different commands
            return;
        }

        int numJobs         = Integer.parseInt(splitReply[1]);
        int messageLength   = Integer.parseInt(splitReply[2]);

        Server[] servers = new Server[numJobs];

        reply = handleMessage("OK");
        splitReply = reply.split(" ");
        
        for(int i = 0; i < numJobs; i++) {
            String serverName   = splitReply[0];
            int serverId        = Integer.parseInt(splitReply[1]);
            String serverState  = splitReply[2];
            int curStartTime    = Integer.parseInt(splitReply[3]);
            core                = Integer.parseInt(splitReply[4]);
            memory              = Integer.parseInt(splitReply[5]);
            disk                = Integer.parseInt(splitReply[6]);
            int wJobs           = Integer.parseInt(splitReply[7]);
            int rJobs           = Integer.parseInt(splitReply[8]);

            servers[i] = new Server(serverName, serverId, serverState, curStartTime, core, memory, disk, wJobs, rJobs);
            
            if(i != numJobs - 1) {
                reply = din.readLine();
            }
        }

        handleMessage("OK");
        handleMessage("QUIT");
    }

    public String handleMessage(String message) throws IOException {
        dout.write(formatOutput(message));
        dout.flush();

        System.out.println("SENT " + message);

        String receivedMessage = din.readLine();
        System.out.println("RCVD " + receivedMessage);

        return receivedMessage;
    }

    public static byte[] formatOutput(String s) {
        return (s + "\n").getBytes();
    }
}
