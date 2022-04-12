import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

public class Main {
    public final static Set<String> Write_Ops = Collections.unmodifiableSet(Set.of("add","addi","fld","fadd","fsub","fmul","fdiv","bne"));
    public static void main(String[] args) throws FileNotFoundException, IOException {
	// write your code here
        /**
        FileInputStream fis = new FileInputStream("resources/config.properties");
        Properties properties = new Properties();
        properties.load(fis);
        String userName = properties.getProperty("username");
        String number = properties.getProperty("money");
        System.out.println(Integer.parseInt(number));
         **/
        /**
        String instruction = "bne R1,$0, loop";
        String target = instruction.split(",")[2].trim();
        System.out.println(target);
         **/
        System.out.println(PhysicalRegisterGenerator(132));

    }
    public static String PhysicalRegisterGenerator(int i ){
        return "p" + i;
    }
}
