import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Arrays;

/**
 * GUI for the Encryption/Decryption application
 */
public class CryptoGUI extends JFrame {
    // GUI Components
    private JTextField filePathField;
    private JTextArea statusArea;
    private JButton browseButton;
    private JButton encryptButton;
    private JButton decryptButton;
    private JComboBox<Integer> keySizeCombo;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private File selectedFile;
    private JPanel mainPanel;
    private static final int DEFAULT_KEY_SIZE = 3;

    /**
     * Constructor - initializes the GUI
     */
    public CryptoGUI() {
        System.out.println("Initializing GUI...");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Failed to set Look and Feel: " + e.getMessage());
        }

        initializeFrame();
        createComponents();
        layoutComponents();
        addEventListeners();

        // Final setup
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        System.out.println("GUI initialization complete");
    }

    /**
     * Initializes the main frame
     */
    private void initializeFrame() {
        System.out.println("Setting up frame...");
        setTitle("File Encryption/Decryption Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));
        setPreferredSize(new Dimension(600, 500));
        setResizable(true);
        
        // Create main panel with padding
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Set background colors
        mainPanel.setBackground(new Color(240, 240, 240));
        getContentPane().setBackground(new Color(240, 240, 240));
        
        // Add main panel to frame
        setContentPane(mainPanel);
        System.out.println("Frame setup complete");
    }

    /**
     * Creates all GUI components
     */
    private void createComponents() {
        System.out.println("Creating components...");
        
        // File selection components
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setPreferredSize(new Dimension(400, 25));
        filePathField.setBackground(Color.WHITE);
        
        browseButton = new JButton("Browse");
        browseButton.setFocusPainted(false);
        browseButton.setBackground(new Color(70, 130, 180));
        browseButton.setForeground(Color.BLACK);
        browseButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Key size selection
        Integer[] keySizes = {3, 4, 5, 6, 7, 8};
        keySizeCombo = new JComboBox<>(keySizes);
        keySizeCombo.setSelectedItem(DEFAULT_KEY_SIZE);
        keySizeCombo.setBackground(Color.WHITE);
        
        // Action buttons
        encryptButton = new JButton("Encrypt");
        encryptButton.setEnabled(false);
        encryptButton.setFocusPainted(false);
        encryptButton.setBackground(new Color(46, 139, 87));
        encryptButton.setForeground(Color.BLACK);
        encryptButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        decryptButton = new JButton("Decrypt");
        decryptButton.setEnabled(false);
        decryptButton.setFocusPainted(false);
        decryptButton.setBackground(new Color(178, 34, 34));
        decryptButton.setForeground(Color.BLACK);
        decryptButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Status components
        statusLabel = new JLabel("Please select a file to begin");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        
        statusArea = new JTextArea(8, 40);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusArea.setBackground(new Color(250, 250, 250));
        
        System.out.println("Components created");
    }

    /**
     * Layouts the GUI components
     */
    private void layoutComponents() {
        System.out.println("Laying out components...");
        
        // File selection panel
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(null, "File Selection", 
                            TitledBorder.DEFAULT_JUSTIFICATION, 
                            TitledBorder.DEFAULT_POSITION, 
                            new Font("Arial", Font.BOLD, 12)),
            new EmptyBorder(5, 5, 5, 5)));
        filePanel.setBackground(new Color(240, 240, 240));
        
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(browseButton, BorderLayout.EAST);
        
        // Key size panel
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keyPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(null, "Key Size", 
                            TitledBorder.DEFAULT_JUSTIFICATION, 
                            TitledBorder.DEFAULT_POSITION, 
                            new Font("Arial", Font.BOLD, 12)),
            new EmptyBorder(5, 5, 5, 5)));
        keyPanel.setBackground(new Color(240, 240, 240));
        
        JLabel keySizeLabel = new JLabel("Number of Columns: ");
        keySizeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        keyPanel.add(keySizeLabel);
        keyPanel.add(keySizeCombo);
        
        // Combine file and key panels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.add(filePanel, BorderLayout.CENTER);
        topPanel.add(keyPanel, BorderLayout.SOUTH);
        
        // Action buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(null, "Actions", 
                            TitledBorder.DEFAULT_JUSTIFICATION, 
                            TitledBorder.DEFAULT_POSITION, 
                            new Font("Arial", Font.BOLD, 12)),
            new EmptyBorder(5, 5, 5, 5)));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(null, "Status", 
                            TitledBorder.DEFAULT_JUSTIFICATION, 
                            TitledBorder.DEFAULT_POSITION, 
                            new Font("Arial", Font.BOLD, 12)),
            new EmptyBorder(5, 5, 5, 5)));
        statusPanel.setBackground(new Color(240, 240, 240));
        
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        statusPanel.add(scrollPane, BorderLayout.SOUTH);
        
        // Add all panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        System.out.println("Components layout complete");
    }

    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        System.out.println("Adding event listeners...");
        
        browseButton.addActionListener(e -> browseFile());
        encryptButton.addActionListener(e -> encryptFile());
        decryptButton.addActionListener(e -> decryptFile());
        
        System.out.println("Event listeners added");
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            encryptButton.setEnabled(true);
            decryptButton.setEnabled(true);
            updateStatus("File selected: " + selectedFile.getName());
            progressBar.setString("Ready to process");
            // Clear previous status messages
            statusArea.setText("");
        }
    }

    private void encryptFile() {
        if (selectedFile == null) return;
        
        // Disable buttons during processing
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Starting encryption process...");
                    progressBar.setString("Encrypting...");
                    progressBar.setIndeterminate(true);
                    
                    String content = Crypto.readFileContent(selectedFile);
                    String baseFilePath = selectedFile.getParent() + File.separator;
                    String originalFileName = selectedFile.getName();
                    
                    String encryptedFilePath = baseFilePath + "encrypted_" + originalFileName;
                    String keyFilePath = baseFilePath + "encryption_key.txt";
                    
                    int keySize = (Integer) keySizeCombo.getSelectedItem();
                    int[] currentKey = Crypto.generateDynamicKey(keySize);
                    
                    // Validate content length
                    if (content.length() % keySize != 0) {
                        // Pad the content if necessary
                        int paddingNeeded = keySize - (content.length() % keySize);
                        StringBuilder paddedContent = new StringBuilder(content);
                        for (int i = 0; i < paddingNeeded; i++) {
                            paddedContent.append(' ');
                        }
                        content = paddedContent.toString();
                    }
                    
                    String encryptedText = Crypto.encrypt(content, currentKey);
                    
                    // Save both key size and key
                    Crypto.saveToFile(keySize + "\n" + Arrays.toString(currentKey), keyFilePath);
                    Crypto.saveToFile(encryptedText, encryptedFilePath);
                    
                    publish("Encryption completed successfully!");
                    publish("Key size used: " + keySize);
                    publish("Encryption key: " + Arrays.toString(currentKey));
                    publish("Encrypted file saved as: " + encryptedFilePath);
                    publish("Key file saved as: " + keyFilePath);
                    
                } catch (Exception e) {
                    publish("Error during encryption: " + e.getMessage());
                    progressBar.setString("Encryption failed");
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(message -> updateStatus(message));
            }
            
            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setString("Encryption complete");
                // Re-enable buttons
                encryptButton.setEnabled(true);
                decryptButton.setEnabled(true);
            }
        };
        
        worker.execute();
    }

 private void decryptFile() {
    if (selectedFile == null) {
        showError("No file selected", "Please select a file to decrypt.");
        return;
    }
    
    // Get currently selected key size
    int selectedKeySize = (Integer) keySizeCombo.getSelectedItem();
    
    // Disable UI elements during processing
    setUIEnabled(false);
    
    SwingWorker<String, String> worker = new SwingWorker<String, String>() {
        @Override
        protected String doInBackground() throws Exception {
            try {
                publish("Starting decryption process...");
                
                // Verify input file exists and is readable
                if (!selectedFile.canRead()) {
                    throw new UserFriendlyException("File Access Error", 
                        "Cannot read the selected file. Please check file permissions.");
                }

                String baseFilePath = selectedFile.getParent() + File.separator;
                String keyFilePath = baseFilePath + "encryption_key.txt";
                File keyFile = new File(keyFilePath);
                
                publish("Checking key file...");
                
                // Verify key file with detailed error message
                if (!keyFile.exists()) {
                    throw new UserFriendlyException("Key File Missing",
                        String.format("Key file not found at: %s\nPlease ensure the key file is in the same directory as the encrypted file.", keyFilePath));
                }
                
                if (!keyFile.canRead()) {
                    throw new UserFriendlyException("Key File Access Error",
                        "Cannot read the key file. Please check file permissions.");
                }

                publish("Reading key file...");
                String keyFileContent = Crypto.readFileContent(keyFile);
                
                // Validate key file format
                if (!keyFileContent.contains("\n")) {
                    throw new UserFriendlyException("Invalid Key File",
                        "Key file is not in the correct format. Expected key size and key data.");
                }

                String[] keyParts = keyFileContent.split("\n", 2);
                
                // Parse key size with error handling
                int originalKeySize;
                try {
                    originalKeySize = Integer.parseInt(keyParts[0].trim());
                } catch (NumberFormatException e) {
                    throw new UserFriendlyException("Invalid Key File",
                        "Key size in key file is not a valid number.");
                }
                
                // Validate key size
                if (selectedKeySize != originalKeySize) {
                    throw new UserFriendlyException("Key Size Mismatch",
                        String.format("File was encrypted with key size %d but trying to decrypt with size %d.\n" +
                                    "Please select key size %d and try again.", 
                                    originalKeySize, selectedKeySize, originalKeySize));
                }

                publish("Reading encrypted file...");
                String encryptedContent = Crypto.readFileContent(selectedFile);
                
                // Parse encryption key
                publish("Processing encryption key...");
                int[] decryptionKey = Crypto.loadEncryptionKey(keyParts[1]);
                
                // Perform decryption
                publish("Decrypting content...");
                String decryptedText = Crypto.decrypt(encryptedContent, decryptionKey);
                
                // Save decrypted content
                publish("Saving decrypted content...");
                String decryptedFilePath = baseFilePath + "decrypted_" + selectedFile.getName();
                Crypto.saveToFile(decryptedText, decryptedFilePath);
                
                return decryptedFilePath;
                
            } catch (UserFriendlyException e) {
                throw e;
            } catch (Exception e) {
                throw new UserFriendlyException("Decryption Error",
                    "An error occurred during decryption: " + e.getMessage());
            }
        }
        
        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                updateStatus(message);
                progressBar.setString(message);
            }
        }
        
        @Override
        protected void done() {
            try {
                String decryptedFilePath = get();
                progressBar.setString("Decryption complete");
                updateStatus("Decryption completed successfully!");
                updateStatus("Decrypted file saved as: " + decryptedFilePath);
                
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof UserFriendlyException) {
                    UserFriendlyException ufe = (UserFriendlyException) cause;
                    showError(ufe.getTitle(), ufe.getMessage());
                } else {
                    showError("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
                }
                progressBar.setString("Decryption failed");
            } finally {
                setUIEnabled(true);
            }
        }
    };
    
    worker.execute();
}

// Helper method to enable/disable UI elements
private void setUIEnabled(boolean enabled) {
    encryptButton.setEnabled(enabled);
    decryptButton.setEnabled(enabled);
    browseButton.setEnabled(enabled);
    keySizeCombo.setEnabled(enabled);
    progressBar.setIndeterminate(!enabled);
}

// Add this custom exception class
private static class UserFriendlyException extends Exception {
    private final String title;
    
    public UserFriendlyException(String title, String message) {
        super(message);
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
}


private void showError(String title, String message) {
    JOptionPane.showMessageDialog(
        this,
        message,
        title,
        JOptionPane.ERROR_MESSAGE
    );
}

private void updateStatus(String message) {
        statusLabel.setText(message);
        statusArea.append(message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

public static void main(String[] args) {
        System.out.println("Starting GUI application...");
        
        // Run on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                CryptoGUI gui = new CryptoGUI();
                
                // These are now handled in the constructor
                System.out.println("GUI should be visible now");
                
            } catch (Exception e) {
                System.err.println("Failed to start GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

}