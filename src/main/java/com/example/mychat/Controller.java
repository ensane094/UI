package com.example.mychat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import server.ClientHandler;

import java.util.List;

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
    @FXML
    public ListView<String> clientList;

    private final ChatClient client;

    public Controller() {
        client = new ChatClient(this);
        client.openConnection();
    }

    public void sendButton(ActionEvent actionEvent) {
        String message =textField.getText();
        client.sendMessage(message);
        message.trim();
        if(message.isEmpty()){
            return;
        }
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
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            final String message = textField.getText();
            final String nick = clientList.getSelectionModel().getSelectedItem();
            textField.setText("/w "+ nick + " " + message);
            textField.requestFocus();
            textField.selectEnd();
        }
    }

    public void updateClientList(List<String> clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }
}