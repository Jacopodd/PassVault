package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.crypto.SecretKey;

public class MasterPasswordManager {
	private static final String MASTER_PASSWORD_FILE = System.getProperty("user.home") + "/PassVaultData/master.dat";
    private static final String MASTER_KEY_FILE = System.getProperty("user.home") + "/PassVaultData/master.key";

    public static void saveMasterPassword(String masterPassword) {
        try {
            String hashedPassword = PasswordHasher.hashPassword(masterPassword);
            SecretKey key = AESEncryption.generateKey();
            String encryptedPassword = AESEncryption.encrypt(hashedPassword, key);

            // Scrive la password criptata su file
            try (FileWriter writer = new FileWriter(MASTER_PASSWORD_FILE)) {
                writer.write(encryptedPassword);
            }

            // Salva la chiave AES su file
            try (FileWriter keyWriter = new FileWriter(MASTER_KEY_FILE)) {
                keyWriter.write(AESEncryption.keyToString(key));
            }

            System.out.println("✅ Master Password salvata con successo!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean verifyMasterPassword(String inputPassword) {
        try {
            File passwordFile = new File(MASTER_PASSWORD_FILE);
            File keyFile = new File(MASTER_KEY_FILE);

            if (!passwordFile.exists() || !keyFile.exists()) {
                System.out.println("❌ Master Password non impostata.");
                return false;
            }

            // Legge la password criptata
            BufferedReader reader = new BufferedReader(new FileReader(MASTER_PASSWORD_FILE));
            String encryptedPassword = reader.readLine();
            reader.close();

            // Legge la chiave AES
            BufferedReader keyReader = new BufferedReader(new FileReader(MASTER_KEY_FILE));
            SecretKey key = AESEncryption.stringToKey(keyReader.readLine());
            keyReader.close();

            // Decripta la password e la confronta con l'input
            String storedHash = AESEncryption.decrypt(encryptedPassword, key);
            return PasswordHasher.verifyPassword(inputPassword, storedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
