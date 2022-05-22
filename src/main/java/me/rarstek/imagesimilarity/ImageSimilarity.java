package me.rarstek.imagesimilarity;

import me.rarstek.imagesimilarity.comparator.AComparable;
import me.rarstek.imagesimilarity.comparator.IComparator;
import me.rarstek.imagesimilarity.comparator.RComparator;
import me.rarstek.imagesimilarity.comparator.impl.HistogramComparator;
import me.rarstek.imagesimilarity.comparator.impl.PixelComparator;
import me.rarstek.imagesimilarity.image.RImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ImageSimilarity {

    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose one comparator type (HISTOGRAM, PIXEL): ");
        final Optional<IComparator> comparatorOptional =
                ComparatorType.getByName(bufferedReader.readLine());

        if (!comparatorOptional.isPresent()) {
            System.out.println("Given comparator doesn't exist");
            return;
        }

        System.out.println("Path to pattern image: ");
        final File patternImageFile = new File(bufferedReader.readLine());
        final BufferedImage patternImage;

        try {
            patternImage = ImageIO.read(patternImageFile);
            patternImage.toString();
        } catch (Exception ignored) {
            System.out.println("Given pattern image is not image");
            return;
        }

        System.out.println("Path to images: ");
        final Path images = Paths.get(bufferedReader.readLine());

        if (!images.toFile().isDirectory()) {
            System.out.println("Given images path must be a directory");
            return;
        }

        System.out.println("Path to result: ");
        final String resultPath = bufferedReader.readLine();
        final File resultFile = new File(resultPath);

        if (!resultFile.isDirectory()) {
            System.out.println("Given result path must be a directory");
            return;
        }

        long start = System.currentTimeMillis();

        if (resultFile.listFiles() != null)
            Arrays.stream(Objects.requireNonNull(resultFile.listFiles()))
                    .forEach(File::delete);

        final RComparator rComparator = RComparator.of(patternImage, comparatorOptional.get());

        Files.walk(images)
                .collect(Collectors.toList())
                .parallelStream()
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(file -> {
                    try {
                        final BufferedImage bufferedImage = ImageIO.read(file);
                        bufferedImage.toString();
                        rComparator.addImage(bufferedImage);
                    } catch (Exception ignored) {}
                });

        rComparator.compare();

        while (!rComparator.isReady()) {}

        int index = 0;
        for (RImage image : new ArrayList<>(rComparator.getImages())
                .parallelStream()
                .sorted(Comparator.comparingInt(AComparable::getMetric).reversed())
                .collect(Collectors.toList())) {
            final File outputFile = new File(
                    resultPath + File.separator + "image" + index + ".jpg");
            ImageIO.write(image.getImage(), "jpg", outputFile);
            index++;
        }

        System.out.println("Ready! It tooks " + (System.currentTimeMillis() - start) + "ms.");
    }

    enum ComparatorType {
        HISTOGRAM(HistogramComparator.class),
        PIXEL(PixelComparator.class);

        final Class<? extends IComparator> comparatorClass;

        ComparatorType(Class<? extends IComparator> comparatorClass) {
            this.comparatorClass = comparatorClass;
        }

        public static Optional<IComparator> getByName(String name) {
            try {
                ComparatorType comparatorType = valueOf(name.toUpperCase());
                return Optional.of(comparatorType.comparatorClass.newInstance());

            } catch (IllegalArgumentException
                    | InstantiationException
                    | IllegalAccessException ignored) {}

            return Optional.empty();
        }
    }

}
