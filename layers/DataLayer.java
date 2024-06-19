package layers;

import java.util.*;

import devices.EndDevices;
import devices.Hub;
import devices.Switch;

import processing.Process;

public class DataLayer {

    public String generateMacAddress() {

        Random rand = new Random();
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
        rand.nextInt(256),
        rand.nextInt(256),
        rand.nextInt(256), 
        rand.nextInt(256), 
        rand.nextInt(256), 
        rand.nextInt(256));
    }
    public void run(int choice, int hubSize) {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            int noOfDevice1, sender, receiver;
            String data;
            List<EndDevices> deviceList1 = new ArrayList<>();
            Map<Integer, Boolean> mapp = new HashMap<>();
            Hub hub = new Hub();
            EndDevices endDevice = new EndDevices();
            Switch switch1 = new Switch();
            int select;

            if (choice == 1) {

                System.out.println("\nEnter the number of end device");
                noOfDevice1 = scanner.nextInt();

                if (noOfDevice1 < 2) {
                    System.out.println("There should be at least two device. Enter a valid number");
                    continue;
                }

                // For Flow Control Protocol Implementation
                Map<Integer, String> flowControl = new HashMap<>();  
                flowControl.put(1, "Stop and Wait ARQ");
                flowControl.put(2, "Selective Repeat");
                // To print this map
                System.out.println("\nChoose a Flow Control Protocol :");
                for (Map.Entry<Integer, String> entry : flowControl.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                select = scanner.nextInt();
                // If input other than 1 or 2
                if (select != 1 && select != 2) {
                    System.out.println("Invalid Entry");
                    continue;
                }
                Random rand = new Random();

                for (int i = 0; i < noOfDevice1; i++) {
                    String mac = generateMacAddress(); 
                    deviceList1.add(new EndDevices(i + 1, mac, ""));
                    switch1.topology(deviceList1.get(i));

                    if (i == 0) {
                        System.out.println("\nConnection status : ");
                    }
                    switch1.printConnection(i);
                }
                // For selecting sender and receiver device
                endDevice.prompt("sender", noOfDevice1, mapp); 
                sender = scanner.nextInt();
                if (!mapp.getOrDefault(sender, false)) {
                    System.out.println("Invalid Entry");
                    continue;
                }
                endDevice.prompt("receiver", noOfDevice1, mapp); 
                receiver = scanner.nextInt();
                if (!mapp.getOrDefault(receiver, false)) {
                    System.out.println("Invalid Entry");
                    continue;
                }
                if (sender == receiver) { 
                    System.out.println("Sender and receiver can't be the same");
                    continue;
                }

                System.out.println("\nInput the message that you would like to send ");
                scanner.nextLine(); // Consume the newline character
                data = scanner.nextLine();

                EndDevices senderDevice = deviceList1.get(sender - 1);
                senderDevice.putData(data);
                System.out.println();

                // Token Passing - Access Control Protocol
                senderDevice.tokenCheck(deviceList1, sender, noOfDevice1);
                switch1.macTable(); 
                System.out.println();

                // For Stop and wait ARQ
                if (select == 1) {
                    senderDevice.stopAndWait();
                    switch1.transmission(deviceList1, sender, receiver);
                    break;
                } 
                // For Selective repeat
                else if (select == 2) {  
                    senderDevice.selectiveRepeat();
                    switch1.transmission(deviceList1, sender, receiver);
                    break;
                }
            }
            // When more than one hub is used 
            else if (choice == 2) {

                List<EndDevices> deviceList2 = new ArrayList<>();
                Switch mainSwitch = new Switch();
                List<Hub> HubList = new ArrayList<>();
                Map<Integer, Boolean> mp2 = new HashMap<>();

                for (int i = 0; i < hubSize; i++) {   
                    
                    HubList.add(new Hub(i + 1));  // Adding hub to hub list
                    mainSwitch.topology(HubList.get(i));  // Connecting all hub with the main switch

                    if (i == 0) {
                        System.out.println("Connection status : \n");
                    }
                    mainSwitch.hubPrintConnection(i);
                }

                System.out.println("\nEnter the number of end devices to be connected with each hub: ");
                int deviceNum = scanner.nextInt();

                if (deviceNum < 2) {
                    System.out.println("There should be at least two devices. Enter a valid number");
                    continue;
                }
                
                int id = 1;
                int k = 0;
                // Put end device in device list & connect them with each hub
                for (Hub hub2 : HubList) {
                    for (int j = 0; j < deviceNum; j++) {
                        deviceList2.add(new EndDevices(id, "", ""));
                        hub2.topology(deviceList2.get(k++));
                        id++;
                    }
                }

                // For each device in the list, print established connection 
                for (int i = 0; i < HubList.size(); i++) {
                    System.out.println();
                    HubList.get(i).connection(i + 1);
                }
                System.out.println();

                // Getting connected end devices of hub[i], storing them in connectedDevices List
                // Mapping of hub and connectedDevices in Switch
                for (int i = 0; i < HubList.size(); i++) {
                    List<EndDevices> connectedDevices = HubList.get(i).getDevices(); 
                    mainSwitch.hubToDeviceMap(i, connectedDevices); 
                }
                mainSwitch.printHubToDeviceMap();

                // Selecting Sender and Receiver device
                int totalDevices = deviceNum * hubSize;
                endDevice.prompt("sender", totalDevices, mp2);  
                sender = scanner.nextInt();
                if (!mp2.getOrDefault(sender, false)) {
                    System.out.println("Invalid Entry");
                    continue;
                }
                endDevice.prompt("receiver", totalDevices, mp2); 
                receiver = scanner.nextInt();
                if (!mp2.getOrDefault(receiver, false)) {
                    System.out.println("Invalid Entry");
                    continue;
                }
                // If sender and receiver are the same
                if (sender == receiver) {  
                    System.out.println("Sender and receiver can't be the same");
                    continue;
                }

                System.out.println("\nInput the message that you would like to send ");
                scanner.nextLine(); // Consume the newline character
                data = scanner.nextLine();

                int sourceHub = mainSwitch.findHubForDevice(sender);
                EndDevices senderDevice2 = deviceList2.get(sender - 1);
                senderDevice2.putData(data);  

                Hub senderHub = HubList.get(sourceHub);
                senderHub.data = data; // Hub receives data
                senderHub.broadcast(deviceList2, sender);
                System.out.println();
                senderHub.transmission(sender, receiver);
                System.out.println();
                
                // Source hub sends data to switch
                int destinationHub = mainSwitch.receiveData(sender, receiver, data); 
                Hub ReceiverHub = HubList.get(destinationHub);

                ReceiverHub.broadcast(deviceList2, sender);
                System.out.println();
                ReceiverHub.transmission(sender, receiver);
                System.out.println();
                
                senderDevice2.sendAck(receiver); 
                ReceiverHub.broadcastAck(sender, receiver, true);
                // ReceiverHub.ack = true;
                System.out.println("");
                mainSwitch.receiveAck(destinationHub);
                mainSwitch.sendAckToHub(sourceHub);
                senderHub.broadcastAck(sender, receiver, true);

                break;
            }
        }
    }
}
