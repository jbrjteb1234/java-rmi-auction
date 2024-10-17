import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
        try (FileInputStream file = new FileInputStream("keys/testKey.aes")){
            //deserialize the input from the file
            byte[] keyBytes = new byte[16];
            if (file.read(keyBytes) != keyBytes.length) {
                throw new IOException("No keys found");
            }
            return new SecretKeySpec(keyBytes, "AES");
        }
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(128);

        SecretKey newKey = gen.generateKey();

        try (FileOutputStream fos = new FileOutputStream("keys/testKey.aes")){
            fos.write(newKey.getEncoded());
        }


        return newKey;
    }
}