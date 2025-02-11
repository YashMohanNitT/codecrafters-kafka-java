import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) {
      // You can use print statements as follows for debugging, they'll be visible when running tests.
      System.err.println("Logs from your program will appear here!");

      // Uncomment this block to pass the first stage
      //
      ServerSocket serverSocket = null;
      int port = 9092;
      ExecutorService executorService = Executors.newFixedThreadPool(8);
      try {
          serverSocket = new ServerSocket(port);
          // Since the tester restarts your program quite often, setting SO_REUSEADDR
          // ensures that we don't run into 'Address already in use' errors
          serverSocket.setReuseAddress(true);
          // Wait for connection from client.

          while (true) {
              Socket finalClientSocket = serverSocket.accept();
              executorService.submit(() -> {
                  try {
                      handleInput(finalClientSocket);
                  } catch (IOException e) {
                      System.out.println("IOException: " + e.getMessage());
//                     throw new RuntimeException(e);
                  }
              });
          }
      } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
      } finally {
          executorService.shutdown();
      }
  }

    private static void handleInput(Socket clientSocket) throws IOException {
        while (true) {
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            Result result = getResult(inputStream);
            int size;
            OutputStream outputStream = clientSocket.getOutputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            writeInBos(byteArrayOutputStream, result);
            size = byteArrayOutputStream.size();
            byte[] message_size = ByteBuffer.allocate(4).putInt(size).array();
            outputStream.write(message_size);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
        }
    }

    private static void writeInBos(ByteArrayOutputStream byteArrayOutputStream, Result result) throws IOException {
        byteArrayOutputStream.write(result.correlation_id());
        if (result.version() < 0 || result.version() > 4) {
            byteArrayOutputStream.write(new byte[]{0, 35});
        } else {
            byteArrayOutputStream.write(new byte[]{0, 0});
            byteArrayOutputStream.write(2);
            byteArrayOutputStream.write(new byte[]{0, 18});
            byteArrayOutputStream.write(new byte[]{0, 3});
            byteArrayOutputStream.write(new byte[]{0, 4});
            byteArrayOutputStream.write(0);
            byteArrayOutputStream.write(new byte[]{0, 0, 0, 0});
            byteArrayOutputStream.write(0);
        }
    }

    private static Result getResult(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        inputStream.readNBytes(2);
        short version = inputStream.readShort();
        byte[] correlation_id = inputStream.readNBytes(4);
        byte[] leftOut = new byte[size - 8];
        inputStream.readFully(leftOut);
        return new Result(version, correlation_id, leftOut);
    }

    private record Result(short version, byte[] correlation_id, byte[] leftOut) {
    }
}
