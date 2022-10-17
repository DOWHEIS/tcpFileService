package file.service;

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

    private void service(Server server) {
        try {
            loop: while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected");

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String command = reader.readLine();
                System.out.println("Command: " + command);
                switch(command) {
                    case "upload":
                        server.upload(socket);
                        break;
                    case "download":
                        String filePath = reader.readLine();
                        server.download(filePath, socket);
                        break;
                    case "delete":
                        break;
                    case "rename":
                        break;
                    case "list":
                        break;
                    case "quit":
                        socket.close();
                        break loop;
                }

            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void download(String filePath, Socket server) {
        try {
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
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public void upload(Socket server) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(server.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("clientUploadedServer.txt"));

        byte[] bytes = new byte[8000];
        int length;
        while ((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
        }

        bufferedOutputStream.close();
        bufferedInputStream.close();
        System.out.println("Upload succeeded");
    }


}
