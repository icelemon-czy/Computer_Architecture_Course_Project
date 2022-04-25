import java.util.*;

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
    public final static Set<String> Write_Ops = Collections.unmodifiableSet(Set.of("add","addi","fld","fadd","fsub","fmul","fdiv"));
    int NF;
    LinkedList<String> undecode_instructions;
    HashSet<String> ops;
    InstructionQueue IQ;
    public DecodeUnit(int NF,InstructionQueue IQ){
        this.NF = NF;
        this.IQ = IQ;
        undecode_instructions = new LinkedList<>();
        ops = new HashSet<>();
        Collections.addAll(ops,new String[]{"add","addi","fld", "fsd","fadd","fsub","fmul","fdiv","bne"});
    }

    public void add(String undecode_instrcution){
        undecode_instructions.add(undecode_instrcution);
    }

    /**
     * For each stage decode upto NF = 4 instructions and put it into Instruction Queue
     */
    public void decode(){
        for(int i = 0 ;i<NF && undecode_instructions.size()>0;i++){
            String[] decodeinstruction =decode(undecode_instructions.removeFirst());
            IQ.add(decodeinstruction);
        }
    }

    /**
     * Step 1. Split undecode instructions into 4 string
     * The first is ops
     * The other three are reg or immediate value or targets
     * For example : fld F0, 0(R1) => String[] fld, F0, 0, R1
     *  fmul F0, F0, F2 => String[] fmul,F0, F0,F2
     *
     *  Step 2. Do Register Renaming
     */
    public String[] decode(String undecode_instruction){
        String[] instructions = new String[4];
        String[] component = undecode_instruction.split(",");
        instructions[0] = component[0].split(" ")[0].trim().toLowerCase();
        instructions[1] = component[0].split(" ")[1].trim();
        if(component[1].contains("(")){
            component[1] = component[1].trim();
            int left = component[1].indexOf("(");
            int right = component[1].length();
            instructions[2] = component[1].substring(0,left);
            instructions[3] = component[1].substring(left+1,right-1);
            return RegisterRename(instructions);
        }
        instructions[2] = component[1].trim();
        instructions[3] = component[2].trim();
        instructions = RegisterRename(instructions);
        /**
        for(String s:instructions){
            System.out.print(s+" ");
        }
        System.out.println();
         **/
        return instructions;
    }

    public String[] RegisterRename(String[] instructions){
        String ops = instructions[0];
        String ArchitectedRegister;
        // For the RAW we do nothing
        for(int i = 2;i<=3;i++){
            if(!(ops.equals("bne") && i==3)) {
                if (isRegister(instructions[i])) {
                    ArchitectedRegister = instructions[i];
                    // Assign a free physical register to Architected Register if it does not original in map table
                    if (!RegisterFile.maptable.containsKey(ArchitectedRegister)) {
                        int freeregister = RegisterFile.freeList.iterator().next();
                        RegisterFile.freeList.remove(freeregister);
                        instructions[i] = "p" + freeregister;
                        RegisterFile.maptable.put(ArchitectedRegister, instructions[i]);
                        RegisterFile.pregister_counter.put(instructions[i],1);
                    } else {
                        instructions[i] = RegisterFile.maptable.get(ArchitectedRegister);
                        RegisterFile.pregister_counter.put(instructions[i],RegisterFile.pregister_counter.get(instructions[i])+1);
                    }
                }
            }
        }

        //For the first register
        ArchitectedRegister = instructions[1];
        // Assign a free physical register to Architected Register if it does not original in map table
        if (!RegisterFile.maptable.containsKey(ArchitectedRegister)) {
            int freeregister = RegisterFile.freeList.iterator().next();
            RegisterFile.freeList.remove(freeregister);
            instructions[1] = "p"+ freeregister;
            RegisterFile.maptable.put(ArchitectedRegister, instructions[1]);
            RegisterFile.pregister_counter.put(instructions[1],1);
        }else{
            // Check the instruction, whether we update the value (W)
            if(Write_Ops.contains(ops)){
                // If ops is one of write ops, get free register and add to map table
                int freeregister = RegisterFile.freeList.iterator().next();
                RegisterFile.freeList.remove(freeregister);
                instructions[1] = "p" + freeregister;
                RegisterFile.maptable.put(ArchitectedRegister, instructions[1]);
                RegisterFile.pregister_counter.put(instructions[1],1);
            }else{
                // If the ops is read ops,like fsd and bne, assign the register from the last ops.
                instructions[1] = RegisterFile.maptable.get(ArchitectedRegister);
                RegisterFile.pregister_counter.put(instructions[1],RegisterFile.pregister_counter.get(instructions[1])+1);
            }
        }

        /**
        for(String s:instructions){
            System.out.print(s+" ");
        }
        System.out.println();
         **/
        return instructions;
    }

    /**
     * Return true if given string is a register
     * Return false if given string is immediate value.
     */
    public static boolean isRegister(String test){
        if((test.charAt(0)<='9' && test.charAt(0)>='0') || test.charAt(0) == '-' || test.charAt(0) == '$'){
            return false;
        }
        return true;
    }

    public static void main(String[] args){
        System.out.println(isRegister("F2"));
        System.out.println(isRegister("100"));
        System.out.println(isRegister("loop"));
    }


}
