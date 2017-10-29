package controllers;

import java.util.ArrayList;

public class WorkerActorProtocol {

    public static class AddNeighbor {
        public KnownWorker kw;

        public AddNeighbor(KnownWorker kw) {
            this.kw = kw;
        }
    }

    public static class PrintInfo { }

    public static class SetWorkerNetwork {}

    public static class Gen25Queries {}

    public static class DumpStates {}

    public static class ProcessQuery {
        public double[] query;
        public ArrayList<String> encountered;

        public ProcessQuery(double[] query, ArrayList<String> encountered) {
            this.query = query;
            this.encountered = encountered;
        }
    }
}
