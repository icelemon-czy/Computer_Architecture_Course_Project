/**
 *  Purpose :
 *   To evaluate this effect of different architecture parameters on a CPU design
 *   by simulating a modified (and simplified) version of the PowerPc 604 and 620 architectures.
 *
 *  Setting :
 *   We will assume a 32-bit architecture that executes a subset of the RISC V ISA
 *   which consists of the following 9 instructions: fld, fsd, add, addi, fadd, fsub, fmul, fdiv, bne.
 *   (See Appendix A)
 *
 *  Simulator Job:
 *   Your simulator should read this input file, recognize the instructions,recognize the different fields of the instructions,
 *   and simulate their execution on the architecture described below in this handout.
 *   our will have to implement the functional+timing simulator.
 *
 *  The simulated architecture:
 *  is a speculative, multi-issue, out of order CPU.
 *
 *  Components:
 *  PC:
 *  Assuming your first instruction resides in the memory location (byte address) 0x00000hex.
 *  That is, the address for the first instruction is 0x00000hex.
 *  PC+4 points to next instruction
 *
 *  Instruction Fetch:
 *  Up to NW=4 instructions can be issued every clock cycle to reservation stations.
 *
 *
 *
 *  Functions:
 *
 *  Register Renaming:
 *  You need to perform register renaming to eliminate the false dependences in the decode stage.
 *  Assuming we have a total of 32 physical registers (p0, p1, p2, ...p31).
 *  You will need to implement a mapping table and a free list of the physical register as we discussed in class.
 *  Also, assuming that all of the physical registers can be used by either integer or floating point instructions.
 *
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Simulator {
    public static void main(String[] args) throws FileNotFoundException{
        HashSet<String> ops = new HashSet<>();
        Collections.addAll(ops,new String[]{"add","addi","fld", "fsd","fadd","fsub","fmul","fdiv","bne"});

        HashMap<Integer,Integer> memorycontent = new HashMap<>();
        TreeMap<Integer,String> instructions = new TreeMap<>();
        HashMap<Integer,String> BTB = new HashMap<>();

        ReadProgram("src/prog.dat", memorycontent,ops,instructions,BTB);
        /* Test for Read Input file （Test PASS)
        System.out.println("Memory Content : ");
        for(int address : memorycontent.keySet()){
            System.out.println(address + ","+ memorycontent.get(address));
        }
        System.out.println("Branch Target Buffer");
        for(int address : BTB.keySet()){
            System.out.println(address + ":"+ BTB.get(address));
        }
        System.out.println("Instrcutions with their PC");
        for(int pc:instructions.keySet()){
            System.out.println("PC: "+ pc+" "+ instructions.get(pc));
        }
        */
    }
    /**
     * Input : input file prog.dat containing a RISC V assembly language program (code segment).
     * Each line in the input file is a RISC V instruction from the aforementioned nine instructions.
     *
     * Goal :
     * 1. Get Memory Content
     * 2. Get all instructions and corresponding address.
     * 3. Get Branch Target Buffer
     */
    public static void ReadProgram(String inputfile, HashMap<Integer,Integer> memorycontent,HashSet<String> ops,TreeMap<Integer,String> instructions,HashMap<Integer,String> BTB)throws FileNotFoundException{
        File file = new File(inputfile);
        Scanner scnr = new Scanner(file);
        int PC = 0;
        while(scnr.hasNextLine()){
            String line = scnr.nextLine();
            /**
             *  Case 1.comment - start with %  skip
             *  Case 2.memory content
             *  Case 3.Instruction
             */
            if(!line.startsWith("%")){
                // Get Memory content
                if('0'<=line.charAt(0) && line.charAt(0) <='9'){
                    int address  = Integer.parseInt(line.split(",")[0].trim());
                    int value = Integer.parseInt(line.split(",")[1].trim());
                    memorycontent.put(address,value);
                }else{
                    // Get Instructions and its address
                    // First We check whether starts with Target Label, If true Then we store Target into BTB
                    if(line.contains(":")){
                        String label =  line.split(":")[0].trim();
                        line = line.split(":")[1].trim();
                        BTB.put(PC,label);
                    }
                    instructions.put(PC,line.trim());
                    PC = PC+4;
                }
            }

        }
    }

}
