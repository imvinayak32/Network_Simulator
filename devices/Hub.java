package devices;

import java.util.*;

public class Hub {

    private int hubId;
    public List<EndDevices> connectedDevices = new ArrayList<>();
    public boolean ack;
    public String data;

    public Hub() {
        this.hubId = 0;
        this.ack = false;
    }

    public Hub(int Id) {
        this.hubId = Id;
    }

    public int getId() {
        return hubId;
    }

    public List<EndDevices> getDevices() {
        return connectedDevices;
    }

    public void topology(EndDevices devices) {
        connectedDevices.add(devices);
    }

    public void connection(int i) {
        for (EndDevices device : connectedDevices) {
            System.out.println("Connection Established between hub " + i + " and End device with ID " + device.getId());
        }
    }

    public void printConnection(int i) {
        System.out.println("Connection successfully created between hub and device " + connectedDevices.get(i).getId());
    }

    public void broadcast(List<EndDevices> devices, int sender) {

        EndDevices senderDevice =  devices.get(sender - 1);
        data = senderDevice.sendData();
        System.out.println("\nMessage \"" + data + "\" is being broadcasted from the Hub");

        for (EndDevices device : connectedDevices) {
            device.putData(data);
        }
    }

    public void transmission(int sender, int receiver) {
        
        System.out.println("\nTransmission status : ");

        for (EndDevices device : connectedDevices) {

            String message = device.sendData();
            int currentDevice = device.getId();
            if (currentDevice != sender) {

                if (currentDevice != receiver) {
                    // discarded by all the devices other than receiver
                    System.out.println("\"" + message + "\" was received by device " + currentDevice + " but it was discarded");
                } else {
                    // accepted by receiver
                    System.out.println("Device " + currentDevice + " received message '" + message + "' successfully");
                }
            }
        }
    }

    public void broadcastAck(int sender, int receiver, Boolean ack) {

        System.out.println("Hub Broadcasts ACK\n");
        // EndDevices receiverDevice = connectedDevices.get(receiver - 1);

        if (ack != false) {
            for (EndDevices device : connectedDevices) {

                int currentDevice = device.getId();
                if (currentDevice != receiver) {

                    if (currentDevice != sender) {
                        // discarded by all the devices other than sender
                        System.out.println("ACK was received by device " + currentDevice + " but it was discarded");
                    } else {
                        // accepted by sender
                        System.out.println("ACK was received by device " + currentDevice + " and it was accepted");
                    }
                }
            }
        }
    }
}
