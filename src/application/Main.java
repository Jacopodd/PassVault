package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.File;
import java.util.Arrays;

import javafx.scene.layout.HBox;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;


public class Main extends Application {
	
	private static final String FOLDER_PATH = System.getProperty("user.home") + File.separator + "PassVaultData";
	
	
	@Override
	public void start(Stage primaryStage) {
		
		// Controlla se la cartella esiste
        File directory = new File(FOLDER_PATH);
        if (!directory.exists()) {
            // Se la cartella NON esiste, mostra la schermata di creazione Master Password
            showSetupScreen(primaryStage);
        } else {
            // Se la cartella ESISTE, mostra la normale schermata di login
            showLoginScreen(primaryStage);
        }
	}
	
	// SCREENS
	
	private void showSetupScreen(Stage primaryStage) {
	    File directory = new File(FOLDER_PATH);
	    
	    // 📌 Carica l'immagine
	    ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/application/PassVaultLogo.png")));
	    logo.setFitWidth(300);
	    logo.setPreserveRatio(true);

	    // 📌 Campi Password
	    PasswordField passwordField = new PasswordField();
	    passwordField.setPromptText("Inserisci la Master Password");
	    //passwordField.setPrefWidth(400); // ✅ Imposta larghezza manuale

	    PasswordField confirmPasswordField = new PasswordField();
	    confirmPasswordField.setPromptText("Ripeti la Master Password");
	    //confirmPasswordField.setPrefWidth(400); // ✅ Imposta larghezza manuale

	    // 📌 Icone occhio per mostrare/nascondere la password
	    Button togglePassword = createTogglePasswordButton(passwordField);
	    Button toggleConfirmPassword = createTogglePasswordButton(confirmPasswordField);

	    // 📌 Avvolgi i campi in `HBox` per controllarne la larghezza
	    HBox passwordBox = new HBox(10, passwordField, togglePassword);
	    passwordBox.setAlignment(Pos.CENTER); 

	    HBox confirmPasswordBox = new HBox(10, confirmPasswordField, toggleConfirmPassword);
	    confirmPasswordBox.setAlignment(Pos.CENTER);

	    // 📌 Bottone per confermare la Master Password
	    Button confirmButton = new Button("Conferma");
	    confirmButton.setOnAction(e -> {
	        String password = passwordField.getText();
	        String confirmPassword = confirmPasswordField.getText();
	        
	        if (password.isEmpty() || confirmPassword.isEmpty()) {
	            showAlert("Errore", "I campi non possono essere vuoti.");
	            return;
	        }
	        
	        if (!password.equals(confirmPassword)) {
	            showAlert("Errore", "Le password non corrispondono.");
	            return;
	        }

	        // Crea la cartella e salva la password
	        if (directory.mkdir()) {
	            System.out.println("Cartella creata: " + FOLDER_PATH);
	            MasterPasswordManager.saveMasterPassword(password);
	            primaryStage.close();
	            showLoginScreen(primaryStage);
	        } else {
	            showAlert("Errore", "Impossibile creare la cartella.");
	        }
	    });

	    // 📌 Layout UI con VBox
	    VBox layout = new VBox(15, logo, passwordBox, confirmPasswordBox, confirmButton);
	    layout.setAlignment(Pos.CENTER);
	    
	    // 📌 Creazione della scena
	    Scene scene = new Scene(layout, 500, 600);
	    scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	    primaryStage.setScene(scene);
	    primaryStage.setTitle("PassVault - Configurazione");
	    primaryStage.show();
	}
    
	private void showLoginScreen(Stage primaryStage) {
    	File directory = new File(FOLDER_PATH);
    	
    	// 📌 Carica l'immagine
	    ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/application/PassVaultLogo.png")));
	    logo.setFitWidth(300);
	    logo.setPreserveRatio(true);
	    
	    // 📌 Campi Password
	    PasswordField passwordField = new PasswordField();
	    passwordField.setPromptText("Inserisci la Master Password");
	    
	    Button togglePassword = createTogglePasswordButton(passwordField);
	    
	    // 📌 Avvolgi i campi in `HBox` per controllarne la larghezza
	    HBox passwordBox = new HBox(10, passwordField, togglePassword);
	    passwordBox.setAlignment(Pos.CENTER); 
	    
	    Button confirmButton = new Button("Conferma");
	    confirmButton.setOnAction(e -> {
	        String password = passwordField.getText();
	        
	        
	        if (password.isEmpty()) {
	            showAlert("Errore", "I campi non possono essere vuoti.");
	            return;
	        }
	        
	        // Controlla con la password salvata
	        if(MasterPasswordManager.verifyMasterPassword(password)) {
	        	showMainScreen(primaryStage);
	        } else {
	        	showAlert("Errore", "Password errata!");
	        }

	        
	    });
	    
	    VBox layout = new VBox(15, logo, passwordBox, confirmButton);
	    layout.setAlignment(Pos.CENTER);
	    
	    // 📌 Creazione della scena
	    Scene scene = new Scene(layout, 500, 600);
	    scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	    primaryStage.setScene(scene);
	    primaryStage.setTitle("PassVault - Accedi");
	    primaryStage.show();
    }

