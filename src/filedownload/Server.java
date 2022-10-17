package filedownload;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    private ServerSocket serverSocket;
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) throws IOException {
        int port= 6000;
        try {
            Server server = new Server(port);
            server.service(server);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void service(Server server) throws IOException {
        loop: while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connected");

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String command = reader.readLine();
            System.out.println("Command: " + command);
            switch(command) {
                case "upload":
                    server.upload();
                    break;
                case "download":
                    server.download("testcopy.txt");
                    break;
                case "delete":
                    break;
                case "rename":
                    break;
                case "list":
                    break;
                case "quit":
                    break loop;
            }

        }
    }

    private void download(String filePath) throws IOException {
        Socket server = serverSocket.accept();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(server.getOutputStream());
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        int length;
        while((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
            bufferedOutputStream.flush();
        }
        bufferedOutputStream.close();
        bufferedInputStream.close();
        server.close();
    }

    public void upload() throws IOException {
        Socket server = serverSocket.accept();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(server.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("testcopy.txt"));

        byte[] bytes = new byte[8000];
        int length;
        while ((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
        }

        bufferedOutputStream.close();
        bufferedInputStream.close();
        server.close();
        System.out.println("Upload succeeded");
    }


}
