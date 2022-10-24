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


        loop:
        while (true) {
            try {
                Socket socket = new Socket(InetAddress.getByName("localhost"), 6000);
                String command = client.getCommand();
                client.sendCommandToServer(socket, command);
                switch (command) {
                    case "upload":
                        fileName = client.getFileName();
                        filePath = "ClientFiles/UploadedFiles/" + fileName;
                        client.sendCommandToServer(socket, fileName);
                        client.upload(socket, filePath);
                        client.getMessageFromServer(socket);
                    case "download":
                        fileName = client.getFileName();
                        filePath = "ServerFiles/UploadedFiles/" + fileName;
                        client.sendCommandToServer(socket, filePath);
                        client.download(socket, fileName);
//                        client.getMessageFromServer(socket);
                        break;
                    case "delete":
                        fileName = client.getFileName();
                        client.sendCommandToServer(socket, fileName);
                        client.getMessageFromServer(socket);
                    case "rename":
                        break;
                    case "list":
                        client.list(socket);
                        break;
//                        client.getMessageFromServer(socket);
                    case "quit":
                        socket.close();
                        break loop;
                    default:
                        client.getMessageFromServer(socket);
                        break;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }


        }

    }

    private void getMessageFromServer(Socket socket) throws IOException {

        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        System.out.println(reader.readLine());


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


    private void upload(Socket socket, String filePath) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            int length;
            while ((length = bufferedInputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, length);
                bufferedOutputStream.flush();
            }
            bufferedOutputStream.close();
            bufferedInputStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void sendCommandToServer(Socket socket, String command) throws IOException {
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(command);
    }

    private void download(Socket socket, String fileName) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("ClientFiles/DownloadedFiles/" + fileName));

            byte[] bytes = new byte[8000];
            int length;
            while ((length = bufferedInputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, length);
            }

            bufferedOutputStream.close();
            bufferedInputStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void list(Socket socket) {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);

            }
            input.close();
            reader.close();
            System.out.println("Listed all current files");

        } catch(IOException e) {
            System.out.println(e.getMessage());
        }


    }

}
