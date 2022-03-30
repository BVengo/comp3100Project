public abstract class Algorithm {
    private enum AlgorithmType {
        LRR
    }

    public static Algorithm getAlgorithm(String a) {
        AlgorithmType type;
        try {
            type = AlgorithmType.valueOf(a.toUpperCase());
        } catch(IllegalArgumentException e) {
            System.out.println("Invalid algorithm type. Defaulting to LRR.");
            type = AlgorithmType.LRR;
        }

        switch(type) {
            case LRR:
                return new LRRAlgorithm();
            default:
                System.out.println("That's a currently unimplemented algorithm. Defaulting to LRR");
                return new LRRAlgorithm();
        }
    }

    /**
     * Schedule a job. 
     * @param job The job to be scheduled
     * @throws Exception
     */
    public void scheduleJob(Job job) {}
}
