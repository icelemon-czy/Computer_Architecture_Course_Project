/**
 *  The architecture has the following functional units with the shown latencies and number of reservation stations.
 *  Unit       |    Latency(cycle) for op           | Reservation Stations          | Instructions executing on the unit
 *  INT          1 (integer and logic operations)            4                              add / addi
 *  Load/Store   1 for address calculation            2 load buffer + 2 store buffer        fld fsd
 *  FPadd        3 (non-pipelined FP add)                    3                              fadd / fsub
 *  FPmult       4 (non-pipelined FP multiply)               4                              fmul
 *  FPdiv        8 (non-pipelined FP divide)                 2                              fdiv
 *  BU           1 (condition and target evaluation)         1                              bne
 */
import java.util.HashMap;
public class ReservationStation {
    // Int -> add addi
    // Example:
    // addi R2, R0, 124
    // add R2, R0,R1
    public class INT{
        final int latency = 1;
        final int number = 4;
        boolean[] busy;
        boolean one_execute; // true if one of the int RS is currently execute
        int current_execute;
        int[] waiting_cycle;
        int current_assign;

        String[] ops;

        // mode : 0: V ,1:in Q, 2:in immediate
        int[] modej;
        int[] modek;

        // source operands (data values) - Vj and Vk
        double[] Vj;// Example : Reg p3
        double[] Vk;

        // the reservation stations producing the source operands (if stall to avoid RAW hazards) - Qj and Qk
        int[] Qj; // Example : ROB3
        int[] Qk;
        //double[] Qj_value;
        //double[] Qk_value;

        // Store the immediate value
        double[] immediate ;

        //Destination example: ROB4
        int[] dest;
        double[] dest_value;

        public INT(){
            busy = new boolean[number];
            ops = new String[number];
            modej = new int[number];
            modek = new int[number];
            Vj = new double[number];
            Vk = new double[number];
            Qj = new int[number];
            Qk = new int[number];
            //Qj_value = new double[number];
            //Qk_value = new double[number];
            immediate  = new double[number];
            dest = new int[number];
            dest_value = new double[number];
            waiting_cycle = new int[number];
            for(int i = 0;i<number;i++){
                waiting_cycle[i] = latency;
            }
            one_execute = false;
            current_execute = 0;
        }

        /**
         * return true if we can issue the instruction
         * return false otherwise
         */
        public boolean can_issue(){
            if(!one_execute){
                return true;
            }
            for(int i = 0;i<number;i++){
                if(!busy[i]){
                    return true;
                }
            }
            return false;
        }

        /**
         * input instruction
         * Issue instruction into the reservation station.
         */
        public void issue(String[] addinstruction,int ROBnumber){
            /**
             * Find the free reservation station
             *  at the next spot of last assigned reservation station
             */
            if(!one_execute){
                current_assign = 0;
                busy[0] = true;
                one_execute =true;
            }else{
                if(busy[0]){
                    for(int m = 1;m<number;m++){
                        if(!busy[m]){
                            current_assign = m;
                            busy[m] = true;
                            break;
                        }
                    }
                }else{
                    if(busy[number]){
                        current_assign = 0;
                        busy[0] = true;
                    }else{
                        for(int m = number-1;m>0;m++){
                            if(busy[m]){
                                current_assign = m+1;
                                busy[m+1] = true;
                                break;
                            }
                        }
                    }
                }
            }
            /*Set up*/
            ops[current_assign] = addinstruction[0];
            // QJ QK VJ VK
            // Check Register status
            // if  Pi == -1 then find value in register vj/vk = Reg[]
            // If not, Qj/Qk  store the ROB index
            int j =  Integer.parseInt(addinstruction[2].substring(1)); // Physical register index
            if(RegisterFile.RegisterStatus[j]==-1){
                Vj[current_assign] = RegisterFile.register_value[j];
                modej[current_assign]=0;
            }else{
                Qj[current_assign] = RegisterFile.RegisterStatus[j]; //Find ROB value later store the ROB number
                modej[current_assign]=1;
            }

            if(addinstruction[0].equals("addi")){
                int imm = Integer.parseInt(addinstruction[3]);
                immediate[current_assign] = imm;
            }else{
                int k =  Integer.parseInt(addinstruction[3].substring(1));
                if(RegisterFile.RegisterStatus[k]==-1){
                    Vk[current_assign] = RegisterFile.register_value[k];
                    modek[current_assign]=0;
                }else{
                    Qk[current_assign] = RegisterFile.RegisterStatus[k]; //Find ROB value later store the ROB number
                    modek[current_assign]=1;
                }
            }
            dest[current_assign]  = ROBnumber;
            int dest_register =  Integer.parseInt(addinstruction[1].substring(1));
            RegisterFile.RegisterStatus[dest_register] = ROBnumber;
        }

