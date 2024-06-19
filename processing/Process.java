/* Read 1
 *   It generates a random integer value for the port between 0 (inclusive) and 65535 (exclusive)
 *   using r.nextInt(65535).
 */
package processing;
import java.util.*;

public class Process {
    
    public int processID;
    public int portNumber;

    public int assignPortNumber(Map<Integer, Process> processMap) {

        Random r = new Random();
        int port = r.nextInt(65535); // Read 1

        // if generated port is already present in processMap or less than 1024.
        while (processMap.containsKey(port) || port < 1024) {
            port = r.nextInt(65535);
        }
        
        return port;
    }
}
