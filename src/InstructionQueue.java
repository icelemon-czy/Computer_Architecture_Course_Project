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

    public void dispatch(){
        for(int i = 0;i<NW;i++){
            String[] ready = ready_instructions.removeFirst();
            for(String s : ready){
                System.out.print(s+" ");
            }
            System.out.println();
        }
    }
    public void Dispatch(String[] instruction){

    }
}
