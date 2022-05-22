package me.rarstek.imagesimilarity.comparer;

import java.awt.image.BufferedImage;

public interface IComparer {

    int compare(BufferedImage pattern,
                BufferedImage image);

}
