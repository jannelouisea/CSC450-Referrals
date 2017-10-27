package controllers;

import java.util.ArrayList;
import java.util.Comparator;

public class WorkerNetwork {

    private static WorkerNetwork instance = null;
    private ArrayList<Worker> workerNetwork;

    private WorkerNetwork() {
        workerNetwork = new ArrayList<>();
    }

    public static WorkerNetwork getInstance() {
        if(instance == null) {
            instance = new WorkerNetwork();
        }
        return instance;
    }

    public void addWorker(Worker w) {
       workerNetwork.add(w);
    }

    public ArrayList<Worker> getNetwork() {
        return workerNetwork;
    }

    public Worker getWorkerFromName(String name) {
        for (Worker w: workerNetwork) {
            if (w.name.compareTo(name) == 0) {
                return w;
            }
        }
        return null;    // TODO: This should be an exception instead
    }

    public void orderWorkers() {
        // Collections.sort(workerNetwork, (Worker w1, Worker w2) -> w1.name.compareTo(w2.name));
        // Can be condensed to
        workerNetwork.sort(Comparator.comparing(Worker::getName));
    }
}
