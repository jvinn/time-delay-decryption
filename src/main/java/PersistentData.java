import org.jasypt.util.text.AES256TextEncryptor;

import java.util.Scanner;

/* Encrypted data is stored in the cipherText variable so data is lost if program terminates */
public class PersistentData {
    public static void main(String[] args) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        textEncryptor.setPassword(Double.toString(Math.random()));

        Scanner userInput = new Scanner(System.in);

        System.out.print("Enter data to be encrypted: ");
        String cipherText = textEncryptor.encrypt(userInput.nextLine());

        System.out.print("Enter minutes to unlock: ");
        Long minutes = Long.parseLong(userInput.nextLine());

        System.out.println("Type \"unlock\" to start decryption timer");

        if(userInput.nextLine().equals("unlock")) {
            StoredData.startTimer(minutes);
            System.out.println("\n" + "Text: " + textEncryptor.decrypt(cipherText));
        }
    }
}
