import java.io.IOException;

/**
 * The Custom Turnaround algorithm.
 * Picks the server with the highest number of total cores. If there is more than one, pick the first 
 * server with the most cores and the least total jobs.
 */
public class CTAlgorithm implements Algorithm {

    public void scheduleJob(Job job) throws IOException {
        String reply = Connection.handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);
        
        reply = Connection.handleMessage("OK");

        Server selected = Client.updateServerFromString(reply);
        int sWaitTime = selected.getParallelEstimateWait(job);

        for(int i = 1; i < numServers; i++) {
            reply = Connection.handleMessage("");

            Server current = Client.updateServerFromString(reply);
            
            int cWaitTime = current.getParallelEstimateWait(job);

            if(
                // Smaller wait time
                (cWaitTime < sWaitTime) ||
                // Same wait time but smaller server
                (cWaitTime == sWaitTime && current.totalCores < selected.totalCores) ||
                // Placeholder for adding more filters
                (false)) {
                    selected = current;
                    sWaitTime = cWaitTime;
            }
        }

        Connection.handleMessage("OK", ".");

        selected.addJob(job);
        Connection.handleMessage("SCHD " + job.jobId + " " + selected.serverName + " " + selected.serverId, "OK");
    }

    @Override
    public String toString() {
        return "Custom Turnaround (CT) Algorithm";
    }
}