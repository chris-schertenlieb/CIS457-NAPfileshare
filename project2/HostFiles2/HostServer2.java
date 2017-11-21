import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;


public class HostServer2 {
  private static ServerSocket welcomeSocket;
  private static final int PORT = 1332;

      public static void main(String[] args) throws IOException{
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

         System.out.println("\nNew transfer request accepted.\n");

         /* spin off new thread to handle all future communication with client */
         ClientHandler handler = new ClientHandler(client);

         handler.start();
     }while (true);  //continue listening; program must be ended by a Ctrl-C
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
    private int dataInputPort = 1236;

    public ClientHandler(Socket socket)
    {
        client = socket;

        try
        {
            /* get input/output streams from the client socket
             * These represent the i/o for the persistent command connection */
            input = new Scanner(client.getInputStream());
            output = new PrintWriter(client.getOutputStream(),true);
        }
        catch(IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    public void run()
    {
           received = input.nextLine();  //this line blocks until message is received
           StringTokenizer tokens = new StringTokenizer(received);
           command = tokens.nextToken();

           do{
           if(command.equals("GET"))
            {
                /* will be of the form RETR <filename> <port> */
                /* send <filename> to client address at <port> */

                String fileName = tokens.nextToken();

                try {
                    dataConnPort = Integer.parseInt(tokens.nextToken());
                } catch (NumberFormatException e1) {
                    /* user typed a bad port number. send error message, wait for next command, parse it, repeat while loop */
                    output.println("Invalid port number. Command must be in the form RETR (string)filename (int)portNumber");
                    continue;
                }

                /* If the file lives where this class lives, the directory will be on the classpath */
                File file = new File(fileName);
                if(!file.exists()) {
                    /* File was not found. Send error message, repeat while loop */
                    output.println(fileName + " cound not be found. Please specify a different file.");
                    continue;
                }

                byte[] bytes = new byte[16 * 1024];
                InputStream fileIn = null;

                try {
                    /* get the file and get an input stream from it */
                    fileIn = new FileInputStream(fileName);

                    /* get an output stream using the client's provided data port number */
                    Socket dataSocket = null;
                    OutputStream dataOutput = null;
                    dataSocket = new Socket(client.getInetAddress(), dataConnPort);
                    dataOutput = dataSocket.getOutputStream();

                    /* there may be a much more efficient way to do this?... */
                    /* read bytes from file and write them to output stream */
                    int count;
                    while ((count = fileIn.read(bytes)) > 0) {
                        dataOutput.write(bytes, 0, count);
                    }

                    /* close streams and socket */
                    dataOutput.close();
                    fileIn.close();
                    dataSocket.close();
					
					command = "";

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
					continue;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
					continue;
                }
            }
          }while(true);
        }
  }
