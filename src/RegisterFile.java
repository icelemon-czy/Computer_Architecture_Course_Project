import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class RegisterFile{
    final static int R = 32;
    TreeSet<Integer> freeList;
    HashMap<String, LinkedList<String>> maptable;
    // Physical Registers store the Actual Values registers(key: id,value: value of register(could be float))
    double[] register_value;

    // Physical Register status
    // -1 means empty
    // i means ROBi
    int[] RegisterStatus;

    public RegisterFile(){
        register_value = new double[R];
        freeList = new TreeSet<>();
        for(int i = 0;i<R;i++){
            freeList.add(i);
        }
        maptable = new HashMap<>();
        RegisterStatus = new int[R];
        for(int i = 0;i<R;i++){
            RegisterStatus[i] = -1;
        }
    }


}
