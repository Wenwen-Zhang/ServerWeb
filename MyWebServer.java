/*--------------------------------------------------------

1. Name / Date: 

	Wenwen Zhang  10/06/2017

2. Java version used, if not the official version for the class:

	build 1.8.0_121-b13

3. Precise command-line compilation examples / instructions:

	> javac MyWebServer.java

4. Precise examples / instructions to run this program:

	a. Start MyWebServer by typing the following command in shell window
		> java MyWebServer
	
	b. Open the Firefox browser, type the following URL to get 
	the directory index or retrieve specific files.
		http://localhost:2540/ 
			
	c. Connect a client to this server at Port 2540 , type the 
	requests manually to get responses.
	
	d. Open the addnums.html page to submit a fake-cgi request and get results back.	

5. List of files needed for running the program.

 	a. MyWebServer.java
 	b. Firefox browser or a client java file
 	c. .txt files, .html files, and sub-directories
 	d. addnums.html

6. Notes:
	a. The "Parent Directory" hot link in the directory index html page will 
	always re-direct the users to the home directory in which this server is started.
	
	b. When the URL contains "..", the browser will automatically direct to the parent 
	directory, but when the request containing ".." is sent from a client, not browser,
	then the request will be denied because of no permission.

----------------------------------------------------------*/

import java.io.*; //Import the java input/output package  
import java.net.*; //Import the java networking package
import java.util.Date; //Import to create time stamps in http header

public class MyWebServer
{
	public static void main(String a[]) throws IOException
	{
		int q_len = 6; //Maximum requests the server can accept simultaneously. 
		int port = 2540; 
		Socket sock; //Create a local socket object. 

		//Initialize a serversocket which is bounded to the specified port number, 
		//and has a capacity of 6 simultaneous requests. 
		ServerSocket servsock = new ServerSocket(port, q_len); 

		//Print the message that the server is working and listening for connections.
		System.out.println("Wenwen Zhang's MyWebServer staring up, listening at port 2540.\n");

		while (true) // The server runs forever, waiting for connections.
		{
			//Server socket listens for requests made from connected clients, 
			//If any, accepts it and assigns to the local socket.
			sock = servsock.accept(); 
			
			// Create a new thread with this connected socket and start running this thread.
			new Worker(sock).start(); 
		}
	}
}

//Inherits Thread class and override the run() method so that the server can be multi-threaded.
class Worker extends Thread 
{
	Socket sock; //A local socket.
	Worker (Socket s) {sock = s;} //Assign s to sock when this constructor is called.

