import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.PriorityQueue;
import java.util.Queue;

class SchedSim {

    public static int maxProcesses; // cap on total processes for simulation
    public static int maxCPUbursts; // cap on total CPU bursts per process
    public static double time = 0; // current time in simulation, starting at zero

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

        String a = args[3].toLowerCase();

        switch (a) {
            case "fcfs":
            case "0":
                algorithm = Algorithm.FCFS;
                break;
            case "sjf":
            case "1":
                algorithm = Algorithm.SJF;
                break;
            case "srtf":
            case "2":
                algorithm = Algorithm.SRTF;
                break;
            case "rr":
            case "3":
                algorithm = Algorithm.RR;
                break;
            default:
                System.out.println("ERROR: Invalid Algorithm Input!");
                System.out.println("Please enter one of the following: 0 for FCFS; 1 for SJF; 2 for SRTF; or 3 for RR");
                break;
        }

        // give some feedback to the user
        System.out.println("Executing the \"" + algorithm.name() + "\" algorithm with at most " + maxProcesses
                + " processes and at most " + maxCPUbursts + " CPU bursts per process.");


        //---------------------------------------------------------------------//
        //---------------------------------SETUP-------------------------------//
        //---------------------------------------------------------------------//

        // you might want to open the binary input file here
        InputStream fr = new FileInputStream(f);

        // initialize data structures
        Queue<Event> eventHeap = new PriorityQueue<>();
        Process[] processes = new Process[maxProcesses];
        Queue<Event> ioQueue = new PriorityQueue<>();
        Queue<Event> readyQueue = new PriorityQueue<>();


        Event initialArrival = new Event(Event.Type.ARRIVAL, 0);
        eventHeap.add(initialArrival);

        //---------------------------------------------------------------------//
        //--------------------------GET EVENT INFORMATION----------------------//
        //---------------------------------------------------------------------//

        try {
            int nextProcessTime = ((int) (fr.read() / 10.0)) & 0xff;
            int numCPUBursts = (fr.read() % maxCPUbursts + 1) & 0xff;
            double cbt = 0;
            double ibt = 0;
            for (int i = 0; i < numCPUBursts; i++) {
                cbt += fr.read() / 25.6;
            }
            int cpuBurstTime = (int) cbt & 0xff;
            for (int i = 0; i < numCPUBursts - 1; i++) {
                ibt += fr.read() / 25.6;
            }
            int ioBurstTime = (int) ibt & 0xff;

            System.out.println(nextProcessTime + " " + numCPUBursts + " " + cpuBurstTime + " " + ioBurstTime);



		    /* DES loop */
            // see pseudocode in the assignment
            // all of your input reading occurs when processing the Arrival event

        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }


        // output statistics
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java SchedSim <filename> <maxProcesses> <maxCPUbursts> <algorithm>");
        System.exit(1);
    }
}
