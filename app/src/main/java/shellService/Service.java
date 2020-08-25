package shellService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

public class Service {
    private ServiceGetText mServiceGetText;
    private Socket socket;
    private ServerSocket serverSocket;
    private CreateServerThread createServerThread = null;

    public Service(ServiceGetText serviceGetText, int PORT){
        mServiceGetText = serviceGetText;

        try{
            // Create ServerSocket
            // Server Port 4521
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server Port:4521");
            while(true){
                System.out.println("Create Socket...");
                socket = serverSocket.accept();
                System.out.println("Create Socket Done");

                createServerThread = new CreateServerThread(socket);
            }
        }catch (Exception e){
            System.out.println("Service Error Exception:" + e.toString());
        }
    }

    class CreateServerThread extends Thread{
        Socket socket;
        BufferedReader bufferedReader;
        PrintWriter printWriter;

        CreateServerThread(Socket s) throws IOException{
            System.out.println("Create CreateServerThread...");
            this.socket = s;
            start();
        }

        public void run(){
            try{
                String line;
                InputStreamReader inputStreamReader;

                // Get PrintWriter
                System.out.println("Create PrintWriter...");
                printWriter = new PrintWriter(socket.getOutputStream());
                System.out.println("Create PrintWriter Done");

                while(!socket.isClosed()){
                    // Init Shell Meg
                    printWriter.print("\r\nShell>");
                    printWriter.flush();

                    // Get Client Text
                    // Create BufferedReader
                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    bufferedReader = new BufferedReader((inputStreamReader));
                    if((line = bufferedReader.readLine()) == null)
                        continue;
                    System.out.println("Create BufferedReader Done");
                    System.out.println("Get Client Text Done");
                    System.out.println("Client Text:" + line);

                    System.out.println("Get Service Return Text...");
                    String repeat = mServiceGetText.getText(line);
                    System.out.println("Service Return Text:" + repeat);

                    System.out.println("PrintWriter println Meg...");
                    printWriter.print(repeat);
                    System.out.println("PrintWriter println Meg Done");

                    System.out.println("PrintWriter flush...");
                    printWriter.flush();
                    System.out.println("PrintWriter flush Done");
                }
            }catch (IOException e){
                System.out.println("Socket Error Exception:" + e.toString());
            }finally {
                close();
            }
        }

        public void close(){
            System.out.println("Close Socket...");

            printWriter.close();

            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Close Socket Done");
        }
    }

    public interface ServiceGetText{
        String getText(String text);
    }
}
