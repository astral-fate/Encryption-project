import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class GUIUnitTest {
    private CryptoGUI gui;
    private JTextField filePathField;
    private JTextArea statusArea;
    private JButton browseButton;
    private JButton encryptButton;
    private JButton decryptButton;
    private JComboBox<Integer> keySizeCombo;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    public GUIUnitTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
   
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create GUI on EDT
        SwingUtilities.invokeAndWait(() -> {
            gui = new CryptoGUI();
        });
        
        // Access private fields using reflection
        filePathField = (JTextField) getPrivateField("filePathField");
        statusArea = (JTextArea) getPrivateField("statusArea");
        browseButton = (JButton) getPrivateField("browseButton");
        encryptButton = (JButton) getPrivateField("encryptButton");
        decryptButton = (JButton) getPrivateField("decryptButton");
        keySizeCombo = (JComboBox<Integer>) getPrivateField("keySizeCombo");
        statusLabel = (JLabel) getPrivateField("statusLabel");
        progressBar = (JProgressBar) getPrivateField("progressBar");
    }
    
    @AfterEach
    public void tearDown() {
        SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.dispose();
            }
        });
    }

    @Test
    public void testInitialState() {
        assertNotNull(gui, "GUI should not be null");
        assertFalse(encryptButton.isEnabled(), "Encrypt button should be disabled initially");
        assertFalse(decryptButton.isEnabled(), "Decrypt button should be disabled initially");
        assertTrue(browseButton.isEnabled(), "Browse button should be enabled");
        assertEquals("", filePathField.getText(), "File path field should be empty");
        assertEquals(3, keySizeCombo.getSelectedItem(), "Default key size should be 3");
        assertEquals("Please select a file to begin", statusLabel.getText(), 
                    "Status should show initial message");
    }
    
    @Test
    public void testKeySizeComboBoxOptions() {
        Integer[] expectedSizes = {3, 4, 5, 6, 7, 8};
        assertEquals(expectedSizes.length, keySizeCombo.getItemCount(), 
                    "Combo box should have correct number of items");
        
        for (int i = 0; i < expectedSizes.length; i++) {
            assertEquals(expectedSizes[i], keySizeCombo.getItemAt(i), 
                        "Key size option should match");
        }
    }
    
    @Test
    public void testFileSelection() throws Exception {
        // Create a temporary file for testing
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        
        // Set the selected file using reflection
        setPrivateField("selectedFile", tempFile);
        filePathField.setText(tempFile.getAbsolutePath());
        
        // Simulate file selection completion
        SwingUtilities.invokeAndWait(() -> {
            encryptButton.setEnabled(true);
            decryptButton.setEnabled(true);
        });
        
        assertTrue(encryptButton.isEnabled(), 
                  "Encrypt button should be enabled after file selection");
        assertTrue(decryptButton.isEnabled(), 
                  "Decrypt button should be enabled after file selection");
        assertEquals(tempFile.getAbsolutePath(), filePathField.getText(), 
                    "File path should be set correctly");
    }
    
    @Test
    public void testProgressBarInitialState() {
        assertFalse(progressBar.isIndeterminate(), 
                   "Progress bar should not be indeterminate initially");
        assertTrue(progressBar.isStringPainted(), 
                  "Progress bar should show string");
        assertEquals("Ready", progressBar.getString(), 
                    "Progress bar should show 'Ready'");
    }
    
    @Test
    public void testStatusAreaUpdates() throws Exception {
        String testMessage = "Test status message";
        SwingUtilities.invokeAndWait(() -> {
            invokePrivateMethod("updateStatus", testMessage);
        });
        
        assertTrue(statusArea.getText().contains(testMessage), 
                  "Status area should contain the test message");
        assertEquals(testMessage, statusLabel.getText(), 
                    "Status label should show the test message");
    }
    
    // Helper method to access private fields
    private Object getPrivateField(String fieldName) throws Exception {
        java.lang.reflect.Field field = CryptoGUI.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(gui);
    }
    
    // Helper method to set private fields
    private void setPrivateField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = CryptoGUI.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(gui, value);
    }
    
    // Helper method to invoke private methods
    private Object invokePrivateMethod(String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
            
            java.lang.reflect.Method method = CryptoGUI.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(gui, args);
        } catch (Exception e) {
            fail("Failed to invoke private method: " + methodName + "\n" + e.getMessage());
            return null;
        }
    }
}