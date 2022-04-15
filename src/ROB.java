/**
 * ROB:
 * A circular reorder buffer (ROB) with NR=16 entries is used with NB=4 Common Data Busses (CDB)
 * connecting the WB stage and the ROB to the reservation stations and the register file.
 * You have to design the policy to resolve contention between the ROB and the WB stage on the CDB busses.
 */
public class ROB {
    public final int NR = 16;
    public int head,tail;
    public boolean first;

    public boolean[] busy;
    // Instruction
    public char[] state;
    // Destination
    // Memory

    public ROB(){
        first = false;
        head = 0;
        tail = 1;
        busy = new boolean[NR];
        state = new char[NR];
    }
    public int can_issue(){
        if(head == tail){
            return -1;
        }
        return head;
    }

}
