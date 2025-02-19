module PassVault {
	requires javafx.controls;
	requires javafx.graphics;
	requires json.simple;
	
	opens application to javafx.graphics, javafx.fxml;
}
