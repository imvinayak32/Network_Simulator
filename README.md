# Network Simulator

## Overview

Network Simulator is a Java-based application that simulates a basic network topology. It allows users to add main devices (Hub, Switch, Router) and end devices, create connections between them, and send messages within the network. This simulator is designed to provide a simple stimualtion.

[Screencast from 2024-06-19 18-33-04.webm](https://github.com/imvinayak32/Network_Simulator/assets/131988601/d30d6761-d04f-4d06-9899-f9e1cc20c02e)

## Features

- Add main devices: Hub, Switch, Router.
- Add end devices and automatically connect them to all main devices.
- Visualize the network topology with links between devices.
- Send messages between devices with acknowledgment.
- Clear all devices and reset the topology.

## Requirements

- Java Development Kit (JDK) 8 or above.
- An Integrated Development Environment (IDE) like IntelliJ IDEA, Eclipse, or NetBeans.

## Getting Started

### Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/yourusername/network-simulator.git
    ```

2. **Navigate to the project directory:**

    ```bash
    cd network-simulator
    ```

3. **Open the project in your preferred IDE.**

### Running the Application

1. **Locate the `NetworkSimulatorGUI.java` file in your IDE.**

2. **Run the `main` method in `NetworkSimulatorGUI.java`.**

3. **The Network Simulator GUI will launch.**

### Using the Application

1. **Add Main Devices:**

    - Select the main device type (Hub, Switch, Router).
    - Enter the number of main devices to add.
    - Click "Add Main Device".

2. **Add End Devices:**

    - Enter the number of end devices to add.
    - Click "Add End Devices".

    **Note:** Each end device will be connected to all main devices, and links will be created.

3. **Send a Message:**

    - Enter the sender ID.
    - Enter the receiver ID.
    - Enter the message text.
    - Click "Send Message".

4. **Remove Topology:**

    - Click the "Remove Topology" button to clear all devices and reset the network topology.

## Project Structure

  - **devices/** : Contains classes representing different types of network devices (e.g., `EndDevices`, `Hub`, `Switch`, `Router`).
  - **layers/** : Contains classes representing different network layers (e.g., `PhysicalLayer`, `DataLayer`, `NetworkLayer`).
  - **processing/** : Contains classes related to message processing.
  - **NetworkSimulatorGUI.java** : The main class for the GUI application.
  - **Prompt.java** : The main class for running in terminal.

## Libraries Used

- **Swing**: For building the graphical user interface.
- **AWT**: For handling basic GUI components and painting.
