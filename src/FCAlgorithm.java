import java.io.IOException;

public class FCAlgorithm implements Algorithm {

    @Override
    public void scheduleJob(Job job) throws IOException {
        String reply = Connection.handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);
        
        reply = Connection.handleMessage("OK");

        // Just select first server
        Server selected = Client.updateServerFromString(reply);

        for(int i = 0; i < numServers - 1; i++) {
            reply = Connection.handleMessage("");
        }

        Connection.handleMessage("OK", ".");

        selected.addJob(job);
        Connection.handleMessage("SCHD " + job.jobId + " " + selected.serverName + " " + selected.serverId, "OK");
    }

}