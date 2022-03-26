
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