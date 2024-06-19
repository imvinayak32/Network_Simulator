package devices;

import java.util.*;
import java.io.*;
import processing.Process;

public class EndDevices {
    private int deviceId;
    private String macAddress;
    private String ipAddress;
    private String message;
    public Map<String, String> arp = new HashMap<>();
    public Map<Integer, Boolean> selective_window = new HashMap<>();
    public int sender_buffer;
    public int receiver_buffer;
    public boolean ack;
    public boolean token;

    public EndDevices() {
        this.deviceId = 0;
        this.macAddress = "";
        this.ack = false;
        this.token = false;
    }
    public EndDevices(int Id, String mac, String ip) {
        this.deviceId = Id;
        this.macAddress = mac;
        this.ipAddress = ip;
    }
    public int getId() { return deviceId; }
    public String getMAC() { return macAddress; }
    public String getIP() { return ipAddress; }
    public void putData(String data) { this.message = data; } // getData
    public String sendData() { return message; }

    // Application layer protocol
    // Callback function to write received data into a string
    public static int writeCallback(char[] contents, int size, int nmemb, StringBuilder output) {
        int totalSize = size * nmemb;
        output.append(contents, 0, totalSize);
        return totalSize;
    }

    public int http() {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter domain name : ");
        String domain = scanner.nextLine();
        System.out.println();
        String command = "curl -s https://" + domain;

        try {
            //Process process = Runtime.getRuntime().exec(command);
            java.lang.Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            System.out.println("Response:\n" + response.toString());
            return 0;
        } 
        catch (IOException e) {
            System.err.println("Error executing command.");
            return 1;
        }
    }

    public void dns() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("DNS \n");
        System.out.print("Enter domain name: ");
        String domain = scanner.nextLine();

        String command = "nslookup " + domain;

