package de.stuttgart.uni.citeprocleak;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CiteprocLeak {
    private static final int REPETITIONS = 1000;
    private static final int MEAN_SIZE = 50;


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CSLItemData item;
        item = new CSLItemDataBuilder()
                .type(CSLType.WEBPAGE)
                .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
                .author("Michel", "Krämer")
                .issued(2016, 11, 20)
                .URL("http://michel-kraemer.github.io/citeproc-java/")
                .accessed(2018, 4, 12)
                .build();

        try {
            System.out.println("Press enter to begin");
            scanner.nextLine();
            System.out.println("Entry\tTime (ms)\tMean last " + MEAN_SIZE + " (ms)");
            long lastsNanos[] = new long[MEAN_SIZE];
            Arrays.fill(lastsNanos, System.nanoTime());
            for (int i = 1; i <= REPETITIONS; i++) {
                CSL.makeAdhocBibliography("ieee", item).makeString();
                printBenchmark(lastsNanos, i);
            }

            // AG 2018-04-13: Don't know why, but the first rendering after this loop takes again a while when using Nashorn
            for (int j=1; j < 5; j++) {
                System.out.println("Press enter to continue");
                Random seed = new Random();
                scanner.nextLine();
                Arrays.fill(lastsNanos, System.nanoTime());
                for (int i = 1; i <= REPETITIONS; i++) {
                    CSLItemDataBuilder itemDataBuilder = new CSLItemDataBuilder();
                    if (seed.nextBoolean()) {
                        itemDataBuilder.type(CSLType.WEBPAGE);
                    }
                    if (seed.nextBoolean()) {
                        itemDataBuilder.title("citeproc-java: A Citation Style Language (CSL) processor for Java");
                    }
                    itemDataBuilder.author("Michel" + seed.nextLong(), "Krämer");
                    if (seed.nextBoolean()) {
                        itemDataBuilder.issued(2016, 11, 20);
                    }
                    if (seed.nextBoolean()) {
                        itemDataBuilder.URL("http://michel-kraemer.github.io/citeproc-java/");
                    }
                    if (seed.nextBoolean()) {
                        itemDataBuilder.accessed(2018, 4, 12);
                    }
                    item = itemDataBuilder.build();
                    CSL.makeAdhocBibliography("ieee", item).makeString();
                    printBenchmark(lastsNanos, i);
                }
            }
            System.out.println("Press enter to end");
            scanner.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printBenchmark(long[] lastsNanos, int i) {
        final int population_size = lastsNanos.length;
        lastsNanos[i% population_size] = System.nanoTime();
        final long currentMillis = TimeUnit.NANOSECONDS.toMillis(lastsNanos[i% population_size] - lastsNanos[(i-1)% population_size]);
        final long currentMeanSize = i< population_size ?i: population_size;
        final long currentMeanMillis = TimeUnit.NANOSECONDS.toMillis((lastsNanos[i% population_size] - lastsNanos[(i+1)% population_size]) / currentMeanSize);
        System.out.printf("%04d\t% 9d\t%d%n", i, currentMillis, currentMeanMillis);
    }
}
