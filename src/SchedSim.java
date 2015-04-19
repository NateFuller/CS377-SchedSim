import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.*;

class SchedSim {

    public static int maxProcesses; // cap on total processes for simulation
    public static int maxCPUbursts; // cap on total CPU bursts per process
    public static double time = 0; // current time in simulation, starting at zero

    public static double nextProcessTime; // time of the next process; used to schedule the next arrival event and should not be saved in the current process object

    public static Queue<Event> eventHeap;
    public static List<Process> processTable;
    public static Queue<Process> ioQueue;
    public static Queue<Process> readyQueue;
    public static InputStream inputStream; // stream used to read in process info
    public static Queue<Process> newProcesses;
    public static LinkedList<Process> procsStats = new LinkedList<>(); // used for keeping statistics on all processes

    public static Device CPU;
    public static Device ioDevice;

    public enum Algorithm { // algorithm to use for entire run of simulation
        FCFS, SJF, SRTF, RR
    }

    public static Algorithm algorithm;

    public static void main(String[] args) throws FileNotFoundException {

        //---------------------------------------------------------------------//
        //----------------------------PARSE INPUT------------------------------//
        //---------------------------------------------------------------------//
        if (args.length < 4 || args.length > 5) {
            System.err.println("ERROR: Invalid Input!");
            printUsageAndExit();
        }

        File f = checkInputAndGetFile(args);

        String a = args[3].toLowerCase();
        algorithm = determineAlgorithm(a);


        // give some feedback to the user
        System.out.println("Executing the \"" + algorithm.name() + "\" algorithm with at most " + maxProcesses
                + " processes and at most " + maxCPUbursts + " CPU bursts per process.");

        //---------------------------------------------------------------------//
        //---------------------------------SETUP-------------------------------//
        //---------------------------------------------------------------------//
        // you might want to open the binary input file here
        inputStream = new FileInputStream(f);

        // initialize data structures
        eventHeap = new PriorityQueue<>();
        processTable = new ArrayList<>();
        ioQueue = new PriorityQueue<>();
        newProcesses = new LinkedList<>();
        getNewProcesses(); //populates newProcesses

        ioDevice = new Device();
        CPU = new Device();

        //---------------------------------------------------------------------//
        //-------------------------------DES LOOP!-----------------------------//
        //---------------------------------------------------------------------//
        switch(algorithm) {
            case FCFS:
                readyQueue = new LinkedList<>(); // just using a linked list; no comparator needed
                double completionTime = FCFS();
                System.out.println("FCFS finished with completion time: " + completionTime + " seconds.");
                break;
            case SJF:
                break;
            case SRTF:
                break;
            case RR:
                break;
            default:
                System.err.println("ERR: Something went wrong...");
                System.exit(1);
        }




        // output statistics

    }

    public static double FCFS() {

        while(!eventHeap.isEmpty()) {
            Event currentEvent = eventHeap.poll();
            time = currentEvent.time;

            switch (currentEvent.type) {
                case ARRIVAL:
                    Process arrivalProcess = newProcesses.remove();
                    processTable.add(arrivalProcess);

                    // no process on CPU means the CPU is idle
                    if (CPU.isIdle()) {
                        // place the process on CPU and set its state to running
                        CPU.currentProcess = arrivalProcess;
                        arrivalProcess.waitTime += time - arrivalProcess.lastWait; // update the wait time of the process
                        arrivalProcess.state = Process.State.RUNNING;

                        // create a new CPU Burst Completion event and add to eventheap
                        eventHeap.add(new Event(Event.Type.CPU_DONE,
                                time + arrivalProcess.cpuBurstSizes[arrivalProcess.currentBurst]));
                    } else { // CPU busy
                        arrivalProcess.state = Process.State.READY;
                        arrivalProcess.lastWait = time; // start waiting because we're not doing any CPU work yet
                        readyQueue.add(arrivalProcess);
                    }

                    break;
                case CPU_DONE:
                    if (CPU.currentProcess.currentBurst == CPU.currentProcess.cpuBurstSizes.length - 1) {
                        CPU.currentProcess.state = Process.State.TERMINATED;
                        CPU.currentProcess.completionTime = time;
                        processTable.remove(CPU.currentProcess);
                    } else if (ioDevice.isIdle()) {
                        // move process from CPU to I/O
                        ioDevice.currentProcess = CPU.currentProcess;
                        ioDevice.currentProcess.waitTime += time - ioDevice.currentProcess.lastWait; // update wait time
                        ioDevice.currentProcess.state = Process.State.IO;

                        // an I/O completion event added to the event queue
                        eventHeap.add(new Event(Event.Type.IO_DONE,
                                time + ioDevice.currentProcess.ioBurstSizes[ioDevice.currentProcess.currentBurst]));
                    } else { // Otherwise it gets put into the IO queue with status waiting
                        Process p = CPU.currentProcess;

                        p.state = Process.State.WAITING;
                        p.lastWait = time; // start waiting because we're not doing I/O work yet
                        ioQueue.add(p); // add to I/O queue
                    }

                    // free up the CPU
                    CPU.currentProcess = null;

                    if (!readyQueue.isEmpty()) {
                        Process readyProcess = readyQueue.poll();
                        CPU.currentProcess = readyProcess;
                        // a new CPU Burst Completion event added to the event queue
                        eventHeap.add(new Event(Event.Type.CPU_DONE, time + readyProcess.cpuBurstSizes[readyProcess.currentBurst]));
                    }

                    break;
                case IO_DONE:
                    readyQueue.add(ioDevice.currentProcess);
                    ioDevice.currentProcess = null; // free up CPU

                    if (CPU.isIdle()) {
                        CPU.currentProcess = readyQueue.poll();
                    }

                    // if a process is waiting for IO
                    if (!ioQueue.isEmpty()) {
                        Process ioProcess = ioQueue.poll();
                        ioDevice.currentProcess = ioProcess;

                        eventHeap.add(new Event(Event.Type.IO_DONE, time + ioProcess.ioBurstSizes[ioProcess.currentBurst]));
                    }

                    break;
                default:
                    System.err.println("Event type unknown! Terminating immediately.");
                    System.exit(1);
            }
        }
        return time;
    }

