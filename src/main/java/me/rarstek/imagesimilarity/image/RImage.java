package me.rarstek.imagesimilarity.image;

import me.rarstek.imagesimilarity.comparator.AComparable;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RImage extends AComparable {

    private BufferedImage image;

    private RImage() {}

    protected RImage setImage(BufferedImage bufferedImage) {
        this.image = bufferedImage;
        return this;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage[] prepareSubImages() {
        try {
            this.image.getHeight();
        } catch (Exception exception) {
            return null;
        }

        BufferedImage[] subImages = new BufferedImage[16];

        int subWidth = this.image.getWidth() / 4;
        int subHeight = this.image.getHeight() / 4;

        int currentImage = 0;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                BufferedImage subImage = new BufferedImage(subWidth, subHeight, this.image.getType());
                Graphics2D imageCreator = subImage.createGraphics();
                imageCreator.drawImage(this.image, 0, 0,
                        subWidth, subHeight,
                        subWidth * j, subHeight * i,
                        subWidth * j + subWidth,
                        subHeight * i + subHeight,
                        null);
                subImages[currentImage++] = subImage;
            }
        }

        return subImages;
    }

    public static RImage of(BufferedImage bufferedImage) {
        return new RImage()
                .setImage(bufferedImage);
    }

}
