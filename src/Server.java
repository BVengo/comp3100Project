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
     * Gets the remaining waiting and running jobs
     */
    public int getNumIncompleteJobs() {
        return wJobs + rJobs;
    }
    
    /**
     * Get all jobs, complete or incomplete
     */
    public int getTotalJobs() {
        return wJobs + rJobs + cJobs;
    }

    /**
     * Return sum of the waiting jobs estimated runtime
     */
    public int getTotalEstimateRuntime() {
        int runtime = 0;

        for(Job job : jobs.values()) {
            if(!job.completed) {
                runtime += job.estRuntime;
            }
        }
        return runtime;
    }

    /**
     * Get an estimate for the time a new job will have to wait, based on potentially having jobs running
     * in parallel on different cores. The buffer on this estimate will grow with the number of waiting jobs,
     * which means it will become increasingly unlikely that more jobs will be allocated to the server.
     * @return An integer indicating the estimate parallel runtime of current jobs
     */
    public int getParallelEstimateWait(Job job) {
        if(job.core <= availableCores) {
            return 0;
        }

        Job[] incompleteJobs = getIncompleteJobs();
        Job[] jobs = new Job[incompleteJobs.length + 1];

        for(int i = 0; i < incompleteJobs.length; i++) {
            jobs[i] = incompleteJobs[i];
        }

        jobs[incompleteJobs.length] = job;

        return(getParallelEstimateWait(jobs));
    }

    /**
     * Get an estimate for the wait and run time of current jobs, based on potentially having jobs running
     * in parallel on different cores. The buffer on this estimate will grow with the number of waiting jobs,
     * which means it will become increasingly unlikely that more jobs will be allocated to the server.
     * @return An integer indicating the estimate parallel runtime of current jobs
     */
    private int getParallelEstimateWait(Job[] incompleteJobs) {
        // Total number of cores for all current jobs
        int totalJobCores = 0;
        for(Job j : incompleteJobs) {
            totalJobCores += j.core;
        }

        // Number of total required cores since some will run parallel
        int numParallel = (int)Math.ceil((float)totalJobCores / (float)totalCores);

        // Maximum times of the jobs expected to be running
        int[] maxEstTimes = new int[numParallel];
        
        for(Job j : incompleteJobs) {
            for(int i = 0; i < maxEstTimes.length; i++) {
                if(j.estRuntime > maxEstTimes[i]) {
                    maxEstTimes[i] = j.estRuntime;
                    break;
                }
            }
        }

        int sumEstTimes = 0;
        for(int time : maxEstTimes) {
            sumEstTimes += time;
        }

        return sumEstTimes;
    }

    public Job[] getIncompleteJobs() {
        Job[] incomplete = new Job[getNumIncompleteJobs()];
        int i = 0;

        for(Job job : jobs.values()) {
            if(!job.completed) {
                incomplete[i] = job;
                i++;
            }
        }
        
        return incomplete;
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