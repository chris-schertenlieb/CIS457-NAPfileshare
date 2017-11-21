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

    public static void main(String[] args)
    {
        Socket server = null;
        String message, command, response, targetUser = "";
        String serverName = "";
        int serverPort;

        Scanner serverInput = null;
        PrintWriter serverOutput = null;
        ArrayList<String> latestSearch = new ArrayList();

        //Set up stream for keyboard entry...
        Scanner userEntry = new Scanner(System.in);

        /* currently, I'm enclosing everything in a giant try block
         * This should be divided into smaller blocks for better error handling */
        try {
            do
            {
                /* display normal prompt preceeding every user entry */
                System.out.print("Enter command ('QUIT' to exit): ");

                /* get user input and tokenize to get command */
                message =  userEntry.nextLine();
                StringTokenizer tokens = new StringTokenizer(message);
                command = tokens.nextToken();


                if(command.equals("CONNECT"))
                {
                    /* check that no connection already exists with a server */
                    if (server != null)
                    {
                        System.out.println("A connection with server " + serverName + " has already been established.");
                        continue;
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

                    System.out.println("Connection with " + serverName + " has been established.");
                    continue;  //repeat while loop
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

                    System.out.println(response);

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
                                results.add(file.getName());
                            }
                        }

                        outputStream.writeObject(results);

                        dataSocket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /* get success/failure message */
                    response = serverInput.nextLine();

                    System.out.println(response);

					dataSocket.close();
					welcomeSocket.close();
                }

                /* done in the form UNREGISTER <username> */
                if(command.equals("UNREGISTER"))
                {
                    // get the username passed
                    String username = tokens.nextToken();

                    // ask the server to unregister this username
					          serverOutput.println("UNREGISTER " + username);
                }

                /* done in the form SEARCH <keyword> */
                if(command.equals("SEARCH"))
                {
                    // get the keyword passed
                    String keyword = tokens.nextToken();

                    // tell the server what we want to search for
        					  serverOutput.println("SEARCH " + PORT + " " + keyword);

                    // set up our dataSocket
          					ServerSocket welcomeSocket = new ServerSocket(PORT);
          					Socket dataSocket = welcomeSocket.accept();

                    // make an object stream that will receive the arraylist of results the server gives us
          					ObjectInputStream input = new ObjectInputStream(dataSocket.getInputStream());
          					try{
                      System.out.print("\n");
                      // read in the results of the search
          						ArrayList<String> results = (ArrayList<String>)input.readObject();
                      // update our global search variable
                      latestSearch = results;
                      // numbering integer helps us format the output
                      int numbering = 0;
                      // print out all our results and stuff
                      System.out.println(results.size() + " results found: ");
          						for(int i=0; i<results.size(); i++){
                        numbering=i+1;
                        System.out.println(numbering + results.get(i));
                      }
          					}catch(Exception e){
          						System.out.println("Unable to read search results!");
          					}
                    System.out.print("\n");
                    input.close();
                    dataSocket.close();
                    welcomeSocket.close();
                }
                /*
                   done in form GET <integer>
                   where the integer is the number from the previous search.
                   i.e. GET 1 would get the first result from the most recent search
                */
                if(command.equals("GET")){
                  // check that we have done at least one search before
                  if(latestSearch.isEmpty()){
                    System.out.println("No search results found");
                    continue;
                  }
                  // get the number the client wants
                  int searchNum = Integer.parseInt(tokens.nextToken());
                  System.out.println("Getting " + latestSearch.get(searchNum));

                  /*
                    initiate a search array and then
                    break the search result the client wants into 3 parts
                    and put them into searchCreds
                    searchCreds[0] = owner of files
                    searchCreds[1] = name of files
                    searchCreds[2] = location of file
                  */
                  String[] searchCreds = new String[3];
                  searchCreds = latestSearch.get(searchNum-1).split(",", 3);

                  // get the file location
                  System.out.println("Getting file location...");
                  String targetServer = searchCreds[2];

                  // make a connection with the location of the file
                  System.out.println("Connecting to file location...");
                  // FIXME: this port shouldn't be hardcoded
                  Socket connSocket = new Socket(targetServer, 1232);

                  /* get input stream from server to receive response */
                  /* get output stream from server to send request */
                  serverInput = new Scanner(connSocket.getInputStream());
                  serverOutput = new PrintWriter(connSocket.getOutputStream(),true);

                  // tell the server that we want to get a file, and give it the filename
                  serverOutput.println("GET " + searchCreds[1] + " " + PORT);

                  // set up the data socket with the server that has the file we want
                  ServerSocket welcomeSocket = new ServerSocket(PORT);
                  Socket dataSocket = welcomeSocket.accept();

                  // the rest of this is essentially just client side RETR from the first project

                  byte [] fileContents = new byte[1000000];

                  File file = new File("./" + searchCreds[1]);

                  FileOutputStream fileStream = new FileOutputStream(file);
                  BufferedOutputStream buffStream = new BufferedOutputStream(fileStream);

                  InputStream fileIn = dataSocket.getInputStream();
                  int bytesRead = 0;

                  bytesRead = fileIn.read(fileContents);
                  buffStream.write(fileContents, 0, bytesRead);

                  buffStream.close();
                  dataSocket.close();
                  welcomeSocket.close();

                  System.out.println("File Saved!");
                }

            }while (!command.equals("QUIT"));

            /* tell the server that you are quitting */
            serverOutput.println("QUIT");

            /* close connection and resources */
            server.close();
            userEntry.close();
            serverOutput.close();
            serverInput.close();
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

    }

}
