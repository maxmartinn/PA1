import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class client {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: java client [serverURL] [port_number]");
      return;
    }

    String host = args[0];
    int port;
    try {
      port = Integer.parseInt(args[1]);
      if (port < 1 || port > 65535) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      System.err.println("Invalid port number. Must be 1..65535.");
      return;
    }
    List<Double> rtts = new ArrayList<>();

    BufferedReader inFromUser = new BufferedReader(
      new InputStreamReader(System.in)
    );

    try (
      Socket clientSocket = new Socket(host, port);
      DataOutputStream outToServer = new DataOutputStream(
        clientSocket.getOutputStream()
      );
      BufferedReader inFromServer = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream())
      )
    ) {
      String helloMsg = inFromServer.readLine();
      if (helloMsg == null) {
        System.err.println("Server closed connection.");
        return;
      }
      System.out.println(helloMsg);

      while (true) {
        System.out.print("Enter a string (letters only) or 'bye' to quit: ");
        String input = inFromUser.readLine();

        long start = System.nanoTime();
        outToServer.writeBytes(input + '\n');
        String resp = inFromServer.readLine();
        long end = System.nanoTime();

        if (resp == null) {
          System.out.println("Server closed connection.");
          break;
        }

        if ("disconnected".equals(resp)) {
          System.out.println("exit");
          break;
        }

        if (resp.startsWith("ERROR:")) {
          System.out.println(resp);
          continue;
        }

        double rtt = (end - start) / 1_000_000.0;
        System.out.println("Server response: " + resp);
        System.out.printf("RTT: %.3f ms%n", rtt);
        rtts.add(rtt);

        if (rtts.size() >= 5) {
          double min = Double.MAX_VALUE;
          double max = Double.MIN_VALUE;
          double sum = 0.0;
          for (double v : rtts) {
            min = Math.min(min, v);
            max = Math.max(max, v);
            sum += v;
          }
          double mean = sum / rtts.size();
          double ss = 0.0;
          for (double v : rtts) {
            double d = v - mean;
            ss += d * d;
          }
          double sd = rtts.size() > 1 ? Math.sqrt(ss / (rtts.size() - 1)) : 0.0;

          System.out.print(
            """
            RTT stats over n=%d successful trials (ms):
            min   = %.3f
            mean  = %.3f
            max   = %.3f
            stddev(sample) = %.3f

            """.formatted(rtts.size(), min, mean, max, sd)
          );
        }
      }
    } catch (IOException e) {
      System.err.println("I/O error: " + e.getMessage());
    }
  }
}
