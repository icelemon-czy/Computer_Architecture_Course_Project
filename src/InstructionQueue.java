import java.util.LinkedList;

/**
 *  Instruction Fetch:
 *  Up to NW=4 instructions can be issued every clock cycle to reservation stations.
 */
public class InstructionQueue {
    int NW;
    public static LinkedList<String[]> ready_instructions;
    ROB rob;

    public InstructionQueue(int NW,ROB rob){
        this.NW = NW;
        this.rob = rob;
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
        for(int i = 0;i<NW && ready_instructions.size()>0;i++){
            String[] ready = ready_instructions.removeFirst();
            /**
            for(String s : ready){
                System.out.print(s+" ");
            }
            System.out.println();
             **/
            if(ready[0].equals("add") || ready[0].equals("addi")){
                if( ReservationStation.INTRS.can_issue() ){
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        // Cannot issue the instruction
                        return;
                    }
                    ReservationStation.INTRS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }else if(ready[0].equals("fld") || ready[0].equals("fsd")) {
                if(ReservationStation.LoadStoreRS.can_issue(ready)) {
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        return;
                    }
                    ReservationStation.LoadStoreRS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }else if(ready[0].equals("fadd") || ready[0].equals("fsub")) {
                if(ReservationStation.FPaddRS.can_issue()) {
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        return;
                    }
                    ReservationStation.FPaddRS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }else if(ready[0].equals("fmul")) {
                if(ReservationStation.FPmulRS.can_issue()) {
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        return;
                    }
                    ReservationStation.FPmulRS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }else if(ready[0].equals("fdiv")) {
                if(ReservationStation.FPdivRS.can_issue()) {
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        return;
                    }
                    ReservationStation.FPdivRS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }else{
                if(ReservationStation.BURS.can_issue()){
                    int ROB_head = rob.can_issue();
                    if(ROB_head == -1){
                        return;
                    }
                    ReservationStation.BURS.issue(ready,ROB_head);
                    rob.issue(ready);
                }
            }
        }
    }
}
