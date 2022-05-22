package me.rarstek.imagesimilarity.comparator;

import com.google.common.collect.Iterables;
import me.rarstek.imagesimilarity.image.RImage;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RComparator {

    private RImage pattern;
    private final Set<RImage> images = new HashSet<>();

    private boolean ready = false;

    private IComparator comparator;

    private RComparator() {}

    protected RComparator setPatternImage(BufferedImage bufferedImage) {
        this.pattern = RImage.of(bufferedImage);
        return this;
    }

    protected RComparator setComparator(final IComparator comparator) {
        this.comparator = comparator;
        return this;
    }

    public RComparator addImage(BufferedImage bufferedImage) {
        this.images.add(RImage.of(bufferedImage));
        return this;
    }

    public boolean isReady() {
        return ready;
    }

    public Set<RImage> getImages() {
        return this.images;
    }

    public void compare() {
        this.ready = false;

        BufferedImage[] patternImages = this.pattern.prepareSubImages();
        ExecutorService threadPool = Executors.newFixedThreadPool(16);

        for (List<RImage> task : Iterables.partition(this.images, 4)) {
            threadPool.submit(() ->
                    task.stream().filter(image -> !image.hasMetric()).forEach(image -> {
                        BufferedImage[] images = image.prepareSubImages();
                        int similarity = 0;

                        for (int i = 0; i < 16; i++)
                            similarity += comparator.compare(images[i], patternImages[i]);

                        image.setMetric(similarity / 16);
                    })
            );
        }

        threadPool.shutdown();

        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}

        this.ready = true;
    }

    public static RComparator of(BufferedImage bufferedImage, IComparator comparator) {
        return new RComparator()
                .setPatternImage(bufferedImage)
                .setComparator(comparator);
    }

}
