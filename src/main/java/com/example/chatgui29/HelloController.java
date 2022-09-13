package com.example.chatgui29;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
//   TextArea textAreaContact;
   VBox usersListVBox;
    boolean isAuth = false;
    String login = null;
    String pass = null;

    protected void auth () throws IOException {
        String token = "";
        //Для авторизации по токену
//        try{
//        FileReader reader = new FileReader("C://Users/MTSUser/Desktop/token.txt");
//        int i;
//        while((i=reader.read())!=-1)
//token += (char) i;
//        }catch(IOException e){
//            System.out.println("token not found");
//        }

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
                                   //textAreaContact.clear();
                                   usersListVBox.getChildren().removeAll();

                                   JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonResponse.get("users").toString() + "\n");
                                   for (int i = 0; i < jsonArray.size(); i++) {
                                       JSONObject jsonUserInfo = (JSONObject) jsonParser.parse(jsonArray.get(i).toString());
                                       String name = jsonUserInfo.get("name").toString();
                                       //textAreaContact.appendText(name + "\n");
                                       Button userBrn = new Button();
                                       userBrn.setText(name);
                                       userBrn.setOnAction(e -> {
                                           textArea.appendText("Нажата кнопка \n");
                                       });

//                                       ArrayList<String> asdasd = new ArrayList<>();//
//                                       if (!asdasd.equals(userBrn.getText())) {//

                                           Platform.runLater(new Runnable() {
                                               @Override
                                               public void run() {
                                                   usersListVBox.getChildren().add(userBrn);//
                                                   //usersListVBox.getChildren().retainAll(userBrn);//
                                                   System.out.println(userBrn.toString());
                                               }
                                           });
                                       //
                                     //  asdasd.add(userBrn.getText());}//
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
                               }else if (jsonResponse.get("messages") != null){
                                   JSONArray messages = (JSONArray) jsonParser.parse(jsonResponse.get("messages").toString());
                                    for (int i=0; i<messages.size(); i++ ){
                                        JSONObject message = (JSONObject) jsonParser.parse(messages.get(i).toString());
                                        String name = message.get("name").toString();
                                        String msg = message.get("msg").toString();
                                        textArea.appendText(name +": "+msg+ "\n");

                                    }


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