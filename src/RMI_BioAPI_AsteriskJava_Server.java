import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMI_BioAPI_AsteriskJava_Server extends UnicastRemoteObject implements RMI_BioAPI_AsteriskJava_Interface {

	public RMI_BioAPI_AsteriskJava_Server(int port) throws RemoteException {
		super(port);
	}

	@Override
	public void retrieve_available_files(String socket_ip, int socket_port) throws Exception {
		Socket soc;
		PrintWriter pw = null;
		File directory = new File("rmifiles");
		
		// Look for subfolder called 'rmifiles'
		if (!directory.isDirectory())
			throw new Exception("The directory rmifiles does not exist");
		File[] fList = directory.listFiles();

		try {
			// Create socket to connect to client
			soc = new Socket(socket_ip, socket_port);
			pw = new PrintWriter(soc.getOutputStream(), true);
			String s;
			pw.println("Xfer Start");
			System.out.println("Xfer Start");
			
			// Search through folder for file specified
			for (File file : fList) {
				if (file.isDirectory())
					s = "Directory";
				else
					s = "File";
				pw.println(s + file.getName());
				System.out.println(s + file.getName());
			}
			
			// Done is the signaling message to terminate socket listener
			pw.println("Done");
			soc.close();
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host.");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to server.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void RPC_FileRead(String Service_UID, String srcFileName, String socket_ip, int socket_port,
			String remote_fileName) throws RemoteException {
		Socket soc;
		PrintWriter pw = null;

		File directory = new File("rmifiles");
		File serverFile = null;

		// Search through subdirectory to find file specified
		try {

			// Look for subfolder called 'rmifiles'
			if (!directory.isDirectory())
				throw new Exception("The directory rmifiles does not exist");
			File[] fList = directory.listFiles();

			// Search through folder for file specified
			for (File file : fList) {
				System.out.println(file.getName() + " " + srcFileName);
				if (file.getName().equals(srcFileName))
					serverFile = file;
			}

			if (serverFile == null)
				throw new Exception("The file " + srcFileName + " is not available in rmifiles folder");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		BufferedReader brf = null;

		try {
			brf = new BufferedReader(new InputStreamReader(new FileInputStream(serverFile.getAbsolutePath())));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		try {
			if (brf == null)
				throw new RuntimeException("Cannot read from closed file " + serverFile.getAbsolutePath() + ".");

			try {

				// Create socket to connect to client
				soc = new Socket(socket_ip, socket_port);
				pw = new PrintWriter(soc.getOutputStream(), true);

				String line = brf.readLine();

				pw.println("Xfer Start");
				while (line != null) {
					System.out.println(line);
					pw.println(line);
					line = brf.readLine();
				}

				// Done is the signaling message to terminate socket listener
				pw.println("Done");

				brf.close();
				soc.close();
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host.");
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to server.");
				e.printStackTrace();
				System.exit(1);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	} // RPC_FileRead

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.println("Syntax - Two arguments expected rmi_registry_port, host_port");
			System.exit(1);
		}

		// Create an instance of RMI_BioAPI_AsteriskJava_Server object
		RMI_BioAPI_AsteriskJava_Server svr = new RMI_BioAPI_AsteriskJava_Server(Integer.parseInt(args[1]));

		System.out.println("RmiRegistry listens at port " + args[0]);
		System.out.println("AsteriskJava RMI Server is ready to listen on " + args[1]);
		System.out.println("Ip address: " + InetAddress.getLocalHost());

		// Create registry and bind remote object 'svr'
		try {
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.bind("RMI_BioAPI_AsteriskJava", svr);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("BioAPI AsteriskJava RMI server started and is listening for requests ... ");
	}

}
