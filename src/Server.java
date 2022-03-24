public class Server implements Comparable<Server> {
    String serverName;
    int serverId;
    String serverState;
    int curStartTime;
    int core;
    int memory;
    int disk;
    int wJobs;
    int rJobs;

    String[] statePriority = {"idle", "inactive", "booting", "active"};

    public Server() {};

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

    public String getKey() {
        return serverName + "-" + serverId;
    }

    public String toString() {
        return getKey() + ", " + serverName + ", " + serverId + ", " + serverState + ", " + curStartTime + 
                          ", " + core + ", " + memory + ", " + disk + ", " + wJobs + ", " + rJobs;
    }

    @Override
    public int compareTo(Server o) {
        int c;
        
        // Prioritise less running jobs
        c = Integer.compare(o.rJobs, rJobs);
        if(c != 0) {
            return c;
        }
        
        // Prioritise less working jobs
        c = Integer.compare(o.wJobs, wJobs);
        if(c != 0) {
            return c;
        }

        int idx1 = -1;
        int idx2 = -1;

        for(int i = 0; i < statePriority.length; i++) {
            if(statePriority[i].equals(this.serverState)) {
                idx1 = i;
            }

            if(statePriority[i].equals(o.serverState)) {
                idx2 = i;
            }
        }

        // Prioritise in array from left to right
        c = Integer.compare(idx2, idx1);
        if(c != 0) {
            return c;
        }
        
        // Prioritise smaller startTime
        c = Integer.compare(o.curStartTime, curStartTime);
        if(c != 0) {
            return c;
        }
        return 0;
    }
}
