package controllers;

import java.util.ArrayList;

public class WorkerBean {

    public String name;
    public double[] needs;
    public double[] expertise;
    public ArrayList<KnownWorker> neighbors;
    public ArrayList<KnownWorker> acquaintances;

    public WorkerBean(String name, double[] needs, double[] expertise) {
        this.name = name;
        this.needs = needs;
        this.expertise = expertise;
        this.neighbors = new ArrayList<>();
        this.acquaintances = new ArrayList<>();
    }

    public void addKnownWorker(KnownWorker kw) {
        neighbors.add(kw);
        acquaintances.add(kw);
    }

    public String getName() {
        return name;
    }
}
