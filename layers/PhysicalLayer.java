/* Read 1
 * 1. mapp.getOrDefault(sender, false): This line retrieves the value associated with the key sender from the mapp map. If the key is not present in the map, it returns the default value false.
   2. !mapp.getOrDefault(sender, false): The exclamation mark (!) negates the result of the previous expression. So, if the sender is not found in the map, or if the value associated with the sender is false, the condition evaluates to true.
   3. If the condition is true, the code block within the if statement is executed, which prints "Invalid Entry" and then continues the loop.
*/
/* Read 2
 * The scanner.nextLine() method is called twice. The first call consumes the newline character that remains in the input buffer 
 * after the user enters the number of end devices. This ensures that the subsequent scanner.nextLine() call reads the actual message input.
 */
package layers;

import java.util.*;

import devices.EndDevices;
import devices.Hub;

public class PhysicalLayer {
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
    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            int noOfDevice, sender, receiver;
            String data;
            Map<Integer, Boolean> mapp = new HashMap<>();
            Hub hub = new Hub();
            EndDevices end = new EndDevices();
            List<EndDevices> deviceList = new ArrayList<>();  // Vector of objects of End devices

            System.out.println("\nEnter the number of end devices: ");
            noOfDevice = scanner.nextInt();

            if (noOfDevice < 2) {
                System.out.println("\nThere should be at least two devices. Enter a valid number");
                continue;
            }
            
            // Creating end devices
            for (int i = 0; i < noOfDevice; i++) {  
                deviceList.add(new EndDevices(i + 1, "", ""));  
                hub.topology(deviceList.get(i));   // Connecting end devices with hub
                                                   // connectionDevice<End device>
                if (i == 0) {
                    System.out.println("\nConnection status :\n");
                }
                hub.printConnection(i); // Print connected devices with hub
            }

            // Selecting Sender
            end.prompt("sender", noOfDevice, mapp); 
            sender = scanner.nextInt();
            if (!mapp.getOrDefault(sender, false)) {  // Read 1
                System.out.println("Invalid Entry");
                continue;
            }

            // Selecting Receiver
            end.prompt("receiver", noOfDevice, mapp); 
            receiver = scanner.nextInt();
            if (!mapp.getOrDefault(receiver, false)) {
                System.out.println("Invalid Entry");
                continue;
            }

            // If sender and receiver are the same
            if (sender == receiver) {  
                System.out.println("Sender and receiver can't be the same");
                continue;
            }

            System.out.println("\nInput the message : ");
            scanner.nextLine(); // Read 2
            data = scanner.nextLine();

            EndDevices SenderDevice = deviceList.get(sender - 1);
            SenderDevice.putData(data);

            hub.broadcast(deviceList, sender);  
            hub.transmission(sender, receiver); 

            SenderDevice.sendAck(receiver);
            hub.broadcastAck(sender, receiver, true);
            break;
        }
    }
}
