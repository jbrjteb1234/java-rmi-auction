import java.io.*;
import java.security.*;
import java.security.spec.*;

public class KeyManager {

    private static final String RSA_PUBLIC_KEY_PATH = "keys/server_public.key";
    private static final String RSA_PRIVATE_KEY_PATH = "keys/server_private.key";

    public static KeyPair loadServerKeyPair() throws Exception {
        try {
            return loadRSAKeyPair();
        } catch (Exception e) {
            System.out.println("Generating new RSA key pair...");
            return generateRSAKeyPair();
        }
    }

    private static KeyPair loadRSAKeyPair() throws Exception {
        PrivateKey privateKey;
        PublicKey publicKey;

        // load private key
        try (FileInputStream fis = new FileInputStream(RSA_PRIVATE_KEY_PATH)) {
            byte[] privateKeyBytes = fis.readAllBytes();
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(privateSpec);
        }

        // load public key
        try (FileInputStream fis = new FileInputStream(RSA_PUBLIC_KEY_PATH)) {
            byte[] publicKeyBytes = fis.readAllBytes();
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(publicSpec);
        }

        return new KeyPair(publicKey, privateKey);
    }

    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // save private key
        try (FileOutputStream fos = new FileOutputStream(RSA_PRIVATE_KEY_PATH)) {
            fos.write(keyPair.getPrivate().getEncoded());
        }

        // save public key
        try (FileOutputStream fos = new FileOutputStream(RSA_PUBLIC_KEY_PATH)) {
            fos.write(keyPair.getPublic().getEncoded());
        }

        return keyPair;
    }
}