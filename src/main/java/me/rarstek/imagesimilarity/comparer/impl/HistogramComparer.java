package me.rarstek.imagesimilarity.comparer.impl;

import me.rarstek.imagesimilarity.comparer.IComparer;

import java.awt.image.BufferedImage;

public class HistogramComparer implements IComparer {

    private int redBins = 4;
    private int greenBins = 4;
    private int blueBins = 4;

    public HistogramComparer() {}

    public HistogramComparer(int redBins, int greenBins, int blueBins) {
        this.redBins = redBins;
        this.greenBins = greenBins;
        this.blueBins = blueBins;
    }

    private float[] filter(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int[] inPixels = new int[width * height];
        float[] histogramData = new float[this.redBins * this.greenBins * this.blueBins];

        this.getRGB(bufferedImage, 0, 0, width, height, inPixels);

        int index = 0;
        int redIdx = 0, greenIdx = 0, blueIdx = 0;
        int singleIndex = 0;
        float total = 0;

        for (int row = 0; row < height; row++) {
            int tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;

                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;

                redIdx = (int) this.getBinIndex(this.redBins, tr, 255);
                greenIdx = (int) this.getBinIndex(this.greenBins, tg, 255);
                blueIdx = (int) this.getBinIndex(this.blueBins, tb, 255);

                singleIndex = redIdx + greenIdx * this.redBins + blueIdx * this.redBins * this.greenBins;

                histogramData[singleIndex]++;
                total++;
            }
        }

        for (int i = 0; i < histogramData.length; i++) {
            histogramData[i] = histogramData[i] / total;
        }

        return histogramData;
    }

    private float getBinIndex(int binCount, int color, int colorMaxValue) {
        float binIndex = ((float) color / (float) colorMaxValue) * (float) binCount;

        if (binIndex >= binCount)
            binIndex = binCount - 1;

        return binIndex;
    }

    private int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();

        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);

        return image.getRGB(x, y, width, height, pixels, 0, width);
    }


    private double calcSimilarity(float[] sourceData, float[] candidateData) {
        double[] mixedData = new double[sourceData.length];
        for (int i = 0; i < sourceData.length; i++) {
            mixedData[i] = Math.sqrt(sourceData[i] * candidateData[i]);
        }

        double similarity = 0;
        for (double mixedDatum : mixedData) {
            similarity += mixedDatum;
        }

        return similarity;
    }


    @Override
    public int compare(BufferedImage pattern, BufferedImage image) {
        return (int) Math.round(this.calcSimilarity(this.filter(pattern), this.filter(image)) * 100);
    }

}