    private static void getNewProcesses(){
        for (int i = 0; i < maxProcesses; i++) {
            Process theProcess = getProcessFromInput();
            newProcesses.add(theProcess); // use for placing arriving processes into processTable
            procsStats.add(theProcess); // keep reference to same exact process in order to use for bookkeeping later
        }
    }

    private static Process getProcessFromInput() {

        double readIn = readByte() / 10.0; // for debugging purposes
        nextProcessTime += readIn;
        // System.out.println("READ: " + readIn + "; NPT: " + nextProcessTime); // debugging
        int numCPUbursts = readByte() % maxCPUbursts + 1;

        Process p = new Process(numCPUbursts);

        for (int i = 0; i < numCPUbursts; i++) {
            p.cpuBurstSizes[i] = readByte() / 25.6;
            //System.out.print(p.cpuBurstSizes[i] + " ");
        }
        //System.out.println();

        for (int i = 0; i < numCPUbursts - 1; i++) {
            p.ioBurstSizes[i] = readByte() / 25.6;
            //System.out.print(p.ioBurstSizes[i] + " ");
        }

        eventHeap.add(new Event(Event.Type.ARRIVAL, nextProcessTime));

        p.state = Process.State.NEW;

        return p;
    }

    private static int readByte() {
        int retVal = -1;
        try {
            retVal = inputStream.read() & 0xff;
        } catch(IOException e) { // probably should terminate if something goes wrong with I/O
            System.err.print(e.getLocalizedMessage());
            System.exit(1);
        }
        return retVal;
    }

    /**
     *
     * @param arg the argument to parse
     * @return Algorithm that the arg matches or exit if invalid
     */
    private static Algorithm determineAlgorithm(String arg) {
        switch (arg) {
            case "fcfs":
            case "0":
                return Algorithm.FCFS;
            case "sjf":
            case "1":
                return Algorithm.SJF;
            case "srtf":
            case "2":
                return Algorithm.SRTF;
            case "rr":
            case "3":
                return Algorithm.RR;
            default:
                System.out.println("ERROR: Invalid Algorithm Input!");
                System.out.println("Please enter one of the following: 0 for FCFS; 1 for SJF; 2 for SRTF; or 3 for RR");
                printUsageAndExit();
        }
        return null;
    }

    /**
     *
     * @param args command line arguments
     * @return File the file to be read from if all the input is sufficient and valid
     * @throws FileNotFoundException if the file does not exist or is a directory
     */
    private static File checkInputAndGetFile(String[] args) throws FileNotFoundException {
        File f = new File(args[0]);
        if (!f.exists() || f.isDirectory()) {
            throw new FileNotFoundException("ERROR: Could not find file named: \"" + args[0] + "\"");
        }

        try {
            maxProcesses = Integer.parseInt(args[1].toLowerCase());
            maxCPUbursts = Integer.parseInt(args[2].toLowerCase());
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Please enter non-negative, nonzero integer values for maxProcesses and maxCPUbursts.");
            printUsageAndExit();
        }

        if (maxProcesses <= 0) {
            System.err.println("ERROR: Please enter a non-negative, nonzero integer value for maxProcesses.");
            printUsageAndExit();
        }

        if (maxCPUbursts <= 0) {
            System.err.println("ERROR: Please enter a non-negative, nonzero value integer for maxCPUbursts.");
            printUsageAndExit();
        }
        return f;
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java SchedSim <filename> <maxProcesses> <maxCPUbursts> <algorithm>");
        System.exit(1);
    }

    private static void debugEventHeap() {
        for (Event e : eventHeap) {
            System.out.println("EVENT: " + e.type);
        }
    }
}
