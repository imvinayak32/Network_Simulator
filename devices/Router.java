package devices;

import java.util.*;

public class Router extends EndDevices {
    public int id;
    public int source;
    public int destination;
    public int weight;
    public final int INF = 99999;
    public String IP1, IP2, IP3, MAC1, MAC2, MAC3;
    public List<Switch> connectedDevices = new ArrayList<>();
    public Map<String, Pair<String, String>> routingTable = new HashMap<>();
    public List<EndDevices> routerEndDeviceList;

    public Router() {}

    public Router(int Id) {
        this.id = Id;
    }

    public int getId() {
        return id;
    }

    public void setAddress(String IP1, String IP2, String IP3, String MAC1, String MAC2, String MAC3) {
        this.IP1 = IP1;
        this.IP2 = IP2;
        this.MAC1 = MAC1;
        this.MAC2 = MAC2;
        this.IP3 = IP3;
    }

    public void topology(EndDevices endD){
        routerEndDeviceList.add(endD);
    }
    public void connectSwitch(Switch s) {
        connectedDevices.add(s);
    }

    public int random(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

    public String generateNID() {

        StringBuilder NID = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int octet = random(0, 255);
            if (i < 2) {
                NID.append(octet).append(".");
            } 
            else {
                NID.append(0).append(".");
            }
        }
        NID.deleteCharAt(NID.length() - 1);
        return NID.toString();
    }

    public String generateClasslessIP(String NID) {

        NID = NID.substring(0, NID.length() - 1);
        // A loop runs four times to generate four octets of the IP address.
        for (int i = 0; i < 4; i++) {
            int octet = random(0, 255);
            if (i == 3) {
                NID += octet + "/24";  // class C (n = 24)
            }
        }
        return NID;
    }

    public boolean sameNID(String sourceIp, String destinationIp) {
        for (int i = 0; i < 6; i++) {
            if (sourceIp.charAt(i) != destinationIp.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public int networkNo(String sourceIp) {
        return IP1.substring(0, 6).equals(sourceIp.substring(0, 6)) ? 1 : 2;
    }

    public void routingTable(Router r, int source) {
        routingTable.put(IP1, new Pair<>("1", "0"));
        routingTable.put(IP2, new Pair<>("2", "0"));
        if (source == 1) {
            routingTable.put(r.IP2, new Pair<>("2", r.IP1));
        } else {
            routingTable.put(r.IP1, new Pair<>("2", r.IP2));
        }
    }

    public void printRoutingTable(int source) {
        System.out.println();
        System.out.println("Routing Table of Router " + source);
        System.out.println(String.format("%-15s %-12s %-10s", "NID", "Interface", "Next Hop"));
        System.out.println(String.format("%-39s", "").replace(' ', '-'));
        for (Map.Entry<String, Pair<String, String>> entry : routingTable.entrySet()) {
            System.out.println(String.format("%-15s %-12s %-10s", entry.getKey(), entry.getValue().getFirst(), entry.getValue().getSecond()));
        }
    }

    public void routingDecision(String destinationIp) {
        for (Map.Entry<String, Pair<String, String>> entry : routingTable.entrySet()) {
            if (sameNID(entry.getKey(), destinationIp)) {
                System.out.println("Sending packet to Network " + entry.getKey() + " on interface " + entry.getValue().getFirst());
                break;
            }
        }
    }

    public void printArpCache(int source) {
        System.out.println();
        System.out.println("ARP Cache of Router " + source + " is as :");
        System.out.println();
        System.out.println("IP\t\tMAC\n");
        for (Map.Entry<String, String> entry : arp.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
            System.out.println();
        }
    }

    public void rip(List<List<Integer>> edges, int numVertices, int source) {
        int[] distance = new int[numVertices];
        Arrays.fill(distance, INF);
        distance[source] = 0;
        int[] nextHop = new int[numVertices];
        Arrays.fill(nextHop, -1);

        for (int i = 1; i <= numVertices - 1; ++i) {
            for (List<Integer> edge : edges) {
                int u = edge.get(0);
                int v = edge.get(1);
                int weight = edge.get(2);

                if (distance[u] != INF && distance[u] + weight < distance[v]) {
                    distance[v] = distance[u] + weight;
                    nextHop[v] = u;
                }
            }
        }

        System.out.println();
        System.out.println("Final Routing table");
        System.out.println("Routing table for Router " + source + ":");
        System.out.println("Destination\tNext Hop\tCost");
        for (int i = 0; i < numVertices; ++i) {
            System.out.print("R" + i + "\t\t");
            if (distance[i] == INF) {
                System.out.print("-\t\t");
            } else if (i == source) {
                System.out.print("-\t\t" + distance[i] + "\n");
            } else {
                if (nextHop[i] != -1 && source != nextHop[i]) {
                    System.out.print("R" + nextHop[i] + "\t\t");
                } else {
                    System.out.print("-\t\t");
                }
                System.out.print(distance[i] + "\n");
            }
        }
    }

    public void initialRoutingTable(List<List<Integer>> edges, int numVertices) {
        System.out.println("Initial Routing Tables:");
        for (int source = 0; source < numVertices; ++source) {
            System.out.println("Routing table for Router " + source + ":");
            System.out.println("Destination\tNext Hop\tCost");
            for (int i = 0; i < numVertices; ++i) {
                System.out.print("R" + i + "\t\t");
                if (i == source) {
                    System.out.print("-\t\t0\n");
                } else {
                    boolean directlyConnected = false;
                    for (List<Integer> edge : edges) {
                        if (edge.get(0) == source && edge.get(1) == i) {
                            System.out.print("-\t\t" + edge.get(2) + "\n");
                            directlyConnected = true;
                            break;
                        }
                    }
                    if (!directlyConnected) {
                        System.out.print("-\t\t-\n");
                    }
                }
            }
            System.out.println();
        }
    }

    private static class Pair<F, S> {
        private F first;
        private S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() { return first; }
        public S getSecond() { return second; }
    }
}
