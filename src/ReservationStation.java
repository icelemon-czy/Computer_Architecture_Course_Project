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

    public class INT{
        final int latency = 1;
        final int number = 4;
        boolean[] busy;
        boolean one_execute; // true if one of the int RS is currently execute
        int current_execute;
        int[] waiting_cycle;
        int current_assign;

        String[] ops;

        // mode : 0: (Vj) register current hold the value,(Qj) 1: register value waiting in ROB 2: immediate Value
        int[] modej;
        int[] modek;

        // source operands (data values) - Vj and Vk
        double[] Vj;// Example : Reg p3
        double[] Vk;

        // the reservation stations producing the source operands (if stall to avoid RAW hazards) - Qj and Qk
        int[] Qj; // Example : ROB3
        int[] Qk;

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
            immediate  = new double[number];
            dest = new int[number];
            dest_value = new double[number];
            waiting_cycle = new int[number];
            for(int i = 0;i<number;i++){
                waiting_cycle[i] = latency;
            }
            one_execute = false;
            current_execute = 0;
            current_assign = 0;
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
                current_execute = 0;
                busy[0] = true;
                one_execute =true;
            }
            else{
                current_assign = (current_assign +1)%number;
                busy[current_assign] = true;
            }

            /*Set up*/
            ops[current_assign] = addinstruction[0];
            // QJ QK VJ VK
            // Check Register status
            // if  Pi == -1 then register hold the value  vj/vk = Reg[]
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
                modek[current_assign] = 2;
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
            dest[current_assign] = ROBnumber;
            ROB.SetState(dest[current_execute], 'i');
            int dest_register =  Integer.parseInt(addinstruction[1].substring(1));
            RegisterFile.SetRegisterStatus(dest_register,ROBnumber);
        }

        /**
         *  Execute (current_execute) instruction
         *  When both operands ready then execute;
         *  if not ready, watch CDB for result;
         *  when both in reservation station, execute;
         */
        public void execute(){
            if(one_execute) {
                /* Change The State of the Instruction In ROB*/
                ROB.SetState(dest[current_execute], 'e');

                // Check if we are stalled
                /* If we are stalled, then watch CDB */
                /* the other reservation stations are still producing the source operands*/
                if(modej[current_execute] == 1){
                    // Watch CDB
                    if(CDB.hasValue(Qj[current_execute])){
                        // CDB has the Value therefore we get value from CDB
                        modej[current_execute] = 0;
                        Vj[current_execute] = CDB.get(Qj[current_execute]);
                    }else{
                        return;
                    }
                }
                if(ops[current_execute].equals("add")){
                    if(modek[current_execute] == 1){
                        // Watch CDB
                        if(CDB.hasValue(Qk[current_execute])){
                            // CDB has the Value therefore we get value from CDB
                            modek[current_execute] = 0;
                            Vk[current_execute] = CDB.get(Qk[current_execute]);
                        }else {
                            return;
                        }
                    }
                }
                /* Then start to execute */
                if(waiting_cycle[current_execute]>0) {
                    waiting_cycle[current_execute]--;
                }
            }
        }

        /**
         * Write on Common Data Bus to all awaiting FUs and reorder buffer
         * Mark reservation station available.
         */
        public void writeback(){
            if(one_execute) {
                if (waiting_cycle[current_execute] == 0 && !CDB.isfull()) {
                    // Finish Execution
                    if (ops[current_execute].equals("add")) {
                        dest_value[current_execute] = Vj[current_execute] + Vk[current_execute];
                    } else {
                        dest_value[current_execute] = Vj[current_execute] + immediate[current_execute];
                    }

                    /* Send Value with ROB number to CDB */
                    int rob = dest[current_execute];

                    CDB.set(rob, dest_value[current_execute]);

                    /* Also Send to ROB */
                    ROB.SetState(rob,'w');
                    ROB.dest_value[rob] = dest_value[current_execute];

                    /* Mark the Reservation Station to unbusy*/
                    waiting_cycle[current_execute] = latency;
                    busy[current_execute] = false;
                    current_execute++;
                    current_execute = current_execute % number;

                    /* Update Execution */
                    if (busy[current_execute]) {
                        one_execute = true;
                    } else {
                        one_execute = false;
                    }
                }
            }
        }
    }

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
            current_assign = 0;
        }

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

        public void issue(String[] addinstruction,int ROBnumber){
            if(!one_execute){
                current_assign = 0;
                current_execute = 0;
                busy[0] = true;
                one_execute =true;
            }
            else{
                current_assign = (current_assign +1)%number;
                busy[current_assign] = true;
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
            ROB.SetState(dest[current_execute], 'i');
            int dest_register =  Integer.parseInt(addinstruction[1].substring(1));
            RegisterFile.RegisterStatus[dest_register] = ROBnumber;
        }

        public void execute(){
            if(one_execute) {
                ROB.SetState(dest[current_execute], 'e');

                if(modej[current_execute] == 1){
                    if(CDB.hasValue(Qj[current_execute])){
                        modej[current_execute] = 0;
                        Vj[current_execute] = CDB.get(Qj[current_execute]);
                    }else{
                        return;
                    }
                }

                if(modek[current_execute] == 1){
                    if(CDB.hasValue(Qk[current_execute])){
                        modek[current_execute] = 0;
                        Vk[current_execute] = CDB.get(Qk[current_execute]);
                    }else{
                        return;
                    }
                }
                /* Then start to execute */
                if(waiting_cycle[current_execute]>0) {
                    waiting_cycle[current_execute]--;
                }
            }
        }

        public void writeback(){
            if(one_execute) {
                if (waiting_cycle[current_execute] == 0 && !CDB.isfull()) {
                    // Finish Execution
                    if (ops[current_execute].equals("fadd")) {
                        dest_value[current_execute] = Vj[current_execute] + Vk[current_execute];
                    } else {
                        dest_value[current_execute] = Vj[current_execute] - Vk[current_execute];
                    }
                    int rob = dest[current_execute];
                    CDB.set(rob, dest_value[current_execute]);
                    /* Also Send to ROB */
                    ROB.SetState(rob,'w');
                    ROB.dest_value[rob] = dest_value[current_execute];

                    waiting_cycle[current_execute] = latency;
                    busy[current_execute] = false;
                    current_execute++;
                    current_execute = current_execute % number;

                    if (busy[current_execute]) {
                        one_execute = true;
                    } else {
                        one_execute = false;
                    }

                }
            }
        }
    }

    public class FPmult{
        int latency  = 4;
        int number = 4;

        boolean[] busy;
        boolean one_execute; // true if one of the int RS is currently execute
        int current_execute;
        int[] waiting_cycle;
        int current_assign;

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

        public FPmult(){
            busy = new boolean[number];
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
            current_assign = 0;
        }

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

        public void issue(String[] addinstruction,int ROBnumber){
            if(!one_execute){
                current_assign = 0;
                current_execute = 0;
                busy[0] = true;
                one_execute =true;
            }
            else{
                current_assign = (current_assign +1)%number;
                busy[current_assign] = true;
            }

            /*Set up*/
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
            ROB.SetState(dest[current_execute], 'i');
            int dest_register =  Integer.parseInt(addinstruction[1].substring(1));
            RegisterFile.RegisterStatus[dest_register] = ROBnumber;
        }

        public void execute(){
            if(one_execute) {
                ROB.SetState(dest[current_execute], 'e');

                if(modej[current_execute] == 1){
                    if(CDB.hasValue(Qj[current_execute])){
                        modej[current_execute] = 0;
                        Vj[current_execute] = CDB.get(Qj[current_execute]);
                    }else{
                        return;
                    }
                }

                if(modek[current_execute] == 1){
                    if(CDB.hasValue(Qk[current_execute])){
                        modek[current_execute] = 0;
                        Vk[current_execute] = CDB.get(Qk[current_execute]);
                    }else{
                        return;
                    }
                }
                /* Then start to execute */
                if(waiting_cycle[current_execute]>0) {
                    waiting_cycle[current_execute]--;
                }
            }
        }

        public void writeback(){
            if(one_execute) {
                if (waiting_cycle[current_execute] == 0 && !CDB.isfull()) {
                    // Finish Execution
                    dest_value[current_execute] = Vj[current_execute] * Vk[current_execute];

                    int rob = dest[current_execute];
                    CDB.set(rob, dest_value[current_execute]);
                    /* Also Send to ROB */
                    ROB.SetState(rob,'w');
                    ROB.dest_value[rob] = dest_value[current_execute];

                    waiting_cycle[current_execute] = latency;
                    busy[current_execute] = false;
                    current_execute++;
                    current_execute = current_execute % number;

                    if (busy[current_execute]) {
                        one_execute = true;
                    } else {
                        one_execute = false;
                    }
                }
            }
        }
    }

    public class FPdiv {
        int latency = 8;
        int number = 2;
        boolean[] busy;
        boolean one_execute; // true if one of the int RS is currently execute
        int current_execute;
        int[] waiting_cycle;
        int current_assign;

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

        public FPdiv(){
            busy = new boolean[number];
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
            current_assign = 0;
        }

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

        public void issue(String[] addinstruction,int ROBnumber){
            if(!one_execute){
                current_assign = 0;
                current_execute = 0;
                busy[0] = true;
                one_execute =true;
            }
            else{
                current_assign = (current_assign +1)%number;
                busy[current_assign] = true;
            }

            /*Set up*/
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
            ROB.SetState(dest[current_execute], 'i');
            int dest_register =  Integer.parseInt(addinstruction[1].substring(1));
            RegisterFile.RegisterStatus[dest_register] = ROBnumber;
        }

        public void execute(){
            if(one_execute) {
                ROB.SetState(dest[current_execute], 'e');
                if(modej[current_execute] == 1){
                    if(CDB.hasValue(Qj[current_execute])){
                        modej[current_execute] = 0;
                        Vj[current_execute] = CDB.get(Qj[current_execute]);
                    }else{
                        return;
                    }
                }
                if(modek[current_execute] == 1){
                    if(CDB.hasValue(Qk[current_execute])){
                        modek[current_execute] = 0;
                        Vk[current_execute] = CDB.get(Qk[current_execute]);
                    }else{
                        return;
                    }
                }

                /* Then start to execute */
                if(waiting_cycle[current_execute]>0) {
                    waiting_cycle[current_execute]--;
                }
            }
        }

        public void writeback(){
            if(one_execute) {
                if(waiting_cycle[current_execute] ==0 && !CDB.isfull()){
                    // Finish Execution
                    dest_value[current_execute] = Vj[current_execute] / Vk[current_execute];

                    int rob =  dest[current_execute];
                    CDB.set(rob,dest_value[current_execute]);
                    /* Also Send to ROB */
                    ROB.SetState(rob,'w');
                    ROB.dest_value[rob] = dest_value[current_execute];
                    waiting_cycle[current_execute] = latency;
                    busy[current_execute] = false;
                    current_execute ++;
                    current_execute = current_execute%number;

                    if(busy[current_execute]){
                        one_execute = true;
                    }else{
                        one_execute = false;
                    }
                }
            }
        }
    }

    public class LoadStoreBuffer{
        int latency = 1;// one cycle for address calculation one cycle for memory access
        int number = 2;
        int number_load;
        int number_store;

        boolean[] busy;
        boolean one_execute; // true if one of the int RS is currently execute
        int current_execute;
        int[] waiting_cycle;
        int current_assign;

        String[] ops;

        // mode : 0: (Vj) register current hold the value,(Qj) 1: register value waiting in ROB
        int[] modej;
        int[] modek;

        // source operands (data values) - Vj and Vk
        double[] Vj;// Example : Reg p3
        double[] Vk;

        // the reservation stations producing the source operands (if stall to avoid RAW hazards) - Qj and Qk
        int[] Qj; // Example : ROB3
        int[] Qk;

        // Store the immediate value
        int[] immediate ;

        //Destination example: ROB4
        int[] dest;
        double[] dest_value;

        public LoadStoreBuffer(){
            number_load = 0;
            number_store = 0;
            busy = new boolean[number];
            ops = new String[number];
            modej = new int[number];
            modek = new int[number];
            Vj = new double[number];
            Vk = new double[number];
            Qj = new int[number];
            Qk = new int[number];
            immediate  = new int[number];
            dest = new int[number];
            dest_value = new double[number];
            waiting_cycle = new int[number];
            for(int i = 0;i<number;i++){
                waiting_cycle[i] = latency;
            }
            one_execute = false;
            current_execute = 0;
            current_assign = 0;
        }

        public boolean can_issue(String[] instruction){
            if(instruction[0].equals("fld")){
                return number_load<number;
            }else{
                return number_store<number;
            }
        }

        public void issue(String[] addinstruction,int ROBnumber){
            if(!one_execute){
                current_assign = 0;
                current_execute = 0;
                busy[0] = true;
                one_execute =true;
            }
            else{
                current_assign = (current_assign +1)%number;
                busy[current_assign] = true;
            }

            /*Set up*/
            ops[current_assign] = addinstruction[0];
            /* Immediate Value */
            immediate[current_assign] = Integer.parseInt(addinstruction[2]);
            if(ops[current_assign].equals("fld")){
                // fld F2, 200(R0)
                int j =  Integer.parseInt(addinstruction[3].substring(1)); // Physical register index
                if(RegisterFile.RegisterStatus[j]==-1){
                    Vj[current_assign] = RegisterFile.register_value[j];
                    modej[current_assign]=0;
                }else{
                    Qj[current_assign] = RegisterFile.RegisterStatus[j]; //Find ROB value later store the ROB number
                    modej[current_assign]=1;
                }
            }else{
                // fsd F0, 0(R2)
                int j =  Integer.parseInt(addinstruction[1].substring(1)); // Physical register index
                if(RegisterFile.RegisterStatus[j]==-1){
                    Vj[current_assign] = RegisterFile.register_value[j];
                    modej[current_assign]=0;
                }else{
                    Qj[current_assign] = RegisterFile.RegisterStatus[j]; //Find ROB value later store the ROB number
                    modej[current_assign]=1;
                }
                int k =  Integer.parseInt(addinstruction[3].substring(1));
                if(RegisterFile.RegisterStatus[k]==-1){
                    Vk[current_assign] = RegisterFile.register_value[k];
                    modek[current_assign]=0;
                }else{
                    Qk[current_assign] = RegisterFile.RegisterStatus[k]; //Find ROB value later store the ROB number
                    modek[current_assign]=1;
                }
                ROB.store_ROB.put(ROBnumber,new ROB.StoreTuple());
            }
            dest[current_assign] = ROBnumber;
            ROB.SetState(dest[current_execute], 'i');
            if(ops[current_assign].equals("fld")) {
                int dest_register = Integer.parseInt(addinstruction[1].substring(1));
                RegisterFile.SetRegisterStatus(dest_register, ROBnumber);
            }
        }

        public void execute(){
            if(one_execute) {
                /* Change The State of the Instruction In ROB*/
                ROB.SetState(dest[current_execute], 'e');

                // Check if we are stalled
                /* If we are stalled, then watch CDB */
                /* the other reservation stations are still producing the source operands*/
                if(modej[current_execute] == 1){
                    // Watch CDB
                    if(CDB.hasValue(Qj[current_execute])){
                        // CDB has the Value therefore we get value from CDB
                        modej[current_execute] = 0;
                        Vj[current_execute] = CDB.get(Qj[current_execute]);
                    }else{
                        return;
                    }
                }
                if(ops[current_execute].equals("fsd")){
                    if(modek[current_execute] == 1){
                        // Watch CDB
                        if(CDB.hasValue(Qk[current_execute])){
                            // CDB has the Value therefore we get value from CDB
                            modek[current_execute] = 0;
                            Vk[current_execute] = CDB.get(Qk[current_execute]);
                        }else {
                            return;
                        }
                    }
                }
                /* Then start to execute */
                if(waiting_cycle[current_execute]>0) {
                    waiting_cycle[current_execute]--;
                }
            }
        }

        public void writeback(){
            if(one_execute) {
                if (waiting_cycle[current_execute] == 0 && !CDB.isfull()) {
                    // Finish Execution Memory Access fld R1 100(R2) M(100 + R2)
                    if (ops[current_execute].equals("fld")) {
                        int address = (int) Vj[current_execute] + immediate[current_execute];
                        /* Send Value with ROB number to CDB */
                        int rob = dest[current_execute];
                        if(ROB.canload(rob,address).address == 2){
                            // Wait for unknown address calculation.
                        }else{
                            if(ROB.canload(rob,address).address == 1) {
                                // Load from memory
                                dest_value[current_execute] = MemoryUnit.load(address);
                            }else {
                                // Load from ROB
                                dest_value[current_execute] = ROB.canload(rob, address).value;
                            }
                            CDB.set(rob, dest_value[current_execute]);
                            /* Also Send to ROB */
                            ROB.SetState(rob,'w');
                            ROB.dest_value[rob] = dest_value[current_execute];
                            /* Mark the Reservation Station to unbusy*/
                            waiting_cycle[current_execute] = latency;
                            busy[current_execute] = false;
                            current_execute++;
                            current_execute = current_execute % number;
                            /* Update Execution */
                            if (busy[current_execute]) {
                                one_execute = true;
                            } else {
                                one_execute = false;
                            }
                        }
                    }else {
                        int rob = dest[current_execute];
                        int address = (int) Vk[current_execute] + immediate[current_execute];
                        ROB.store(rob,address,Vj[current_execute]);
                        /* Also Send to ROB */
                        ROB.SetState(rob,'w');
                        /* Mark the Reservation Station to unbusy*/
                        waiting_cycle[current_execute] = latency;
                        busy[current_execute] = false;
                        current_execute++;
                        current_execute = current_execute % number;
                        /* Update Execution */
                        if (busy[current_execute]) {
                            one_execute = true;
                        } else {
                            one_execute = false;
                        }
                    }
                }
            }
        }
    }

    public class BU{
        int latency = 1;
        int number = 1;
        int remaincycle = 1;

        boolean busy;

        // mode : 0: (Vj) register current hold the value,(Qj) 1: register value waiting in ROB 2: immediate Value
        int modej;
        int modek;

        // source operands (data values) - Vj and Vk
        double Vj;// Example : Reg p3
        double Vk;

        // the reservation stations producing the source operands (if stall to avoid RAW hazards) - Qj and Qk
        int Qj; // Example : ROB3
        int Qk;

        // Store the immediate value
        double immediate;

        //Destination example: ROB4
        // If dest value = 1 Then it is equal
        // otherwise (0) not equal
        int dest;
        double dest_value;

        public BU(){
            busy = false;
            modej = 0;
            modek = 0;
            Vj = 0;
            Vk = 0;
            Qj = 0;
            Qk = 0;
            immediate = 0;
            dest = 0;
            dest_value = 0;
        }

        public boolean can_issue(){
            if(busy){
                return false;
            }
            return true;
        }

        public void issue(String[] addinstruction,int ROBnumber){
            int j =  Integer.parseInt(addinstruction[1].substring(1)); // Physical register index
            if(RegisterFile.RegisterStatus[j]==-1){
                Vj = RegisterFile.register_value[j];
                modej=0;
            }
            else{
                Qj= RegisterFile.RegisterStatus[j]; //Find ROB value later store the ROB number
                modej=1;
            }

            if(isImmediate(addinstruction[2])){
                int imm = Integer.parseInt(addinstruction[2].substring(1));
                immediate = imm;
                modek = 2;
            }
            else{
                int k =  Integer.parseInt(addinstruction[2].substring(1));
                if(RegisterFile.RegisterStatus[k]==-1){
                    Vk= RegisterFile.register_value[k];
                    modek=0;
                }else{
                    Qk = RegisterFile.RegisterStatus[k]; //Find ROB value later store the ROB number
                    modek=1;
                }
            }

            dest = ROBnumber;
            ROB.SetState(dest, 'i');
        }

        public void execute(){
            if(busy) {
                ROB.SetState(dest, 'e');
                if (modej == 1) {
                    if (CDB.hasValue(Qj)) {
                        modej = 0;
                        Vj = CDB.get(Qj);
                    } else {
                        return;
                    }
                }
                if (modek == 1) {
                    if (CDB.hasValue(Qk)) {
                        modek = 0;
                        Vk = CDB.get(Qk);
                    } else {
                        return;
                    }
                }
                /* Then start to execute */
                if(remaincycle >0){
                    remaincycle--;
                }
            }
        }

        public void writeback(){
            if(busy) {
                if(remaincycle == 0 && !CDB.isfull()){
                    if(Vj == Vk) {
                        dest_value = 1;
                    }else{
                        dest_value  =0;
                    }
                    int rob = dest;
                    /* Also Send to ROB */
                    ROB.SetState(rob,'w');
                    ROB.dest_value[rob] = dest_value;
                    busy= false;
                    remaincycle = latency;
                }
            }
        }
    }


    public boolean isImmediate(String s){
        return s.charAt(0) == '$';
    }

    INT INTRS;
    LoadStoreBuffer LoadStoreRS;
    FPadd FPaddRS;
    FPmult FPmulRS;
    FPdiv FPdivRS;
    BU BURS;

    public ReservationStation(RegisterFile RF){
        INTRS = new INT();
        LoadStoreRS = new LoadStoreBuffer();
        FPaddRS = new FPadd();
        FPmulRS = new FPmult();
        FPdivRS = new FPdiv();
        BURS = new BU();
    }

    public void EXE(){
        INTRS.execute();
        LoadStoreRS.execute();
        FPaddRS.execute();
        FPmulRS.execute();
        FPdivRS.execute();
        BURS.execute();
    }

    public void WB(){
        INTRS.writeback();
        LoadStoreRS.writeback();
        FPaddRS.writeback();
        FPmulRS.writeback();
        FPdivRS.writeback();
        BURS.writeback();
    }


}
