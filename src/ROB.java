/**
 * ROB:
 * A circular reorder buffer (ROB) with NR=16 entries is used with NB=4 Common Data Busses (CDB)
 * connecting the WB stage and the ROB to the reservation stations and the register file.
 * You have to design the policy to resolve contention between the ROB and the WB stage on the CDB busses.
 */
import java.util.*;

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
    public static String[][] instructions;
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
        String ops = instruction[0];
        first = false;
        busy[tail] = true;
        for(int i = 0;i<4;i++) {
            instructions[tail][i] = instruction[i];
        }
        state[tail] = 'i';
        if(DecodeUnit.Write_Ops.contains(ops)) {
            dest[tail] = Integer.parseInt(instruction[1].substring(1));
        }
        tail++;
        tail = tail%NR;
    }

    /**
     * Return:
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

    /* Allow Other Parts to Change State */
    public static void SetState(int ROBnumber,char s){
        state[ROBnumber] = s;
    }

    /**
     * For each instruction, there is three types of commitment
     * 1. Normal Commit : add, addi, fadd, fsub, fmul, fdiv fld
     *      - Update the Register
     * 2. Store commit : fsd
     *      - Update the Memory
     * 3. Branch Prediction (bne)
     *      - Flush everything
     *
     * For each physical register, add to freelist if no architecture register points to it
     */
    public final static Set<String> Normal_Ops = Collections.unmodifiableSet(Set.of("add","addi","fld","fadd","fsub","fmul","fdiv"));

    public boolean Commit(){
        if(!busy[head]){
            return false;
        }
        for(int i = 0;i<NR;i++){
            if(busy[head] && state[head] == 'w'){
                System.out.println("Commit " +head);
                String ops = instructions[head][0];
                if(Normal_Ops.contains(ops)){
                    RegisterFile.update(dest[head],head,dest_value[head]);
                    CDB.remove(head);
                    busy[head] = false;
                    state[head] = 'c';
                    head ++;
                    head = head%NR;
                    for(String component : instructions[head]){
                        if(RegisterFile.pregister_counter.containsKey(component)){
                            int counter = RegisterFile.pregister_counter.get(component)-1;
                            if(counter == 0){
                                //Release the physical register to free list
                                RegisterFile.freeList.add(Integer.parseInt(component.substring(1)));
                            }else{
                                RegisterFile.pregister_counter.put(component,counter);
                            }

                        }
                    }
                }else if(ops.equals("fsd")){
                    // Store commit
                    int address = store_ROB.get(head).address;
                    double value = store_ROB.get(head).value;
                    MemoryUnit.store(address,value);
                    store_ROB.remove(address);
                    busy[head] = false;
                    state[head] = 'c';
                    head ++;
                    head = head%NR;
                    for(String component : instructions[head]){
                        if(RegisterFile.pregister_counter.containsKey(component)){
                            int counter = RegisterFile.pregister_counter.get(component)-1;
                            if(counter == 0){
                                //Release the physical register to free list
                                RegisterFile.freeList.add(Integer.parseInt(component.substring(1)));
                            }else{
                                RegisterFile.pregister_counter.put(component,counter);
                            }

                        }
                    }
                }
                else{
                    // Branch prediction.
                    /*0 not take 1 take*/
                    int take = InstructionUnit.decision.removeFirst();
                    int recoverypc = InstructionUnit.OtherPC.removeFirst();
                    TreeSet<Integer> recoveryfreelist = InstructionUnit.freeLists.removeFirst();
                    HashMap<String,String> recoverymap = InstructionUnit.maptables.removeFirst();
                    int predict = (int) dest_value[head];
                    if(take == predict){
                        // success predict
                        busy[head] = false;
                        state[head] = 'c';
                        head++;
                        head = head%NR;
                        for(String component : instructions[head]){
                            if(RegisterFile.pregister_counter.containsKey(component)){
                                int counter = RegisterFile.pregister_counter.get(component)-1;
                                if(counter == 0){
                                    //Release the physical register to free list
                                    RegisterFile.freeList.add(Integer.parseInt(component.substring(1)));
                                }else{
                                    RegisterFile.pregister_counter.put(component,counter);
                                }

                            }
                        }
                    }else{
                        // Unsuccessfully predict
                        BranchPredictor.change_state();
                        InstructionUnit.pc = recoverypc;
                        for(int m =0;m<NR;m++){
                            if(busy[m]){
                                store_ROB.remove(m);
                                for(int k= 0;k<RegisterFile.R;k++){
                                    if(RegisterFile.RegisterStatus[k] == m){
                                        RegisterFile.RegisterStatus[k] = -1;
                                    }
                                }
                                for(String component : instructions[m]){
                                    if(RegisterFile.pregister_counter.containsKey(component)){
                                        int counter = RegisterFile.pregister_counter.get(component)-1;
                                        if(counter == 0){
                                            //Release the physical register to free list
                                            RegisterFile.freeList.add(Integer.parseInt(component.substring(1)));
                                        }else{
                                            RegisterFile.pregister_counter.put(component,counter);
                                        }

                                    }
                                }
                            }
                        }
                        /*Flush Everything in ROB*/
                        first = true;
                        busy = new boolean[NR];
                        state = new char[NR];
                        instructions = new String[NR][4];
                        dest = new int[NR];
                        dest_value = new double[NR];
                        head++;
                        head = head%NR;
                        tail = head;
                        /* CDB*/
                        CDB.cdb = new HashMap<>();
                        /* Register File */
                        RegisterFile.RegisterStatus = new int[RegisterFile.R];
                        for(int r = 0;r<RegisterFile.R;r++){
                            RegisterFile.RegisterStatus[r] = -1;
                        }
                        /* Reservation Station */
                        ReservationStation.flush();
                        /*Register Renaming*/
                        RegisterFile.maptable = recoverymap;
                        RegisterFile.freeList = recoveryfreelist;
                    }
                }
            }else{
                //System.out.print(busy[head]+" ");
                //System.out.print(state[head]+" ");
                //for(int l = 0;l<4;l++){
                  //  System.out.print(instructions[head][l]+" ");
                //}
                //System.out.println();
                return true;
            }
        }
        return true;
    }

    public void display(){
        for(int i = 0;i<NR;i++){
            if(busy[(head+i)%NR]){
                System.out.print((head +i)%NR + " ");
                for(int k=0;k<4;k++) {
                    System.out.print(instructions[(head +i)%NR][k]+" ");
                }
                System.out.println(state[(head +i)%NR]);
            }
        }
    }

}
