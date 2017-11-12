import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CentralizedServer {

    private static ServerSocket welcomeSocket;
    private static final int PORT = 1234;

    public static void main(String[] args) throws IOException
    {
        try
        {
            /* This is the main socket that listens for incoming connections.
             * Once a "control connection" is established between a client,
             * a thread is spun off to handle all further communication with
             * that client. This socket only handles the first incoming 
             * connection request. */
            welcomeSocket = new ServerSocket(PORT);
        }
        catch (IOException ioEx)
        {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

        do
        {
            Socket client = welcomeSocket.accept();

            System.out.println("\nNew client accepted.\n");

            /* spin off new thread to handle all future communication with client */
            ClientHandler handler = new ClientHandler(client);

            handler.start();
        } while (true);  //continue listening; program must be ended by a Ctrl-C
    }
}

class ClientHandler extends Thread
{
    private Socket client;
    private Scanner input;
    private PrintWriter output;
    private int dataConnPort;
    private String command;
    private String received;

    public ClientHandler(Socket socket)
    {
        client = socket;

        try
        {
            /* get input/output streams from the client socket 
             * These represent the i/o for the persistent command connection */
            input = new Scanner(client.getInputStream());
            output = new PrintWriter(client.getOutputStream(), true);
        }
        catch(IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    public void run()
    {
        /* loop until client sends QUIT command */
        /* all commands must be in the form COMMAND PARAMETERS */
        do
        {
            /* Get the next command */
            received = input.nextLine();  //this line blocks until message is received
            StringTokenizer tokens = new StringTokenizer(received);
            command = tokens.nextToken();
            
            if(command.equals("REGISTER"))
            {
                /* Initialize a new user into the system
                 * Command should be in the form REGISTER username hostname connectionSpeed */
                
                /* parse the username, connection speed, and hostname from incoming message */
                String username = tokens.nextToken();
                String hostname = tokens.nextToken();
                String connectionSpeed = tokens.nextToken();
                
                /* account for connection speeds with spaces?
                 * TODO: remove if unnecessary
                 */
                while (tokens.hasMoreTokens())
                {
                    connectionSpeed += " " + tokens.nextToken();
                }

                try
                {
                    TextDatabase.insertRowIntoUsers(username, hostname, connectionSpeed);
                }
                catch (Exception e)
                {
                    /* send error message if preexisting user; continue*/
                    System.out.println(e.getMessage());
                    output.println(e.getMessage());
                    output.flush();
                    continue;
                }

                
                /* Send confirmation message */ 
                output.println("Username accepted. Initiate transfer of filelist.");
                
                /* Wait for STOR command (taken from previous project)
                 * Will be in the form STOR portNum.
                 * Since both ArrayList and String are serializable,
                 *  we can send the object itself over the connection,
                 *  rather than a file.
                 */

                received = input.nextLine();
                tokens = new StringTokenizer(received);
                tokens.nextToken(); //skip over the "STOR"

                /* Get data port */
                try {
                    dataConnPort = Integer.parseInt(tokens.nextToken());
                } catch (NumberFormatException e1) {
                    System.out.println("Invalid port number. Aborting user registration.");
                    output.println("Invalid port number. Aborting user registration.");
                    TextDatabase.deleteUserCascade(username);
                    continue;
                }

                try {
                    /* establish data connection */
                    Socket dataSocket = new Socket(client.getInetAddress(), dataConnPort);

                    ObjectInputStream inputStream = new ObjectInputStream(dataSocket.getInputStream());
                    
                    ArrayList<String> fileList = (ArrayList<String>) inputStream.readObject();

                    /* Store user file information in file database */
                    for(String file: fileList)
                    {
                        TextDatabase.insertRowIntoFiles(username, file);
                    }
                    
                    dataSocket.close();
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                    output.println(e.getMessage());
                    TextDatabase.deleteUserCascade(username);
                }

                System.out.println("User registration successful");
                output.println("User registration successful");
            }
            
            if(command.equals("UNREGISTER"))
            {
                /* Command will be of the form UNREGISTER username */
                /* Note: currently, any user can UNREGISTER another user 
                 *  as long as they know the username.
                 *  We'd need to force a login
                 *  or pair IP addresses with usernames
                 *  to keep this safe from malicious users */

                String username = tokens.nextToken();
                
                TextDatabase.deleteUserCascade(username);
                
                output.println("Operation complete.");
                System.out.println("Operation complete.");
            }
    
            if(command.equals("SEARCH"))
            {
                /* Command will be of the form SEARCH portNum keyword */
                int dataConnPort = Integer.parseInt(tokens.nextToken());
                String keyword = tokens.nextToken();
                
                /* DEBUG */
                System.out.println("Search for " + keyword + " just initiated");
                
                ArrayList<String> results = TextDatabase.searchByKeyword(keyword);
                
                try {
                    // get our data connection going
                    Socket dataSocket = new Socket(client.getInetAddress(), dataConnPort);
                    
                    ObjectOutputStream outputStream = new ObjectOutputStream(dataSocket.getOutputStream());
                    
                    outputStream.writeObject(results);

                    dataSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } while (!command.equals("QUIT"));
        
        /* client has sent command QUIT */
        try
        {
            if (client!=null)
            {
                System.out.println("Closing down connection...");
                client.close();
            }
        }
        catch(IOException ioEx)
        {
            System.out.println("Unable to disconnect!");
        }
    }
    
}