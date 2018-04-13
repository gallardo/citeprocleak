package de.ustutt.citeproc;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Benchmarks {
    private static final int REPETITIONS = 200;
    private static final int MEAN_SIZE = 50;

    /**
     * @param intro intro text
     * @param pw    to write to
     */
    private static void printHeader(String intro, PrintWriter pw) {
        pw.println(intro);
        pw.println("Entry\tTime (ms)\tMean last " + MEAN_SIZE + " (ms)");
    }

    public static String benchmarkSameItemUsingAdhoc() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        printHeader("Benchmark: same item using makeAdhocBibliography", pw);
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
            try {
                CSL.makeAdhocBibliography("ieee", item).makeString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            printBenchmark(lastsNanos, i, true, pw);
        }
        return "" + sw;
    }

    public static String benchmarkSameItemUsingItemDataProviderRecreatingCSL() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        printHeader("Benchmark: use same item provided by ItemDataProvider recreating CSL in each loop", pw);

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
            CSL citeproc = null;
            try {
                citeproc = new CSL(itemDataProvider, "ieee");
            } catch (IOException e) {
                e.printStackTrace();
            }

            citeproc.setOutputFormat("html");
            citeproc.registerCitationItems("ID-0");
            citeproc.makeBibliography().makeString();
            printBenchmark(lastsNanos, i, true, pw);
        }
        return "" + sw;
    }

    public static String benchmarkSameItemUsingItemDataProviderReuseCSL() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        printHeader("Benchmark: use same item provided by ItemDataProvider reusing CSL in all loops", pw);

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
        CSL citeproc = null;
        try {
            citeproc = new CSL(itemDataProvider, "ieee");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= REPETITIONS; i++) {
            final String ID = "ID-" + UUID.randomUUID().toString();
            itemDataProvider.setItem(new CSLItemDataBuilder()
                    .id(ID)
                    .type(CSLType.WEBPAGE)
                    .title("" + i + " - citeproc-java: A Citation Style Language (CSL) processor for Java")
                    .author("Michel", "Krämer")
                    .issued(2016, 11, 20)
                    .URL("http://michel-kraemer.github.io/citeproc-java/")
                    .accessed(2018, 4, 12)
                    .build());
            citeproc.setOutputFormat("html");
            citeproc.registerCitationItems(ID);
            citeproc.makeBibliography().makeString();
            printBenchmark(lastsNanos, i, false, pw);
        }
        pw.printf("Total time: %d ms (%d ns); Mean time: %d us%n",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos),
                (System.nanoTime() - startNanos),
                TimeUnit.NANOSECONDS.toMicros((System.nanoTime() - startNanos) / REPETITIONS));
        return "" + sw;
    }

    static String benchmarkRandomItemUsingAdhoc() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        printHeader("Benchmark: use a random created item using makeAdhocBibliography", pw);
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
            try {
                CSL.makeAdhocBibliography("ieee", item).makeString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            printBenchmark(lastsNanos, i, true, pw);
        }
        return "" + sw;
    }

    /**
     * @param printAll if {@code false}, print only the first 100 entries and after them, only once every 100. Else
     *                 print all entries
     * @param pw       Writer to write to
     */
    private static void printBenchmark(long[] lastsNanos, int i, boolean printAll, PrintWriter pw) {
        final int population_size = lastsNanos.length;
        lastsNanos[i % population_size] = System.nanoTime();
        final long currentMillis = TimeUnit.NANOSECONDS.toMillis(lastsNanos[i % population_size] - lastsNanos[(i - 1) % population_size]);
        final long currentMeanSize = i < population_size ? i : population_size;
        final long currentMeanMillis = TimeUnit.NANOSECONDS.toMillis((lastsNanos[i % population_size] - lastsNanos[(i + 1) % population_size]) / currentMeanSize);
        if (printAll) {
            pw.printf("%05d\t% 9d\t%d%n", i, currentMillis, currentMeanMillis);
        } else if ((i < 100) || (i % 100 == 0)) {
            pw.printf("%05d\t% 9d\t%d%n", i, currentMillis, currentMeanMillis);
        }
    }
}
