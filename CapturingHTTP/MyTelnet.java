/** A client for InetServer. 

Wenwen Zhang
-----------------------------------------------*/

import java.io.*; // Import the input/output package
import java.net.*; // Import the Java networking package

public class MyTelnet
{
	public static String serverName;
	public static int port;
	
	public static void main(String args[])
	{
		/* Create a string serverName to store the name of the connected server.
		If this client is connecting to a server other than the local host, type in the server name,
		otherwise, it will connect to the localhost.
		Then the output will show the server name and the port number that this client is using.
		*/
		
		if (args.length < 1)
		{
			serverName = "localhost";
			port = 2540;
		}
		else
		{
			serverName = args[0];
			port = Integer.parseInt(args[1]);
		}
			
			
		System.out.println();	
		System.out.println("Wenwen Zhang's MyTelnet. \n");
		System.out.println("Using server: " + serverName + ", Port: " + port);
		System.out.println();

		/* Create a BufferedReader object to store the user's input.*/
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try
		{
			String name;
		
			do
			{
				String request = "";

				while((name = in.readLine()) != null)
				{
					request = request + name + "\n";
					if (name.isEmpty()) break;
				}
				
				getReply(request, serverName);
				
//				if(name.indexOf("quit") < 0)
//				{
//					System.out.println("Host: " + serverName + ":" + port);
//					
//					getReply(name, serverName);
//				}
					
			}
			while(name.indexOf("quit") < 0);

			System.out.println("Cancelled by user request."); 
		}

		/* If anything goes wrong, catch the exception, and print out the error information,
		so that the program can keep running.
		*/
		catch(IOException x) 
		{
			x.printStackTrace();
		}
	}

	static void getReply(String name, String serverName)
	{
		Socket sock; // Create a local socket object.
		
		BufferedReader fromServer;
		PrintStream toServer; 
		String textFromServer;

		try
		{
			// Create a socket connecting to the the server and the specified port number passed in.
			sock = new Socket(serverName, port); 

			// Create a bufferedReader object to buffer the input stream got from the socket.
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			// Create a printStream to store the output stream that will be sending to the server.
			toServer = new PrintStream(sock.getOutputStream());
			toServer.println(name);
			toServer.flush();

			
			// Read the message (one line at a time) stored in the bufferedReader object, and print it out.
			//textFromServer = fromServer.readLine(); 
			while ((textFromServer = fromServer.readLine()) != null)
				System.out.println(textFromServer);
			
			sock.close(); // Close the local socket.
		}
		catch(IOException x)
		{
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}

}