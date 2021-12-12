import java.io.*;
import java.util.Scanner;
import org.jasypt.util.text.*;

/* Encrypted data is stored in a .txt so the program doesn't need to be running constantly to store data */
public class StoredData {
    public static void main(String[] args) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String encryptionPassword = Double.toString(Math.random());
        textEncryptor.setPassword(encryptionPassword);

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

            cipherText = textEncryptor.encrypt(minutes + " " + plainText);
            userInput.close();

            try {
                FileWriter fw = new FileWriter(file);
                fw.write(cipherText);
                System.out.println("Text has been encrypted. Run the program again to decrypt.");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Serialize Password */
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("password.ser"));
                out.writeDouble(Double.parseDouble(encryptionPassword));
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /* Unlock and decrypt data */
        else if(encryptOrDecrypt.equals("decrypt")) {
            userInput.close();

            try {
                Scanner fileScanner = new Scanner(file);
                cipherText = fileScanner.nextLine();
                fileScanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Get password using deserialization */
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream("password.ser"));
                textEncryptor.setPassword(Double.toString(ois.readDouble()));
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Below line won't work unless updated with the previous password from the .ser file */
            String[] combinedPlainText = textEncryptor.decrypt(cipherText).split(" ");
            minutes = Long.parseLong(combinedPlainText[0]);
            plainText = combinedPlainText[1];

            startTimer(minutes);
            System.out.println("\n" + "Decrypted data: " + plainText);
        }
        else {
            System.out.println("Invalid input. Type \"encrypt\" or \"decrypt\" next time!");
        }
    }

    public static void startTimer(Long minutes) {
        long curTimeSeconds = (System.currentTimeMillis() / 1000);
        long endTimeSeconds = curTimeSeconds + (minutes * 60);

        System.out.println("Unlock timer started!");

        while(curTimeSeconds <= endTimeSeconds) {
            if(curTimeSeconds < (System.currentTimeMillis() / 1000)) {
                System.out.print("\r" + (endTimeSeconds - curTimeSeconds) + " seconds remaining");
                curTimeSeconds = (System.currentTimeMillis() / 1000);
            }
        }
    }
}
