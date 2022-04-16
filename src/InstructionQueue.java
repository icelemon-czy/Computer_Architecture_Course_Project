import java.util.LinkedList;

/**
 *  Instruction Fetch:
 *  Up to NW=4 instructions can be issued every clock cycle to reservation stations.
 */
public class InstructionQueue {
    int NW;
    LinkedList<String[]> ready_instructions;
    ROB rob;
    ReservationStation reservationStation;

    public InstructionQueue(int NW,ROB rob, ReservationStation reservationStation){
        this.NW = NW;
        this.rob = rob;
        this.reservationStation = reservationStation;
        this.ready_instructions = new LinkedList<>();
    }

    public void add(String[] ready_instruction){
        ready_instructions.add(ready_instruction);
    }

    /**
     * Issue (sometimes called Dispatch)
     * If a RES station and a ROB are free,
     * issue the instruction to the RES station
     * after reading ready registers and renaming non-ready registers
     */
    public void dispatch(){
        for(int i = 0;i<NW;i++){
            String[] ready = ready_instructions.removeFirst();
            /**
            for(String s : ready){
                System.out.print(s+" ");
            }
            System.out.println();
             **/
            if(ready[0].equals("add") || ready[0].equals("addi")){
                if( reservationStation.INTRS.can_issue() ){
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        // Cannot issue the instruction
                        return;
                    }
                    reservationStation.INTRS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }
        }
    }
    public void Dispatch(String[] instruction){

    }
}
