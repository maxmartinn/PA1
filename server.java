import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server {

    private static boolean isAlphabeticOnly(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isLetter(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java server [port_number]");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number. Must be an integer 1..65535.");
            System.exit(1);
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                System.out.println("Client connected from " + clientSocket.getInetAddress());
                out.println("Hello!");

                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        System.out.println("Client disconnected unexpectedly.");
                        break;
                    }

                    System.out.println("Received: " + line);

                    if (line.equals("bye")) {
                        out.println("disconnected");
                        System.out.println("Client requested termination. Shutting down server.");
                        break;
                    }

                    if (!isAlphabeticOnly(line)) {
                        out.println("ERROR: Non-alphabet characters detected. Please type again.");
                        continue;
                    }

                    out.println(line.toUpperCase());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(1);
        }
    }
}
