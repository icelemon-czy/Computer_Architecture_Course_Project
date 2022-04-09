import java.util.LinkedList;

/**
 *  Instruction Fetch:
 *  Up to NW=4 instructions can be issued every clock cycle to reservation stations.
 */
public class InstructionQueue {
    LinkedList<String[]> ready_instructions;

    public void add(String[] ready_instruction){
        ready_instructions.add(ready_instruction);
    }
}
