import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class client {

    private static double mean(List<Double> xs) {
        double sum = 0.0;
        for (double x : xs) {
            sum += x;
        }
        return sum / xs.size();
    }

    private static double sampleStdDev(List<Double> xs) {
        int n = xs.size();
        if (n < 2) {
            return 0.0;
        }
        double mu = mean(xs);
        double ss = 0.0;
        for (double x : xs) {
            double d = x - mu;
            ss += d * d;
        }
        return Math.sqrt(ss / (n - 1));
    }

    private static void printStats(List<Double> rttsMs) {
        if (rttsMs.size() < 5) {
            System.out.println("Not enough successful RTT samples to compute stats (need at least 5).");
            return;
        }
        double min = Collections.min(rttsMs);
        double max = Collections.max(rttsMs);
        double mu = mean(rttsMs);
        double sd = sampleStdDev(rttsMs);

        System.out.println();
        System.out.println("RTT stats over n=" + rttsMs.size() + " successful trials (ms):");
        System.out.printf("min   = %.3f%n", min);
        System.out.printf("mean  = %.3f%n", mu);
        System.out.printf("max   = %.3f%n", max);
        System.out.printf("stddev(sample) = %.3f%n", sd);
        System.out.println();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java client [serverURL] [port_number]");
            System.exit(1);
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number. Must be an integer 1..65535.");
            System.exit(1);
            return;
        }

        List<Double> rttsMs = new ArrayList<>();

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            String hello = in.readLine();
            if (hello == null) {
                System.err.println("Server closed connection immediately.");
                System.exit(1);
            }
            System.out.println(hello);

            while (true) {
                System.out.print("Enter a string (letters only) or 'bye' to quit: ");
                String input = scanner.nextLine();

                long startNs = System.nanoTime();
                out.println(input);

                String resp = in.readLine();
                long endNs = System.nanoTime();

                if (resp == null) {
                    System.out.println("Server closed connection.");
                    break;
                }

                double rttMs = (endNs - startNs) / 1_000_000.0;

                if (resp.equals("disconnected")) {
                    System.out.println("exit");
                    break;
                }

                if (resp.startsWith("ERROR:")) {
                    System.out.println(resp);
                    continue;
                }

                System.out.println("Server response: " + resp);
                rttsMs.add(rttMs);
                System.out.printf("RTT: %.3f ms%n", rttMs);

                if (rttsMs.size() >= 5) {
                    printStats(rttsMs);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            System.exit(1);
        }
    }
}
