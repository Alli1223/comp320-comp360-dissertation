package AsimovConform.SubAgents.BFS;

class BFSQueueAvg {
    private double rating;
    private int counter;

    BFSQueueAvg() {
        resetAvg();
    }

    void addAvgRating(double r) {
        rating += r;
        counter++;
    }

    void subAvgRating(double r) {
        rating -= r;
        counter--;
    }

    double calcAvgRating() {
        return rating / counter;
    }

    void resetAvg() {
        rating = 0;
        counter = 0;
    }
}
