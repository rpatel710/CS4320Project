import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessScheduling { 
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

    public static List<Process> readProcesses(String filename) {
        List<Process> processes = new ArrayList<>();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processes;
    }

    public static List<Process> fcfs(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        for (Process p : processes) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.waitingTime = currentTime;
            currentTime += p.burstTime;
            p.turnAroundTime = currentTime;
        }
        return processes;
    }

    public static List<Process> sjf(List<Process> processes) {
        processes.sort(Comparator.comparing((Process p) -> p.arrivalTime).thenComparing(p -> p.burstTime));
        final int[] currentTime = {0};
        List<Process> completedProcesses = new ArrayList<>();
        while (!processes.isEmpty()) {
            List<Process> availableProcesses = processes.stream().filter(p -> p.arrivalTime <= currentTime[0]).collect(Collectors.toList());
            if (availableProcesses.isEmpty()) {
                currentTime[0]++;
                continue;
            }
            Process shortestProcess = availableProcesses.stream().min(Comparator.comparing(p -> p.burstTime)).orElse(null);
            processes.remove(shortestProcess);

            if (currentTime[0] < shortestProcess.arrivalTime) {
                currentTime[0] = shortestProcess.arrivalTime;
            }

            shortestProcess.waitingTime = currentTime[0] - shortestProcess.arrivalTime;
            shortestProcess.turnAroundTime = shortestProcess.waitingTime + shortestProcess.burstTime;
            currentTime[0] += shortestProcess.burstTime;
            completedProcesses.add(shortestProcess);
        }
        return completedProcesses;
    }

    public static void ganttChart(List<Process> processes) {
        StringBuilder chart = new StringBuilder();
        int currentTime = 0;

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

        int totalWaitingTime = 0;
        int totalTurnAroundTime = 0;

        System.out.println("\nProcess\tWaiting Time\tTurnaround Time");
        for (Process p : processes) {
            System.out.println(p + "\t" + p.waitingTime + "\t\t" + p.turnAroundTime);
            totalWaitingTime += p.waitingTime;
            totalTurnAroundTime += p.turnAroundTime;
        }

        double avgWaitingTime = (double) totalWaitingTime / processes.size();
        double avgTurnAroundTime = (double) totalTurnAroundTime / processes.size();

        System.out.println("\nAverage Waiting Time: " + avgWaitingTime);
        System.out.println("Average Turnaround Time: " + avgTurnAroundTime);
    }

    public static void main(String[] args) {
        List<Process> processes1 = readProcesses("lib/processes.txt");
        fcfs(processes1);
        System.out.println("First Come First Serve (FCFS) Scheduling");
        ganttChart(processes1);

        List<Process> processes2 = readProcesses("lib/processes.txt");
        processes2 = sjf(processes2);
        System.out.println("\nShortest Job First (SJF) Scheduling");
        ganttChart(processes2);
    }
}
