import java.io.File;

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
 *  The architecture has the following functional units with the shown latencies and number of reservation stations.
 *  Unit       |    Latency(cycle) for op           | Reservation Stations          | Instructions executing on the unit
 *  INT          1 (integer and logic operations)            4                              add / addi
 *  Load/Store   1 for address calculation            2 load buffer + 2 store buffer        fld fsd
 *  FPadd        3 (non-pipelined FP add)                    3                              fadd / fsub
 *  FPmult       4 (non-pipelined FP multiply)               4                              fmul
 *  FPdiv        8 (non-pipelined FP divide)                 2                              fdiv
 *  BU           1 (condition and target evaluation)         1                              bne
 *
 *  BHT/BTB:
 *   1-bit dynamic branch predictor (initialized to predict not taken) with 16-entry branch target buffer (BTB) is used.
 *   It hashes the address of a branch, L, to an entry in the BTB using bits 7-4 of L.
 *
 *  Decode Unit:
 *  The decode unit decodes (in a separate cycle) the instructions fetched by the fetch unit
 *  and stores the decoded instructions in an instruction queue which can hold up to NI instructions.
 *  For simplicity, we assume that NI has unlimited entries.
 *  That is, your instruction window size is unlimited and holds all the instructions fetched.
 *
 *  ROB:
 *  A circular reorder buffer (ROB) with NR=16 entries is used with NB=4 Common Data Busses (CDB) connecting the WB stage
 *  and the ROB to the reservation stations and the register file.
 *  You have to design the policy to resolve contention between the ROB and the WB stage on the CDB busses.
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
public class Simulator {
    public static void main(String[] args){
    }
    /**
     * Input : input file prog.dat containing a RISC V assembly language program (code segment).
     * Each line in the input file is a RISC V instruction from the aforementioned nine instructions.
     */
    public static void ReadProgram(File input){

    }

    // Pass Test
    public static class BranchPrediction_DFA{
        // One bit dynamic branch predictor
        // 0- Not take  (initialized to predict not taken)
        // 1 -take
        private int predictor;
        public BranchPrediction_DFA(){
            predictor = 0;
        }
        public int predict(){
            return predictor;
        }
        public void change_state(int result){
            if(result != predictor){
                predictor = 1-predictor;
            }
        }

    }
}
