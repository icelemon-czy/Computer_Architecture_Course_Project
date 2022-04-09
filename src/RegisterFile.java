import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class RegisterFile{
    final int R = 32;
    TreeSet<Integer> freeList;
    HashMap<String, LinkedList<Integer>> maptable;
    // Physical Registers store the Actual Values registers(key: id,value: value of register(could be float))
    HashMap<Integer,Double> registers;

    public RegisterFile(){
        registers = new HashMap<>();
        freeList = new TreeSet<>();
        for(int i = 0;i<R;i++){
            freeList.add(i);
        }
        maptable = new HashMap<>();
    }


}
