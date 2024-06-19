
import java.util.*;

import layers.PhysicalLayer;
import layers.DataLayer;
import layers.NetworkLayer;

public class Prompt {
    public void run() {

        Scanner scanner = new Scanner(System.in);
        while (true) {

            int choice, hubs = 0;
            Map<Integer, String> allDevices = new HashMap<>();
            allDevices.put(0, "Hub");
            allDevices.put(1, "Switch");
            allDevices.put(2, "Router");
            System.out.println("\n\nChoose a device ");
            
            for (Map.Entry<Integer, String> entry : allDevices.entrySet()) {
                System.out.println(entry.getKey() + 1 + ": " + entry.getValue());
            }

            System.out.println("\nYour choice: ");
            choice = scanner.nextInt();

            switch (choice) {

                case 1: {
                    System.out.println("\nEnter the number of hubs required: ");
                    hubs = scanner.nextInt();

                    if (hubs == 1) {
                        PhysicalLayer p = new PhysicalLayer();
                        p.run();
                    } else {
                        DataLayer d = new DataLayer();
                        d.run(2, hubs);
                    }
                    break;
                }

                case 2: {
                    DataLayer d = new DataLayer();
                    d.run(1, hubs);
                    break;
                }

                case 3: {
                    NetworkLayer n = new NetworkLayer();
                    n.run();
                    break;
                }
                
                default:
                    System.out.println("Invalid Entry");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        Prompt p = new Prompt();
        p.run();
    }
}
