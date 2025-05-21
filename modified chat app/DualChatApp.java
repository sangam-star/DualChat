import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class DualChatApp extends JFrame {
    private JTextArea clientChatArea, serverChatArea;
    private JTextField clientInput, serverInput;
    private JButton clientSend, serverSend;

    private ServerSocket serverSocket;
    private Socket serverSideSocket;
    private Socket clientSocket;

    private BufferedReader clientReader, serverReader;
    private PrintWriter clientWriter, serverWriter;

    public DualChatApp() {
        setTitle("Client ↔ Server Chat");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        initClientPanel();
        initServerPanel();
        setupNetworking();

        setVisible(true);
    }

    private void initClientPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Client Side"));

        clientChatArea = new JTextArea();
        clientChatArea.setEditable(false);

        clientInput = new JTextField();
        clientSend = new JButton("Send");

        clientSend.addActionListener(e -> sendFromClient());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(clientInput, BorderLayout.CENTER);
        inputPanel.add(clientSend, BorderLayout.EAST);

        panel.add(new JScrollPane(clientChatArea), BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void initServerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Side"));

        serverChatArea = new JTextArea();
        serverChatArea.setEditable(false);

        serverInput = new JTextField();
        serverSend = new JButton("Send");

        serverSend.addActionListener(e -> sendFromServer());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(serverInput, BorderLayout.CENTER);
        inputPanel.add(serverSend, BorderLayout.EAST);

        panel.add(new JScrollPane(serverChatArea), BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void setupNetworking() {
        new Thread(() -> {
            try {
                // Start Server
                serverSocket = new ServerSocket(7777);
                appendServerText("Server started. Waiting for connection...");
                serverSideSocket = serverSocket.accept();
                appendServerText("Client connected.");

                serverReader = new BufferedReader(new InputStreamReader(serverSideSocket.getInputStream()));
                serverWriter = new PrintWriter(serverSideSocket.getOutputStream(), true);

                // Read from client (on server side)
                new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = serverReader.readLine()) != null) {
                            appendServerText("Client: " + msg);
                        }
                    } catch (IOException e) {
                        appendServerText("Client disconnected.");
                    }
                }).start();

            } catch (IOException e) {
                appendServerText("Server error: " + e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                // Connect client
                Thread.sleep(500); // Wait for server to start
                clientSocket = new Socket("127.0.0.1", 7777);
                appendClientText("Connected to server.");

                clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read from server (on client side)
                new Thread(() -> {
                    try {
                        String msg;
                        while ((msg = clientReader.readLine()) != null) {
                            appendClientText("Server: " + msg);
                        }
                    } catch (IOException e) {
                        appendClientText("Server disconnected.");
                    }
                }).start();

            } catch (IOException | InterruptedException e) {
                appendClientText("Client error: " + e.getMessage());
            }
        }).start();
    }

    private void sendFromClient() {
        String text = clientInput.getText().trim();
        if (!text.isEmpty()) {
            clientWriter.println(text);
            appendClientText("Me: " + text);
            clientInput.setText("");
        }
    }

    private void sendFromServer() {
        String text = serverInput.getText().trim();
        if (!text.isEmpty()) {
            serverWriter.println(text);
            appendServerText("Me: " + text);
            serverInput.setText("");
        }
    }

    private void appendClientText(String msg) {
        SwingUtilities.invokeLater(() -> clientChatArea.append(msg + "\n"));
    }

    private void appendServerText(String msg) {
        SwingUtilities.invokeLater(() -> serverChatArea.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DualChatApp::new);
    }
}
