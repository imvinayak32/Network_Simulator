package layers;

import java.util.*;
import devices.EndDevices;
import devices.Router;
import devices.Switch;
import processing.Process;

public class NetworkLayer {

    private Switch switch1 = new Switch();
    private Switch switch2 = new Switch();
    private EndDevices endDevice = new EndDevices();
    private List<EndDevices> deviceList = new ArrayList<>();
    private List<Process> processList = new ArrayList<>();
    private Map<Integer, Process> processMap = new HashMap<>();
    private Map<Integer, Boolean> mapp = new HashMap<>();
    private String message;
    private String ip;
    private int scheme;
    private DataLayer dataLayerObj = new DataLayer();
    private Router routerObj = new Router();

    private String createNID() {
        return routerObj.generateNID();
    }

    public void stopSimulation(){
        System.exit(0);
    }
    public void startSimulation() {
        run();
    }

    private static class Pair<F, S> {
        private F first;
        private S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }

    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("\nSelect routing scheme : ");
            System.out.println("1. Static Routing");
            System.out.println("2. Dynamic Routing");
            scheme = scanner.nextInt();
            System.out.println();

            // Static Routing
            if (scheme == 1) {  

                Router router1 = new Router();
                Router router2 = new Router();
                Router router = new Router();

                String nid1 = createNID(); // X.X.X.X
                String nid2 = createNID();
                String nid3 = createNID();
                String nid4 = createNID(); // Y.Y.Y.Y

                router1.setAddress(nid1, nid2, "", dataLayerObj.generateMacAddress(), dataLayerObj.generateMacAddress(), "");
                router2.setAddress(nid3, nid4, "", dataLayerObj.generateMacAddress(), dataLayerObj.generateMacAddress(), "");

                List<String> ipv4 = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    if (i < 2) {
                        ip = router.generateClasslessIP(nid1);  // ipv4[0] & ipv4[1] using nid1
                        ipv4.add(ip);
                    } else {
                        ip = router.generateClasslessIP(nid4);  // ipv4[2] & ipv4[3] using nid4
                        ipv4.add(ip);
                    }
                }
                // End deviceList in Network 1
                deviceList.add(new EndDevices(1, dataLayerObj.generateMacAddress(), ipv4.get(0))); 
                deviceList.add(new EndDevices(2, dataLayerObj.generateMacAddress(), ipv4.get(1)));
                // End deviceList in Network 2
                deviceList.add(new EndDevices(4, dataLayerObj.generateMacAddress(), ipv4.get(2))); 
                deviceList.add(new EndDevices(5, dataLayerObj.generateMacAddress(), ipv4.get(3)));

                router1.connectSwitch(switch1);
                router2.connectSwitch(switch2);

                for (int i = 0; i < 4; i++) {
                    Process p = new Process();
                    processList.add(p);
                }
                for (int i = 0; i < 4; i++) {
                    processMap.put(processList.get(i).assignPortNumber(processMap), processList.get(i));
                }

                int sender, receiver;
                endDevice.prompt("Sender", 4, mapp);
                sender = scanner.nextInt();
                endDevice.prompt("Receiver", 4, mapp);
                receiver = scanner.nextInt();
                if (sender == receiver) { 
                    System.out.println("Sender and receiver can't be the same ");
                    continue;
                }

                int appProtocol;
                System.out.println("\nChoose a protocol :");
                System.out.println("1. HTTP");
                System.out.println("2. DNS");
                appProtocol = scanner.nextInt();

                System.out.println("\nEnter a message: ");
                message = scanner.next();
                System.out.println();

                EndDevices senderDevice = deviceList.get(sender - 1);
                EndDevices receiverDevice = deviceList.get(receiver - 1);
                senderDevice.putData(message);

                String sourceIp = senderDevice.getIP();
                String destinationIp = receiverDevice.getIP();
                System.out.println();

                int sourcePort, destinationPort;
                Random rand = new Random();
                int random = rand.nextInt(4);
                sourcePort = processList.get(random).assignPortNumber(processMap);
                // Port No : 1. HTTP - 80 & 2. DNS - 53
                destinationPort = (appProtocol == 1) ? 80 : 53;

                System.out.println("Source IP : " + sourceIp);
                System.out.println("Source Port : " + sourcePort);
                System.out.println("Destination IP : " + destinationIp);
                System.out.println("Destination Port : " + destinationPort);

                try { 
                    Thread.sleep(4000); 
                } 
                catch (InterruptedException e) {
                     e.printStackTrace(); 
                }
                System.out.println();
                
                // Initialize ARP cache
                for (int i = 0; i < 4; i++) { 
                    String deviceIp = deviceList.get(i).getIP();
                    String deviceMac = deviceList.get(i).getMAC();
                    deviceList.get(i).arpCache(deviceIp, deviceMac);
                }
                // Connecting end deviceList to respective switches
                switch1.connectedDevices.add(deviceList.get(0));
                switch1.connectedDevices.add(deviceList.get(1));
                switch2.connectedDevices.add(deviceList.get(2));
                switch2.connectedDevices.add(deviceList.get(3));
                senderDevice.printArpCache();

