package com.manorllc.beerRate.model;

/**
 * Pojo representing statistics for a beer
 *
 */
public class Stats {

    private int count = 0;
    private int min = 0;
    private int max = 0;
    private double mean = 0;
    private double median = 0;
    // TODO: Consider adding a mode

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }
}
