import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Host {
    private static InetAddress host;
    private static final int PORT = 1236;

//    public static void main(String[] args)
//    {
//        
//    }
    
    public void Host(){
    	
    }
    
    public void setCommand(String command){
    	this.command = command;
    }
    
    public ArrayList<String> getResponse(){
	    Socket server = null;
	    String message, command, response, serverName = "";
	    int serverPort;
	    ArrayList<String> results = new ArrayList();
	    
	    
	    Scanner serverInput = null;
	    PrintWriter serverOutput = null;
	
	    //Set up stream for keyboard entry...
	    //Scanner userEntry = new Scanner(System.in);
	
	    /* currently, I'm enclosing everything in a giant try block
	     * This should be divided into smaller blocks for better error handling */
	    try {
            /* display normal prompt preceeding every user entry */
            //System.out.print("Enter command ('QUIT' to exit): ");
            
            /* get user input and tokenize to get command */
            StringTokenizer tokens = new StringTokenizer(message);
            command = tokens.nextToken();
            
            
            if(command.equals("CONNECT"))
            {
                /* check that no connection already exists with a server */
                if (server != null)
                {
                   results.add("Connection already exists!");
                }
                
                /*check for correct parameters
                 * must be in the form CONNECT servername/IP port */
                serverName = tokens.nextToken();
                serverPort = Integer.parseInt(tokens.nextToken());
                server = new Socket(serverName, serverPort);

                /* get input stream from server to receive response */
                /* get output stream from server to send request */
                serverInput = new Scanner(server.getInputStream());
                serverOutput = new PrintWriter(server.getOutputStream(),true);
                
                results.add("Connection Established!");
            }
            
            if(command.equals("REGISTER"))
            {
                /* Command should be in the form REGISTER username hostname connectionSpeed */
                
                /* check that a connection with a server exists */
                if (server == null)
                {
                    System.out.println("A connection with a server has not been established.");
                    continue;
                }
                
                /* pull values from command line */
                String username = tokens.nextToken();
                String hostname = tokens.nextToken();
                String connectionSpeed = tokens.nextToken();
                
                /* send command to central server */
                serverOutput.println("REGISTER " + username + " " + hostname + " " + connectionSpeed);
                
                /* get input stream to read response to the data socket */
                response = serverInput.nextLine();
                
                results.add(response);
                
                /* TODO: Check for success/failure in response ? */
                
                /* send STOR command in the form STOR portNum */
                serverOutput.println("STOR " + PORT);
                
                //set up data socket with server
                ServerSocket welcomeSocket = new ServerSocket(PORT);
                Socket dataSocket = welcomeSocket.accept();

                
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(dataSocket.getOutputStream());
                    
                    /* get list of all files in current directory */
                    File folder = new File(".");  //the folder for this process
                    File[] listOfFiles = folder.listFiles();  //this object contains all files AND folders in the current directory

                    ArrayList<String> results = new ArrayList<String>();
                    
                    /* Iterate through and add the path to the list only if the file object is indeed a file (not a directory) */
                    for (File file : listOfFiles) {
                        if (file.isFile()) {
                            results.add(file.getAbsolutePath());
                        }
                    }
                    
                    outputStream.writeObject(results);

                    dataSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                /* get success/failure message */
                response = serverInput.nextLine();
                
                results.add(response);
            }
            
            if(command.equals("UNREGISTER"))
            {
                String username = tokens.nextToken();
				
				serverOutput.println("UNREGISTER " + username);
				results.add("Unregistered!");
            }
            
            if(command.equals("SEARCH"))
            {
                String keyword = tokens.nextToken();
				
				serverOutput.println("SEARCH " + PORT + " " + keyword);
				
				ServerSocket welcomeSocket = new ServerSocket(PORT);
				Socket dataSocket = welcomeSocket.accept();
				
				ObjectInputStream input = new ObjectInputStream(dataSocket.getInputStream());
				try{
					ArrayList<String> results = (ArrayList<String>)input.readObject();
					System.out.println(results);
				}catch(Exception e){
					System.out.println("Unable to read search results!");
				}
            }
            
            if(command.equals("QUIT")){
    	        /* tell the server that you are quitting */
    	        serverOutput.println("QUIT");
    	        
    	        /* close connection and resources */
    	        server.close();
    	        userEntry.close();
    	        serverOutput.close();
    	        serverInput.close();
            }
                            

	    } catch (NumberFormatException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (UnknownHostException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    
	    return results;
    
    }
}