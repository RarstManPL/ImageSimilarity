package me.rarstek.imagesimilarity.comparer;

public abstract class AComparable {

    private Integer metric = null;

    public int getMetric() {
        return this.metric == null ? 0 : this.metric;
    }

    public boolean hasMetric() {
        return this.metric != null;
    }

    public AComparable setMetric(int metric) {
        this.metric = metric;
        return this;
    }

}
