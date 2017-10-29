package controllers;

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
}
