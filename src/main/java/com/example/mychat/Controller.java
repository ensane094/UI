package com.example.mychat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Controller {
    @FXML
    private javafx.scene.control.TextArea TextArea;
    @FXML
    private javafx.scene.control.TextField TextField;

    public void SendButton(ActionEvent actionEvent) {
        String message =TextField.getText();
        if(message.isEmpty()){
            return;
        }
        TextArea.appendText(message+"\n");
        TextField.clear();
        TextField.requestFocus();
    }
}