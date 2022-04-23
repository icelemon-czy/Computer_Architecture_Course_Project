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
    public int head;// Head is also a Pointer points to the next commit instruction.
    public static int tail;
    public boolean first;

    /* ROB status Table*/
    public boolean[] busy;
    public String[][] instructions;
    public static char[] state;/* C:Commit, E:Execute, W: Write Results I:Issue*/
    public int[] dest; // dest: physical Register
    public double[] dest_value;

    public ROB(){
        first = true;
        head = 0;
        tail = 0;
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
            return tail;
        }
        if(head == tail){
            return -1;
        }
        return tail;
    }

    public void issue(String[] instruction) {
        busy[tail] = true;
        for(int i = 0;i<4;i++) {
            instructions[tail][i] = instruction[i];
        }
        state[tail] = 'i';
        dest[tail] = Integer.parseInt(instruction[1].substring(1));
        tail++;
        tail = tail%NR;
    }

    /**
     * Allow Other Parts to Change State
     */
    public static void SetState(int ROBnumber,char s){
        state[ROBnumber] = s;
    }


    /**
     * For each instruction, there is three types of commitment
     * 1. Normal Commit
     *      - Update the Register
     * 2. Store commit
     * 3. Branch Prediction
     */
    public void Commit(){

    }

}
