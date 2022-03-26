/**
 * ROB:
 * A circular reorder buffer (ROB) with NR=16 entries is used with NB=4 Common Data Busses (CDB)
 * connecting the WB stage and the ROB to the reservation stations and the register file.
 * You have to design the policy to resolve contention between the ROB and the WB stage on the CDB busses.
 */
public class ROB {
    private final int NR = 16;
    private int head,tail;
    private boolean[] busy;
    // Instruction
    private char[] state;
    // Destination
    // Memory

    public ROB(){
        head = 0;
        tail = 0;
        busy = new boolean[NR];
        state = new char[NR];
    }

}
