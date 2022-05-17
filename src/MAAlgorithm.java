import java.io.IOException;

public class MAAlgorithm implements Algorithm {

    public void scheduleJob(Job job) throws IOException {
        String reply = Connection.handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);
        
        reply = Connection.handleMessage("OK");

        Server selected = Client.updateServerFromString(reply);

        for(int i = 1; i < numServers; i++) {
            reply = Connection.handleMessage("");

            Server current = Client.updateServerFromString(reply);

            if( // Prioritise no jobs
                // (current.getIncompleteJobs() == 0 && selected.getIncompleteJobs() > 0) ||
                // // Prioritise active with less jobs over booting
                // (selected.serverState.equals("booting") && current.serverState.equals("active") && 
                //  selected.getIncompleteJobs() >= current.getIncompleteJobs()) ||
                // Prioritise less available cores (greater than required)
                (job.core <= current.availableCores && (current.availableCores <= selected.availableCores || selected.availableCores < job.core)) ||
                // Then prioritise lower estimated runtime
                // (current.getEstimatedRuntime() < selected.getEstimatedRuntime()) ||
                // Placeholder for adding more
                (false)) {
                    selected = current;
            }
        }

        Connection.handleMessage("OK", ".");

        selected.addJob(job);
        Connection.handleMessage("SCHD " + job.jobId + " " + selected.serverName + " " + selected.serverId, "OK");
    }

    @Override
    public String toString() {
        return "MAA Algorithm";
    }
}