        /**
         *  Execute (current_execute) instruction
         *  If the execution finish
         */
        public void execute(){
            if(one_execute) {
                // Check if we are stalled
                /* the other reservation stations are still producing the source operands*/
                if(modej[current_execute] == 1){
                    return;
                }
                if(ops[current_execute].equals("add")){
                    if(modek[current_execute] == 1){
                        return;
                    }
                }
                /* Then start to execute */
                if(waiting_cycle[current_execute]>0) {
                    waiting_cycle[current_execute]--;
                }

                if(waiting_cycle[current_execute] ==0){
                    // Finish Execution
                    if(ops[current_execute].equals("add")){
                        dest_value[current_execute] = Vj[current_execute] + Vk[current_execute];
                    }else{
                        dest_value[current_execute] = Vj[current_execute] + immediate[current_execute];
                    }
                    /* TODO Send Value with ROB number to CDB */
                    // int ROB =  dest[current_execute];

                    /* Mark the Reservation Station to unbusy*/
                    waiting_cycle[current_execute] = latency;
                    busy[current_execute] = false;
                    current_execute ++;
                    current_execute = current_execute%number;
                }
            }
        }
    }

    // FPadd -> fadd / fsub
    // Example:
    // fadd R2, R0, R1
    // fsub R2, R0, R1
    public class FPadd{
        int latency = 3;
        int number = 3;

        boolean[] busy;
        boolean one_execute; // true if one of the int RS is currently execute
        int current_execute;
        int[] waiting_cycle;
        int current_assign;

        String[] ops;

        // mode : 0: V,1:in Q
        int[] modej;
        int[] modek;

        // source operands (data values) - Vj and Vk
        double[] Vj;// Example : Reg p3
        double[] Vk;

        // the reservation stations producing the source operands (if stall to avoid RAW hazards) - Qj and Qk
        int[] Qj; // Example : ROB3
        int[] Qk;

        //Destination example: ROB4
        int[] dest;
        double[] dest_value;

        public FPadd(){
            busy = new boolean[number];
            ops = new String[number];
            modej = new int[number];
            modek = new int[number];
            Vj = new double[number];
            Vk = new double[number];
            Qj = new int[number];
            Qk = new int[number];
            dest = new int[number];
            dest_value = new double[number];
            waiting_cycle = new int[number];
            for(int i = 0;i<number;i++){
                waiting_cycle[i] = latency;
            }
            one_execute = false;
            current_execute = 0;
        }

        /**
         * return true if we can issue the instruction
         * return false otherwise
         */
        public boolean can_issue(){
            if(!one_execute){
                return true;
            }
            for(int i = 0;i<number;i++){
                if(!busy[i]){
                    return true;
                }
            }
            return false;
        }

        /**
         * input instruction
         * Issue instruction into the reservation station.
         */
        public void issue(String[] addinstruction,int ROBnumber){
            /**
             * Find the free reservation station
             *  at the next spot of last assigned reservation station
             */
            if(!one_execute){
                current_assign = 0;
                busy[0] = true;
                one_execute =true;
            }
            else{
                if(busy[0]){
                    for(int m = 1;m<number;m++){
                        if(!busy[m]){
                            current_assign = m;
                            busy[m] = true;
                            break;
                        }
                    }
                }else{
                    if(busy[number]){
                        current_assign = 0;
                        busy[0] = true;
                    }else{
                        for(int m = number-1;m>0;m++){
                            if(busy[m]){
                                current_assign = m+1;
                                busy[m+1] = true;
                                break;
                            }
                        }
                    }
                }
            }

            /*Set up*/
            ops[current_assign] = addinstruction[0];
            // QJ QK VJ VK
            // Check Register status
            // if  Pi == -1 then find value in register vj/vk = Reg[]
            // If not, Qj/Qk  store the ROB index
            int j =  Integer.parseInt(addinstruction[2].substring(1)); // Physical register index
            if(RegisterFile.RegisterStatus[j]==-1){
                Vj[current_assign] = RegisterFile.register_value[j];
                modej[current_assign]=0;
            }else{
                Qj[current_assign] = RegisterFile.RegisterStatus[j]; //Find ROB value later store the ROB number
                modej[current_assign]=1;
            }

            int k =  Integer.parseInt(addinstruction[3].substring(1));
            if(RegisterFile.RegisterStatus[k]==-1) {
                Vk[current_assign] = RegisterFile.register_value[k];
                modek[current_assign] = 0;
            }else {
                Qk[current_assign] = RegisterFile.RegisterStatus[k]; //Find ROB value later store the ROB number
                modek[current_assign] = 1;
            }

            dest[current_assign]  = ROBnumber;
            int dest_register =  Integer.parseInt(addinstruction[1].substring(1));
            RegisterFile.RegisterStatus[dest_register] = ROBnumber;
        }
    }


    public class FPmult{
        int latency  = 4;
        int number = 4;
    }
    public class FPdiv {
        int latency = 8;
        int number = 2;
    }

    public class LoadStore{
        int latency = 1;
        int number = 2;
    }

    public class BU{
        int latency = 1;
        int number = 1;
    }

    INT INTRS;
    LoadStore LoadStoreRS;
    FPadd FPaddRS;
    FPmult FPmulRS;
    FPdiv FPdivRS;
    BU BURS;

    //RegisterFile RF;
    public ReservationStation(RegisterFile RF){
        //this.RF = RF;
        INTRS = new INT();

    }

}
