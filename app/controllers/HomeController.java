package controllers;

import akka.actor.ActorRef;
import play.Environment;
import org.kie.api.runtime.KieContainer;
import org.kie.api.KieServices;
import play.mvc.*;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.kie.api.runtime.KieSession;

import javax.inject.Inject;
import akka.actor.ActorSystem;
import static akka.pattern.Patterns.ask;
import controllers.WorkerActorProtocol.*;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scala.concurrent.Future;
import java.util.Comparator;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final String SUCCESS_STATUS = "success";
    private final String ERROR_STATUS = "error";

    private final int ITEM_COUNT = 4;
    public final KieSession kieSession;

    private KieContainer kc;
    private ActorSystem referrals;
    private WorkerNetwork workerNetwork;

    private Timeout printInfoTimeout;
    private Timeout gen25QueriesTimeout;

    @Inject
    public HomeController(Environment environment) {
       // initiate drools
        KieServices kieServices = new KieServicesImpl();
        KieContainer kc = kieServices.getKieClasspathContainer(environment.classLoader());
        kieSession = kc.newKieSession("HelloWorldKS");

        // initiate actor system
        referrals = ActorSystem.create("referrals");

        workerNetwork = WorkerNetwork.getInstance();
        printInfoTimeout = new Timeout(Duration.create(2, "seconds"));
        gen25QueriesTimeout = new Timeout(Duration.create(30, "seconds"));
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
     *
     * @param response
     * @return
     */
    private Result createResult(ObjectNode response) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.out);
            return ok(response);
        }
    }

    public Result postGraph() {
        ObjectNode response = Json.newObject();

        JsonNode json = request().body().asJson();
        if (json == null) {
            response.put("status", ERROR_STATUS);
            response.put("message", "Could not get body of POST request");
            return createResult(response);
        }

        processJsonGraph(json);
        ArrayList<ActorRef> network = workerNetwork.getNetwork();
        for(ActorRef w: network) {
            w.tell(new SetWorkerNetwork(), null);
            Future<Object> gen25QueriesFuture = ask(w, new Gen25Queries(), gen25QueriesTimeout);
            try {
                String result = (String) Await.result(gen25QueriesFuture, gen25QueriesTimeout.duration());
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        response.put("status", SUCCESS_STATUS);
        return createResult(response);
    }

    private void processJsonGraph(JsonNode root) {
        ArrayList<WorkerBean> workers = new ArrayList<>();

        for (JsonNode item: root) {
            String name = item.findPath("name").textValue();
            double needs[] = getDoubleArrayFromJsonNode(item, "needs");
            double expertise[] = getDoubleArrayFromJsonNode(item, "expertise");

            WorkerBean wb = new WorkerBean(name, needs, expertise);

            JsonNode neighborsArray = item.findPath("neighbors");
            for (JsonNode neighbor: neighborsArray) {
                String n_name = neighbor.findPath("name").textValue();
                double n_sociability[] = getDoubleArrayFromJsonNode(neighbor, "sociability");
                double n_expertise[] = getDoubleArrayFromJsonNode(neighbor, "expertise");

                wb.addKnownWorker(new KnownWorker(n_name, n_expertise, n_sociability));
            }
            workers.add(wb);
        }

        workers.sort(Comparator.comparing(WorkerBean::getName));

        for(WorkerBean worker: workers) {
            ActorRef workerRef = referrals.actorOf(WorkerActor.getProps(worker), worker.name);
            workerNetwork.addWorker(worker.name, workerRef);
        }

    }

    private double[] getDoubleArrayFromJsonNode (JsonNode node, String fieldName) {
        double[] array = new double[ITEM_COUNT];
        JsonNode jsonArray = node.findPath(fieldName);
        if (jsonArray.isArray()) {
            int idx = 0;
            for(JsonNode val: jsonArray) {
                array[idx] = val.asDouble();
                idx++;
                if (idx == ITEM_COUNT)
                    break;
            }
        }
        return array;
    }

    public Result processQueryForWorker(String workerName, String query) {
        return ok("processQuery: " + workerName + " " + query + "\n");
    }

    /**
     *
     * @param name
     * @return
     */
    public Result getStatesForWorker(String name) {
        ObjectNode response = Json.newObject();
        ActorRef workerRef = workerNetwork.getWorkerFromName(name);

        if (workerRef == null) {
            response.put("status", ERROR_STATUS);
            response.put("message", "Actor " + name + " does not exist.");
        } else {
            Future<Object> dumpStatesFuture = ask(workerRef, new DumpStates(), printInfoTimeout);
            try {
                WorkerStates result = (WorkerStates) Await.result(dumpStatesFuture, gen25QueriesTimeout.duration());
                response.put("status", SUCCESS_STATUS);

                // print out neighbors
                ArrayList<ObjectNode> neighbors = new ArrayList<>();
                for (KnownWorker kw: result.neighbors) {
                    ObjectNode n = Json.newObject();
                    n.put("name", kw.name);
                    n.putPOJO("expertise", kw.expertise);
                    n.putPOJO("sociability", kw.sociability);
                    neighbors.add(n);
                }
                response.putArray("neighbors").addAll(neighbors);

                // print out acquaintances
                ArrayList<ObjectNode> acquaintances = new ArrayList<>();
                for (KnownWorker kw: result.acquaintances) {
                    ObjectNode n = Json.newObject();
                    n.put("name", kw.name);
                    n.putPOJO("expertise", kw.expertise);
                    n.putPOJO("sociability", kw.sociability);
                    acquaintances.add(n);
                }
                response.putArray("acquaintances").addAll(acquaintances);

            } catch (Exception e) {
                response.put("status", ERROR_STATUS);
                response.put("message", "Unable to dump states for actor " + name + ".");
                // e.printStackTrace(System.out);
            }
        }

        return createResult(response);
    }

    public Result getMessages() {
        return ok("getMessages\n");
    }
}