	public void run()
	{
		PrintStream out = null; 
		BufferedReader in = null;

		try
		{ 
			//Create a BufferedReader to buffer the input stream obtained from the socket,
			//and a PrintStream instance to print the output stream that will be sending to the client. 			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());			

			try
			{				
				//Read the raw request from the connected clients.
				String reqRaw = in.readLine();
				
				//Do the following only if the request is not null
				if (reqRaw != null) 
				{
					//Print the requests to the server's console.
					System.out.println("Request received: " + reqRaw);
					
					//Check if the requests are valid.
					//If invalid, send a html file showing the error message to the clients.
					if (badRequest(reqRaw))
					{										
						//Call the utility function sendError() to format 
						//and send the error showing html page.
						sendError(out, "401", "Bad request", 
					              "Your browser sent a request that this server could not understand.");
						
						//Print to the console the response action to the specific request.
						//In this case, the request is being denied since they are not valid.
						System.out.println("Action: Denied. Bad Request.\n");
					}
					else
					{
						//Process the raw requests to get the core part.
						//Get the sub-strings by removing "GET " and " HTTP/1.1"
						String reqToRead = reqRaw.substring(4, reqRaw.length() - 9).trim();
						
						//Check if the requests are permitted. If the request is not allowed,
						//send a html file showing the error message to the clients.
						if(requestNotAllowed(reqToRead))
						{
							//Call the utility function sendError() to format 
							//and send the error showing html page.
							sendError(out, "403", "Forbidden", 
										"You don't have permission to access the requested URL.");
							
							//Print to the console the response action to the specific request.
							//In this case, the request is being denied since no permissions.
							System.out.println("Action: Denied. No permission to access.\n");
						}
						else
						{							
							//Get the root directory for later use.
							String dirRoot = new File(".").getCanonicalPath();
							
							//Concatenate the processed request with root directory 
							//to get a valid path name. 
							String path = dirRoot + reqToRead;
							
							//Use the pathname to create a File instance so that the 
							//directories or contents are accessible.
							File inf = new File(path);
							
							//If the path name is a directory, send back a dynamically 
							//created directory index html page with hot links.
							if (inf.isDirectory())
							{
								//If file is a directory and the path doesn't end with a slash, 
								//add the slash to the request so that it could be concatenated 
								//with the files' name to be a complete path.
								if(!path.endsWith("/"))
								{
									reqToRead += "/";
								}						
								
								//Retrieve all the files in the directory.
								File[] files = inf.listFiles();
								
								//Create a string which contains html contents.
								//Firstly create the header of the index page
								String DirIndex = "<html><body><pre>\r\n" 
												  + "<h1> Index of Wenwen Zhang's MyWebServer" 
												  + reqToRead + "</h1>\r\n";								

//***********The following commented-out codes were used to send back a plain text index file. ****************
//								
//								for (int i = 0; i < files.length; i++)
//								{
//									index += (files[i].getName() + "\r\n");														
//								}
//								
//								out.print("HTTP/1.1 200 OK\r\nDate: " + new Date() + "\r\n" 
//											+ "Server: MyWebServer\r\n" + "Content-Length: " 
//											+ DirIndex.length() + "\r\nContent-Type: text/plain\r\n\r\n"
//											+ DirIndex + "\r\n");
//
								
								//Create the Parent Directory hot link in the index page which will 
								//always take the users back to the directory in which 
								//this server is started.
								DirIndex += "<a href=\"" + "/" + "\">" 
												+ "Parent Directory" + "</a> <br>\r\n";
								
								//Use a for loop to get all the files in the directory
								for (int i = 0; i < files.length; i++)
								{
																	
									//Add each file/directory's name to the index page with a hot link.
									DirIndex += ("<a href=\"" + reqToRead + files[i].getName() + "\">" 
												+ files[i].getName() + "</a> <br>\r\n");		
													
								}
								
								//Add these tags to complete formating the html contents.
								DirIndex += "</pre></body></html>";								
								
								//Send appropriate http header along with this index page to the client
								out.print(getHeader() + "Content-Length: " + DirIndex.length() 
										+ "\r\nContent-Type: text/html\r\n\r\n" + DirIndex + "\r\n");								
								
								//Print to the console the response action to the specific request.
								//In this case, the directory index page is being sent.
								System.out.println("Action: Directory index sent.\n");								
							}
							else
							{
								//If the path name is a file, retrieve that file and send the contents
								//along with an correct http header to the client.
								
								//If the file has an extension of fake-cgi, retrieve the names and 
								//the numbers to create a response html page to show the user's name
								//and the sum of the numbers.
								if (reqToRead.indexOf("cgi") != -1) 
								{
									//Call addnums method to process the request and send the result.
									addnums(out, reqToRead);
									
									//Print to the console the response action to the specific request.
									//In this case, the result page is being sent.
									System.out.println("Action: Result sent.\n");
								}
								else 
								{
									//If the file is a regular file(e.g. .txt, .java and .html),
									//send the contents in a html page.
									try
									{
										//Open the file in buffered reader.
										BufferedReader contents = new BufferedReader(new FileReader(inf));
										
										//Send the contents by getFileContent method
										out.print(getHeader() + getFileContent(contents, path));
										
										//Print to the console the response action to the 
										//specific request. In this case, the file is being sent. 
										System.out.println("Action: File sent.\n");
									}
									//Catch the error if the file is not found.
									catch (FileNotFoundException e)  
									{
										//Call the utility function sendError() to format 
										//and send the error showing html page.
										sendError(out, "404", "Not Found", 
												"The requested URL was not found on this server.");
										
										//Print to the console the response action to the specific request.
										//In this case, the request is denied since not such file exists.
										System.out.println("Action: Denied. File not found.\n");
									}									
								}														
							}					
						}
					}
				}							
			}

			//If anything goes wrong, keep the program running by catch the exception 
			//and print out the error message.
			catch(IOException x) 
			{
				System.out.println("Server read error");
				x.printStackTrace();
			}

			sock.close(); //Close this local socket.
		}
		//Catch the error that raised in file IO processings.
		catch(IOException ioe)
		{
			System.out.println(ioe);
		}
	}
	
	//This method is to check if the request is a valid request.
	private static boolean badRequest(String req)
	{
		//If the request doesn't begins with "GET", or
		//doesn't end with HTTP/1.0 or HTTP/1.1, or
		//there is nothing inside the request.
		//then this request is not valid.
		return (!req.startsWith("GET") || req.length() < 14 
				|| !(req.endsWith("HTTP/1.0") || req.endsWith("HTTP/1.1")));
	}
	
