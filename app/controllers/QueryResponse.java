package controllers;

public class QueryResponse {
    public QueryResponseType type;
    public double[] answer;
    public String refName;

    public QueryResponse(QueryResponseType type) {
        this.type = type;
    }

    public QueryResponse(QueryResponseType type, double[] answer) {
        this.type = type;
        this.answer = answer;
    }

    public QueryResponse(QueryResponseType type, String refName) {
        this.type = type;
        this.refName = refName;
    }
}
