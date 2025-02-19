package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.crypto.SecretKey;

public class PasswordManager {
	private static final String BASE_FOLDER = System.getProperty("user.home") + File.separator + "PassVaultData";

    // 📌 Salva una nuova password
	public static void savePassword(String serviceName, String username, String password) {
	    try {
	        // Crea la cartella principale
	        File baseDir = new File(BASE_FOLDER);
	        if (!baseDir.exists()) {
	            baseDir.mkdir();
	        }

	        // Crea la cartella del servizio
	        long timestamp = System.currentTimeMillis() / 1000;
	        String folderName = serviceName + "_" + timestamp;
	        File serviceFolder = new File(BASE_FOLDER, folderName);
	        if (!serviceFolder.exists()) {
	            serviceFolder.mkdir();
	        }

	        // Genera la chiave AES
	        SecretKey key = AESEncryption.generateKey();
	        String encryptedPassword = AESEncryption.encrypt(password, key);

	        // 📄 Crea il JSON con username e password criptata
	        JSONObject json = new JSONObject();
	        json.put("username", username);
	        json.put("password", encryptedPassword);

	        // 📄 Salva il file JSON
	        File dataFile = new File(serviceFolder, "data.json");
	        try (FileWriter writer = new FileWriter(dataFile)) {
	            writer.write(json.toString());
	        }

	        // 🔑 Salva la chiave AES
	        File keyFile = new File(serviceFolder, "psw.key");
	        try (FileWriter keyWriter = new FileWriter(keyFile)) {
	            keyWriter.write(AESEncryption.keyToString(key));
	        }

	        System.out.println("✅ Credenziali salvate per " + serviceName);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static String[] retrieveCredentials(String serviceFolderName) {
	    try {
	        File serviceFolder = new File(BASE_FOLDER, serviceFolderName);
	        if (!serviceFolder.exists()) {
	            System.out.println("❌ Servizio non trovato!");
	            return null;
	        }

	        // 📂 1️⃣ Estrarre il nome del servizio rimuovendo il timestamp
	        String serviceName = serviceFolderName.replaceAll("_\\d+$", ""); // Rimuove tutto dopo "_"

	        // 📂 2️⃣ Legge la chiave AES salvata
	        File keyFile = new File(serviceFolder, "psw.key");
	        BufferedReader keyReader = new BufferedReader(new FileReader(keyFile));
	        SecretKey key = AESEncryption.stringToKey(keyReader.readLine());
	        keyReader.close();

	        // 📄 3️⃣ Legge il file JSON con le credenziali
	        JSONParser parser = new JSONParser();
	        JSONObject json = (JSONObject) parser.parse(new FileReader(serviceFolder + "/data.json"));

	        // 🔐 4️⃣ Decripta la password
	        String decryptedPassword = AESEncryption.decrypt((String) json.get("password"), key);
	        String username = (String) json.get("username");

	        return new String[]{serviceName, username, decryptedPassword};

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}
