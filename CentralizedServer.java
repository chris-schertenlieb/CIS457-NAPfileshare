import java.util.ArrayList;


public class CentralizedServer {

    public static void main(String[] args) {
        
        ArrayList<String> result = TextDatabase.getAllUsernames();
        
        for (String username : result)
        {
            System.out.println(username);
        }
        

    }
}
