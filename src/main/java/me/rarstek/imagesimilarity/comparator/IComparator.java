package me.rarstek.imagesimilarity.comparator;

import java.awt.image.BufferedImage;

public interface IComparator {

    int compare(BufferedImage pattern,
                BufferedImage image);

}
