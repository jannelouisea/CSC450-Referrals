package controllers;

import java.util.ArrayList;

public class WorkerStates {

    public ArrayList<KnownWorker> neighbors;
    public ArrayList<KnownWorker> acquaintances;

    public WorkerStates(ArrayList<KnownWorker> neighbors, ArrayList<KnownWorker> acquaintances) {
        this.neighbors = neighbors;
        this.acquaintances = acquaintances;
    }
}
