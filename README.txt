README

Daniel Laizer
ID# 27616321
Nathan Fuller
ID# 26506886
=======================================================================
=======================================================================
Outline:
	-setup program to take in input
	-created necessary data structures for each implementation 
	-created methods to create processes from the input file and parameters
	-setup switch statements for different algorithms
	-implemented FCFS
	-created printStats method to compute average wait and completion times
	-created method to handle preemption
	-implemented SJF with comparator of total runtimes of processes
	-implemented SRJF with comparator of remaining time until completion for processes
	-updated program to take in a time quantum as input
	-implemented RR
=======================================================================
=======================================================================
Design:
Device.java
	-chose to create a Device class which allows for easier-to-understand manipulation of the processes that are currently affiliated with either the CPU or I/O devices.
Process.java
	-added variables to the Process class in order to keep track of wait time
	-added variables to the Process class in order to keep track of how much work a Process has done at a time, this helps us determine the remaining time for a process in SRTF
Event.java
	-made Events Comparable in order to order to sort the events by their ‘time’ attribute when added to the event heap
SchedSim.java
	-we use a switch/case statement to determine how to handle the input. e.g., we switch on the desired algorithm and choose which method to call based on the algorithm
	-each algorithm is given its own method
	-each algorithm’s method determines how to handle events in its own way through use of a switch/case statement
	-FCFS uses a simple LinkedList. This algorithm attempts to strictly follow the steps outlined in the Lab 3 document.
	-SJF uses a Priority Queue with an anonymous Comparator function to sort based on the totalRunTime which is equal to the sum of the CPU bursts for the Process
	-SRTF uses a Priority Queue with an anonymous Comparator function to sort based on the remaining time till completion (found using Process.timeTillCompletion())
	-RR uses a simple LinkedList without any comparator, however we structure the algorithm to update the burst sizes in the event that the burst was shorter or longer than the quantum that was passed in by the user
	-Preemption (for SJF and SRTF) uses the comparator function of the readyQueue in order to determine whether a process should preempt the currently running process on the CPU. (see CPUisPreemptedBy() in SchedSim.java)
	-printStats simply computes the average wait/completion times of the processes that were all kept in memory in a separate data structure throughout the running of this program
=======================================================================
=======================================================================
Known Issues/Existing Problems:
	-SJF/SRTF fails on most input. This is most likely due to multiple CPU_DONE events being processed back-to-back leading to a NullPointerException. This Exception is most likely being caused when we free up the CPU and no other processes are able to get placed onto the CPU because the readyQueue is empty and we then attempt to reference CPU.currentProcess.
	-SJF/SRTF print out the same stats, which does not seem likely, however we are limited to small input and cannot rigorously test due to the issue mentioned immediately above. 
	-Overall statistics produced by this program (for any algorithm) may be incorrect as we do not have sample output to compare ours to.



