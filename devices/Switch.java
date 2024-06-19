package devices;

import java.util.*;

public class Switch {

    private int switchId;
    private Map<Integer, List<Integer>> hubDeviceMap = new HashMap<>();
    private Map<Integer, String> macTable = new HashMap<>();
    private List<Hub> connectedHubs = new ArrayList<>();
    private String data;
    public List<EndDevices> connectedDevices = new ArrayList<>();

    public Switch() {
        this.data = "";
    }

    public Switch(String message) {
        this.data = message;
    }

    public Switch(int Id) {
        this.switchId = Id;
    }

    public int getId() {
        return switchId;
    }

    public void topology(EndDevices devices) {
        connectedDevices.add(devices);
    }

    public void printConnection(int i) {
        System.out.println("Connection successfully established between switch & device with MAC Address: " + connectedDevices.get(i).getMAC());
    }

    public void topology(Hub hubs) {
        connectedHubs.add(hubs);
    }

    public void hubPrintConnection(int i) {
        System.out.println("Connection successfully established between switch & hub with Hub ID: " + connectedHubs.get(i).getId());
    }

    public List<EndDevices> getDevices() {
        return connectedDevices;
    }

    public void hubToDeviceMap(int hubId, List<EndDevices> devices) {

        List<Integer> deviceIds = new ArrayList<>();
        for (EndDevices device : devices) {
            deviceIds.add(device.getId());
        }
        hubDeviceMap.put(hubId, deviceIds);
    }

    public void printHubToDeviceMap() {
        System.out.println("Mapping of Hub and End devices, stored in switch");
        System.out.println();
        for (Map.Entry<Integer, List<Integer>> entry : hubDeviceMap.entrySet()) {
            int hubId = entry.getKey() + 1;
            List<Integer> deviceIds = entry.getValue();
            System.out.print("End devices ");
            for (int i = 0; i < deviceIds.size(); i++) {
                System.out.print(deviceIds.get(i));
                if (i < deviceIds.size() - 1) {
                    System.out.print(",");
                }
            }
            System.out.println(" are connected to hub " + hubId);
        }
        System.out.println();
    }
    // Find deviceID in list of IDs(value) for every key in hubDeviceMap
    public int findHubForDevice(int deviceId) {
        for (Map.Entry<Integer, List<Integer>> entry : hubDeviceMap.entrySet()) {
            int hubId = entry.getKey();
            List<Integer> deviceIds = entry.getValue();
            if (deviceIds.contains(deviceId)) {
                return hubId;
            }
        }
        return -1;
    }

    public void macTable() {
        for (EndDevices device : connectedDevices) {
            int id = device.getId();
            String mac = device.getMAC();
            macTable.put(id, mac);
        }
    }

    public int receiveData(int sender, int receiver, String message) {
        data = message;

        int sourceHub = findHubForDevice(sender);
        int destinationHub = findHubForDevice(receiver);

        System.out.println("Switch received \"" + message + "\" from hub " + (sourceHub + 1));
        connectedHubs.get(destinationHub).data = message;
        System.out.println("Switch sends " + message + " to hub " + (destinationHub + 1));
        return destinationHub;
    }

    public void transmission(List<EndDevices> devices, int sender, int receiver) {
        System.out.println("\nTransmission Status :");
        
        boolean token = connectedDevices.get(sender - 1).token;
        String data = devices.get(sender - 1).sendData();
        if (token) {
            System.out.println(data + " sent successfully from device with MAC " + macTable.get(sender) + " to " + macTable.get(receiver) + " via switch");
        }
    }

    public void sendAck(int sender) {
        boolean ack = connectedDevices.get(sender - 1).ack;
        if (ack) {
            System.out.println("ACK was successfully received by sender with MAC Address " + macTable.get(sender));
        } else {
            System.out.println("ACK not received by sender");
        }
    }

    public void receiveAck(int destinationHub) {
        if (connectedHubs.get(destinationHub).ack) {
            System.out.println("Hub " + (destinationHub + 1) + " sends ACK to switch");
        }
    }

    public void sendAckToHub(int sourceHub) {
        connectedHubs.get(sourceHub).ack = true;
        System.out.println("Switch sends ACK to Hub " + (sourceHub + 1));
    }

    public String broadcastArp(String destinationIp, Router r, int network) {
        System.out.println("\nSwitch broadcast ARP request : ");
        System.out.println("Who is " + destinationIp + " ?");

        for (EndDevices device : connectedDevices) {
            String result = device.arp.get(destinationIp);
            if (result != null && !result.isEmpty()) {
                System.out.println("ARP Reply :- Source IP : " + device.getIP() + " Source MAC : " + device.getMAC());
                return device.getMAC();
            }
        }
        if (network == 1) {
            System.out.println("ARP Reply by Default Gateway : Source IP : " + r.IP1 + " Source MAC : " + r.MAC1);
            return r.MAC1;
        } 
        else {
            System.out.println("ARP Reply by Default Gateway : Source IP : " + r.IP2 + " Source MAC : " + r.MAC2);
            return r.MAC2;
        }
    }

    public void sendMessage(EndDevices devices, String destinationIP) {
        System.out.println();
        String destinationMac = devices.arp.get(destinationIP);
        String sourceMac = devices.getMAC();
        System.out.println(devices.sendData() + " sent successfully from device with MAC " + sourceMac + " to " + destinationMac);
    }
}
