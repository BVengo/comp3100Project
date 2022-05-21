import java.io.IOException;

/**
 * The Largest-Round-Robin algorithm.
 * Picks the server with the highest number of total cores. If there is more than one, pick the first 
 * server with the most cores and the least total jobs.
 */
public class LRRAlgorithm implements Algorithm {

    public void scheduleJob(Job job) throws IOException {
        String reply = Connection.handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);
        
        reply = Connection.handleMessage("OK");

        // Empty server has cores, wJobs, and rJobs initialised to 0
        Server lServer = new Server();

        for(int i = 0; i < numServers; i++) {
            Server cServer = Client.updateServerFromString(reply);
            
            // Picks the server with the highest number of total cores. If there is more than one, pick the first 
            // server with the most cores and the least total jobs.
            int sJobs = cServer.getTotalJobs();
            int lsJobs = lServer.getTotalJobs();

            if(cServer.totalCores > lServer.totalCores || (cServer.serverName.equals(lServer.serverName) && sJobs < lsJobs)) {
                lServer = cServer;
            }
            
            if(i != numServers - 1) {
                reply = Connection.handleMessage("");
            }
        }

        Connection.handleMessage("OK", ".");

        lServer.addJob(job);
        Connection.handleMessage("SCHD " + job.jobId + " " + lServer.serverName + " " + lServer.serverId, "OK");
    }

    @Override
    public String toString() {
        return "LRR Algorithm";
    }
}