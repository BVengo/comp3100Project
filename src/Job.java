public class Job {
    int submitTime; // Time of initial submission or time of re-submission after the job is pre-empted/failed/killed
    int jobId;      // Sequence number based on initial submission time
    int estRuntime; // Estimated runtime based on actual runtime
    int core;       // Number of required CPU cores
    int memory;     // Amount of required RAM (MB) 
    int disk;       // Amount of required storage (MB)

    public Job(int submitTime, int jobId, int estRuntime, int core, int memory, int disk) {
       this.submitTime = submitTime;
       this.jobId = jobId;
       this.estRuntime = estRuntime;
       this.core = core;
       this.memory = memory;
       this.disk = disk;
    }

    public String toString() {
        return submitTime + ", " + jobId + ", " + estRuntime + ", " + core + ", " + memory + ", " + disk;
    }
}
