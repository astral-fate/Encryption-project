import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;

public class CryptoFunctionalTest {
    
    private File testDirectory;
    private File inputFile;
    private File encryptedFile;
    private File decryptedFile;
    private File keyFile;
    
    public CryptoFunctionalTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create temporary test directory
        testDirectory = Files.createTempDirectory("crypto_test").toFile();
        
        // Create test files
        inputFile = new File(testDirectory, "input.txt");
        encryptedFile = new File(testDirectory, "encrypted_input.txt");
        decryptedFile = new File(testDirectory, "decrypted_input.txt");
        keyFile = new File(testDirectory, "encryption_key.txt");
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up all test files
        if (testDirectory != null && testDirectory.exists()) {
            deleteDirectory(testDirectory);
        }
    }
    
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    @Test
    public void testCompleteEncryptionWorkflow() throws IOException {
        // Prepare test data
        String originalText = "This is a test message\nwith multiple lines\nto encrypt and decrypt.";
        Files.write(inputFile.toPath(), originalText.getBytes());
        
        // Read the input file
        String content = Crypto.readFileContent(inputFile);
        assertEquals(originalText, content, "File content should be read correctly");
        
        // Generate encryption key
        int keySize = 5; // Use 5 columns for this test
        int[] key = Crypto.generateDynamicKey(keySize);
        
        // Encrypt the content
        String encryptedText = Crypto.encrypt(content, key);
        assertNotEquals(originalText, encryptedText, "Encrypted text should be different from original");
        
        // Save encrypted text and key
        Crypto.saveToFile(encryptedText, encryptedFile.getPath());
        Crypto.saveToFile(keySize + "\n" + java.util.Arrays.toString(key), keyFile.getPath());
        
        // Verify files were created
        assertTrue(encryptedFile.exists(), "Encrypted file should exist");
        assertTrue(keyFile.exists(), "Key file should exist");
        
        // Read back and decrypt
        String encryptedContent = Crypto.readFileContent(encryptedFile);
        String keyContent = Crypto.readFileContent(keyFile);
        String[] keyParts = keyContent.split("\n", 2);
        int originalKeySize = Integer.parseInt(keyParts[0].trim());
        int[] loadedKey = Crypto.loadEncryptionKey(keyParts[1]);
        
        assertEquals(keySize, originalKeySize, "Key size should match");
        assertArrayEquals(key, loadedKey, "Loaded key should match original key");
        
        // Decrypt the content
        String decryptedText = Crypto.decrypt(encryptedContent, loadedKey);
        assertEquals(originalText, decryptedText, "Decrypted text should match original");
    }

    @Test
    public void testEncryptionWithSpecialCharacters() throws IOException {
        String specialText = "Special chars: !@#$%^&*()\nTabs:\t\t\tSpaces:   \nČhinese:你好";
        Files.write(inputFile.toPath(), specialText.getBytes());
        
        int[] key = Crypto.generateDynamicKey(4);
        String encrypted = Crypto.encrypt(specialText, key);
        String decrypted = Crypto.decrypt(encrypted, key);
        
        assertEquals(specialText, decrypted, "Special characters should be preserved");
    }

    @Test
    public void testLargeFileEncryption() throws IOException {
        // Create a large test file
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Line ").append(i).append(": Some test content with numbers 12345 and letters abcde\n");
        }
        Files.write(inputFile.toPath(), largeContent.toString().getBytes());
        
        String content = Crypto.readFileContent(inputFile);
        int[] key = Crypto.generateDynamicKey(7);
        
        // Encrypt
        String encrypted = Crypto.encrypt(content, key);
        Crypto.saveToFile(encrypted, encryptedFile.getPath());
        
        // Decrypt
        String encryptedContent = Crypto.readFileContent(encryptedFile);
        String decrypted = Crypto.decrypt(encryptedContent, key);
        
        assertEquals(content, decrypted, "Large file content should be preserved");
    }

    @Test
    public void testMultipleEncryptionRounds() throws IOException {
        String originalText = "Testing multiple encryption rounds";
        int[] key1 = Crypto.generateDynamicKey(3);
        int[] key2 = Crypto.generateDynamicKey(4);
        int[] key3 = Crypto.generateDynamicKey(5);
        
        // First round
        String firstEncryption = Crypto.encrypt(originalText, key1);
        
        // Second round
        String secondEncryption = Crypto.encrypt(firstEncryption, key2);
        
        // Third round
        String thirdEncryption = Crypto.encrypt(secondEncryption, key3);
        
        // Decrypt in reverse order
        String thirdDecryption = Crypto.decrypt(thirdEncryption, key3);
        String secondDecryption = Crypto.decrypt(thirdDecryption, key2);
        String firstDecryption = Crypto.decrypt(secondDecryption, key1);
        
        assertEquals(originalText, firstDecryption, 
                    "Text should survive multiple encryption/decryption rounds");
    }

    @Test
    public void testFilesWithDifferentEncodings() throws IOException {
        // Test UTF-8 content
        String utf8Text = "UTF-8 text with special characters: ñ á é í ó ú";
        Files.write(inputFile.toPath(), utf8Text.getBytes("UTF-8"));
        
        int[] key = Crypto.generateDynamicKey(6);
        String content = Crypto.readFileContent(inputFile);
        String encrypted = Crypto.encrypt(content, key);
        String decrypted = Crypto.decrypt(encrypted, key);
        
        assertEquals(utf8Text, decrypted, "UTF-8 encoded text should be preserved");
    }
    
    @Test
    public void testEmptyLinesAndWhitespace() throws IOException {
        String textWithSpaces = "\n\nFirst line\n  Indented line  \n\nLast line\n\n";
        Files.write(inputFile.toPath(), textWithSpaces.getBytes());
        
        int[] key = Crypto.generateDynamicKey(4);
        String content = Crypto.readFileContent(inputFile);
        String encrypted = Crypto.encrypt(content, key);
        String decrypted = Crypto.decrypt(encrypted, key);
        
        assertEquals(textWithSpaces, decrypted, 
                    "Empty lines and whitespace should be preserved");
    }
    
    @Test
    public void testErrorRecovery() throws IOException {
        // Test recovery from corrupted key file
        String originalText = "Test error recovery";
        int[] key = Crypto.generateDynamicKey(3);
        String encrypted = Crypto.encrypt(originalText, key);
        
        // Save corrupted key
        String corruptedKey = "Invalid Key Format";
        Files.write(keyFile.toPath(), corruptedKey.getBytes());
        
        // Attempt to load corrupted key
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Crypto.loadEncryptionKey(corruptedKey);
        });
        assertTrue(exception.getMessage().contains("Invalid key format"));
        
        // Test recovery by using correct key
        String decrypted = Crypto.decrypt(encrypted, key);
        assertEquals(originalText, decrypted, 
                    "Should be able to decrypt with correct key after error");
    }
}