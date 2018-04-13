package de.ustutt.citeprocleak;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CiteprocLeakSerial {
    private static final int REPETITIONS = 300;
    private static final int MEAN_SIZE = 100;


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Benchmark: same item");
        System.out.println("Press enter to begin");
        scanner.nextLine();
        System.out.println("Entry\tTime (ms)\tMean last " + MEAN_SIZE + " (ms)");

        benchmarkSameItemUsingAdhoc();

//         AG 2018-04-13: Don't know why, but the first rendering after this loop takes again a while when using Nashorn
        for (int j = 1; j <= 3; j++) {
            System.out.println("Benchmark: random item");
            System.out.println("Press enter to continue");
            scanner.nextLine();
            System.out.println("Entry\tTime (ms)\tMean last " + MEAN_SIZE + " (ms)");
            benchmarkRandomItemUsingAdhoc();
        }

        System.out.println("Benchmark: use ItemDataProvider");
        System.out.println("Press enter to continue");
        scanner.nextLine();
        System.out.println("Entry\tTime (ms)\tMean last " + MEAN_SIZE + " (ms)");
        benchmarkSameItemUsingItemDataProviderRecreatingCSL();

        System.out.println("Benchmark: use ItemDataProvider reusing CSL");
        System.out.println("Press enter to continue");
        scanner.nextLine();
        System.out.println("Entry\tTime (ms)\tMean last " + MEAN_SIZE + " (ms)");
        benchmarkSameItemUsingItemDataProviderReuseCSL();
        System.out.println("Press enter to end");
        scanner.nextLine();
    }

    private static void benchmarkSameItemUsingAdhoc() throws IOException {
        CSLItemData item;
        item = new CSLItemDataBuilder()
                .type(CSLType.WEBPAGE)
                .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
                .author("Michel", "Krämer")
                .issued(2016, 11, 20)
                .URL("http://michel-kraemer.github.io/citeproc-java/")
                .accessed(2018, 4, 12)
                .build();
        long[] lastsNanos;
        lastsNanos = new long[MEAN_SIZE];
        Arrays.fill(lastsNanos, System.nanoTime());
        for (int i = 1; i <= REPETITIONS; i++) {
            CSL.makeAdhocBibliography("ieee", item).makeString();
            printBenchmark(lastsNanos, i, true);
        }
    }

    private static void benchmarkSameItemUsingItemDataProviderRecreatingCSL() throws IOException {

        long[] lastsNanos;
        lastsNanos = new long[MEAN_SIZE];
        Arrays.fill(lastsNanos, System.nanoTime());
        for (int i = 1; i <= REPETITIONS; i++) {
            ItemDataProvider itemDataProvider = new ItemDataProvider() {
                private static final String ID0 = "ID-0";
                private final String[] IDS = {ID0};

                @Override
                public CSLItemData retrieveItem(String id) {
                    return new CSLItemDataBuilder()
                            .id(ID0)
                            .type(CSLType.WEBPAGE)
                            .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
                            .author("Michel", "Krämer")
                            .issued(2016, 11, 20)
                            .URL("http://michel-kraemer.github.io/citeproc-java/")
                            .accessed(2018, 4, 12)
                            .build();
                }

                @Override
                public String[] getIds() {
                    return null;
                }
            };
            CSL citeproc = new CSL(itemDataProvider, "ieee");

            citeproc.setOutputFormat("html");
            citeproc.registerCitationItems("ID-0");
            citeproc.makeBibliography().makeString();
            printBenchmark(lastsNanos, i, true);
        }
    }

    private static void benchmarkSameItemUsingItemDataProviderReuseCSL() throws IOException {
        class FakeItemDataProvider implements ItemDataProvider {
            private CSLItemData item;

            private void setItem(CSLItemData item) {
                this.item = item;
            }
            @Override
            public CSLItemData retrieveItem(String id) {
                return item;
            }

            @Override
            public String[] getIds() {
                return null;
            }
        }
        FakeItemDataProvider itemDataProvider = new FakeItemDataProvider();

        long startNanos = System.nanoTime();
        long[] lastsNanos;
        lastsNanos = new long[MEAN_SIZE];
        Arrays.fill(lastsNanos, System.nanoTime());
        CSL citeproc = new CSL(itemDataProvider, "ieee");

        for (int i = 1; i <= REPETITIONS; i++) {
            itemDataProvider.setItem(new CSLItemDataBuilder()
                    .id("ID-" + i)
                    .type(CSLType.WEBPAGE)
                    .title("" + i + " - citeproc-java: A Citation Style Language (CSL) processor for Java")
                    .author("Michel", "Krämer")
                    .issued(2016, 11, 20)
                    .URL("http://michel-kraemer.github.io/citeproc-java/")
                    .accessed(2018, 4, 12)
                    .build());
            citeproc.setOutputFormat("html");
            citeproc.registerCitationItems("ID-" + i);
            citeproc.makeBibliography().makeString();
            printBenchmark(lastsNanos, i, false);
        }
        System.out.printf("Total time: %d ms (%d ns); Mean time: %d us%n",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos),
                (System.nanoTime() - startNanos),
                TimeUnit.NANOSECONDS.toMicros((System.nanoTime() - startNanos) / REPETITIONS));
    }

    private static void benchmarkRandomItemUsingAdhoc() throws IOException {
        CSLItemData item;
        long[] lastsNanos;
        Random seed = new Random();
        lastsNanos = new long[MEAN_SIZE];
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
            printBenchmark(lastsNanos, i, true);
        }
    }

    /**
     * @param printAll if {@code false}, print only the first 100 entries and after them, only once every 100. Else
     *                 print all entries
     */
    private static void printBenchmark(long[] lastsNanos, int i, boolean printAll) {
        final int population_size = lastsNanos.length;
        lastsNanos[i % population_size] = System.nanoTime();
        final long currentMillis = TimeUnit.NANOSECONDS.toMillis(lastsNanos[i % population_size] - lastsNanos[(i - 1) % population_size]);
        final long currentMeanSize = i < population_size ? i : population_size;
        final long currentMeanMillis = TimeUnit.NANOSECONDS.toMillis((lastsNanos[i % population_size] - lastsNanos[(i + 1) % population_size]) / currentMeanSize);
        if (printAll) {
            System.out.printf("%05d\t% 9d\t%d%n", i, currentMillis, currentMeanMillis);
        } else if ((i < 100) || (i%100==0)) {
            System.out.printf("%05d\t% 9d\t%d%n", i, currentMillis, currentMeanMillis);
        }
    }
}