                // check = 0 -> If sender and receiver are in same network
                // check = 1 -> If sender and receiver are in different network
                boolean check = router.sameNID(sourceIp, destinationIp);

                // network = 1 -> first 6 bits of source ip and ip1 are same 
                // network = 2 -> first 6 bits of source ip and ip1 are different
                int network = router1.networkNo(sourceIp);

                if (check) {
                    // Both sender and receiver are in same network
                    String isPresent = senderDevice.arp.get(destinationIp);

                    // Receiver is not in ARP cache of sender
                    if (isPresent == null || isPresent.isEmpty()) {  
                        // Send ARP request
                        System.out.println("\nSender sends ARP request");

                        if (network == 1) {
                            String destinationMac = switch1.broadcastArp(destinationIp, router, network);
                            // Sender updates its ARP cache
                            senderDevice.arpCache(destinationIp, destinationMac);

                            System.out.println("Updated ARP cache :");
                            senderDevice.printArpCache();
                            // Send message
                            System.out.println();
                            try { 
                                Thread.sleep(1000); 
                            } 
                            catch (InterruptedException e) { 
                                e.printStackTrace(); 
                            }

                            System.out.println("\nLog of TCP Packets sent from client to server\n");
                            System.out.println("Protocol used : Selective Repeat\n");
                            endDevice.selectiveRepeat();
                            System.out.println();
                            switch1.sendMessage(senderDevice, destinationIp);
                            System.out.println();

                            if (destinationPort == 80) 
                                endDevice.http();
                            else 
                                endDevice.dns();
                        } 
                        else if (network == 2) {

                            String destinationMac = switch2.broadcastArp(destinationIp, router, network);
                            // Sender updates its ARP cache
                            senderDevice.arpCache(destinationIp, destinationMac);
                            System.out.println("Updated ARP cache :");
                            senderDevice.printArpCache();

                            // Send message
                            System.out.println();
                            try { 
                                Thread.sleep(1000); 
                            } 
                            catch (InterruptedException e) { 
                                e.printStackTrace(); 
                            }

                            System.out.println("Log of TCP Packets sent from client to server\n");
                            System.out.println("Protocol used : Selective Repeat\n");
                            endDevice.selectiveRepeat();
                            System.out.println();
                            switch2.sendMessage(senderDevice, destinationIp);
                            System.out.println();

                            if (destinationPort == 80) 
                                endDevice.http();
                            else
                                endDevice.dns();
                        }
                    } 
                    // Destination IP is in ARP cache of sender, no need to send ARP request
                    else { 
                        if (network == 1) {
                            System.out.println("\nLog of TCP Packets sent from client to server\n");
                            System.out.println("\nProtocol used : Selective Repeat\n");
                            System.out.println();
                            endDevice.selectiveRepeat();
                            System.out.println();
                            switch1.sendMessage(senderDevice, destinationIp);
                            System.out.println();

                            if (destinationPort == 80) 
                                endDevice.http();
                            else 
                                endDevice.dns();
                        } 
                        else if (network == 2) {
                            System.out.println("\nLog of TCP Packets sent from client to server\n");
                            System.out.println("\nProtocol used : Selective Repeat\n");
                            System.out.println();
                            endDevice.selectiveRepeat();
                            System.out.println();
                            switch2.sendMessage(senderDevice, destinationIp);
                            System.out.println();

                            if (destinationPort == 80) 
                                endDevice.http();
                            else 
                                endDevice.dns();
                        }
                    }
                }
                // Sender and receiver are in two different networks
                else {
                    // Sender checks for destination IP in its ARP cache
                    String result = senderDevice.arp.get(destinationIp);

                    // Receiver is not in ARP cache of sender
                    if (result == null || result.isEmpty()) {
                        // Sender sends ARP request
                        System.out.println();
                        System.out.println("Sender sends ARP request");

                        if (network == 1) {
                            // Switch broadcast ARP request
                            String destinationMac = switch1.broadcastArp(destinationIp, router1, network);
                            // Sender updates its ARP cache
                            senderDevice.arpCache(router1.IP1, destinationMac);
                            System.out.println("Updated ARP cache :");
                            senderDevice.printArpCache();

                            // Print routing table
                            router1.routingTable(router2, 1);
                            System.out.println();
                            router1.printRoutingTable(network);

                            // Check ARP CACHE of router
                            router1.arpCache(router1.IP1, router1.MAC1);
                            router1.arpCache(router1.IP2, router1.MAC2);
                            router1.arpCache(router2.IP1, router2.MAC1);
                            System.out.println();
                            router1.printArpCache(network);
                            System.out.println();

                            // Traverse through routing table and check for NID that matches destination IP
                            router1.routingDecision(destinationIp);

                            router2.routingTable(router1, 2);
                            System.out.println();
                            router2.printRoutingTable(2);

                            // Check ARP CACHE of router
                            router2.arpCache(router2.IP1, router2.MAC1);
                            router2.arpCache(router2.IP2, router2.MAC2);
                            router2.arpCache(router1.IP2, router1.MAC2);
                            System.out.println();
                            router2.printArpCache(2);
                            System.out.println();
                            System.out.println("Router 2 sends ARP request ");
                            System.out.println();

                            // Switch will broadcast ARP request
                            destinationMac = switch2.broadcastArp(destinationIp, router, 1);
                            // Sender updates its ARP cache
                            deviceList.get(sender - 1).arpCache(destinationIp, destinationMac);
                            System.out.println();
                            try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
                            System.out.println("Log of TCP Packets sent from client to server\n");
                            System.out.println("\nProtocol used : Selective Repeat\n");
                            System.out.println();
                            endDevice.selectiveRepeat();
                            System.out.println();
                            switch2.sendMessage(senderDevice, destinationIp);
                            System.out.println();
                            if (destinationPort == 80) {
                                endDevice.http();
                            } else {
                                endDevice.dns();
                            }
                        } 
                        else if (network == 2) {
                            // Switch broadcast ARP request
                            String destinationMac = switch2.broadcastArp(destinationIp, router2, network);
                            // Sender updates its ARP cache
                            deviceList.get(sender - 1).arpCache(router2.IP2, destinationMac);
                            System.out.println("Updated ARP cache :");
                            deviceList.get(sender - 1).printArpCache();
                            // Print routing table
                            router2.routingTable(router1, 2);
                            System.out.println();
                            router2.printRoutingTable(network);
                            // Check ARP CACHE of router
                            router2.arpCache(router2.IP1, router2.MAC1);
                            router2.arpCache(router2.IP2, router2.MAC2);
                            router2.arpCache(router1.IP2, router2.MAC2);
                            System.out.println();
                            router2.printArpCache(network);
                            System.out.println();
                            // Traverse through routing table and check for NID that matches destination IP
                            router2.routingDecision(destinationIp);

                            router1.routingTable(router2, 1);
                            System.out.println();
                            router1.printRoutingTable(1);
                            // Check ARP CACHE of router 1
                            router1.arpCache(router1.IP1, router1.MAC1);
                            router1.arpCache(router1.IP2, router1.MAC2);
                            router1.arpCache(router2.IP1, router2.MAC1);
                            System.out.println();
                            router1.printArpCache(1);
                            System.out.println();
                            System.out.println("Router 1 sends ARP request ");
                            System.out.println();
                            // Switch will broadcast ARP request
                            destinationMac = switch1.broadcastArp(destinationIp, router, 2);
                            // Sender updates its ARP cache
                            deviceList.get(sender - 1).arpCache(destinationIp, destinationMac);
                            System.out.println();
                            try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }
                            System.out.println("Log of TCP Packets sent from client to server\n");
                            System.out.println("\nProtocol used : Selective Repeat\n");
                            System.out.println();
                            endDevice.selectiveRepeat();
                            System.out.println();
                            switch1.sendMessage(deviceList.get(sender - 1), destinationIp);
                            System.out.println();
                            if (destinationPort == 80) {
                                endDevice.http();
                            } else {
                                endDevice.dns();
                            }
                        }
                    }
                }
            }
            // Dynamic Routing
            else if (scheme == 2) {
                int enter;

                System.out.println("\nProtocol Used : RIP (Routing Information Protocol) \n");
                // RIP - Configure routers

                int numVertices;
                System.out.print("Enter the number of Routers: ");
                numVertices = scanner.nextInt();

                if (numVertices > 15) {
                    System.out.println("Maximum Hop Count in RIP is 15");
                    System.out.println("Enter a valid number ");
                    continue;
                }

                List<Pair<Router, Router>> routers = new ArrayList<>();
                List<List<Integer>> edges = new ArrayList<>();
                int numEdges;

                System.out.println("\nEnter the number of links : ");
                numEdges = scanner.nextInt();

                System.out.println("\nInput router number as per 0 based indexing");
                for (int i = 0; i < numEdges; ++i) {

                    int source, destination, wt;

                    System.out.println("Edge " + (i + 1) + ":\n");
                    System.out.print("First Router: ");
                    source = scanner.nextInt();
                    Router router1 = new Router(source);

                    System.out.print("Second Router: ");
                    destination = scanner.nextInt();
                    Router router2 = new Router(destination);

                    routers.add(new Pair<>(router1, router2));
                }
                for (Pair<Router, Router> it : routers) {
                    List<Integer> temp = Arrays.asList(it.getFirst().getId(), it.getSecond().getId(), 1);
                    edges.add(new ArrayList<>(temp));
                    temp = Arrays.asList(it.getSecond().getId(), it.getFirst().getId(), 1);
                    edges.add(new ArrayList<>(temp));
                }

                Router router = new Router();
                router.initialRoutingTable(edges, numVertices);
                System.out.println();
                for (int source = 0; source < numVertices; ++source) {
                    router.rip(edges, numVertices, source);
                }

            }
            // Not Static or Dynamic Routing
            else {
                System.out.println("Invalid Choice");
                continue;
            }
            break;
        }
    }
}
