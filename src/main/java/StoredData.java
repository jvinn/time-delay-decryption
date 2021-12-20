import java.io.*;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import org.apache.commons.codec.binary.Hex;
import org.jasypt.util.text.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/* Encrypted data is stored in a .txt so the program doesn't need to be running constantly to store ciphertext */
public class StoredData {
    private static double iterationsPerSecond;

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        runBackgroundTasks();

        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String password = Double.toString(Math.random());

        Scanner userInput = new Scanner(System.in);
        System.out.print("Type \"encrypt\" or \"decrypt\": ");
        String encryptOrDecrypt = userInput.nextLine();

        File file = new File("out.txt");
        long minutes;
        String plainText;
        String cipherText = "";

        /* Encrypt and lock data */
        if(encryptOrDecrypt.equals("encrypt")) {

            userInput = new Scanner(System.in);

            System.out.print("Enter data to be encrypted: ");
            plainText = userInput.nextLine();

            System.out.print("Enter time delay to decrypt (minutes): ");
            minutes = Long.parseLong(userInput.nextLine());

            String key = getKeyFromPBKDF2(password);
            textEncryptor.setPassword(key);

            cipherText = textEncryptor.encrypt(minutes + ":" + plainText);
            userInput.close();

            try {
                FileWriter fw = new FileWriter(file);
                fw.write(password + "\n" + cipherText);
                System.out.println("Text has been encrypted. Run the program again to decrypt.");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /* Unlock and decrypt data */
        else if(encryptOrDecrypt.equals("decrypt")) {
            userInput.close();

            try {
                Scanner fileScanner = new Scanner(file);
                password = fileScanner.nextLine();
                cipherText = fileScanner.nextLine();
                fileScanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String key = getKeyFromPBKDF2(password);
            textEncryptor.setPassword(key);
            String[] combinedPlainText = textEncryptor.decrypt(cipherText).split(":");
            minutes = Long.parseLong(combinedPlainText[0]);
            plainText = combinedPlainText[1];

//            startTimer(minutes);
            System.out.println("\n" + "Decrypted data: " + plainText);
        }
        else {
            System.out.println("Invalid input. Type \"encrypt\" or \"decrypt\" next time!");
        }
    }

    public static double calcIterationsPerSecond() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "any password will do";
        String salt = "any salt will do";
        int iterations = 1000000;
        int keyLength = 512;

        double startTime = System.currentTimeMillis();

        /* Secret key is unassigned since all we want is the time it takes to derive it */
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        skf.generateSecret(spec).getEncoded();

        double duration = System.currentTimeMillis() - startTime;

        return iterations / (duration / 1000.0);
    }

    public static void startTimer(Long minutes) {
        double curTimeSeconds = (System.currentTimeMillis() / 1000.0);
        double endTimeSeconds = curTimeSeconds + (minutes * 60);

        System.out.println("Unlock timer started!");

        while(curTimeSeconds <= endTimeSeconds) {
            if(curTimeSeconds < (System.currentTimeMillis() / 1000.0)) {
                System.out.print("\r" + (endTimeSeconds - curTimeSeconds) + " seconds remaining");
                curTimeSeconds = (System.currentTimeMillis() / 1000.0);
            }
        }
    }

    public static String getKeyFromPBKDF2(String passwordString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordArray = passwordString.toCharArray();
        byte[] salt = passwordString.getBytes();
        int iterations = 1000;
        int keyLength = 512;

        PBEKeySpec spec = new PBEKeySpec(passwordArray, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return new String(Hex.encodeHex(hash));
    }

    public static void runBackgroundTasks() {
        /* This thread determines in the background how many iterations should be set for each machine */
        new Thread(() -> {
            try {
                iterationsPerSecond = calcIterationsPerSecond();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
