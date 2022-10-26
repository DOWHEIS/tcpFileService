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

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Syntax: Server <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try {
            Server server = new Server(port);
            server.service(server);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void service(Server server) {

        loop:
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Connected");

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String command = reader.readLine();
                System.out.println("Command: " + command);
                String fileName;
                switch (command) {
                    case "upload":
                        fileName = reader.readLine();
                        server.upload(socket, fileName);
                        break;
                    case "download":
                        String filePath = reader.readLine();
                        server.download(filePath, socket);
                        break;
                    case "delete":
                        fileName = reader.readLine();
                        server.delete(socket, fileName);
                        break;
                    case "rename":
                        fileName = reader.readLine();
                        String[] fileNames = fileName.split("&");
                        server.rename(socket, fileNames[0], fileNames[1]);
                        break;
                    case "list":
                        server.list(socket);
                        break;
                    case "quit":
                        serverSocket.close();
                        break loop;
                    default:
                        server.sendMessageToClient(socket, "Server: Invalid command");
                        break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }


    }

    private void download(String filePath, Socket server) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(server.getOutputStream());
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            int length;
            while ((length = bufferedInputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, length);
                bufferedOutputStream.flush();
            }

            bufferedInputStream.close();
            bufferedOutputStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public void upload(Socket server, String fileName) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(server.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("ServerFiles/UploadedFiles/" + fileName));

        byte[] bytes = new byte[8000];
        int length;
        while ((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
        }
        bufferedOutputStream.close();
        bufferedInputStream.close();
        this.sendMessageToClient(server, "Server: " + fileName + " uploaded!");
    }

    private void delete(Socket server, String fileName) {

        File fileToDelete = new File("ServerFiles/UploadedFiles/" + fileName);

        if (fileToDelete.delete()) {
            this.sendMessageToClient(server, "Server: " + fileName + " deleted!");
        } else {
            this.sendMessageToClient(server, "Server: Failed to delete " + fileName);
        }

    }

    private void sendMessageToClient(Socket server, String message) {
        try {
//            serverSocket.accept();
            OutputStream output = server.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    private void rename(Socket server, String fileName, String newFileName){
        File oldFile = new File("ServerFiles/UploadedFiles/" + fileName);
        File newFile = new File("ServerFiles/UploadedFiles/" + newFileName);
        if (oldFile.renameTo(newFile)){
            this.sendMessageToClient(server, "Server: " + fileName + " successfully renamed to " + newFileName);
        }else {
            this.sendMessageToClient(server, "Server: Failed to rename " + fileName + " to " + newFileName);
        }

    }
    private void list (Socket socket) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        String path = "ServerFiles/UploadedFiles/";
        File directory = new File(path);
        String[] fileList = directory.list();
        if(fileList!=null)
        for (String s : fileList) {
            System.out.println("-".repeat(24));
            writer.println("|" + s + "|");
            System.out.println("-".repeat(24));

        }
        output.close();
        writer.close();
        System.out.println("\nListed all current files");



    }


}
