class SchedSim {

	public static int maxProcesses; // cap on total processes for simulation
	public static int maxCPUbursts; // cap on total CPU bursts per process
	public static double time = 0; // current time in simulation, starting at zero

	public enum Algorithm { // algorithm to use for entire run of simulation
		FCFS, SJF, SRTF, RR
	}	

	public static Algorithm algorithm;

	public static void main(String [] args) {
		// parse arguments
		// you might want to open the binary input file here
		
		// initialize data structures

		/* DES loop */
		// see psudeocode in the assignment
		// all of your input reading occurs when processing the Arrival event

		// output statistics
	}

}
