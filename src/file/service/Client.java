package file.service;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        String filePath;
        try {
            Socket socket = new Socket(InetAddress.getByName("localhost"), 6000);
            loop: while(true) {
                String command = client.getCommand();
                client.sendCommandToServer(socket, command);
                switch(command) {
                    case "upload":
                        filePath = client.getFilePath();
                        client.upload(socket, filePath);
                        break;
                    case "download":
                        filePath = client.getFilePath();
                        client.sendCommandToServer(socket, filePath);
                        client.download(socket);
                        break;
                    case "delete":
                        break;
                    case "rename":
                        break;
                    case "list":
                        break;
                    case "quit":
                        break loop;
                    case "":
                }
            }


        } catch(IOException e) {
            System.out.println(e.getMessage());
        }


    }

    private String getCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter command: upload, download, delete, rename, list, quit");
        return scanner.nextLine();
    }

    private String getFilePath() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file path:");
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
        socket.close();
        bufferedInputStream.close();
    }

    private void sendCommandToServer(Socket socket, String command) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(command);
    }

    private void download(Socket socket) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("clientDownloadedFromSerer.txt"));

        byte[] bytes = new byte[8000];
        int length;
        while ((length = bufferedInputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, length);
        }

        bufferedOutputStream.close();
        bufferedInputStream.close();
        socket.close();
        System.out.println("Downloadsucceeded");
    }

}