        try {
            //Process process = Runtime.getRuntime().exec(command);
            java.lang.Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Failed to execute the command.");
        }
    }

    public void sendAck(int receiver) {
        //this.ack = true;
        System.out.println("\nAcknowledgement Status : ");
        System.out.println("Device " + receiver + " sends back ACK to Hub \n");
    }
    // Accesss Control Protocol
    public void tokenCheck(List<EndDevices> devices, int sender, int size) {
        Random rand = new Random();
        System.out.println("\nToken status : ");
        int i = rand.nextInt(size);
        // until sender doesn't have access to token
        while (!devices.get(sender - 1).token) {
            // logical ring
            int currentDevice = i % size;
            devices.get(currentDevice).token = true;
            if (currentDevice != (sender - 1)) {
                System.out.println("Currently sender doesn't have access to channel. Token is at device " + (currentDevice + 1) + " Waiting to get access ");
            }
            i++;
            try { 
                Thread.sleep(1000);
            }
            catch (InterruptedException e) { 
                e.printStackTrace(); 
            }
        }
        System.out.println("Sender has access to channel now");
    }

    public void sender(List<Integer> window) {
        Random rand = new Random();
        System.out.println();
        // window sliding
        int i = 0;
        while (i < window.size()) {
            ack = false;
            int timeout_duration = 4;
            int sending_time = rand.nextInt(6);
            int receiving_time = rand.nextInt(6);
            sender_buffer = window.get(i);
            // sending packet to receiver
            int AckNo;

            try{ 
                Thread.sleep(sending_time * 1000); 
            } 
            catch (InterruptedException e) {
                 e.printStackTrace();
            }
            
            // packet got lost then resend it
            if (sending_time > timeout_duration) {
                System.out.println("Sender sends packet with sequence number " + window.get(i) + " but it got lost");
                continue;
            }
            // packet didn't get lost
            else {
                System.out.println("Sender sends packet with sequence number " + sender_buffer);
                AckNo = receiver(window, i);
                // receiver reaches at the end of window
                if (AckNo == -1) {
                    System.out.println("Done");
                    break;
                }
                if (receiving_time > timeout_duration) {
                    // ACK got lost
                    System.out.println("ACK " + AckNo + " got lost");
                    continue; // resend packet
                } 
                else {
                    if (ack) {
                        System.out.println("Sender receives ACK " + AckNo); // ACK Received
                        i++;
                    }
                }
            }
        }
    }

    public int receiver(List<Integer> window, int i) {
        int j = 0;
        if (sender_buffer == window.get(j) && i == j && j < window.size()) {
            receiver_buffer = sender_buffer;
            ack = true;
            j++;
            if (j == window.size()) {
                return -1;
            }
            return window.get(j);
        } 
        else {
            System.out.println("Packet " + window.get(i) + " was discarded as it a duplicate ");
            ack = true;
            return window.get(j);
        }
    }

    public void stopAndWait() {

        int windowSize = 7;
        List<Integer> window = new ArrayList<>();
        for (int i = 0; i < windowSize; i++) {
            // add 0 for even and 1 for odd value of i
            window.add(i % 2 == 0 ? 0 : 1);
        }
        System.out.println("\nTransmission Status :");
        sender(window);
    }

    public int selectiveReceiver(int packet) {
        selective_window.put(packet, true);
        int AckNo = packet;
        // slide receiving window if consecutive elements are marked
        int count = 0;
        for (int j = 0; j < selective_window.size(); j++) {
            if (!selective_window.getOrDefault(j, false)) {
                break;
            }
            count++;
        }
        int Rn = count;
        ack = true;
        return AckNo;
    }

    public void selectiveSender() {
        Random rand = new Random();
        int Sn = 0, Sf = 1, S_z = selective_window.size();
        int i = 0, AckNo;
        while (i < S_z) {
            ack = false;
            int timeout_duration = 4;
            int sending_time = rand.nextInt(6);
            int receiving_time = rand.nextInt(6);

            try { Thread.sleep(sending_time * 1000); } catch (InterruptedException e) { e.printStackTrace(); }
            if (sending_time > timeout_duration) {
                // packet got lost
                System.out.println("Sender sends packet with sequence number " + i + " but it got lost");
                i++;
                continue;
            } else {
                int packet = i;
                System.out.println("Sender sends packet with sequence number " + packet);
                AckNo = selectiveReceiver(packet);

                if (receiving_time > timeout_duration) {
                    // ACK got lost
                    System.out.println("ACK " + AckNo + " got lost");
                    i++;
                } else {
                    if (ack) {
                        System.out.println("ACK " + AckNo + " received");
                        int count = 0;
                        // slide window if consecutive elements in window are marked
                        for (int j = 0; j <= AckNo; j++) {
                            if (!selective_window.getOrDefault(j, false)) {
                                break;
                            }
                            count++;
                        }
                        Sf = count;

                        i++;
                        Sn = i;
                    }
                }
            }
        }
        // timeout
        if (i == S_z) {
            System.out.println();
            System.out.println("Time out occurred");
            // check which packet is not received and resend it
            for (int j = 0; j < selective_window.size(); j++) {
                if (!selective_window.getOrDefault(j, false)) {
                    System.out.println("Resending Packet " + j + " as it wasn't received");
                    // resending packet
                    AckNo = selectiveReceiver(j);
                    System.out.println("ACK " + AckNo + " received");
                }
            }
        }
    }

    public void selectiveRepeat() {
        System.out.println();
        int size = 8;
        for (int i = 0; i < size; i++) {
            selective_window.put(i, false);
        }
        selectiveSender();
    }

    //  For selecting Sender and Receiver device
    public void prompt(String DeviceType, int d, Map<Integer, Boolean> mp) {
        for (int i = 1; i <= d; i++) {
            mp.put(i, true);
        }
        System.out.println("\nChoose the " + DeviceType + " device - ");
        for (int i = 0; i < mp.size(); i++) {
            System.out.println((i + 1) + " : " + "device " + (i + 1));
        }
    }

    public void arpCache(String ip, String mac) {
        arp.put(ip, mac);
    }

    public void printArpCache() {
        System.out.println("\nARP Cache of sender is as :\n");
        System.out.println("IP\t\t\tMAC\n");
        for (Map.Entry<String, String> entry : arp.entrySet()) {
            System.out.println(entry.getKey() + "\t\t" + entry.getValue());
            System.out.println();
        }
    }
}
