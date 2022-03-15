import java.net.*;
import java.io.*;

public class Client {
    static Socket s;
    static DataInputStream din;
    static DataOutputStream dout;
    static BufferedReader br;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Please provide a host name (localhost) and port number (6666)");
            System.exit(0);
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        if(port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535 (inclusive).");
        }

        if(port < 1024) {
            System.out.println("Using system ports. Make sure this is what you want!");
        }

        try{      
            Socket s = new Socket(hostname, port);

            din = new DataInputStream(s.getInputStream());  
            dout = new DataOutputStream(s.getOutputStream());  
            br = new BufferedReader(new InputStreamReader(System.in));  

            System.out.println("Connection with server established.");

            String message = "";
            String receivedMessage = "";

            while(!message.equals("BYE")) {
                System.out.println("What would you like to say? (write HELO first and then BYE for this task)\n");
                message = br.readLine();
                dout.writeUTF(message);
                dout.flush();

                receivedMessage = din.readUTF();
                System.out.println("Server responded: " + receivedMessage);
            }
            
            din.close();
            s.close();
        } catch(Exception e) {
            System.out.println(e);
        }  
    }
}
