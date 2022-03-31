public abstract class AlgorithmFactory {

    public static Algorithm getAlgorithm(String a) {
        
        a = a.toUpperCase();
        
        switch(a) {
            case "LRR":
                return new LRRAlgorithm();
            default:
                System.out.println("That's a currently unimplemented algorithm. Defaulting to LRR");
                return new LRRAlgorithm();
        }
    }
}
