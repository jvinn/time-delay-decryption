import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import org.apache.commons.codec.binary.Hex;
import org.jasypt.util.text.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/* Encrypted data is stored in a .txt so the program doesn't need to be running constantly to store ciphertext */
public class DelayedDecryption {
    private static double iterationsPerSecond;

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
        int iterations = 0;

        /* This thread determines in the background how many iterations should be set for each machine */
        Thread thread = new Thread(() -> {
            try {
                iterationsPerSecond = calcIterationsPerSecond();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();

        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String password = Double.toString(Math.random());

        Scanner userInput = new Scanner(System.in);
        System.out.print("Type \"encrypt\" or \"decrypt\": ");
        String encryptOrDecrypt = userInput.nextLine();

        File file = new File("out.txt");
        String plainText;
        String cipherText = "";

        /* Encrypt and lock data */
        if(encryptOrDecrypt.equals("encrypt")) {
            userInput = new Scanner(System.in);

            System.out.print("Enter data to be encrypted: ");
            plainText = userInput.nextLine();

            System.out.print("Enter decryption delay as \"x minutes\" or \"x seconds\": ");
            long delay = userInput.nextLong();
            String delayUnits = userInput.nextLine();

            if(delayUnits.contains("minutes") || delayUnits.contains("seconds")) {
                long duration = delayUnits.contains("minutes") ? delay * 60 : delay;

                thread.join();

                Thread displayTimeRemaining = new Thread(() -> startTimer(duration));

                System.out.println("Encrypting...");
                displayTimeRemaining.start();

                iterations = (int) (duration * (iterationsPerSecond));
                String key = getKeyFromPBKDF2(password, iterations);
                textEncryptor.setPassword(key);

                cipherText = textEncryptor.encrypt(plainText);
                userInput.close();

                try {
                    FileWriter fw = new FileWriter(file);
                    fw.write(password + "\n" + iterations + "\n" + cipherText);
                    displayTimeRemaining.join();
                    System.out.println("Text has been encrypted. Run the program again to decrypt.");
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Invalid input. Type \"x minutes\" or \" x seconds\" next time!");
            }
        }
        /* Unlock and decrypt data */
        else if(encryptOrDecrypt.equals("decrypt")) {
            userInput.close();

            try {
                Scanner fileScanner = new Scanner(file);
                password = fileScanner.nextLine();
                iterations = Integer.parseInt(fileScanner.nextLine());
                cipherText = fileScanner.nextLine();
                fileScanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            thread.join();

            int finalIterations = iterations;
            Thread displayTimeRemaining = new Thread(() -> {
                System.out.println("Decrypting...");
                startTimer((long) (finalIterations / iterationsPerSecond));
            });
            displayTimeRemaining.start();

            String key = getKeyFromPBKDF2(password, iterations);
            textEncryptor.setPassword(key);
            plainText = textEncryptor.decrypt(cipherText);

            System.out.println("Decrypted data: " + plainText);
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

    public static void startTimer(Long seconds) {
        double curTimeSeconds = (System.currentTimeMillis() / 1000.0);
        double endTimeSeconds = curTimeSeconds + seconds;

        while(curTimeSeconds <= endTimeSeconds) {
            if(curTimeSeconds < (System.currentTimeMillis() / 1000.0)) {
                System.out.print("\r" + Math.round(endTimeSeconds - curTimeSeconds) + " seconds remaining");
                curTimeSeconds = (System.currentTimeMillis() / 1000.0);
            }
        }
        System.out.print("\r0 seconds remaining\n");
    }

    public static String getKeyFromPBKDF2(String passwordString, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordArray = passwordString.toCharArray();
        byte[] salt = passwordString.getBytes();
        int keyLength = 512;

        PBEKeySpec spec = new PBEKeySpec(passwordArray, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return new String(Hex.encodeHex(hash));
    }
}
