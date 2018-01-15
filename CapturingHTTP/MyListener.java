/** A multithreaded server for InetClient. 

Wenwen Zhang
-----------------------------------------------*/

import java.io.*; // Import the java input/output package  
import java.net.*; // Import the java networking package

public class MyListener
{
	public static void main(String a[]) throws IOException
	{
		int q_len = 6; // Maximum requests the server can accept simultaneously. 
		int port = 2540; 
		Socket sock; // Create a local socket object. 

		/* Create a serversocket object which is bounded to the specified port number, 
		and has a capacity of 6 simultaneous requests. 
		*/
		ServerSocket servsock = new ServerSocket(port, q_len); 

		// Print out the message that the server is working and listening for connections.
		System.out.println();
		System.out.println("Wenwen Zhang's MyListener staring up, listening at port 2540.\n");

		while (true) // The server runs forever, waiting for connections.
		{
			/* Server socket listens for connection requests made from clients, 
			and if any, accepts it and assigns to the local socket.
			*/
			sock = servsock.accept(); 
			
			// Create a new thread with this connected local socket and start running this thread.
			new Worker(sock).start(); 
		}
	}
}

/* Inherits Thread class and override the run() method so that the server can be multi-threaded.*/
class Worker extends Thread 
{
	Socket sock; // A local socket object sock.
	Worker (Socket s) {sock = s;} // Assign s to sock when this constructor is called.

	public void run()
	{
		PrintStream out = null; 
		BufferedReader in = null; 

		try
		{
			/* 
			Create a BufferedReader object to buffer the input stream obtained from the socket,
			and a PrintStream object to store the output stream that will be sending to the client. 
			*/
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());

			try
			{
				String name;
				
				while((name = in.readLine()) != null)
				{
					System.out.println(name);
				}
			
			}

			/* If anything goes wrong, keep the program running by catch the exception 
			and print out the error message.
			*/
			catch(IOException x) 
			{
				System.out.println("Server read error");
				x.printStackTrace();
			}

			sock.close(); // Close this local socket.
		}
		catch(IOException ioe)
		{
			System.out.println(ioe);
		}
	}

}

