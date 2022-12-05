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
            server.serverSocket.setReuseAddress(true);

            while(true) {
                Socket client = server.serverSocket.accept();

                System.out.println("New Client");
                Service service = new Service(client);

                new Thread(service).start();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }



    private static class Service implements Runnable {
        private final Socket clientSocket;

        private Service(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }


        public void run() {
            loop:
            while (true) {
                try {
//                    Socket socket = clientSocket.accept();
//                    System.out.println("Connected");

                    InputStream input = clientSocket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    String command = reader.readLine();
                    System.out.println("Command: " + command);
                    String fileName;
                    switch (command) {
                        case "upload":
                            fileName = reader.readLine();
                            this.upload(clientSocket, fileName);
                            break;
                        case "download":
                            String filePath = reader.readLine();
                            this.download(filePath, clientSocket);
                            break;
                        case "delete":
                            fileName = reader.readLine();
                            this.delete(clientSocket, fileName);
                            break;
                        case "rename":
                            fileName = reader.readLine();
                            String[] fileNames = fileName.split("&");
                            this.rename(clientSocket, fileNames[0], fileNames[1]);
                            break;
                        case "list":
                            this.list(clientSocket);
                            break;
                        case "quit":
                            clientSocket.close();
                            break loop;
                        default:
                            this.sendMessageToClient(clientSocket, "Server: Invalid command");
                            break;
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
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
            server.close();
            //this will not send because socket is closed
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
                output.close();
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



    }





}
