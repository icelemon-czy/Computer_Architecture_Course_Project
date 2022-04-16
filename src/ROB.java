/**
 * ROB:
 * A circular reorder buffer (ROB) with NR=16 entries is used with NB=4 Common Data Busses (CDB)
 * connecting the WB stage and the ROB to the reservation stations and the register file.
 * You have to design the policy to resolve contention between the ROB and the WB stage on the CDB busses.
 */
public class ROB {
    /**      Busy   Instruction[]   State   Dest    Value
     * ROB0   no    fld F6，34（R2） Commit    F6      xxx
     * ROB1   yes
     *
     * ROB_(NR-1)
     * */
    public final int NR = 16;
    public int head,tail;// Head is also a Pointer points to the next commit instruction.
    public boolean first;

    /* ROB status Table*/
    public boolean[] busy;
    public String[][] instructions;
    public char[] state;/* C:Commit, E:Execute, W: Write Results I:Issue*/
    public int[] dest; // dest:
    public double[] dest_value;

    public ROB(){
        first = true;
        head = 0;
        tail = 0; // point to the first unavailable instruction
        busy = new boolean[NR];
        state = new char[NR];
        instructions = new String[NR][4];
        dest = new int[NR];
        dest_value = new double[NR];
    }
    /* head ****** tail*/  /* tail ***  head*/
    public int can_issue(){
        if(first){
            first = false;
            return head;
        }
        if(head == tail){
            return -1;
        }
        return head;
    }

    public void issue(String[] instruction) {
        busy[head] = true;
        for(int i = 0;i<4;i++) {
            instructions[head][i] = instruction[i];
        }
        state[head] = 'i';
        dest[head] = Integer.parseInt(instruction[1].substring(1));
        head++;
        head = head%NR;
    }

    // TODO API pass value
    public void WB_passvalue(){

    }

}
