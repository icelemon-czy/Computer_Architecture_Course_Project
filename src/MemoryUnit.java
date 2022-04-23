/**
 * Responsibility :
 * 1. Store Memory Content ( pair of address and value)
 * 2. Store (address,value)
 * 3. Load memory (address)
 */
import java.util.HashMap;
public class MemoryUnit {
    static HashMap<Integer,Integer> memorycontent;
    public MemoryUnit(){
        memorycontent = new HashMap<>();
    }
    public static void store(int address,int value){
        memorycontent.put(address,value);
    }
    public static int load(int address){
        return memorycontent.get(address);
    }

    // Display everything inside memory unit, only for validation
    public void display(){
        System.out.println("Memory Content : ");
        for(int address: memorycontent.keySet()){
            System.out.println("Memory address: "+address + ",Value: "+memorycontent.get(address) );
        }
    }
}
