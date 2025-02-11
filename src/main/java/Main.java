import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    // 
     ServerSocket serverSocket = null;
     Socket clientSocket = null;
     int port = 9092;
     try {
       serverSocket = new ServerSocket(port);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);
       // Wait for connection from client.
       clientSocket = serverSocket.accept();

       InputStream inputStream = clientSocket.getInputStream();
       inputStream.readNBytes(4);
       byte[] request_api_key = inputStream.readNBytes(2);
       byte[] request_api_version = inputStream.readNBytes(2);
       short version = ByteBuffer.wrap(request_api_version).getShort();
       byte[] correlation_id = inputStream.readNBytes(4);

       System.out.println(Arrays.toString(request_api_key));
       System.out.println(Arrays.toString(request_api_version));
       System.out.println(Arrays.toString(correlation_id));

       OutputStream outputStream = clientSocket.getOutputStream();
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
       byteArrayOutputStream.write(correlation_id);
       if (version < 0 || version > 4) {
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
       int size = byteArrayOutputStream.size();
       byte[] message_size = ByteBuffer.allocate(4).putInt(size).array();

       System.out.println(Arrays.toString(message_size));
       System.out.println(Arrays.toString(byteArrayOutputStream.toByteArray()));

       outputStream.write(message_size);
       outputStream.write(byteArrayOutputStream.toByteArray());
       outputStream.flush();
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     } finally {
       try {
         if (clientSocket != null) {
           clientSocket.close();
         }
       } catch (IOException e) {
         System.out.println("IOException: " + e.getMessage());
       }
     }
  }
}
