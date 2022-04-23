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
 *
 *
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Simulator {
    public static int getProperty(String propertyname)throws FileNotFoundException, IOException{
        FileInputStream fis = new FileInputStream("resources/config.properties");
        Properties properties = new Properties();
        properties.load(fis);
        return Integer.parseInt(properties.getProperty(propertyname));
    }

    // All the magic Start here.
    public static void main(String[] args)throws FileNotFoundException, IOException {
        /**
         * Set up configuration
         */

        int NF = getProperty("NF");
        int NW = getProperty("NW");
        //int NB = Integer.parseInt(properties.getProperty("NB"));
        //int NR = Integer.parseInt(properties.getProperty("NR"));

        MemoryUnit memoryUnit = new MemoryUnit();
        InstructionCache instructionCache = new InstructionCache();
        RegisterFile registerFile = new RegisterFile();
        ROB rob = new ROB();
        ReservationStation reservationStation = new ReservationStation(registerFile);
        InstructionQueue instructionQueue = new InstructionQueue(NW,rob,reservationStation);
        BranchTargetBuffer branchTargetBuffer = new BranchTargetBuffer();
        BranchPredictor branchPredictor = new BranchPredictor();
        DecodeUnit decodeUnit = new DecodeUnit(registerFile,NF,instructionQueue);
        InstructionUnit instructionUnit = new InstructionUnit(NF,instructionCache,decodeUnit,branchPredictor,branchTargetBuffer);

        ReadProgram("src/prog.dat", memoryUnit,instructionCache,branchTargetBuffer);
        /**
        memoryUnit.display();
        branchTargetBuffer.display();
        instructionCache.display();
        */

        for(int cycle = 0;cycle<2;cycle++){
            instructionUnit.fetch();
            decodeUnit.decode();
            instructionQueue.dispatch();
        }
        /**
        for(String s :registerFile.maptable.keySet()){
            System.out.println(s);
        }
         **/

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
    public static void ReadProgram(String inputfile, MemoryUnit memoryUnit,InstructionCache instructionCache,BranchTargetBuffer branchTargetBuffer)
            throws FileNotFoundException{
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
                //Memory content
                if('0'<=line.charAt(0) && line.charAt(0) <='9'){
                    int address  = Integer.parseInt(line.split(",")[0].trim());
                    int value = Integer.parseInt(line.split(",")[1].trim());
                    memoryUnit.store(address,value);
                }else{
                    // Get Instructions and its address
                    // First We check whether starts with Target Label, If true Then we store Target into BTB
                    if(line.contains(":")){
                        String label =  line.split(":")[0].trim();
                        line = line.split(":")[1].trim();
                        branchTargetBuffer.add(label,PC);
                    }
                    instructionCache.put(PC,line.trim());
                    PC = PC+4;
                }
            }
        }
    }


}