	//This method is to check if the request is trying to access 
	//files or directories that it is not allowed to.
	private static boolean requestNotAllowed(String req)
	{
		//The request will be denied if it contains ".." or "/.ht" and "~".
		return (req.indexOf("..")!= -1 || req.indexOf("/.ht") != -1 
				|| req.endsWith("~"));
	}
	
	//This method is to get the file contents including the length, and 
	//get the correct MINE type.  
	private static String getFileContent(BufferedReader f, String path)
	{
		String mineType = "";
		
		//Check the files' extensions, get the correct MINE type.
		//In this case, only the .html, .txt, and .java is recognized.
		
		if(path.endsWith(".html")||path.endsWith(".htm"))
			mineType = "text/html";
		else if (path.endsWith(".txtl")||path.endsWith(".java"))
			mineType = "text/plain";
		else
			mineType = "text/plain";
		
		String contents = ""; //To get the contents.
		
		try
		{
			//Create a string builder to obtain the contents 
			//from the buffered reader.
			StringBuilder getString = new StringBuilder();
			
			//To get contents of each line in the file.
			String line;
			
			//Use a while loop to keep reading the file
			while (true) 
			{
				//Read the file line by line.
				line = f.readLine(); 
				
				//The end of the file is reached, stop looping.
				if(line == null)  
					break;
				else
				{
					//Append the line read and "\n" to the string builder.
					getString.append(line); 
					getString.append("\n");
				}
			}
			
			//Convert the string builder to string so that we can call length() 
			//to get the length, and concatenate it to the http header.
			contents = getString.toString();
		}
		catch(Exception e) //Catch exceptions.
		{
			e.printStackTrace(System.out);
		}		
		
		//Complete the http header using correct MINE type and send 
		//the contents.
		String result = "Content-Length: " + contents.length() + "\r\nContent-Type: " 
				  + mineType + "\r\n\r\n" + contents + "\r\n";
		
		return result;
	}
	
	//This method is to create a response page to the fake-cgi request.
	private static void addnums(PrintStream out, String req)
	{
		//Using these indexes to retrieve names and numbers from the request.
		int eqIdx, andIdx;
		
		//Get the index of the first "=" sign and first "&" sign.
		eqIdx = req.indexOf("="); 
		andIdx = req.indexOf("&", eqIdx + 1);
		
		//Get the name which is between the first "=" and first "&".
		String name = req.substring(eqIdx + 1, andIdx);
		
		//Get the index of the second "=" sign and second "&" sign.
		eqIdx = req.indexOf("=", andIdx + 1);
		andIdx = req.indexOf("&", eqIdx + 1);
		//Get the first number which is between the second "=" and second "&".
		int n1 = Integer.parseInt(req.substring(eqIdx + 1, andIdx));
		
		//Get the index of the third "=" sign.
		eqIdx = req.indexOf("=", andIdx + 1);
		//Get the second number which is right following the third "=" sign.
		int n2 = Integer.parseInt(req.substring(eqIdx + 1));
		
		int sum = n1 + n2; //Calculate the sum of these two numbers.	
		
		//Create a html page that shows the name and the sum.
		String response = "<html><body><center>\r\n" + "<br><h1> Dear " 
							+ name + "</h1>\r\n" + "<h1> The sum of " 
							+ n1 + " and " + n2 +  " is " + sum 
							+ ".</h1>\r\n" + "</center></body></html>";	
		
		//Complete the http header using correct MINE type and send 
		//the response page.
		String result = getHeader() + "Content-Length: " + response.length() 
						+ "\r\nContent-Type: text/html\r\n\r\n"
						+ response + "\r\n";
		
		out.print(result);		
	}
	
	//This is an utility method to get the first part of the http header,
	//the rest of the header will be completed depending on the type of requests.
	private static String getHeader()
	{
		return "HTTP/1.1 200 OK\r\nDate: " + new Date() 
				+ "\r\n" + "Server: MyWebServer\r\n";
	}
	
	//This is an utility method to send error message to the client based on the error type.
	private static void sendError(PrintStream out, String errCode, String err, String errMessage)
	{	
		out.print("HTTP/1.1 " + errCode + " " + err + "\r\n\r\n" 
				  + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" 
				  + "<title>" + errCode + " " + err + "</title>\r\n" 
				  + "</head><body>\r\n" +"<h1>" + err + "</h1>\r\n" + "<p>" + errMessage 
				  + "</p>\r\n" + "<HR><address>MyWebServer: Localhost at "
				  + " Port 2540" + "</address>\r\n" + "</body></html>\r\n");
	}

}


