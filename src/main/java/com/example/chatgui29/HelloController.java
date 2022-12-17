package com.example.chatgui29;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
    VBox usersListVBox;
    boolean isAuth = false;
    String login = null;
    String pass = null;
    int to_id = 0;

    /*Авторизация*/
    protected void auth() throws IOException {
        String token = "";

        /*Код для авторизации по токену
*        try {
*            FileReader reader = new FileReader("C://Users/User/Desktop/token.txt");
*        int i;
*            while((i=reader.read())!=-1)
*                 token += (char) i;
*        } catch(IOException e) {
*            System.out.println("token not found");
*        }*/

        if (token.equals("")) {
            textArea.appendText("Введите логин: " + "\n");
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("login", "");
            jsonObject.put("pass", "");
            jsonObject.put("token", token);
            out.writeUTF(jsonObject.toJSONString());
        }
    }

    @FXML
    protected void handlerSend() throws IOException {
        String text = textField.getText();
            if (text.equals("")) return;
                textField.clear();
                textField.requestFocus();
                textArea.appendText(text + "\n");
            if (isAuth) {
                JSONObject request = new JSONObject();
                request.put("msg", text);
                request.put("to_id", to_id);
            out.writeUTF(request.toJSONString());
        } else {
            if (login == null) {
                login = text;
                textArea.appendText("Введите пароль: " + "\n");
            } else if (pass == null) {
                pass = text;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("login", login);
                jsonObject.put("pass", pass);
                jsonObject.put("token", "");
                out.writeUTF(jsonObject.toJSONString());
                login = null;
                pass = null;
            }
        }
    }

    /*Устанавливаем соединение*/
    @FXML
    public void connect() {
        try {
            Socket socket = new Socket("localhost", 9443);
            this.out = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            connectBtn.setDisable(true);
            sendBtn.setDisable(false);
            /*Открываем поток*/
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (!isAuth) {
                                auth();//запускаем авторизацию
                            }
                            String response = is.readUTF();
                            JSONParser jsonParser = new JSONParser();
                            JSONObject jsonResponse = (JSONObject) jsonParser.parse(response);
                            System.out.println(jsonResponse.get("authResult"));
                                if (jsonResponse.get("users") != null) {
                                    usersListVBox.getChildren().removeAll();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        usersListVBox.getChildren().clear();
                                    }
                                });

                JSONArray jsonArray = (JSONArray) jsonParser.parse(jsonResponse.get("users").toString() + "\n");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonUserInfo = (JSONObject) jsonParser.parse(jsonArray.get(i).toString());
                        String name = jsonUserInfo.get("name").toString();
                        int id = Integer.parseInt(jsonUserInfo.get("id").toString());
                            Button userBrn = new Button();
                                userBrn.setText(name);
                                userBrn.setOnAction(e -> {
                            textArea.appendText("Нажата кнопка \n");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("getHistoryMessage", id);
                to_id = id;
                textArea.clear();
            try {
                out.writeUTF(jsonObject.toJSONString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
           Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    usersListVBox.getChildren().add(userBrn);
                }
           });
        }
        /*Получение json с сервера и распаковка по ключу*/
    } else if (jsonResponse.get("msg") != null) {
        textArea.appendText(jsonResponse.get("msg").toString() + "\n");
    } else if (jsonResponse.get("authResult") != null) {
         isAuth = jsonResponse.get("authResult").toString().equals("succses");
         String token = jsonResponse.get("token").toString();
         FileOutputStream fos = new FileOutputStream("C://Users/User/Desktop/token.txt");
         byte[] buffer = token.getBytes();
         fos.write(buffer);
         fos.close();
    } else if (jsonResponse.get("messages") != null) {
         JSONArray messages = (JSONArray) jsonParser.parse(jsonResponse.get("messages").toString());
    for (int i = 0; i < messages.size(); i++) {
         JSONObject message = (JSONObject) jsonParser.parse(messages.get(i).toString());
         String name = message.get("name").toString();
         String msg = message.get("msg").toString();
         textArea.appendText(name + ": " + msg + "\n");
         }
    } else if (jsonResponse.get("privateMessages") != null) {
         JSONArray messages = (JSONArray) jsonParser.parse(jsonResponse.get("privateMessages").toString());
             for (int i = 0; i < messages.size(); i++) {
                 JSONObject singleJsonArray = (JSONObject) jsonParser.parse(messages.get(i).toString());
                 String msg = singleJsonArray.get("msg").toString();
                 textArea.appendText(msg + "\n");
             }
         }
    } catch (IOException | ParseException e) {
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