/**
 * Responsibility :
 * 1. Store Memory Content ( pair of address and value)
 * 2. Store (address,value)
 * 3. Load memory (address)
 */
import java.util.HashMap;
public class MemoryUnit {
    HashMap<Integer,Integer> memorycontent;
    public MemoryUnit(){
        memorycontent = new HashMap<>();
    }
    public void store(int address,int value){
        memorycontent.put(address,value);
    }
    public int load(int address){
        return memorycontent.get(address);
    }
}