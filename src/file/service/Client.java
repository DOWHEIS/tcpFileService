package file.service;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Client client = new Client();
        String filePath;
        String fileName;
        String newFileName;
        try {
            loop: while(true) {
                Socket socket = new Socket(InetAddress.getByName("localhost"), 6000);
                String command = client.getCommand();
                String response;
                client.sendCommandToServer(socket, command);
                switch(command) {
                    case "upload":
                        fileName = client.getFileName();
                        filePath = "ClientFiles/UploadedFiles/" + fileName;
                        client.sendCommandToServer(socket, fileName);
                        client.upload(socket, filePath);
                        response = client.getMessageFromServer(socket);
                        System.out.println(response);
                    case "download":
                        fileName = client.getFileName();
                        filePath = "ServerFiles/UploadedFiles/" + fileName;
                        client.sendCommandToServer(socket, filePath);
                        client.download(socket, fileName);
                        response = client.getMessageFromServer(socket);
                        System.out.println(response);
                    case "delete":
                        fileName= client.getFileName();
                        client.sendCommandToServer(socket, fileName);
                        response = client.getMessageFromServer(socket);
                        System.out.println(response);
                    case "rename":
                        break;
                    case "list":
                        client.list(socket);
                        response = client.getMessageFromServer(socket);
                        System.out.println(response);
                    case "quit":
                        socket.close();
                    case "":
                }
            }


        } catch(IOException e) {
            System.out.println(e.getMessage());
        }


    }

    private String getMessageFromServer(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        return reader.readLine();
    }

    private String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter command: upload, download, delete, rename, list, quit");
        return scanner.nextLine();
    }

    private String getFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name:");
        return scanner.nextLine();
    }

    private String getNewFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name:");
        return scanner.nextLine();
    }



    private void upload(Socket socket, String filePath) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        int length;
        while((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
            bufferedOutputStream.flush();
        }
        bufferedOutputStream.close();
        bufferedInputStream.close();
    }

    private void sendCommandToServer(Socket socket, String command) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(command);
    }

    private void download(Socket socket, String fileName) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("ClientFiles/DownloadedFiles/" + fileName));

        byte[] bytes = new byte[8000];
        int length;
        while ((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
        }

        bufferedOutputStream.close();
        bufferedInputStream.close();
    }
    private void list(Socket socket) throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);

        }
        input.close();
        reader.close();
        System.out.println("Listed all current files");



    }


}
