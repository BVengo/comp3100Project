public class LRRAlgorithm extends Algorithm {
    @Override
    public void scheduleJob(Job job) {
        String reply = Client.handleMessage("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        String[] dataDetails = reply.split(" ");

        int numServers = Integer.parseInt(dataDetails[1]);
        
        reply = Client.handleMessage("OK");

        // Empty server has cores, wJobs, and rJobs initialised to 0
        Server lServer = new Server();

        for(int i = 0; i < numServers; i++) {
            Server cServer = Client.updateServerFromString(reply);
            
            // Picks the server with the highest number of total cores. If there is more than one, pick the first 
            // server with the most cores and the least total jobs.
            int sJobs = cServer.wJobs + cServer.rJobs + cServer.cJobs;
            int lsJobs = lServer.wJobs + lServer.rJobs + lServer.cJobs;

            if(cServer.totalCores > lServer.totalCores || (cServer.serverName.equals(lServer.serverName) && sJobs < lsJobs)) {
                lServer = cServer;
            }
            
            if(i != numServers - 1) {
                reply = Client.handleMessage("");
            }
        }

        Client.handleMessage("OK", ".");

        lServer.addJob(job);
        Client.handleMessage("SCHD " + job.jobId + " " + lServer.serverName + " " + lServer.serverId, "OK");
    }
}