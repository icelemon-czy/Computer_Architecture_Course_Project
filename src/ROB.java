/**
 * ROB:
 * A circular reorder buffer (ROB) with NR=16 entries is used with NB=4 Common Data Busses (CDB)
 * connecting the WB stage and the ROB to the reservation stations and the register file.
 * You have to design the policy to resolve contention between the ROB and the WB stage on the CDB busses.
 */
import java.util.HashMap;
public class ROB {
    /**      Busy   Instruction[]   State   Dest    Value
     * ROB0   no    fld F6，34（R2） Commit    F6      xxx
     * ROB1   yes   fsd F6, 34(R2)   xxxx     xx      xxx
     * ROB2   yes   bne r1, $0,LOOP  xxxx     xx      xxx  1 equal, 0 not equal
     * ROB3   yes   add r1,r2,r3     xxxx     R1      xxx
     * ROB_(NR-1)
     * */
    public static class StoreTuple{
        int address;
        double value;
        public StoreTuple(){
            address = -1;
            value = -1;
        }
        public StoreTuple(int address,double value){
            this.address = address;
            this.value = value;
        }
    }

    public static final int NR = 16;
    public static int head;// Head is also a Pointer points to the next commit instruction.
    public static int tail;
    public boolean first;

    /* ROB status Table*/
    public boolean[] busy;
    public String[][] instructions;
    public static char[] state;/* C:Commit, E:Execute, W: Write Results I:Issue*/
    public int[] dest; // dest: physical Register
    public static double[] dest_value;

    /* Store ops ROB-Address-Value*/
    public static HashMap<Integer,StoreTuple> store_ROB = new HashMap<>();
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
            return tail;
        }
        if(head == tail){
            return -1;
        }
        return tail;
    }

    public void issue(String[] instruction) {
        first = false;
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
     * [0,0]: can load from memory
     * [1,value]: can load from ROB
     * [2,0] : can not load
     */
    public static StoreTuple canload(int ROBnumber,int address){
        if(store_ROB.size()==0){
            return new StoreTuple(0,0);
        }
        int unknownaddress = 0;
        /* head - ROBnumber*/
        for(int i = ROBnumber;i!=head;){
            if(store_ROB.containsKey(i)){
                if(store_ROB.get(i).address == -1){
                    unknownaddress ++;
                }else if(store_ROB.get(i).address == address){
                        return new StoreTuple(1,store_ROB.get(i).value);
                }else{
                    // By pass
                }
            }
            i = i-1;
            if(i<0){
                i = NR-1;
            }
            i = i%NR;
        }
        if(unknownaddress>0){
            return new StoreTuple(2,0);
        }else{
            return new StoreTuple(0,0);
        }
    }

    public static void store(int ROBnumber,int address,double value){
        store_ROB.put(ROBnumber,new StoreTuple(address,value));
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
