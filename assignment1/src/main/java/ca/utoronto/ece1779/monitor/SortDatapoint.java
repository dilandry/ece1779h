package ca.utoronto.ece1779.monitor;

import com.amazonaws.services.cloudwatch.model.Datapoint;

import java.util.Comparator;

public class SortDatapoint implements Comparator<Datapoint> {
    public int compare(Datapoint a, Datapoint b) {
        return a.getTimestamp().compareTo(b.getTimestamp());
    }
}
