/**
 * Responsibility :
 * 1. Store Memory Content ( pair of address and value)
 * 2. Store (address,value)
 * 3. Load memory (address)
 */
import java.util.HashMap;
public class MemoryUnit {
    public static HashMap<Integer,Double> memorycontent = new HashMap<>();
    public static void store(int address,double value){
        memorycontent.put(address,value);
    }
    public static double load(int address){
        if(memorycontent.containsKey(address)) {
            return memorycontent.get(address);
        }else{
            return 0;
        }
    }

    // Display everything inside memory unit, only for validation
    public static void display(){
        System.out.println("Memory Content : ");
        for(int address: memorycontent.keySet()){
            System.out.println("Memory address: "+address + ",Value: "+memorycontent.get(address) );
        }
    }
}
