package controllers;

import org.apache.commons.lang3.ArrayUtils;
import referral_helper.QueryGenerator;
import java.util.ArrayList;
import java.util.HashMap;

import akka.actor.*;
import static akka.pattern.Patterns.ask;
import controllers.WorkerActorProtocol.*;
import referral_helper.Utils;
import scala.concurrent.Future;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

public class WorkerActor extends UntypedAbstractActor {

    public final int NO_NEIGHBOR = -1;
    public final String START = "start";

    public String name;
    public double[] needs;
    public double[] expertise;
    public ArrayList<KnownWorker> neighbors;
    public ArrayList<KnownWorker> acquaintances;
    public WorkerNetwork workerNetwork;
    private Timeout processQueryTimeout;

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
        else if (msg instanceof DumpStates)
            dumpStates();
        else if (msg instanceof ProcessQuery)
            processQuery(((ProcessQuery) msg).query);
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
        processQueryTimeout = new Timeout(Duration.create(30, "seconds"));
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

            // Get neighbor's scores
            int bestNeighborIdx = chooseBestNeighbor(query);
            int secondBestNeighborIdx = getSecondBestNeighbor(bestNeighborIdx);

            if (bestNeighborIdx == NO_NEIGHBOR) { }
            else {
                // ask best neighbor and update referMap
                KnownWorker bestNeighbor = this.neighbors.get(bestNeighborIdx);
                ActorRef bestNeighborRef = workerNetwork.getWorkerFromName(bestNeighbor.name);
                // askInitQuery
                askQuery(bestNeighborRef, bestNeighbor.name, query);
            }

        }
        sender().tell("DONE", getSelf());
    }

    public void askInitQuery(double[] query) {
        HashMap<String, String> referMap = new HashMap<>();

        // check if the actorRef name in the encountered...
        // if so do not send an ask
        // else add to encountered and then ask
    }

    public void askQuery(ActorRef worker, String name, double[] query) {
        /*
            Future<Object> processQueryFuture = ask(worker, new ProcessQuery(), processQueryTimeout);
            // Send a SEND message with drools here

            // Update the referMap
            if (referMap.isEmpty()) {
                referMap.put(START, name);
            } else {
                referMap.put(this.name, name);
            }

            try {
                QueryResponse response = (QueryResponse) Await.result(processQueryFuture, processQueryTimeout.duration());
                // send a RECEIVE message with drools here
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        */
    }

    public void processQuery(ProcessQuery pq) {
        QueryResponse response = null;
        // Check self
        // Will assume that is actor is asked to process query then they are not in the encountered list
        pq.encountered.add(this.name);
        if (Utils.isExpertiseMatch(this.expertise, pq.query)) {
            double[] answer = Utils.genAnswer(this.expertise, pq.query);
            response = new QueryResponse(QueryResponseType.SUCCESS, answer, this.name);
        } else {
            // Choose neighbors
            int bestNeighborIdx = chooseBestNeighbor(pq.query);
            KnownWorker bestNeighbor = this.neighbors.get(bestNeighborIdx);
            int secondBestNeighborIdx = getSecondBestNeighbor(bestNeighborIdx);
            KnownWorker secondBestNeighbor = this.neighbors.get(secondBestNeighborIdx);

            if (bestNeighborIdx > NO_NEIGHBOR && !pq.encountered.contains(bestNeighbor.name)) {
                if (Utils.isExpertiseMatch(bestNeighbor.sociability, pq.query) || Utils.isExpertiseMatch(bestNeighbor.expertise, pq.query))
                    response = new QueryResponse(QueryResponseType.REFER, bestNeighbor.name);
            } else if (secondBestNeighborIdx > NO_NEIGHBOR && !pq.encountered.contains(secondBestNeighbor.name)) {
                if (Utils.isExpertiseMatch(secondBestNeighbor.sociability, pq.query) || Utils.isExpertiseMatch(secondBestNeighbor.expertise, pq.query))
                    response = new QueryResponse(QueryResponseType.REFER, secondBestNeighbor.name);
            }

            // If no neighbors can respond
            if (response == null) {
                response = new QueryResponse(QueryResponseType.REFUSE);
            }
        }

        sender().tell(response, getSelf());

    }

    public int chooseBestNeighbor(double[] query) {
        double w = Utils.getWeightOfSociability();
        double bestScore = -1.0;
        int bestNeighbor = NO_NEIGHBOR;

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
            return NO_NEIGHBOR;
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
