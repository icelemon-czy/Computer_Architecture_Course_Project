import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

public class Main {

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
        LinkedList<Integer> ints = new LinkedList<>();
        for(int i = 0;i<10;i++){
            ints.add(i);
        }
        System.out.println(ints.removeFirst());
        System.out.println(ints.removeFirst());
        System.out.println(ints.removeFirst());

    }
}
