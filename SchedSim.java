import java.io.File;
import java.io.FileNotFoundException;

class SchedSim {

	public static int maxProcesses; // cap on total processes for simulation
	public static int maxCPUbursts; // cap on total CPU bursts per process
	public static double time = 0; // current time in simulation, starting at zero

	public enum Algorithm { // algorithm to use for entire run of simulation
		FCFS, SJF, SRTF, RR
	}	

	public static Algorithm algorithm;

	public static void main(String [] args) throws FileNotFoundException{
		// parse arguments
		if (args.length < 4 || args.length > 5) {
			System.err.println("Invalid Input!");
			printUsageAndExit();
		}

		File f = new File(args[0]);
		if (!f.exists() || f.isDirectory()) {
			System.out.println();
			throw new FileNotFoundException("Could not find file named: \"" + args[0] + "\"");
		}

		String a = args[3].toLowerCase();

		switch (a) {
			case "fcfs":
				algorithm = Algorithm.FCFS;
				break;
			case "sjf":
				algorithm = Algorithm.SJF;
				break;
			case "srtf":
				algorithm = Algorithm.SRTF;
				break;
			case "rr":
				algorithm = Algorithm.RR;
				break;
			default:
				System.out.println("Invalid Algorithm Input!");
				System.out.println("Please enter one of the following: FCFS, SJF, SRTF, RR");
				break;
		}

		// you might want to open the binary input file here
		
		// initialize data structures

		/* DES loop */
		// see psudeocode in the assignment
		// all of your input reading occurs when processing the Arrival event

		// output statistics
	}

	private static void printUsageAndExit() {
		System.err.println("Usage: java SchedSim <filename> <maxProcesses> <maxCPUbursts> <algorithm>");
		System.exit(1);
	}
}
