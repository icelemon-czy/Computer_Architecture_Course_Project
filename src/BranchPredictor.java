import java.util.LinkedList;
/**
 *   BHT/BTB:
 *   1-bit dynamic branch predictor (initialized to predict not taken) with 16-entry branch target buffer (BTB) is used.
 *   It hashes the address of a branch, L, to an entry in the BTB using bits 7-4 of L.
 */
// Pass Test
public  class BranchPredictor{
    // One bit dynamic branch predictor
    // 0- Not take  (initialized to predict not taken)
    // 1 -take
    private static int predictor;
    private LinkedList<Integer> prediction;
    public BranchPredictor(){
        predictor = 0;
        prediction = new LinkedList<>();
    }
    public int predict(){
        prediction.add(predictor);
        return predictor;
    }
    public static void change_state(){
        predictor = 1-predictor;
    }

}