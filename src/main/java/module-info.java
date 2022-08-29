module com.example.chatgui29 {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens com.example.chatgui29 to javafx.fxml;
    exports com.example.chatgui29;
}