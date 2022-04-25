import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class RegisterFile{
    final static int R = 32;
    public static TreeSet<Integer> freeList = new TreeSet<>();
    public static HashMap<String, String> maptable = new HashMap<>();
    public static HashMap<String, Integer> pregister_counter = new HashMap<>();

    // Physical Registers store the Actual Values registers(key: id,value: value of register(could be float))
    public static double[] register_value;

    // Physical Register status
    // -1 means Register currently hold the value
    // i means ROBi
    public static int[] RegisterStatus;

    public RegisterFile(){
        for(int i = 0;i<R;i++){
            freeList.add(i);
        }
        register_value = new double[R];
        RegisterStatus = new int[R];
        for(int i = 0;i<R;i++){
            RegisterStatus[i] = -1;
        }
    }

    public static void update(int register, int ROBnumber,double value){
        if(RegisterStatus[register] != ROBnumber){
            System.err.println("Potential Error");
        }
        RegisterStatus[register] = -1;
        register_value[register] = value;
    }

    public static void SetRegisterStatus(int register,int ROBNmber){
        RegisterStatus[register]  = ROBNmber;
    }

}
