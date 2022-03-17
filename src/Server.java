public class Server {
    String serverName;
    int serverId;
    String serverState;
    int curStartTime;
    int core;
    int memory;
    int disk;
    int wJobs;
    int rJobs;

    public Server(String serverName, int serverId, String serverState, int curStartTime, int core, int memory, int disk, int wJobs, int rJobs) {
        this.serverName = serverName;
        this.serverId = serverId;
        this.serverState = serverState;
        this.curStartTime = curStartTime;
        this.core = core;
        this.memory = memory;
        this.disk = disk;
        this.wJobs = wJobs;
        this.rJobs = rJobs;
    }

    public String toString() {
        return serverName + ", " + serverId + ", " + serverState + ", " + curStartTime + ", " + core + ", " + memory + ", " + disk + ", " + wJobs + ", " + rJobs;
    }    
}
