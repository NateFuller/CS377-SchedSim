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

	public int id; // helps for debugging

	public int numCPUbursts = 0;
	public int currentBurst = 0; // indicates which of the series of bursts is currently being handled. state can be used to determine what kind of burst
	public double completedTime = 0; // used to calculate remaining time till completion if burst is descheduled
	public double waitTime = 0; // we are defining this to be whenever the process is waiting; either waiting for CPU or I/O. Basically whenever the process is not doing any work.
	public double lastWait = 0; // the time at which this process last started waiting
	public double completionTime = 0; // the time that this process completes at
	public double totalRunTime = 0;

	public Process(int numCPUbursts) {
		this.numCPUbursts = numCPUbursts;
		cpuBurstSizes = new double[numCPUbursts];
		ioBurstSizes = new double[numCPUbursts - 1];
	}


}