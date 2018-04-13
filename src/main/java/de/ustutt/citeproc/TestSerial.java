package de.ustutt.citeproc;

import java.util.Scanner;

public class TestSerial {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("Press enter to begin");
        scanner.nextLine();
        System.out.println("" + Benchmarks.benchmarkSameItemUsingAdhoc());

//         AG 2018-04-13: Don't know why, but the first rendering after this loop takes again a while when using Nashorn
        for (int j = 1; j <= 3; j++) {
            System.out.println("Press enter to begin next test");
            scanner.nextLine();
            System.out.println("" + Benchmarks.benchmarkRandomItemUsingAdhoc());
        }

        System.out.println("Press enter to begin next test");
        scanner.nextLine();
        System.out.println("" + Benchmarks.benchmarkSameItemUsingItemDataProviderRecreatingCSL());

        System.out.println("Press enter to begin next test");
        scanner.nextLine();
        System.out.println("" + Benchmarks.benchmarkSameItemUsingItemDataProviderReuseCSL());

        System.out.println("Press enter to end");
        scanner.nextLine();
    }

}
