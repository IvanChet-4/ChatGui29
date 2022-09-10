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

import java.io.*;
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
    boolean isAuth = false;
    String login = null;
    String pass = null;

    protected void auth () throws IOException {
        String token = "";
        try{
        FileReader reader = new FileReader("C://Users/MTSUser/Desktop/token.txt");
        int i;
        while((i=reader.read())!=-1)
token += (char) i;}catch(IOException e){
            System.out.println("token not found");
        }

        if (token.equals("")){
            textArea.appendText("Введите логин: " + "\n");
        }else{JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", "");
        jsonObject.put("pass", "");
        jsonObject.put("token", token);
        out.writeUTF(jsonObject.toJSONString());
        }

    }

    @FXML
    protected void  handlerSend() throws IOException {
    String text = textField.getText();
    if(text.equals("")) return;
    textField.clear();
    textField.requestFocus();
    textArea.appendText(text + "\n");
    if (isAuth){
        JSONObject request = new JSONObject();
        request.put("msg", text);
        out.writeUTF(request.toJSONString());
    }else{
        if (login == null) {
            login = text;
            textArea.appendText("Введите пароль: " + "\n");
        } else if(pass == null){
            pass = text;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("login", login);
            jsonObject.put("pass", pass);
            jsonObject.put("token", "");
            out.writeUTF(jsonObject.toJSONString());
            login = null; pass = null;
        }
    }
   }

   @FXML
    public void connect(){
       try {
           Socket   socket = new Socket("localhost", 9443);
           this.out = new DataOutputStream(socket.getOutputStream());
           DataInputStream is = new DataInputStream(socket.getInputStream());
           connectBtn.setDisable(true);
           sendBtn.setDisable(false);

           Thread thread = new Thread(new Runnable() {
               @Override
               public void run() {

                   while (true) {

                       try {
                           //System.out.println(isAuth + " do if");
                           if (!isAuth){
                            //   System.out.println(isAuth + " in if");
                           auth();}//

                           String response = is.readUTF();
                           JSONParser jsonParser = new JSONParser();
                           JSONObject jsonResponse = (JSONObject) jsonParser.parse(response);
                           System.out.println(jsonResponse.get("authResult"));


                               if (jsonResponse.get("users") != null) {
                                   textAreaContact.clear();
                                   JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonResponse.get("users").toString() + "\n");
                                   for (int i = 0; i < jsonArray.size(); i++) {
                                       JSONObject jsonUserInfo = (JSONObject) jsonParser.parse(jsonArray.get(i).toString());
                                       String name = jsonUserInfo.get("name").toString();
                                       textAreaContact.appendText(name + "\n");
                                   }
                               } else if (jsonResponse.get("msg") != null) {
                                   textArea.appendText(jsonResponse.get("msg").toString() + "\n");
                               } else if (jsonResponse.get("authResult") != null) {
                                   isAuth = jsonResponse.get("authResult").toString().equals("succses");
                                   String token = jsonResponse.get("token").toString();
                                   FileOutputStream fos =new FileOutputStream("C://Users/MTSUser/Desktop/token.txt");
                                   byte[] buffer = token.getBytes();
                                   fos.write(buffer);
                                   fos.close();
                               }

                               //textArea.appendText(is.readUTF()+"\n");
                           } catch(IOException | ParseException e){
                               e.printStackTrace();
                           }

                       }
                   }

           });
           thread.start();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
}