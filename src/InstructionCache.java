import java.util.HashMap;

public class InstructionCache {
    int pc;
    HashMap<Integer,String> instrcutions;
    public InstructionCache(){
        pc = 0;
        instrcutions = new HashMap<>();
    }
    public void put(int PC,String instruction){
        instrcutions.put(PC,instruction);
    }

    public String get(int pc){
        if(!instrcutions.containsKey(pc)){
            return null;
        }
        return instrcutions.get(pc);
    }

    // Display everything inside instruction cache, only for validation
    public void display(){
        System.out.println("Instrcutions with their Address");
        for(int address:instrcutions.keySet()){
            System.out.println("Address: "+ address+" "+ instrcutions.get(address));
        }
    }
}
