import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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
        readyQueue = new PriorityQueue<>();

        Device ioDevice = new Device();
        Device CPU = new Device();

        // add initial event so we can get into the while loop below
        eventHeap.add(new Event(Event.Type.ARRIVAL, 0));

        //---------------------------------------------------------------------//
        //-------------------------------DES LOOP!-----------------------------//
        //---------------------------------------------------------------------//
        while(!eventHeap.isEmpty()) {
            Event currentEvent = eventHeap.poll();
            time = currentEvent.time;

            switch(currentEvent.type) {
                case ARRIVAL:
                    Process p = getProcessFromInput();
                    processTable.add(p);

                    if (CPU.currentProcess == null) {
                        // place the process on CPU and set its state to running
                        CPU.currentProcess = p;
                        p.state = Process.State.RUNNING;

                        // create a new CPU Burst Completion event and add to eventheap
                        // time = the time after 1st cpu burst of the process
                        eventHeap.add(new Event(Event.Type.CPU_DONE, time + p.cpuBurstSizes[0], p));
                    } else { // CPU busy
                        p.state = Process.State.READY;
                        readyQueue.add(p);
                    }
                    break;
                case CPU_DONE:
                    Process p = currentEvent.process;
                    

            }
        }

        /* DES loop */
        // see pseudocode in the assignment
        // all of your input reading occurs when processing the Arrival event


        // output statistics

    }

    private static Process getProcessFromInput() {

        double nextProcessTime = readByte() / 10.0;
        int numCPUbursts = readByte() % maxCPUbursts + 1;

        Process p = new Process(numCPUbursts);

        for (int i = 0; i < numCPUbursts; i++) {
            p.cpuBurstSizes[i] = readByte() / 25.6;
        }
        for (int i = 0; i < numCPUbursts - 1; i++) {
            p.ioBurstSizes[i] = readByte() / 25.6;
        }

        System.out.println(nextProcessTime + " " + numCPUbursts);
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
            System.err.println();
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
}
