package me.rarstek.imagesimilarity.comparator.impl;

import me.rarstek.imagesimilarity.comparator.IComparator;

import java.awt.image.BufferedImage;

public class HistogramComparator implements IComparator {

    private int redBins = 4;
    private int greenBins = 4;
    private int blueBins = 4;

    private float[] filter(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int[] inPixels = new int[width * height];
        float[] histogramData = new float[redBins * greenBins * blueBins];

        inPixels = this.getRGB(bufferedImage, width, height, inPixels);

        int index, redIdx, greenIdx, blueIdx, singleIndex;
        float total = 0;

        for (int row = 0; row < height; row++) {
            int tr, tg, tb;

            for (int col = 0; col < width; col++) {
                index = row * width + col;

                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;

                redIdx = (int) this.getBinIndex(redBins, tr);
                greenIdx = (int) this.getBinIndex(greenBins, tg);
                blueIdx = (int) this.getBinIndex(blueBins, tb);

                singleIndex = redIdx + greenIdx * redBins + blueIdx * redBins * greenBins;

                histogramData[singleIndex]++;
                total++;
            }
        }

        for (int i = 0; i < histogramData.length; i++)
            histogramData[i] = histogramData[i] / total;

        return histogramData;
    }

    private float getBinIndex(int binCount, int color) {
        float binIndex = ((float) color / 255) * (float) binCount;

        if (binIndex >= binCount)
            binIndex = binCount - 1;

        return binIndex;
    }

    private int[] getRGB(BufferedImage image, int width, int height, int[] pixels) {
        int type = image.getType();

        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            return (int[]) image.getRaster().getDataElements(0, 0, width, height, pixels);

        return image.getRGB(0, 0, width, height, pixels, 0, width);
    }

    private double calcSimilarity(float[] sourceData, float[] candidateData) {
        double[] mixedData = new double[sourceData.length];
        for (int i = 0; i < sourceData.length; i++)
            mixedData[i] = Math.sqrt(sourceData[i] * candidateData[i]);

        double similarity = 0;
        for (double mixedDatum : mixedData)
            similarity += mixedDatum;

        return similarity;
    }

    @Override
    public int compare(BufferedImage pattern, BufferedImage image) {
        return (int) Math.round(this.calcSimilarity(this.filter(pattern), this.filter(image)) * 100);
    }

}
