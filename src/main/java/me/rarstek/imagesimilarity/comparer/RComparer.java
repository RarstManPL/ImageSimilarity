package me.rarstek.imagesimilarity.comparer;

import me.rarstek.imagesimilarity.image.RImage;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RComparer {

    private RImage pattern;
    private Set<RImage> images = new HashSet<>();

    private IComparer comparer;

    private RComparer() {}

    protected RComparer setPatternImage(BufferedImage bufferedImage) {
        this.pattern = RImage.of(bufferedImage);
        return this;
    }

    protected RComparer setComparer(final IComparer comparer) {
        this.comparer = comparer;
        return this;
    }

    public RComparer addImage(BufferedImage bufferedImage) {
        this.images.add(RImage.of(bufferedImage));
        return this;
    }

    public RComparer addImage(BufferedImage... bufferedImages) {
        Arrays.stream(bufferedImages)
                .forEach(this::addImage);
        return this;
    }

    public Set<RImage> getImages() {
        return this.images;
    }

    public void compare() {
        BufferedImage[] patternImages = this.pattern.prepareSubImages();

        this.images.stream().filter(image -> !image.hasMetric()).forEach(image -> {
            BufferedImage[] images = image.prepareSubImages();
            int similarity = 0;

            for (int i = 0; i < 16; i++) {
                similarity += this.comparer.compare(images[i], patternImages[i]);
            }

            image.setMetric(similarity / 16);
        });
    }

    public static RComparer of(BufferedImage bufferedImage, IComparer comparer) {
        return new RComparer()
                .setPatternImage(bufferedImage)
                .setComparer(comparer);
    }

}
