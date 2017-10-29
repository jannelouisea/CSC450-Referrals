package controllers;

import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.HashMap;

public class WorkerNetwork {

    private static WorkerNetwork instance = null;
    private ArrayList<ActorRef> workerNetwork;
    private HashMap<String, ActorRef> workerMap;

    private WorkerNetwork() {
        workerNetwork = new ArrayList<>();
        workerMap = new HashMap<>();
    }

    public static WorkerNetwork getInstance() {
        if(instance == null) {
            instance = new WorkerNetwork();
        }
        return instance;
    }

    public void addWorker(String name, ActorRef w) {
       workerNetwork.add(w);
       workerMap.put(name, w);
    }

    public ArrayList<ActorRef> getNetwork() {
        return workerNetwork;
    }

    public ActorRef getWorkerFromName(String name) {
        return workerMap.get(name);
    }

}
