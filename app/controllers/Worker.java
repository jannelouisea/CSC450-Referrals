package controllers;

import java.util.ArrayList;

public class Worker {
    public String name;
    public double[] needs;
    public double[] expertise;
    public ArrayList<KnownWorker> neighbors;
    public WorkerNetwork workerNetwork;

    public Worker(String name, double[] needs, double[] expertise) {
        this.name = name;
        this.needs = needs;
        this.expertise = expertise;
        this.neighbors = new ArrayList<>();
        workerNetwork = null;
    }

    public void setWorkerNetwork() {
        workerNetwork = WorkerNetwork.getInstance();
    }

    public String getName() {
        return name;
    }

    public void addNeighbor(KnownWorker kw) {
        neighbors.add(kw);
    }

    public void printInfo() {
        System.out.println("Name: " + name);
        System.out.print("Needs: ");
        for (double val: needs) {
            System.out.print("" + val + " ");
        }
        System.out.println();
        System.out.print("Expertise: ");
        for (double val: needs) {
            System.out.print("" + val + " ");
        }
        System.out.println();
        for (KnownWorker kw: neighbors) {
            kw.printInfo();
        }
    }
}
