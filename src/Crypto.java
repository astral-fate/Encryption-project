
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;

public class Crypto {

    /**
     * Generates a dynamic encryption key
     * @param numColumns the size of the key
     * @return an array representing the encryption key
     */
    static int[] generateDynamicKey(int numColumns) {
        if (numColumns <= 0) {
            throw new IllegalArgumentException("Number of columns must be greater than zero.");
        }
        List<Integer> keyList = new ArrayList<>();
        for (int i = 0; i < numColumns; i++) {
            keyList.add(i);
        }
        Collections.shuffle(keyList);
        return keyList.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Encrypts the given text using the provided key
     * @param text text to encrypt
     * @param key encryption key
     * @return encrypted text
     */
    public static String encrypt(String text, int[] key) {
        validateKey(key);  
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        // Store newline positions for later restoration
        List<Integer> newlinePositions = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                newlinePositions.add(i);
            }
        }

        // Remove all newlines for processing
        text = text.replaceAll("\\R", "");
        
        int numColumns = key.length;
        int numRows = (int) Math.ceil((double) text.length() / numColumns);
        StringBuilder paddedText = new StringBuilder(text);
        
        // Pad the text with spaces if necessary
        while (paddedText.length() < numRows * numColumns) {
            paddedText.append(' ');
        }

        // Create and fill the matrix
        char[][] matrix = new char[numRows][numColumns];
        for (int i = 0; i < numRows; i++) {
            String row = paddedText.substring(i * numColumns, 
                                        Math.min((i + 1) * numColumns, paddedText.length()));
            matrix[i] = Arrays.copyOf(row.toCharArray(), numColumns);
            // Fill any remaining spaces with spaces
            for (int j = row.length(); j < numColumns; j++) {
                matrix[i][j] = ' ';
            }
        }

        // Build encrypted text
        StringBuilder encryptedText = new StringBuilder();
        for (char[] row : matrix) {
            for (int k : key) {
                encryptedText.append(row[k]);
            }
        }

        // Reinsert newlines at their original positions
        for (int pos : newlinePositions) {
            if (pos < encryptedText.length()) {
                encryptedText.insert(pos, '\n');
            }
        }

