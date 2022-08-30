package com.example.chatgui29;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HelloController {
   private DataOutputStream out;
   @FXML
    TextField textField;

   @FXML
    TextArea textArea;

   @FXML
    Button sendBtn;

   @FXML
   Button connectBtn;

   @FXML
   TextArea textAreaContact;

    @FXML
    protected void  handlerSend() throws IOException {
    String text = textField.getText();
    textField.clear();
    textField.requestFocus();
    textArea.appendText(text + "\n");
    out.writeUTF(text);
   }

   @FXML
    public void connect(){
       try {
           Socket   socket = new Socket("localhost", 9443);
           out = new DataOutputStream(socket.getOutputStream());
           DataInputStream is = new DataInputStream(socket.getInputStream());
           connectBtn.setDisable(true);
           sendBtn.setDisable(false);
           Thread thread = new Thread(new Runnable() {
               @Override
               public void run() {
                   while (true){
                       try{
                           String response = is.readUTF();
                           JSONParser jsonParser = new JSONParser();
                           JSONObject jsonResponse =(JSONObject) jsonParser.parse(response);
                            if (jsonResponse.get("users") != null){
                                textAreaContact.clear();
                                JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonResponse.get("users").toString() + "\n");
                                    for (int i=0; i < jsonArray.size();i++){
                                    JSONObject jsonUserInfo = (JSONObject) jsonParser.parse(jsonArray.get(i).toString());
                                    String name =  jsonUserInfo.get("name").toString();
                                    textAreaContact.appendText(name + "\n");
                                }
                            } else if (jsonResponse.get("msg") != null) {
                                textArea.appendText(jsonResponse.get("msg").toString() + "\n");
                            }
                           //textArea.appendText(is.readUTF()+"\n");
                       } catch (IOException | ParseException e) {
                           e.printStackTrace();
                       }
                   }
               }
           });
           thread.start();
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }
}