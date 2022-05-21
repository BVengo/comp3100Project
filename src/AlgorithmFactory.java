public abstract class AlgorithmFactory {

    public static Algorithm getAlgorithm(String a) {
        
        a = a.toUpperCase();
        
        switch(a) {
            case "LRR":
                return new LRRAlgorithm();
            case "FC":
                return new FCAlgorithm();
            case "CT":
                return new CTAlgorithm();
            default:
                System.out.println("That's currently an unimplemented algorithm. Defaulting to LRR");
                return new LRRAlgorithm();
        }
    }
}