	private void showMainScreen(Stage primaryStage) {
	    Label titleLabel = new Label("PassVault - Gestione Password");

	    // ✅ Logo
	    ImageView logo = new ImageView();
	    logo.setImage(new Image(getClass().getResourceAsStream("/application/PassVaultLogo.png")));
        logo.setFitWidth(300);
        logo.setPreserveRatio(true);
	    

	    // ✅ Barra di ricerca
	    TextField searchField = new TextField();
	    searchField.setPromptText("Cerca...");
	    searchField.setPrefWidth(200);
	    searchField.getStyleClass().add("text-field");
	    searchField.setId("searchField"); // Imposta ID per il CSS

	    // ✅ Lista delle password salvate
	    ListView<HBox> passwordListView = new ListView<>();
	    ObservableList<HBox> passwordItems = FXCollections.observableArrayList();
	    passwordListView.getStyleClass().add("list-view");
	    passwordListView.setPrefHeight(400);
	    passwordListView.setPadding(new Insets(10, 0, 10, 0));

	    // 🔄 Carica le cartelle presenti in "PassVaultData"
	    File baseDir = new File(System.getProperty("user.home") + File.separator + "PassVaultData");
	    if (baseDir.exists() && baseDir.isDirectory()) {
	    	File[] serviceFolders = baseDir.listFiles(File::isDirectory);
	    	if (serviceFolders != null) {
	    		Arrays.sort(serviceFolders, (a, b) -> b.getName().compareTo(a.getName())); // Ordina per data

	            for (File folder : serviceFolders) {	          
	            	String folderName = folder.getName();
	                
	            	// ✅ Recupera i dati salvati
	                String[] credentials = PasswordManager.retrieveCredentials(folderName);
	                if (credentials == null) continue; // Salta se non riesce a leggere i dati
	                
	                String serviceName = credentials[0]; // Nome servizio senza timestamp
	                String username = credentials[1]; // Username salvato
	                String passwordDencrypted = credentials[2]; // Password decriptata
	                
	                // ✅ Campo username visibile
	                Label userLabel = new Label("👤 " + username);
	                userLabel.setId("userLabel");
	                
	                // ✅ Campo password nascosta
	                TextField passwordField = new TextField();
	                passwordField.setPromptText("••••••••");
	                passwordField.setEditable(false);
	                
	                // ✅ Bottone per mostrare la password
	                Button showButton = new Button("👁");
	                showButton.setOnAction(e -> {
	                	if (passwordField.getText().equals("••••••••")) {
	                		passwordField.setText(passwordDencrypted);
	                	} else {
	                		passwordField.setText("••••••••");
	                	}
	                });
	                
	                // ✅ Riga con servizio, username, password e bottone
	                Label serviceLabel = new Label("🔹 " + serviceName);
	                serviceLabel.setId("serviceLabel");
	                VBox serviceBox = new VBox(serviceLabel, userLabel);
	                HBox row = new HBox(10, serviceBox, passwordField, showButton);
	                row.setAlignment(Pos.CENTER_LEFT);
	                passwordItems.add(row);
	            }
	        }
	    }

	    passwordListView.setItems(passwordItems);
	    
	    // ✅ Bottone per salvare una nuova password
	    Button addPasswordButton = new Button("Salva nuova password");
	    addPasswordButton.setId("addPasswordButton");
	    addPasswordButton.setOnAction(e -> {
	        showNewPasswordScreen(primaryStage);
	    });
	    

	    // 🔍 Filtro in tempo reale
	    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
	        ObservableList<HBox> filteredItems = FXCollections.observableArrayList();
	        
	        for (HBox row : passwordItems) {
	            VBox serviceBox = (VBox) row.getChildren().get(0); // ✅ Ottieni il VBox
	            Label serviceLabel = (Label) serviceBox.getChildren().get(0); // ✅ Prendi la prima Label dentro il VBox
	            
	            if (serviceLabel.getText().toLowerCase().contains(newValue.toLowerCase())) {
	                filteredItems.add(row);
	            }
	        }
	        
	        passwordListView.setItems(filteredItems);
	    });

	    // 📌 Layout principale
	    VBox layout = new VBox(10, logo, titleLabel, searchField, passwordListView, addPasswordButton);
	    layout.setAlignment(Pos.CENTER);
	    layout.setPrefSize(700, 600);
	    VBox.setMargin(addPasswordButton, new Insets(20, 0, 20, 0)); // 20px sopra e sotto

	    Scene mainScene = new Scene(layout);
	    mainScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	    primaryStage.setScene(mainScene);
	    primaryStage.setTitle("PassVault - Home");
	}

	private void showNewPasswordScreen(Stage primaryStage) {
		Label titleLabel = new Label("PassVault - Gestione Password");
		
		// Logo
	    ImageView logo = new ImageView();
	    logo.setImage(new Image(getClass().getResourceAsStream("/application/PassVaultLogo.png")));
        logo.setFitWidth(300);
        logo.setPreserveRatio(true);
        
        // Campi
        TextField nameField = new TextField();
        nameField.setPromptText("Inserisci nome del portale");
        nameField.setPrefWidth(300);
        nameField.setMaxWidth(300);
        
        TextField userField = new TextField();
        userField.setPromptText("Inserisci l'username/mail");
        userField.setPrefWidth(300);
        userField.setMaxWidth(300);
        
        VBox userBox = new VBox(10, nameField, userField);
        userBox.setAlignment(Pos.CENTER);
        
	    PasswordField passwordField = new PasswordField();
	    passwordField.setPromptText("Inserisci la Password associata");
	    
	    Button togglePassword = createTogglePasswordButton(passwordField);
	    
	    // 📌 Avvolgi i campi in `HBox` per controllarne la larghezza
	    /*HBox formBox = new HBox(10, nameField, userField, passwordField, togglePassword);
	    formBox.setAlignment(Pos.CENTER); */
	    
	    HBox psswBox = new HBox(10, passwordField, togglePassword);
	    psswBox.setAlignment(Pos.CENTER);
	    
	    Button confirmButton = new Button("Conferma");
	    confirmButton.setOnAction(e -> {
	    	String nome = nameField.getText().trim();
	    	String user = userField.getText().trim();
	        String password = passwordField.getText();
	        
	        
	        if (password.isEmpty() || nome.isEmpty() || user.isEmpty()) {
	            showAlert("Errore", "I campi non possono essere vuoti!");
	            return;
	        }
	        
	        // ✅ Salvataggio della password nel file
	        PasswordManager.savePassword(nome, user, password);

	        // ✅ Messaggio di conferma
	        showAlert("Successo", "Le credenziali per " + nome + " sono state salvate con successo!");
	        
	        showMainScreen(primaryStage);

	        
	    });
	    
	    Button resumeButton = new Button("Indietro");
	    resumeButton.setOnAction(e -> {
	        showMainScreen(primaryStage);
	    });
	    
	    HBox row = new HBox(10, resumeButton, confirmButton);
        row.setAlignment(Pos.CENTER);
	    
	    //VBox layout = new VBox(15, logo, formBox, confirmButton);
	    VBox layout = new VBox(15, logo, userBox, psswBox, row);
	    layout.setAlignment(Pos.CENTER);
	    
	    // 📌 Creazione della scena
	    Scene scene = new Scene(layout, 500, 600);
	    scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

	    primaryStage.setScene(scene);
	    primaryStage.setTitle("PassVault - Accedi");
	    primaryStage.show();
	}
	
	// UTILITIES
	
    // Metodo per creare un bottone che mostra/nasconde la password
    private Button createTogglePasswordButton(PasswordField passwordField) {
        Button toggleButton = new Button("👁");
        toggleButton.setOnAction(e -> {
            if (passwordField.getPromptText().equals(passwordField.getText())) {
                passwordField.setPromptText("");
                passwordField.setText(passwordField.getText());
            } else {
                passwordField.setPromptText(passwordField.getText());
                passwordField.setText("");
            }
        });
        return toggleButton;
    }
		
    // Metodo per mostrare un Alert in caso di errore
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    
    
	public static void main(String[] args) {
		launch(args);
	}
	
	
	private static void createAppDirectory() {
        // Percorso nella cartella utente dell'OS
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + File.separator + "PassVaultData";

        File directory = new File(folderPath);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("✅ Cartella creata: " + folderPath);
            } else {
                System.out.println("❌ Errore nella creazione della cartella.");
            }
        } else {
            System.out.println("📂 La cartella esiste già: " + folderPath);
        }
    }

}
