package controllers;

import akka.actor.ActorRef;

import java.util.ArrayList;

public class WorkerNetwork {

    private static WorkerNetwork instance = null;
    private ArrayList<ActorRef> workerNetwork;

    private WorkerNetwork() {
        workerNetwork = new ArrayList<>();
    }

    public static WorkerNetwork getInstance() {
        if(instance == null) {
            instance = new WorkerNetwork();
        }
        return instance;
    }

    public void addWorker(ActorRef w) {
       workerNetwork.add(w);
    }

    public ArrayList<ActorRef> getNetwork() {
        return workerNetwork;
    }

    public ActorRef getWorkerFromName(String name) {
        for (ActorRef w: workerNetwork) {
            /*
            if (w.name.compareTo(name) == 0) {
                return w;
            }
            */
        }
        return null;    // TODO: This should be an exception instead
    }

    public void orderWorkers() {
        // Collections.sort(workerNetwork, (WorkerActor w1, WorkerActor w2) -> w1.name.compareTo(w2.name));
        // Can be condensed to
        // workerNetwork.sort(Comparator.comparing(WorkerActor::getName));
    }
}
