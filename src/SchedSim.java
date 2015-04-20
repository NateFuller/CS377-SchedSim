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

    public static double nextProcessTime = 0; // time of the next process; used to schedule the next arrival event and should not be saved in the current process object

    public static Queue<Event> eventHeap;
    public static List<Process> processTable;
    public static Queue<Process> ioQueue;
    public static Queue<Process> readyQueue;
    public static InputStream inputStream; // stream used to read in process info
    public static Queue<Process> newProcesses;
    public static LinkedList<Process> procsStats = new LinkedList<>(); // used for keeping statistics on all processes

    public static int currentProcess = 0; // used for creating process IDs

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
        ioQueue = new LinkedList<>();
        processTable = new ArrayList<>();
        newProcesses = new LinkedList<>();
        getNewProcesses(); //populates newProcesses

        ioDevice = new Device();
        CPU = new Device();
        double completionTime = 0;

        //---------------------------------------------------------------------//
        //-------------------------------DES LOOP!-----------------------------//
        //---------------------------------------------------------------------//
        switch(algorithm) {
            case FCFS:
                readyQueue = new LinkedList<>(); // just using a linked list; no comparator needed
                completionTime = FCFS();
                System.out.println("FCFS finished with completion time: " + completionTime + " seconds.");
                printStats();
                break;

            case SJF:
                readyQueue = new PriorityQueue<>(11, new Comparator<Process>() {
                    // p1 would be the process to get preempted by p2
                    public int compare(Process p1, Process p2){
                        int howToCompare = (int)(p1.totalRunTime - p2.totalRunTime);
                        return howToCompare;
                    }
                });
                completionTime = SJF();
                System.out.println("SJF finished with completion time: " + completionTime + " seconds.");
                printStats();
                break;
            case SRTF:
                readyQueue = new PriorityQueue<>(11, new Comparator<Process>() {
                    // p1 would be the process to get preempted by p2
                    public int compare(Process p1, Process p2){
                        int howToCompare = (int)((p1.totalRunTime - p1.completedTime) - (p2.totalRunTime - p2.completedTime));
                        return howToCompare;
                    }
                });
                completionTime = SJF();
                System.out.println("SJF finished with completion time: " + completionTime + " seconds.");
                printStats();
                break;
            case RR:
                break;
            default:
                System.err.println("ERR: Something went wrong...");
                System.exit(1);
        }
    }

    public static double FCFS() {

        while(!eventHeap.isEmpty()) {
            Event currentEvent = eventHeap.poll();
            time = currentEvent.time;


            switch (currentEvent.type) {
                case ARRIVAL:
                    Process arrivalProcess = newProcesses.remove(); // get one of the new Processes
                    processTable.add(arrivalProcess); // add it to the table of Processes

                    // no process on CPU means the CPU is idle
                    if (CPU.isIdle()) {
                        CPU.currentProcess = arrivalProcess; // place the process on CPU and set its state to running
                        arrivalProcess.state = Process.State.RUNNING;
                        arrivalProcess.waitTime += time - arrivalProcess.lastWait; // update the wait time since the process is now doing work
                        // System.out.println("WAIT TIME = " + arrivalProcess.waitTime); // debugging

                        // create a new CPU Burst Completion event and add to eventHeap
                        eventHeap.add(new Event(Event.Type.CPU_DONE,
                                time + arrivalProcess.cpuBurstSizes[arrivalProcess.currentBurst]));

                    } else { // CPU busy
                        arrivalProcess.state = Process.State.READY; // set state; waiting for CPU, on ready queue
                        arrivalProcess.lastWait = time; // start waiting because we're not doing any CPU work yet
                        readyQueue.add(arrivalProcess); // add to readyQueue
                    }

                    break;
                case CPU_DONE:
                    // System.out.println("CURRENT BURST: " + CPU.currentProcess.currentBurst); // debugging
                    if (CPU.currentProcess.currentBurst == CPU.currentProcess.cpuBurstSizes.length - 1) {
                        CPU.currentProcess.state = Process.State.TERMINATED;
                        CPU.currentProcess.completionTime = time;
                        // System.out.println("Removed Element: " + processTable.remove(CPU.currentProcess));

                    } else if (ioDevice.isIdle()) {
                        // move process from CPU to I/O
                        ioDevice.currentProcess = CPU.currentProcess;
                        // System.out.println("Process ID: " + ioDevice.currentProcess.id + "; Current Time: " + time + "; Waited for: " + (time - ioDevice.currentProcess.lastWait) + " seconds;");
                        ioDevice.currentProcess.waitTime += time - ioDevice.currentProcess.lastWait; // update the wait time since the process is now doing work
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
                        readyProcess.waitTime += time - readyProcess.lastWait; // update the wait time since the process is now doing work
                        // System.out.println("READY PROCESS WAIT TIME: " + readyProcess.waitTime + " " + time + " " + readyProcess.lastWait);
                        // a new CPU Burst Completion event added to the event queue
                        eventHeap.add(new Event(Event.Type.CPU_DONE, time + readyProcess.cpuBurstSizes[readyProcess.currentBurst]));
                    }

                    break;
                case IO_DONE:
                    ioDevice.currentProcess.currentBurst++; // increment the current burst (here, I'm counting a "burst" to be when a process has completed a round of CPU AND I/O.)
                    readyQueue.add(ioDevice.currentProcess);
                    ioDevice.currentProcess.lastWait = time; // start waiting because we're not doing any CPU work yet
                    ioDevice.currentProcess = null; // free up IO device

                    // if the CPU is idle, put a process on it!
                    if (CPU.isIdle()) {
                        Process p = readyQueue.poll();
                        CPU.currentProcess = p;
                        p.waitTime += time - p.lastWait; // update the wait time since the process is now doing work

                        eventHeap.add(new Event(Event.Type.CPU_DONE, time + p.cpuBurstSizes[p.currentBurst]));
                    }

                    // if a process is waiting for IO
                    if (!ioQueue.isEmpty()) {
                        Process ioProcess = ioQueue.poll();
                        ioDevice.currentProcess = ioProcess;
                        ioDevice.currentProcess.waitTime += time - ioDevice.currentProcess.lastWait; // update the wait time since the process is now doing work

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

    public static double SJF(){

        while(!eventHeap.isEmpty()) {
            Event currentEvent = eventHeap.poll();
            time = currentEvent.time;

            switch(currentEvent.type) {
                case ARRIVAL:
                    Process arrivalProcess = newProcesses.remove(); // get one of the new Processes
                    processTable.add(arrivalProcess); // add it to the table of Processes

                    // no process on CPU means the CPU is idle
                    if (CPU.isIdle()) {
                        CPU.currentProcess = arrivalProcess; // place the process on CPU and set its state to running
                        arrivalProcess.state = Process.State.RUNNING;
                        arrivalProcess.lastWorked = time; // mark that this process is beginning to do work
                        arrivalProcess.waitTime += time - arrivalProcess.lastWait; // update the wait time since the process is now doing work
                        // System.out.println("WAIT TIME = " + arrivalProcess.waitTime); // debugging

                        // create a new CPU Burst Completion event and add to eventHeap
                        eventHeap.add(new Event(Event.Type.CPU_DONE,
                                time + arrivalProcess.cpuBurstSizes[arrivalProcess.currentBurst]));

                    } else { // CPU busy
                        if (CPUisPreemptedBy(arrivalProcess)) {
                            CPU.currentProcess.lastWait = time; // the CPU process is preempted and begins to wait
                            CPU.currentProcess.completedTime = time - CPU.currentProcess.lastWorked; // update how much this work this process has completed
                            CPU.currentProcess.state = Process.State.READY;

                            readyQueue.add(CPU.currentProcess); // add the preempted process to the ready queue

                            CPU.currentProcess = arrivalProcess; // give the CPU to the arrival process that preempted
                            arrivalProcess.state = Process.State.RUNNING;
                            arrivalProcess.lastWorked = time; // mark that this new process is beginning to do work
                            arrivalProcess.waitTime += time - arrivalProcess.lastWait; // update the wait time of the arrival process since it is now doing work

                            eventHeap.add(new Event(Event.Type.CPU_DONE,
                                    time + arrivalProcess.cpuBurstSizes[arrivalProcess.currentBurst]));
                        } else {
                            arrivalProcess.state = Process.State.READY; // set state; waiting for CPU, on ready queue
                            arrivalProcess.lastWait = time; // start waiting because we're not doing any CPU work yet
                            readyQueue.add(arrivalProcess); // add to readyQueue
                        }
                    }
                    break;
                case CPU_DONE:
                    break;
                case IO_DONE:
                    break;
            }
        }
        return time;
    }

    /**
     *
     * @param p the process that may possibly preempt that which is currently running on the CPU
     * @return whether or not p should preempt the CPU
     */
    private static boolean CPUisPreemptedBy(Process p) {
        int result = ((PriorityQueue)readyQueue).comparator().compare(CPU.currentProcess, p);
        return result > 0; // greater than 0 = preempt, otherwise do not preempt
    }

    private static void printStats() {
        double avgCompletion = 0, avgWait = 0;
        int size = procsStats.size();
        for (int i = 0; i < size; i++) {
            Process p = procsStats.remove();
            // System.out.println("PROCESS: " + p.id + " - Completion Time = " + p.completionTime + "; Wait Time = " + p.waitTime);
            avgCompletion += p.completionTime;
            avgWait += p.waitTime;
        }
        System.out.println("Average Completion Time: " + (double)(avgCompletion/size));
        System.out.println("Average Wait Time: " + (double)(avgWait/size));
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

        eventHeap.add(new Event(Event.Type.ARRIVAL, nextProcessTime));
        nextProcessTime += readIn;
        // System.out.println("READ: " + readIn + "; NPT: " + nextProcessTime); // debugging
        int numCPUbursts = readByte() % maxCPUbursts + 1;

        Process p = new Process(numCPUbursts);
        p.id = ++currentProcess;

        for (int i = 0; i < numCPUbursts; i++) {
            p.cpuBurstSizes[i] = readByte() / 25.6;
            //System.out.print(p.cpuBurstSizes[i] + " ");
            p.totalRunTime += p.cpuBurstSizes[i];
        }
        //System.out.println();

        for (int i = 0; i < numCPUbursts - 1; i++) {
            p.ioBurstSizes[i] = readByte() / 25.6;
            //System.out.print(p.ioBurstSizes[i] + " ");
            p.totalRunTime += p.ioBurstSizes[i];
        }
        System.out.println("Process ID: " + p.id + "; Total Run Time: " + p.totalRunTime);


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
