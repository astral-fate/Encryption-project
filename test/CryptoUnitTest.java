import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CryptoUnitTest {
    
    private static final String TEST_TEXT = "Hello World";
    private static final int[] TEST_KEY = {2, 0, 1}; // 3-column key
    private File tempFile;
    
    public CryptoUnitTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary file for testing
        tempFile = File.createTempFile("test", ".txt");
        Files.write(tempFile.toPath(), TEST_TEXT.getBytes());
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up temporary file
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testGenerateDynamicKey() {
        int keySize = 3;
        int[] key = Crypto.generateDynamicKey(keySize);
        
        // Check key size
        assertEquals(keySize, key.length, "Key should have specified length");
        
        // Check that key contains all numbers from 0 to keySize-1
        boolean[] found = new boolean[keySize];
        for (int k : key) {
            assertTrue(k >= 0 && k < keySize, "Key values should be within range");
            assertFalse(found[k], "Key should not contain duplicates");
            found[k] = true;
        }
        
        // Check that all numbers were used
        for (boolean b : found) {
            assertTrue(b, "All numbers should be used in key");
        }
    }
    
    @Test
    public void testGenerateDynamicKeyInvalidInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.generateDynamicKey(0);
        });
        assertEquals("Number of columns must be greater than zero.", exception.getMessage());
    }

    @Test
    public void testEncryptDecryptRoundTrip() {
        String original = "Hello World";
        int[] key = {2, 0, 1};
        
        String encrypted = Crypto.encrypt(original, key);
        String decrypted = Crypto.decrypt(encrypted, key);
        
        assertEquals(original, decrypted, "Decrypted text should match original");
    }
    
    @Test
    public void testEncryptWithNewlines() {
        String original = "Hello\nWorld";
        String encrypted = Crypto.encrypt(original, TEST_KEY);
        String decrypted = Crypto.decrypt(encrypted, TEST_KEY);
        
        assertEquals(original, decrypted, "Text with newlines should be preserved");
    }
    
    @Test
    public void testEncryptWithPadding() {
        String original = "Hello"; // 5 characters for 3-column key
        String encrypted = Crypto.encrypt(original, TEST_KEY);
        String decrypted = Crypto.decrypt(encrypted, TEST_KEY);
        
        assertEquals(original, decrypted.trim(), "Padding should be handled correctly");
    }

    @Test
    public void testEncryptInvalidInput() {
        // Test null text
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.encrypt(null, TEST_KEY);
        });
        assertEquals("Text cannot be null or empty", exception.getMessage());
        
        // Test empty text
        exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.encrypt("", TEST_KEY);
        });
        assertEquals("Text cannot be null or empty", exception.getMessage());
        
        // Test null key
        exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.encrypt(TEST_TEXT, null);
        });
        assertEquals("Invalid key: key cannot be null or empty.", exception.getMessage());
    }

    @Test
    public void testDecryptInvalidInput() {
        // Test null text
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.decrypt(null, TEST_KEY);
        });
        assertEquals("Encrypted text cannot be null or empty", exception.getMessage());
        
        // Test empty text
        exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.decrypt("", TEST_KEY);
        });
        assertEquals("Encrypted text cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testFileOperations() throws IOException {
        // Test reading file
        String content = Crypto.readFileContent(tempFile);
        assertEquals(TEST_TEXT, content, "File content should match test text");
        
        // Test writing file
        File outputFile = File.createTempFile("output", ".txt");
        String testOutput = "Test output content";
        Crypto.saveToFile(testOutput, outputFile.getPath());
        
        String readContent = new String(Files.readAllBytes(outputFile.toPath()));
        assertEquals(testOutput, readContent.trim(), "Written content should match test output");
        
        // Clean up
        outputFile.delete();
    }

    @Test
    public void testLoadEncryptionKey() {
        String keyString = "[2, 0, 1]";
        int[] key = Crypto.loadEncryptionKey(keyString);
        
        assertArrayEquals(TEST_KEY, key, "Loaded key should match test key");
    }

    @Test
    public void testLoadEncryptionKeyInvalidFormat() {
        // Test invalid format
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.loadEncryptionKey("[2, a, 1]");
        });
        assertTrue(exception.getMessage().contains("Invalid key format"));
        
        // Test null input
        exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.loadEncryptionKey(null);
        });
        assertEquals("Key string cannot be null or empty.", exception.getMessage());
        
        // Test empty input
        exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.loadEncryptionKey("");
        });
        assertEquals("Key string cannot be null or empty.", exception.getMessage());
    }
    
    @Test
    public void testInvalidKeyValues() {
        // Test key with invalid range
        int[] invalidKey = {3, 1, 2}; // 3 is out of range for 3-element key
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.encrypt(TEST_TEXT, invalidKey);
        });
        assertTrue(exception.getMessage().contains("values must be between 0 and"));
        
        // Test key with duplicates
        int[] duplicateKey = {1, 1, 2};
        exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.encrypt(TEST_TEXT, duplicateKey);
        });
        assertEquals("Invalid key: key contains duplicate values.", exception.getMessage());
    }
}