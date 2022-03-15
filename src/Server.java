import java.net.*;
import java.io.*;

public class Server {
    static ServerSocket ss;
    static Socket s;

    static DataInputStream din;
    static DataOutputStream dout;
    static BufferedReader br;

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Please provide a port number (6666)");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);

        if(port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535 (inclusive).");
        }

        if(port < 1024) {
            System.out.println("Using system ports. Make sure this is what you want!");
        }

        try {  
            ss = new ServerSocket(port);  

            System.out.println("Server established. Awaiting client connection and message");
            s = ss.accept();  

            din = new DataInputStream(s.getInputStream());  
            dout = new DataOutputStream(s.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));

            String receivedMessage = "";
            String response = "";
            
            while(!receivedMessage.equals("BYE")) {
                receivedMessage = din.readUTF();

                System.out.println("Client Message: " + receivedMessage);

                switch(receivedMessage) {
                    case "HELO":
                        response = "G'DAY";
                        break;
                    case "BYE":
                        response = "BYE";
                        break;
                    default:
                        response = "I wasn't expecting that input! Please say 'HELO' or 'BYE'";
                }

                System.out.println("Responding with: " + response);
                dout.writeUTF(response);
                dout.flush();
            } 
            
            din.close();
            s.close();          
            ss.close();
        } catch(Exception e) {
            System.out.println(e);
        }  
    }
}
