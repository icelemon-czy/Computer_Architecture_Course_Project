public class InstructionUnit{
    int pc;
    int NF;
    InstructionCache instructioncache;
    DecodeUnit decodeunit;
    BranchPredictor branchpredictor;
    BranchTargetBuffer branchtargetbuffer;
    public InstructionUnit(int nf,InstructionCache instructionCache,DecodeUnit decodeUnit,BranchPredictor branchPredictor,BranchTargetBuffer branchTargetBuffer){
        pc = 0;
        NF = nf;
        instructioncache = instructionCache;
        decodeunit = decodeUnit;
        branchpredictor = branchPredictor;
        branchtargetbuffer = branchTargetBuffer;
    }

    /**
     * Each cycle Fetch NF instructions from instruction cache and store into Decode unit.
     *
     *  If the instruction we fetch is branch instruction
     *  Update PC with branch history buffer (branch predictor's) help
     *
     * Return true if we fetch all NF instructions
     * Return false if we reach the end of file.
     */
    public boolean fetch(){
        for(int i = 0;i<NF;i++){
            String instruction = instructioncache.get(pc);
            if(instruction.length() == 0){
                return false;
            }
            // whether it is a branch instruction
            if(instruction.charAt(0) == 'b'){
                // Update PC based on Branch Predictor
                int taken = branchpredictor.predict();
                if(taken == 0 ){
                    // Not Taken
                    pc = pc+4;
                }else{
                    // Take the Branch
                    String target = instruction.split(",")[2].trim();
                    pc = branchtargetbuffer.getAddress(target);
                }
            }else{
                pc = pc+4;
            }
            // Put instruction into Decoder Unit
            decodeunit.add(instruction);
        }
        return true;
    }
    /**
     * If ROB find out
     */


}
