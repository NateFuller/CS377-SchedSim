/* these will be used in the Event Heap and all queues */
class Event {

	public enum Type { // corresponds to "Events to be Processed" in assignment
		ARRIVAL, CPU_DONE, IO_DONE 
	}

	public final Type type; // constant type of event, might want to make this final
	public double time; // when this event will occur
	public Process process; // the process affiliated with this event, helps in SchedSim.java

	public Event(Type type, double time, Process process) {
		this.type = type;
		this.time = time;
		this.process = process;
	}

}
