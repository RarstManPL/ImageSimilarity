package me.rarstek.imagesimilarity.comparator.impl;

import me.rarstek.imagesimilarity.comparator.IComparator;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PixelComparator implements IComparator {

    private BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = newImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return newImage;
    }

    private BufferedImage[] matchSize(BufferedImage image1, BufferedImage image2) {
        int width = Math.max(image1.getWidth(), image2.getWidth());
        int height = Math.max(image1.getHeight(), image2.getHeight());

        return new BufferedImage[]{
                this.resizeImage(image1, width, height),
                this.resizeImage(image2, width, height)
        };
    }

    private int handleCompare(BufferedImage pattern, BufferedImage image) {
        BufferedImage[] resizedImages = this.matchSize(pattern, image);
        pattern = resizedImages[0];
        image = resizedImages[1];

        long diff = 0;
        for (int j = 0; j < pattern.getHeight(); j++) {
            for (int i = 0; i < pattern.getWidth(); i++) {
                int pixel1 = pattern.getRGB(i, j);
                int pixel2 = image.getRGB(i, j);

                Color color1 = new Color(pixel1, true);
                Color color2 = new Color(pixel2, true);

                diff += Math.abs(color1.getRed() - color2.getRed())
                        + Math.abs(color1.getGreen() - color2.getGreen())
                        + Math.abs(color1.getBlue() - color2.getBlue());
            }
        }

        return (int) -(Math.floor(diff / 255.0));
    }

    @Override
    public int compare(BufferedImage pattern, BufferedImage image) {
        return this.handleCompare(pattern, image);
    }

}
