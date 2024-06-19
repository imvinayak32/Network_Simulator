import devices.*;
import layers.*;
import processing.Process;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class NetworkSimulatorGUI extends JFrame {

    private JPanel controlPanel;
    private JComboBox<String> mainDeviceTypeCombo;
    private JTextField noOfMainDevicesField;
    private JTextField noOfEndDevicesField;
    private JPanel addMainDevicePanel;
    private JButton addMainDeviceButton;
    private JButton removeTopologyButton;
    private JPanel addEndDevicesPanel;
    private JButton addEndDevicesButton;
    private JPanel messagePanel;
    private JButton sendMessageButton;
    private JTextField senderField;
    private JTextField receiverField;
    private JTextField messageField;
    private JTextArea outputArea;
    private TopologyPanel topologyPanel;
    private java.util.List<Point> deviceLocations;
    private Map<Integer, String> messageHistory;

    private java.util.List<EndDevices> devices = new ArrayList<>();
    private Map<Integer, EndDevices> deviceMap = new HashMap<>();
    private Map<Integer, Hub> hubs = new HashMap<>();
    private Map<Integer, Switch> switches = new HashMap<>();
    private Map<Integer, Router> routers = new HashMap<>();

    private PhysicalLayer physicalLayer = new PhysicalLayer();
    private DataLayer dataLayer = new DataLayer();
    private NetworkLayer networkLayer = new NetworkLayer();
    private Hub hub;
    private Switch switchDevice;
    private Router router;

    public NetworkSimulatorGUI() {

        setTitle("Network Simulator");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        deviceLocations = new ArrayList<>();
        messageHistory = new HashMap<>();

        controlPanel = new JPanel(new FlowLayout());
        controlPanel.setPreferredSize(new Dimension(300, 900));
        controlPanel.setBackground(Color.LIGHT_GRAY);
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control Panel"));


        // Add Main device Panel
        addMainDevicePanel = new JPanel();
        addMainDevicePanel.setLayout(new GridBagLayout());
        addMainDevicePanel.setBorder(BorderFactory.createTitledBorder("Main Device Panel"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] options = {"Hub", "Switch", "Router"};
        mainDeviceTypeCombo = new JComboBox<>(options);
        noOfMainDevicesField = new JTextField(5);

        addMainDeviceButton = new JButton("Add Main Device");
        addMainDeviceButton.addActionListener(new AddMainDeviceButtonListener());

        gbc.gridx = 0;
        gbc.gridy = 0;
        addMainDevicePanel.add(new JLabel("Main Device Type:"), gbc);

        gbc.gridx = 1;
        addMainDevicePanel.add(mainDeviceTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        addMainDevicePanel.add(new JLabel("Number of Main Devices:"), gbc);

        gbc.gridx = 1;
        addMainDevicePanel.add(noOfMainDevicesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        addMainDevicePanel.add(addMainDeviceButton, gbc);

        controlPanel.add(addMainDevicePanel);


        // End Device Panel
        addEndDevicesPanel = new JPanel(new GridBagLayout());
        addEndDevicesPanel.setBorder(BorderFactory.createTitledBorder("Add End Devices"));
        addEndDevicesPanel.setVisible(true);
        GridBagConstraints gbcEndDevices = new GridBagConstraints();
        gbcEndDevices.fill = GridBagConstraints.HORIZONTAL;
        gbcEndDevices.insets = new Insets(5, 5, 5, 5);

        noOfEndDevicesField = new JTextField(5);
        addEndDevicesButton = new JButton("Add End Devices");
        addEndDevicesButton.addActionListener(new AddEndDevicesButtonListener());

        gbcEndDevices.gridx = 0;
        gbcEndDevices.gridy = 0;
        addEndDevicesPanel.add(new JLabel("Number of End Devices:"), gbcEndDevices);

        gbcEndDevices.gridx = 1;
        addEndDevicesPanel.add(noOfEndDevicesField, gbcEndDevices);

        gbcEndDevices.gridx = 0;
        gbcEndDevices.gridy = 1;
        gbcEndDevices.gridwidth = 2;
        addEndDevicesPanel.add(addEndDevicesButton, gbcEndDevices);

        controlPanel.add(addEndDevicesPanel);


        // Message Panel
        messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Message Panel"));
        messagePanel.setVisible(true);
        GridBagConstraints gbcMessage = new GridBagConstraints();
        gbcMessage.fill = GridBagConstraints.HORIZONTAL;
        gbcMessage.insets = new Insets(5, 5, 5, 5);

        senderField = new JTextField();
        receiverField = new JTextField();
        messageField = new JTextField();

        sendMessageButton = new JButton("Send Message");
        sendMessageButton.addActionListener(new SendMessageButtonListener());

        gbcMessage.gridx = 0;
        gbcMessage.gridy = 0;
        messagePanel.add(new JLabel("Sender ID:"), gbcMessage);

        gbcMessage.gridx = 1;
        messagePanel.add(senderField, gbcMessage);

        gbcMessage.gridx = 0;
        gbcMessage.gridy = 1;
        messagePanel.add(new JLabel("Receiver ID:"), gbcMessage);

        gbcMessage.gridx = 1;
        messagePanel.add(receiverField, gbcMessage);

        gbcMessage.gridx = 0;
        gbcMessage.gridy = 2;
        messagePanel.add(new JLabel("Message:"), gbcMessage);

        gbcMessage.gridx = 1;
        messagePanel.add(messageField, gbcMessage);

        gbcMessage.gridx = 0;
        gbcMessage.gridy = 3;
        gbcMessage.gridwidth = 2;
        messagePanel.add(sendMessageButton, gbcMessage);

        controlPanel.add(messagePanel);

        // Remove Topology Button
        removeTopologyButton = new JButton("Remove Topology");
        removeTopologyButton.addActionListener(new RemoveTopologyButtonListener());
        controlPanel.add(removeTopologyButton);


        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        topologyPanel = new TopologyPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(topologyPanel), scrollPane);
        splitPane.setResizeWeight(0.7);

        add(controlPanel, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);
    }

    private class AddMainDeviceButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String deviceType = (String) mainDeviceTypeCombo.getSelectedItem();
            int numDevices = Integer.parseInt(noOfMainDevicesField.getText());

            if (deviceType.isEmpty() || numDevices <= 0) {
                outputArea.append("Invalid input.\n");
                return;
            }

            for (int i = 0; i < numDevices; i++) {
                int id = deviceLocations.size() + 1;
                Point point = new Point(deviceLocations.size() * 50 + 20, 50);
                deviceLocations.add(point);

                switch (deviceType) {
                    case "Hub":
                        hub = new Hub(id);
                        hubs.put(id, hub);
                        topologyPanel.addDevice(point, "Hub " + id, Color.RED, "H");
                        break;
                    case "Switch":
                        switchDevice = new Switch(id);
                        switches.put(id, switchDevice);
                        topologyPanel.addDevice(point, "Switch " + id, Color.GREEN, "S");
                        break;
                    case "Router":
                        router = new Router(id);
                        routers.put(id, router);
                        topologyPanel.addDevice(point, "Router " + id, Color.BLUE, "R");
                        break;
                }
            }

            outputArea.append("Added " + numDevices + " " + deviceType + "(s)\n");
            addEndDevicesPanel.setVisible(true);
        }
    }

    private class AddEndDevicesButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int numEndDevices = Integer.parseInt(noOfEndDevicesField.getText());

            if (numEndDevices <= 0) {
                outputArea.append("Invalid input.\n");
                return;
            }

            for (int i = 0; i < numEndDevices; i++) {
                int id = devices.size() + 1;
                EndDevices endDevice = new EndDevices(id, physicalLayer.generateMacAddress(), "IP" + id);
                devices.add(endDevice);
                deviceMap.put(id, endDevice);

                Point point = new Point((deviceLocations.size() * 50 + 20), (deviceLocations.size() % 2 == 0) ? 100 : 150);
                deviceLocations.add(point);
                topologyPanel.addDevice(point, "End " + id, Color.ORANGE, "E");

                // Connect end devices to all main devices
                for (Hub hub : hubs.values()) {
                    hub.topology(endDevice);
                    topologyPanel.addLink(point, deviceLocations.get(hub.getId() - 1));
                }

                for (Switch switchDevice : switches.values()) {
                    switchDevice.topology(endDevice);
                    topologyPanel.addLink(point, deviceLocations.get(switchDevice.getId() - 1));
                }

                for (Router router : routers.values()) {
                    router.topology(endDevice);
                    topologyPanel.addLink(point, deviceLocations.get(router.getId() - 1));
                }
            }

            outputArea.append("Added " + numEndDevices + " End Device(s)\n");
        }
    }

    private class RemoveTopologyButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            devices.clear();
            deviceMap.clear();
            hubs.clear();
            switches.clear();
            routers.clear();
            deviceLocations.clear();
            topologyPanel.clearDevices();
            outputArea.append("Topology removed.\n");
        }
    }

    private class SendMessageButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int sender = Integer.parseInt(senderField.getText());
            int receiver = Integer.parseInt(receiverField.getText());
            String message = messageField.getText();

            if (message.isEmpty()) {
                outputArea.append("Message cannot be empty.\n");
                return;
            }

            if (deviceMap.containsKey(sender) && deviceMap.containsKey(receiver)) {
                EndDevices senderDevice = deviceMap.get(sender);
                EndDevices receiverDevice = deviceMap.get(receiver);

                // First added hub
                if (!hubs.isEmpty()) {
                    Hub hub = hubs.values().iterator().next();
                    hub.broadcast(devices, sender);
                    hub.transmission(sender, receiver);
                    senderDevice.sendAck(receiver);
                    hub.broadcastAck(sender, receiver, true);
                }

                outputArea.append("Message from " + sender + " to " + receiver + ": " + message + "\n");
                messageHistory.put(sender, message);
            } else {
                outputArea.append("Invalid Sender or Receiver ID\n");
            }
        }
    }

    private class TopologyPanel extends JPanel {

        private java.util.List<Point> devices;
        private java.util.List<String> deviceLabels;
        private java.util.List<Color> deviceColors;
        private java.util.List<String> deviceShapes;
        private java.util.List<Line> links;

        public TopologyPanel() {
            this.devices = new ArrayList<>();
            this.deviceLabels = new ArrayList<>();
            this.deviceColors = new ArrayList<>();
            this.deviceShapes = new ArrayList<>();
            this.links = new ArrayList<>();
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Point clickPoint = e.getPoint();
                    for (int i = 0; i < devices.size(); i++) {
                        Point devicePoint = devices.get(i);
                        Rectangle deviceBounds = new Rectangle(devicePoint.x, devicePoint.y, 30, 30);
                        if (deviceBounds.contains(clickPoint)) {
                            showDeviceDetails(i + 1);
                        }
                    }
                }
            });
        }

        public void addDevice(Point point, String label, Color color, String shape) {
            devices.add(point);
            deviceLabels.add(label);
            deviceColors.add(color);
            deviceShapes.add(shape);
            repaint(); // Trigger repaint to update the topology
        }

        public void clearDevices() {
            devices.clear();
            deviceLabels.clear();
            deviceColors.clear();
            deviceShapes.clear();
            links.clear();
            repaint();
        }

        public void addLink(Point from, Point to) {
            links.add(new Line(from, to));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw connections
            g.setColor(Color.BLACK);
            for (Line link : links) {
                g.drawLine(link.from.x + 15, link.from.y + 15, link.to.x + 15, link.to.y + 15); // Adjust for shape center
            }

            // Draw each device with its shape, color, and label
            for (int i = 0; i < devices.size(); i++) {
                Point point = devices.get(i);
                String label = deviceLabels.get(i);
                Color color = deviceColors.get(i);
                String shape = deviceShapes.get(i);

                g.setColor(color);
                if ("H".equals(shape)) {
                    g.fillRect(point.x, point.y, 30, 30);
                } else if ("S".equals(shape)) {
                    g.fillRoundRect(point.x, point.y, 30, 30, 10, 10);
                } else if ("R".equals(shape)) {
                    g.fillOval(point.x, point.y, 30, 30);
                } else if ("E".equals(shape)) {
                    g.fillOval(point.x, point.y, 20, 20);
                }

                g.setColor(Color.BLACK);
                g.drawString(label, point.x, point.y - 5);
            }
        }

        private void showDeviceDetails(int deviceId) {
            EndDevices device = deviceMap.get(deviceId);
            if (device != null) {
                JOptionPane.showMessageDialog(this, "Device ID: " + deviceId + "\nMAC Address: " + device.getMAC() + "\nIP Address: " + device.getIP(), "Device Details", JOptionPane.INFORMATION_MESSAGE);
            } else if (hubs.containsKey(deviceId)) {
                JOptionPane.showMessageDialog(this, "Hub ID: " + deviceId, "Device Details", JOptionPane.INFORMATION_MESSAGE);
            } else if (switches.containsKey(deviceId)) {
                JOptionPane.showMessageDialog(this, "Switch ID: " + deviceId, "Device Details", JOptionPane.INFORMATION_MESSAGE);
            } else if (routers.containsKey(deviceId)) {
                JOptionPane.showMessageDialog(this, "Router ID: " + deviceId, "Device Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Device not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class Line {
        Point from, to;

        Line(Point from, Point to) {
            this.from = from;
            this.to = to;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NetworkSimulatorGUI gui = new NetworkSimulatorGUI();
            gui.setVisible(true);
        });
    }
}
