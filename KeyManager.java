import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.KeyGenerator;

public class KeyManager {
    public static SecretKey loadKey() throws Exception {
        try{
            return loadKeyRoutine();
        } catch (Exception e){
            try{
                return generateKey();
            } catch (Exception e1){
                System.out.println("Error generating new key");
                return null;
            }
        }
    }

    private static SecretKey loadKeyRoutine() throws Exception {
        try (FileInputStream file = new FileInputStream("keys/testKey.aes");
            //deserialize the input from the file
            ObjectInputStream object = new ObjectInputStream(file)) {
            return (SecretKey) object.readObject();
        }
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(128);

        SecretKey newKey = gen.generateKey();

        try (FileOutputStream fos = new FileOutputStream("keys/testKey.aes");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(newKey);
        }


        return newKey;
    }
}