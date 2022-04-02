import java.util.LinkedList;
import java.util.HashSet;
import java.util.Collections;
/**
 *  Decode Unit:
 *  The decode unit decodes (in a separate cycle) the instructions fetched by the fetch unit
 *  and stores the decoded instructions in an instruction queue which can hold up to NI instructions.
 *  For simplicity, we assume that NI has unlimited entries.
 *  That is, your instruction window size is unlimited and holds all the instructions fetched.
 *
 *  Register Renaming:
 *  You need to perform register renaming to eliminate the false dependences in the decode stage.
 *  Assuming we have a total of 32 physical registers (p0, p1, p2, ...p31).
 *  You will need to implement a mapping table and a free list of the physical register as we discussed in class.
 *  Also, assuming that all of the physical registers can be used by either integer or floating point instructions.
 */
public class DecodeUnit {

    LinkedList<String> undecode_instructions;
    HashSet<String> ops;

    public DecodeUnit(){
        undecode_instructions = new LinkedList<>();
        ops = new HashSet<>();
        Collections.addAll(ops,new String[]{"add","addi","fld", "fsd","fadd","fsub","fmul","fdiv","bne"});
    }

    public void add(String undecode_instrcution){
        undecode_instructions.add(undecode_instrcution);
    }

    /**
     * Split undecoded instructions into 4 string
     * The first is ops
     * The other three are reg or immediate value or targets
     */
    public String[] decode(String undecode_instruction){
        String[] instructions = new String[4];
        String[] component = undecode_instruction.split(",");
        instructions[0] = component[0].split(" ")[0].trim();
        instructions[1] = component[0].split(" ")[1].trim();
        if(component[1].contains("(")){
            component[1] = component[1].trim();
            int left = component[1].indexOf("(");
            int right = component[1].length();
            instructions[2] = component[1].substring(0,left);
            instructions[3] = component[1].substring(left+1,right-1);
            return instructions;
        }
        instructions[2] = component[1].trim();
        instructions[3] = component[2].trim();
        return instructions;
    }

    public void decode(){

    }

}
