package controllers;

import referral_helper.QueryGenerator;
import java.util.ArrayList;
import akka.actor.*;
import static akka.pattern.Patterns.ask;
import controllers.WorkerActorProtocol.*;
import referral_helper.Utils;

public class WorkerActor extends UntypedAbstractActor {

    public String name;
    public double[] needs;
    public double[] expertise;
    public ArrayList<KnownWorker> neighbors;
    public ArrayList<KnownWorker> acquaintances;
    public WorkerNetwork workerNetwork;

    public static Props getProps(WorkerBean wb) {
        return Props.create(WorkerActor.class, () -> new WorkerActor(wb));
    }

    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof AddNeighbor)
            reactionToAddNeighbor((AddNeighbor) msg);
        else if (msg instanceof PrintInfo)
            reactionToPrintInfo();
        else if (msg instanceof SetWorkerNetwork)
            setWorkerNetwork();
        else if (msg instanceof Gen25Queries)
            gen25Queries();
        else if (msg instanceof  DumpStates)
            dumpStates();
        else
            unhandled(msg);
    }

    public WorkerActor(WorkerBean wb) {
        this.name = wb.name;
        this.needs = wb.needs;
        this.expertise = wb.expertise;
        this.neighbors = wb.neighbors;
        assert this.neighbors.size() == Utils.getMaxNumOfNeighbors();
        this.acquaintances = wb.acquaintances;
        workerNetwork = null;
    }

    public void setWorkerNetwork() {
        workerNetwork = WorkerNetwork.getInstance();
    }

    public String getName() {
        return name;
    }

    public void reactionToAddNeighbor(WorkerActorProtocol.AddNeighbor msg) {
        neighbors.add(msg.kw);
    }

    public void gen25Queries() {
        int numOfQueries = QueryGenerator.getInstance().NUM_OF_QUERIES;
        System.out.println("===================================== " + this.name);
        for (int i = 0; i < numOfQueries; i++) {
            double query[] = QueryGenerator.getInstance().genQuery(this.name, this.needs);
            askQuery(query);
        }
        sender().tell("DONE", getSelf());
    }

    public void askQuery(double[] query) {
        // determine which neighbor to ask
        int bestNeighbor = chooseBestNeighbor(query);
        int secondBestNeighbor = getSecondBestNeighbor(bestNeighbor);
        System.out.println("BestNeighbor: " + bestNeighbor);
        System.out.println("SecondBestNeighbor: " + secondBestNeighbor);
    }

    public int chooseBestNeighbor(double[] query) {
        double w = Utils.getWeightOfSociability();
        double bestScore = -1.0;
        int bestNeighbor = -1;

        int i = 0;
        for(KnownWorker kw: this.neighbors) {
            double score = (w * innerProduct(query, kw.sociability)) + (w * innerProduct(query, kw.expertise));

            if (score > bestScore) {
                bestScore = score;
                bestNeighbor = i;
            }

            i++;
        }

        return bestNeighbor;
    }

    public int getSecondBestNeighbor(int i) {
        if (this.neighbors.size() == 2)
            return (i == 0) ? 1 : 0;
        else
            return -1;
    }

    public double innerProduct(double[] a1, double[] a2) {
        double innerProduct = 0.0;
        assert a1.length == a2.length;
        for (int i = 0; i < a1.length; i++) {
            innerProduct += (a1[i] * a2[i]);
        }
        return innerProduct;
    }

    public void dumpStates() {
        sender().tell(new WorkerStates(this.acquaintances, this.neighbors), getSelf());
    }

    public void reactionToPrintInfo() {
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
