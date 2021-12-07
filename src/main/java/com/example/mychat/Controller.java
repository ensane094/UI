package com.example.mychat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

public class Controller {
    @FXML
    private HBox loginBox;
    @FXML
    private javafx.scene.control.TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private javafx.scene.control.TextArea textArea;
    @FXML
    private javafx.scene.control.TextField textField;
    @FXML
    private HBox messageBox;

    private final ChatClient client;

    public Controller() {
        client = new ChatClient(this);
        client.openConnection();
    }

    public void sendButton(ActionEvent actionEvent) {
        String message =textField.getText();
        message.trim();
        if(message.isEmpty()){
            return;
        }
        textArea.appendText(message+"\n");
        textField.clear();
        textField.requestFocus();
    }

    public void btnAuthClick(ActionEvent actionEvent) {
            client.sendMessage("/auth " + loginField.getText() + " " + passwordField.getText());
            textField.requestFocus();
    }

    public void addMessage(String message) {
        textArea.appendText(message + "\n");
    }

    public void setAuth(boolean succes) {
        loginBox.setVisible(!succes);
        messageBox.setVisible(succes);
        textArea.setVisible(succes);
    }
}