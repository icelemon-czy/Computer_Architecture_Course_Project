import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

public class RegisterFile{
    final static int R = 64;
    public static TreeSet<Integer> freeList = new TreeSet<>();
    public static HashMap<String, String> maptable = new HashMap<>();
    public static HashSet<String> canfree_pregister = new HashSet<>();

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
        //if(RegisterStatus[register] != ROBnumber){
         //   System.err.println("Potential Error");
        //}
        RegisterStatus[register] = -1;
        register_value[register] = value;
    }

    public static void SetRegisterStatus(int register,int ROBNmber){
        RegisterStatus[register]  = ROBNmber;
    }

    public static void display(){
        System.out.println("Register File(non-zero value):");
        for(int i = 0;i<R;i++){
            if(register_value[i] != 0){
                System.out.println("p"+i+" : "+ register_value[i]);
            }
        }
    }

    public static void Mapdispaly(){
        System.out.println("Map:");
        for(String s :RegisterFile.maptable.keySet()){
            System.out.print(s+" ");
            System.out.println(RegisterFile.maptable.get(s));
        }
    }


}
