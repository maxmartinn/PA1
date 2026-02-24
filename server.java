import java.io.*;
import java.net.*;

public class server {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: java server [port_number]");
      return;
    }

    int port;
    try {
      port = Integer.parseInt(args[0]);
      if (port < 1 || port > 65535) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      System.err.println("Invalid port number. Must be 1..65535.");
      return;
    }

    try (
      ServerSocket serverSocket = new ServerSocket(port);
      Socket clientSocket = serverSocket.accept();
      BufferedReader in = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream())
      );
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
    ) {
      out.println("Hello!");

      String line;
      while ((line = in.readLine()) != null) {
        System.out.println(line);
        if ("bye".equals(line)) {
          out.println("disconnected");
          break;
        }

        boolean ok = !line.isEmpty();
        for (int i = 0; i < line.length() && ok; i++) {
          ok = Character.isLetter(line.charAt(i));
        }

        out.println(
          ok
            ? line.toUpperCase()
            : "ERROR: Non-alphabet characters detected. Please type again."
        );
      }
    } catch (IOException e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }
}
