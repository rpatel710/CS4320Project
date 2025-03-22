import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessScheduling { 
    // Create Process Object to hold process information
    static class Process {
        private int pid;
        private int arrivalTime;
        private int burstTime;
        private int priority;
        private int turnAroundTime;
        private int waitingTime;

        public Process(int pid, int arrivalTime, int burstTime, int priority) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
            this.turnAroundTime = 0;
            this.waitingTime = 0;
        }

        @Override
        public String toString() {
            return "P" + String.valueOf(pid);
        }
    }

    // Function to read Process from file
    public static List<Process> readProcesses(String filename) {
        // Create a list to store processes
        List<Process> processes = new ArrayList<>();

        // Read processes from file
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] data = line.split("\\s+");
                int pid = Integer.parseInt(data[0]);
                int arrivalTime = Integer.parseInt(data[1]);
                int burstTime = Integer.parseInt(data[2]);
                int priority = Integer.parseInt(data[3]);
                processes.add(new Process(pid, arrivalTime, burstTime, priority));
            }
        } catch (IOException e) {   // Handle file reading error
            e.printStackTrace();
        }
        return processes;       // Return list of all processes
    }

    // Function to implement First Come First Serve (FCFS) Scheduling
    public static List<Process> fcfs(List<Process> processes) {
        // Sort processes based on arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        // Initialize current time
        int currentTime = 0;

        // Simulate scheduling and calculate waiting time and turnaround time for each process
        for (Process p : processes) {
            // Update waiting time, turnaround time, and current time
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.waitingTime = currentTime - p.arrivalTime;
            currentTime += p.burstTime;
            p.turnAroundTime = currentTime - p.arrivalTime;
        }

        // Return list of completed processes
        return processes;
    }

    // Function to implement Shortest Job First (SJF) Scheduling
    public static List<Process> sjf(List<Process> processes) {
        // Sort processes based on arrival time and burst time
        processes.sort(Comparator.comparing((Process p) -> p.arrivalTime).thenComparing(p -> p.burstTime));

        // Initialize current time
        final int[] currentTime = {0};

        // Simulate scheduling and calculate waiting time and turnaround time for each process
        List<Process> completedProcesses = new ArrayList<>();       // List to store completed processes
        while (!processes.isEmpty()) {
            // Filter available processes at current time and sort them based on burst time
            List<Process> availableProcesses = processes.stream().filter(p -> p.arrivalTime <= currentTime[0]).collect(Collectors.toList());

            // If no process is available at current time, increment current time and continue
            if (availableProcesses.isEmpty()) {
                currentTime[0]++;
                continue;
            }

            // Select the shortest process and remove it from the list
            Process shortestProcess = availableProcesses.stream().min(Comparator.comparing(p -> p.burstTime)).orElse(null);
            processes.remove(shortestProcess);
            
            // Update waiting time, turnaround time, and current time
            if (currentTime[0] < shortestProcess.arrivalTime) {
                currentTime[0] = shortestProcess.arrivalTime;
            }

            // Calculate waiting time and turnaround time for the process
            shortestProcess.waitingTime = currentTime[0] - shortestProcess.arrivalTime;
            shortestProcess.turnAroundTime = shortestProcess.waitingTime + shortestProcess.burstTime;
            currentTime[0] += shortestProcess.burstTime;
            completedProcesses.add(shortestProcess);
        }

        // Return list of completed processes
        return completedProcesses;
    }

    // Function to print Gantt Chart and calculate average waiting time and turnaround time
    public static void ganttChart(List<Process> processes) {
        StringBuilder chart = new StringBuilder();
        int currentTime = 0;

        // Print Gantt Chart
        for (Process p : processes) {
            chart.append("| P").append(p.pid).append(" ");
            currentTime += p.burstTime;
        }
        chart.append("|\n");
        currentTime = 0;
        for (Process p : processes) {
            chart.append(String.format("%-5d", currentTime));
            currentTime += p.burstTime;
        }
        chart.append(String.format("%-5d", currentTime));
        System.out.println(chart.toString());

        // Initialize total waiting time and total turnaround time
        int totalWaitingTime = 0;
        int totalTurnAroundTime = 0;

        // Print waiting time and turnaround time for each process
        System.out.println("\nProcess\tWaiting Time\tTurnaround Time");
        for (Process p : processes) {
            System.out.println(p + "\t" + p.waitingTime + "\t\t" + p.turnAroundTime);
            // Update total waiting time and total turnaround time
            totalWaitingTime += p.waitingTime;
            totalTurnAroundTime += p.turnAroundTime;
        }

        // Calculate average waiting time and average turnaround time
        double avgWaitingTime = (double) totalWaitingTime / processes.size();
        double avgTurnAroundTime = (double) totalTurnAroundTime / processes.size();

        // Print average waiting time and average turnaround time
        System.out.println("\nAverage Waiting Time: " + avgWaitingTime);
        System.out.println("Average Turnaround Time: " + avgTurnAroundTime);
    }

    public static void main(String[] args) {
        // Read processes from file for FCFS scheduling and simulate scheduling
        List<Process> processes1 = readProcesses("lib/processes.txt");
        fcfs(processes1);
        System.out.println("First Come First Serve (FCFS) Scheduling");
        ganttChart(processes1);

        // Read processes from file for SJF scheduling and simulate scheduling
        List<Process> processes2 = readProcesses("lib/processes.txt");
        processes2 = sjf(processes2);
        System.out.println("\nShortest Job First (SJF) Scheduling");
        ganttChart(processes2);
    }
}
