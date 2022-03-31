import java.util.HashMap;

/**
 *
 */
public class InstructionCache {
    int pc;
    HashMap<Integer,String> instrcutions;
    public InstructionCache(){
        pc = 0;
        instrcutions = new HashMap<>();
    }
    public void put(int PC,String instruction){
        instrcutions.put(pc,instruction);
    }

    public String get(int pc){
        return instrcutions.get(pc);
    }

    // Display everything inside instruction cache, only for validation
    public void display(){
        System.out.println("Instrcutions with their PC");
        for(int pc:instrcutions.keySet()){
            System.out.println("PC: "+ pc+" "+ instrcutions.get(pc));
        }
    }
}
