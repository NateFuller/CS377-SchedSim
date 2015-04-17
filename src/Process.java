import java.util.List;
import java.util.ArrayList;

/* model for a process, you will need a collection of these */
class Process {

	public enum State { // added IO to indicate a process is on the I/O device
    	NEW, READY, RUNNING, IO, WAITING, TERMINATED
	}	
	State state = State.NEW; // current state in the state machine from figure 3.2

	/* Put data structures to hold size of CPU and I/O bursts here */
	public double[] cpuBurstSizes;
	public double[] ioBurstSizes;

	public int numCPUbursts;
	int currentBurst; // indicates which of the series of bursts is currently being handled. state can be used to determine what kind of burst
	double completedTime = 0; // used to calculate remaining time till completion if burst is descheduled

	public Process(int numCPUbursts) {
		this.numCPUbursts = numCPUbursts;
		cpuBurstSizes = new double[numCPUbursts];
		ioBurstSizes = new double[numCPUbursts - 1];
	}
}