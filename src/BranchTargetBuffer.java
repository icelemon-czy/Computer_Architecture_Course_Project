/**
 * Responsibility :
 * 1. Store Target and Address ( pair of target and address)
 * 2. Add (target,address)
 * 3. Search (target,address)
 */
import java.util.HashMap;
public class BranchTargetBuffer {
    static HashMap<String,Integer> branchTarget;
    public BranchTargetBuffer(){
        branchTarget = new HashMap<>();
    }
    public static void add(String target,int address){
        branchTarget.put(target,address);
    }
    public static int getAddress(String target){
        return branchTarget.get(target);
    }

    public void display(){
        System.out.println("Branch Target Buffer");
        for(String target : branchTarget.keySet()){
            System.out.println(branchTarget.get(target) + ":"+ target);
        }
    }

}
