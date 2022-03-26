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
    private int predictor;
    public BranchPredictor(){
        predictor = 0;
    }
    public int predict(){
        return predictor;
    }
    public void change_state(int result){
        if(result != predictor){
            predictor = 1-predictor;
        }
    }

}