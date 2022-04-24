import java.util.HashMap;
import java.util.LinkedList;
import java.io.*;
import java.util.TreeSet;

public class InstructionUnit{
    public static int pc = 0;
    public static int NF;
    InstructionCache instructioncache;
    DecodeUnit decodeunit;
    BranchPredictor branchpredictor;
    /*It records the pc we might recover if we take the wrong branch.*/
    public static LinkedList<Integer> OtherPC = new LinkedList<>();
    /*It records the decision we have made*/
    public static LinkedList<Integer> decision = new LinkedList<>();
    /*Records the Map table and free list*/
    public static LinkedList<TreeSet<Integer>> freeLists = new LinkedList<>();
    public static LinkedList<HashMap<String, String>> maptables = new LinkedList<>();

    public InstructionUnit(InstructionCache instructionCache,DecodeUnit decodeUnit,BranchPredictor branchPredictor) throws FileNotFoundException, IOException{
        NF = Simulator.getProperty("NF");
        instructioncache = instructionCache;
        decodeunit = decodeUnit;
        branchpredictor = branchPredictor;
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
                String target = instruction.split(",")[2].trim();
                if(taken == 0 ){
                    // Not Taken
                    pc = pc+4;
                    decision.add(0);
                    OtherPC.add(BranchTargetBuffer.getAddress(target));
                }else{
                    // Take the Branch
                    pc = BranchTargetBuffer.getAddress(target);
                    decision.add(1);
                    OtherPC.add(pc+4);
                }
                TreeSet<Integer> freelistcp = new TreeSet<>();
                for(Integer fr : RegisterFile.freeList){
                    freelistcp.add(fr);
                }
                freeLists.add(freelistcp);
                HashMap<String,String> maptablecp = new HashMap<>();
                for(String ar : RegisterFile.maptable.keySet()){
                    maptablecp.put(ar,RegisterFile.maptable.get(ar));
                }
                maptables.add(maptablecp);
            }else{
                pc = pc+4;
            }
            // Put instruction into Decoder Unit
            decodeunit.add(instruction);
        }
        return true;
    }
}
