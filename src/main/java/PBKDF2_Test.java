import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import org.apache.commons.codec.binary.Hex;


public class PBKDF2_Test {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] password = "test".toCharArray();
        byte[] salt = "test".getBytes();
        int iterations = 1000000;
        int keyLength = 512;

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        System.out.println(Hex.encodeHex(hash));
    }
}
