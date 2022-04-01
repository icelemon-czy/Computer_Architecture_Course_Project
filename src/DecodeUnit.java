import java.util.LinkedList;

/**
 *  Decode Unit:
 *  The decode unit decodes (in a separate cycle) the instructions fetched by the fetch unit
 *  and stores the decoded instructions in an instruction queue which can hold up to NI instructions.
 *  For simplicity, we assume that NI has unlimited entries.
 *  That is, your instruction window size is unlimited and holds all the instructions fetched.
 */
public class DecodeUnit {
    LinkedList<String> undecode_instructions;
    public DecodeUnit(){
        undecode_instructions = new LinkedList<>();
    }
    public void add(String undecode_instrcution){
        undecode_instructions.add(undecode_instrcution);
    }
}
