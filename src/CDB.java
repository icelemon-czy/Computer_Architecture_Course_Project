import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class CDB {
    // ROB Number +  Value
    static HashMap<Integer,Double> cdb = new HashMap<>();
    static int NB ;
    public CDB()throws FileNotFoundException, IOException{
        cdb = new HashMap<>();
        NB = Simulator.getProperty("NB");
    }
    public static boolean hasValue(int robid){
        return cdb.containsKey(robid);
    }
    public static double get(int robid){
        return cdb.get(robid);
    }
    public static void remove(int robid){
        cdb.remove(robid);
    }
    public static void set(int robid,double value){
        cdb.put(robid,value);
    }
    public static boolean isfull(){
        if(cdb.size() < NB){
            return false;
        }else{
            return true;
        }
    }

    public static void display(){
        if(cdb.size()>0){
            System.out.println("CDB");
            for(Integer i : cdb.keySet()){
                System.out.println(i + "  "+cdb.get(i));
            }
        }
    }
}