        return encryptedText.toString();
    }

    /**
     * Decrypts the given text using the provided key
     * @param encryptedText text to decrypt
     * @param key decryption key
     * @return decrypted text
     */
    public static String decrypt(String encryptedText, int[] key) {
        validateKey(key);
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }

        // Store newline positions for later restoration
        List<Integer> newlinePositions = new ArrayList<>();
        for (int i = 0; i < encryptedText.length(); i++) {
            if (encryptedText.charAt(i) == '\n') {
                newlinePositions.add(i);
            }
        }

        // Remove all newlines for processing
        encryptedText = encryptedText.replaceAll("\\R", "");

        int numColumns = key.length;
        int textLength = encryptedText.length();
        
        // Add padding if necessary
        if (textLength % numColumns != 0) {
            int paddingNeeded = numColumns - (textLength % numColumns);
            StringBuilder paddedText = new StringBuilder(encryptedText);
            for (int i = 0; i < paddingNeeded; i++) {
                paddedText.append(' ');
            }
            encryptedText = paddedText.toString();
        }

        int numRows = encryptedText.length() / numColumns;
        char[][] matrix = new char[numRows][numColumns];
        int index = 0;

        // Fill the matrix with encrypted text
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                matrix[i][j] = encryptedText.charAt(index++);
            }
        }

        // Create reverse mapping
        int[] reverseKey = new int[numColumns];
        for (int i = 0; i < key.length; i++) {
            reverseKey[key[i]] = i;
        }

        // Build decrypted text
        StringBuilder decryptedText = new StringBuilder();
        for (char[] row : matrix) {
            char[] decryptedRow = new char[numColumns];
            for (int i = 0; i < numColumns; i++) {
                decryptedRow[reverseKey[i]] = row[i];
            }
            decryptedText.append(decryptedRow);
        }

        // Reinsert newlines at their original positions
        for (int pos : newlinePositions) {
            if (pos < decryptedText.length()) {
                decryptedText.insert(pos, '\n');
            }
        }

        return decryptedText.toString().stripTrailing();
    }

    /**
     * Validates the encryption/decryption key
     * @param key the key to validate
     */
    private static void validateKey(int[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Invalid key: key cannot be null or empty.");
        }
        
        Set<Integer> uniqueValues = new HashSet<>();
        for (int k : key) {
            if (k < 0 || k >= key.length) {
                throw new IllegalArgumentException("Invalid key: values must be between 0 and " + (key.length - 1));
            }
            if (!uniqueValues.add(k)) {
                throw new IllegalArgumentException("Invalid key: key contains duplicate values.");
            }
        }
    }

    /**
     * Reads content from a file
     * @param file the file to read
     * @return the content of the file as a string
     */
    static String readFileContent(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File error: invalid file or file path.");
        }
        
        try {
            // Read all content at once to preserve original formatting
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes).trim();
        } catch (IOException e) {
            throw new IOException("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Saves content to a file
     * @param content the content to save
     * @param filePath the path of the file to save to
     */
    static void saveToFile(String content, String filePath) throws IOException {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }

        File file = new File(filePath);
        // Create parent directories if they don't exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Use system-specific line separator
        content = content.replaceAll("\\R", System.lineSeparator());
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    /**
     * Loads encryption key from a string
     * @param keyString the string containing the key array
     * @return the encryption key as an integer array
     */
    static int[] loadEncryptionKey(String keyString) {
        if (keyString == null || keyString.isEmpty()) {
            throw new IllegalArgumentException("Key string cannot be null or empty.");
        }

        try {
            // Remove brackets and split by comma
            String cleanKeyString = keyString.replaceAll("[\\[\\]]", "").trim();
            
            // Handle possible spaces after commas
            String[] keyParts = cleanKeyString.split("\\s*,\\s*");
            
            // Parse each number
            int[] key = new int[keyParts.length];
            for (int i = 0; i < keyParts.length; i++) {
                key[i] = Integer.parseInt(keyParts[i].trim());
            }
            
            // Validate the key
            validateKey(key);
            return key;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid key format: " + e.getMessage());
        }
    }

    /**
     * Command line interface method
     */
    private static void runCommandLine() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Running in command-line mode...");

        while (true) {
            System.out.println("\nPlease choose an option:");
            System.out.println("1 - Provide a file path");
            System.out.println("0 - Exit");

            String userOption = scanner.nextLine().trim();

            if ("0".equals(userOption)) {
                System.out.println("Exiting program.");
                break;
            }

            try {
                if ("1".equals(userOption)) {
                    System.out.print("Please enter the file path: ");
                    String filePath = scanner.nextLine();
                    File file = new File(filePath);

                    String content = readFileContent(file);
                    
                    String baseFilePath = file.getParent() + File.separator;
                    String originalFileName = file.getName();
                    
                    String encryptedFilePath = baseFilePath + "encrypted_" + originalFileName;
                    String keyFilePath = baseFilePath + "encryption_key.txt";
                    String decryptedFilePath = baseFilePath + "decrypted_" + originalFileName;

                    System.out.println("Please choose an action:");
                    System.out.println("1 - Encrypt");
                    System.out.println("2 - Decrypt");

                    String action = scanner.nextLine();
                    if ("1".equals(action)) {
                        int[] currentKey = generateDynamicKey(3);
                        String encryptedText = encrypt(content, currentKey);

                        saveToFile(encryptedText, encryptedFilePath);
                        saveToFile(Arrays.toString(currentKey), keyFilePath);
                        System.out.println("Encryption key saved to: " + keyFilePath);
                        System.out.println("Encrypted text saved to: " + encryptedFilePath);

                    } else if ("2".equals(action)) {
                        String keyContent = readFileContent(new File(keyFilePath));
                        int[] currentKey = loadEncryptionKey(keyContent);
                        String decryptedText = decrypt(content, currentKey);
                        saveToFile(decryptedText, decryptedFilePath);
                        System.out.println("Decrypted text saved as: " + decryptedFilePath);
                    } else {
                        System.err.println("Invalid action.");
                    }
                } else {
                    System.err.println("Invalid option.");
                }
            } catch (IOException e) {
                System.err.println("File error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    /**
     * Main method - launches GUI by default
     * Use --cli argument to run in command-line mode
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            runCommandLine();
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    CryptoGUI gui = new CryptoGUI();
                    System.out.println("GUI started successfully");
                } catch (Exception e) {
                    System.err.println("Failed to start GUI: " + e.getMessage());
                    e.printStackTrace();
                    System.out.println("Falling back to command-line mode...");
                    runCommandLine();
                }
            });
        }
    }
}