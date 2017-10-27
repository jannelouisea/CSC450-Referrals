package controllers;

public class KnownWorker {

    public String name;
    public double[] expertise;
    public double[] sociability;

    public KnownWorker (String name, double[] expertise, double[] sociability) {
        this.name = name;
        this.expertise = expertise;
        this.sociability = sociability;
    }

    public void printInfo() {
        System.out.println("Name: " + name);
        System.out.print("Expertise: ");
        for (double val: expertise) {
            System.out.print("" + val + " ");
        }
        System.out.println();
        System.out.print("Sociability: ");
        for (double val: sociability) {
            System.out.print("" + val + " ");
        }
        System.out.println();
    }
}
