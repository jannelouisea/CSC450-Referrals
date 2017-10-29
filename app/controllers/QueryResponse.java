package controllers;

public class QueryResponse {
    public QueryResponseType type;
    public double[] answer;
    public String nameAnswered;
    public String refName;

    // Used in failures
    public QueryResponse(QueryResponseType type) {
        this.type = type;
    }

    // Used in answered
    public QueryResponse(QueryResponseType type, double[] answer, String nameAnswered) {
        this.type = type;
        this.answer = answer;
        this.nameAnswered = nameAnswered;
    }

    // Used in referrals
    public QueryResponse(QueryResponseType type, String refName) {
        this.type = type;
        this.refName = refName;
    }
}
