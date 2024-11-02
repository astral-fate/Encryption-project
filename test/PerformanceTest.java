import java.io.*;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class PerformanceTest {
    private static final String TEST_DIR = "performance_test_files/";
    private Map<String, File> testFiles;
    private static final int[] TEST_KEY = {2, 0, 1}; // Fixed key for consistent testing

//    @Before
    public void setUp() throws IOException {
        // Create test directory
        new File(TEST_DIR).mkdirs();
        testFiles = new HashMap<>();
        
        // Create test files of different sizes
        createTestFiles();
    }

    private void createTestFiles() throws IOException {
        // Define different file sizes to test (in bytes)
        int[] fileSizes = {
            1_024,        // 1 KB
            10_240,       // 10 KB
            102_400,      // 100 KB
            1_048_576,    // 1 MB
            10_485_760    // 10 MB
        };

        for (int size : fileSizes) {
            String fileName = TEST_DIR + "test_" + (size / 1024) + "KB.txt";
            File file = createFileWithSize(fileName, size);
            testFiles.put(fileName, file);
        }
    }

    private File createFileWithSize(String fileName, int sizeInBytes) throws IOException {
        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Create content with a mix of characters to simulate real text
            StringBuilder content = new StringBuilder();
            String baseText = "This is a test file with some content. ";
            
            while (content.length() < sizeInBytes) {
                content.append(baseText)
                      .append(System.currentTimeMillis())
                      .append("\n");
            }
            
            // Trim to exact size
            if (content.length() > sizeInBytes) {
                content.setLength(sizeInBytes);
            }
            
            writer.write(content.toString());
        }
        return file;
    }

    @Test
    public void testEncryptionPerformance() throws IOException {
        System.out.println("\n=== Encryption Performance Test ===");
        System.out.printf("%-15s %-15s %-15s %-15s%n", 
                         "File Size", "Read Time", "Encrypt Time", "Write Time");
        System.out.println("------------------------------------------------");

        for (Map.Entry<String, File> entry : testFiles.entrySet()) {
            File file = entry.getValue();
            long fileSize = file.length();

            // Measure file reading time
            long startRead = System.nanoTime();
            String content = Crypto.readFileContent(file);
            long readTime = System.nanoTime() - startRead;

            // Measure encryption time
            long startEncrypt = System.nanoTime();
            String encrypted = Crypto.encrypt(content, TEST_KEY);
            long encryptTime = System.nanoTime() - startEncrypt;

            // Measure file writing time
            String encryptedFileName = TEST_DIR + "encrypted_" + file.getName();
            long startWrite = System.nanoTime();
            Crypto.saveToFile(encrypted, encryptedFileName);
            long writeTime = System.nanoTime() - startWrite;

            // Print results
            System.out.printf("%-15s %-15.2f %-15.2f %-15.2f%n",
                    formatFileSize(fileSize),
                    readTime / 1_000_000.0,    // Convert to milliseconds
                    encryptTime / 1_000_000.0,
                    writeTime / 1_000_000.0);

            // Verify encryption worked
            File encryptedFile = new File(encryptedFileName);
            assertTrue("Encrypted file should exist", encryptedFile.exists());
            assertTrue("Encrypted file should have content", encryptedFile.length() > 0);
        }
    }

    @Test
    public void testDecryptionPerformance() throws IOException {
        System.out.println("\n=== Decryption Performance Test ===");
        System.out.printf("%-15s %-15s %-15s %-15s%n", 
                         "File Size", "Read Time", "Decrypt Time", "Write Time");
        System.out.println("------------------------------------------------");

        for (Map.Entry<String, File> entry : testFiles.entrySet()) {
            File originalFile = entry.getValue();
            
            // First encrypt the file
            String content = Crypto.readFileContent(originalFile);
            String encrypted = Crypto.encrypt(content, TEST_KEY);
            String encryptedFileName = TEST_DIR + "encrypted_" + originalFile.getName();
            Crypto.saveToFile(encrypted, encryptedFileName);
            File encryptedFile = new File(encryptedFileName);

            // Now measure decryption performance
            long startRead = System.nanoTime();
            String encryptedContent = Crypto.readFileContent(encryptedFile);
            long readTime = System.nanoTime() - startRead;

            long startDecrypt = System.nanoTime();
            String decrypted = Crypto.decrypt(encryptedContent, TEST_KEY);
            long decryptTime = System.nanoTime() - startDecrypt;

            String decryptedFileName = TEST_DIR + "decrypted_" + originalFile.getName();
            long startWrite = System.nanoTime();
            Crypto.saveToFile(decrypted, decryptedFileName);
            long writeTime = System.nanoTime() - startWrite;

            // Print results
            System.out.printf("%-15s %-15.2f %-15.2f %-15.2f%n",
                    formatFileSize(originalFile.length()),
                    readTime / 1_000_000.0,    // Convert to milliseconds
                    decryptTime / 1_000_000.0,
                    writeTime / 1_000_000.0);

            // Verify decryption worked
            File decryptedFile = new File(decryptedFileName);
            assertTrue("Decrypted file should exist", decryptedFile.exists());
            assertEquals("Decrypted content should match original",
                    content.trim(), decrypted.trim());
        }
    }

    @Test
    public void testMemoryUsage() throws IOException {
        System.out.println("\n=== Memory Usage Test ===");
        System.out.printf("%-15s %-20s %-20s%n", 
                         "File Size", "Before Memory (MB)", "After Memory (MB)");
        System.out.println("------------------------------------------------");

        for (Map.Entry<String, File> entry : testFiles.entrySet()) {
            File file = entry.getValue();
            
            // Force garbage collection before test
            System.gc();
            long memoryBefore = Runtime.getRuntime().totalMemory() - 
                               Runtime.getRuntime().freeMemory();

            // Perform encryption and decryption
            String content = Crypto.readFileContent(file);
            String encrypted = Crypto.encrypt(content, TEST_KEY);
            String decrypted = Crypto.decrypt(encrypted, TEST_KEY);

            long memoryAfter = Runtime.getRuntime().totalMemory() - 
                              Runtime.getRuntime().freeMemory();

            System.out.printf("%-15s %-20.2f %-20.2f%n",
                    formatFileSize(file.length()),
                    memoryBefore / (1024.0 * 1024.0),
                    memoryAfter / (1024.0 * 1024.0));

            // Verify correctness
            assertEquals("Decrypted content should match original",
                    content.trim(), decrypted.trim());
        }
    }

    @Test
    public void testConcurrentPerformance() throws InterruptedException {
        System.out.println("\n=== Concurrent Performance Test ===");
        System.out.println("Testing concurrent encryption/decryption operations...");

        int numThreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[numThreads];
        long startTime = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (Map.Entry<String, File> entry : testFiles.entrySet()) {
                        File file = entry.getValue();
                        String content = Crypto.readFileContent(file);
                        String encrypted = Crypto.encrypt(content, TEST_KEY);
                        String decrypted = Crypto.decrypt(encrypted, TEST_KEY);
                        assertEquals("Concurrent operation should maintain correctness",
                                content.trim(), decrypted.trim());
                    }
                } catch (IOException e) {
                    fail("Concurrent operation failed: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        long totalTime = System.nanoTime() - startTime;
        System.out.printf("Concurrent test with %d threads completed in %.2f seconds%n",
                numThreads, totalTime / 1_000_000_000.0);
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }

//    @After
    public void tearDown() {
        // Cleanup test files
        for (File file : testFiles.values()) {
            file.delete();
            new File(TEST_DIR + "encrypted_" + file.getName()).delete();
            new File(TEST_DIR + "decrypted_" + file.getName()).delete();
        }
        new File(TEST_DIR).delete();
    }

    private void assertTrue(String encrypted_file_should_exist, boolean exists) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}