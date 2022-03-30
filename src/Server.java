import java.rmi.UnexpectedException;
import java.util.HashMap;

public class Server {
    String serverName;
    int serverId        = 0;
    String serverState  = "inactive";
    int curStartTime    = -1;
    int totalCores      = 0;
    int availableCores  = 0;
    int totalMemory     = 0; 
    int availableMemory = 0;
    int totalDisk       = 0;
    int availableDisk   = 0;
    int wJobs           = 0;
    int rJobs           = 0;
    int cJobs           = 0;

    HashMap<Integer, Job> jobs = new HashMap<Integer, Job>();

    String[] statePriority = {"idle", "inactive", "active", "booting"};

    public Server() {};
    
    public Server(String serverName, int serverId, String serverState, int curStartTime, int core, int memory, int disk, int wJobs, int rJobs) {
        this.serverName = serverName;
        this.serverId = serverId;
        this.serverState = serverState;
        this.curStartTime = curStartTime;
        this.totalCores = core;
        this.availableCores = core;
        this.totalMemory = memory;
        this.availableMemory = memory;
        this.totalDisk = disk;
        this.availableDisk = disk;
        this.wJobs = wJobs;
        this.rJobs = rJobs;
    }
    
    /**
     * Adds a job to be completed
     * @param job The job to be completed
     */ 
    public void addJob(Job job) {
        jobs.put(job.jobId, job);
    }

    /**
     * Increment the completed jobs
     */
    public void completeJob(int jobId, int runTime) throws UnexpectedException {
        if(!jobs.containsKey(jobId)) {
            // This should never trigger, hence the hard exit.
            throw new UnexpectedException("Completed job could not be found.");
        }

        jobs.get(jobId).complete(runTime);
        cJobs++;
    }

    /**
     * Provides the unique server key, made up of the server name and id.
     * @return A string of the server key
     */
    public String getKey() {
        return serverName + "-" + serverId;
    }
    

    /**
     * Returns a nicely formatted string for printing that summarises the object variables
     * @return A formatted string for printing
     */
    public String toString() {
        return "key: " + getKey() + ", name: " + serverName + ", id: " + serverId + "\n" +
                "state: " + serverState + ", startTime: " + curStartTime + "\n" + 
                "total cores: " + totalCores + ", available cores: " + availableCores + "\n" +
                "total memory: " + totalMemory + ", available memory: " + availableMemory + "\n" +
                "total disk: " + totalDisk + ", available disk: " + availableDisk + "\n" +
                "working jobs: " + wJobs + ", running jobs: " + rJobs;
    }
}