package sample;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import sun.plugin.javascript.navig.Array;

public class Controller {

    final FileChooser fileChooser = new FileChooser();
    Stage stage;
    ArrayList<String> result = null;
    String nameFile="";
    Socket socket;
    HashMap<String, byte[]> hash;
    ObjectOutputStream os;
    ObjectInputStream ois;
    InputStream is;

    public Controller() throws IOException {
        this.socket = new Socket("localhost", 56700);
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<String> view;

    @FXML
    private Button delete;

    @FXML
    private Button dowload;

    @FXML
    private TextField text;

    @FXML
    private Button showFiles;

    @FXML
    private Button upload;

    @FXML
    void initialize() {
       showFiles.setOnAction(event -> {
           try {
               os = new ObjectOutputStream(socket.getOutputStream());
               hash = new HashMap<String, byte[]>();
               hash.put("Show", null);

               os.writeObject(hash);
               Thread.sleep(1500);

               is = socket.getInputStream();
               if (is.available() > 0) {
                   byte[] bytes = new byte[is.available()];
                   is.read(bytes);
                   String serverMess = new String(bytes);
                   int index = serverMess.indexOf(";");
                   serverMess = serverMess.substring(index + 1, serverMess.length());
                   result = new ArrayList<String>(Arrays.asList(serverMess.split(";")));
               }
           } catch (Exception e) {
               System.out.println("init error: " + e);
           }
           // создаем список объектов
           ObservableList<String> names = FXCollections.observableArrayList(result);
           view.setItems(names);
       });



        MultipleSelectionModel<String> langsSelectionModel = view.getSelectionModel();
        // устанавливаем слушатель для отслеживания изменений
        langsSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue){
                delete.setDisable(false);
                dowload.setDisable(false);
                text.setText("File: " + newValue);
                nameFile=newValue;
            }
        });


        delete.setOnAction(event -> {
                try {
                    os = new ObjectOutputStream(socket.getOutputStream());
                    hash = new HashMap<String, byte[]>();
                    hash.put("Delete", nameFile.getBytes("UTF-8"));
                    os.writeObject(hash);

                    result.removeIf(p -> p.equals(nameFile));
                    ObservableList<String> names = FXCollections.observableArrayList(result);
                    view.setItems(names);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });

        upload.setOnAction(event -> {
            stage = new Stage();
            File dir =  fileChooser.showOpenDialog(stage);
            if (dir != null) {
                try {
                    os = new ObjectOutputStream(socket.getOutputStream());
                    hash = new HashMap<String, byte[]>();
                    hash.put("Upload;"+dir.getName(), convertFileInByte(dir));
                    os.writeObject(hash);
                    os.flush();
                    if(result != null) {
                        result.removeIf(p -> p.equals(dir.getName()));
                        result.add(dir.getName());
                        ObservableList<String> names = FXCollections.observableArrayList(result);
                        view.setItems(names);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dowload.setOnAction(event -> {

            try {
                os = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                hash = new HashMap<String, byte[]>();
                hash.put("Dowload", nameFile.getBytes());
                os.writeObject(hash);
                os.flush();

                Thread.sleep(500);

                    stage = new Stage();
                    fileChooser.setTitle("Save Document: "+nameFile);//Заголовок диалога
                    fileChooser.setInitialFileName(nameFile);
                    File file = fileChooser.showSaveDialog(stage);//Указываем текущую сцену
                    hash = (HashMap<String, byte[]>) ois.readObject();
                    convertByteInFile((byte[])hash.values().toArray()[0], file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        });
    }
    //////////////////////////////////
    public byte[] convertFileInByte(File file)
    {
        byte[] fileBytes = new byte[(int) file.length()];
        try(FileInputStream inputStream = new FileInputStream(file))
        {
            inputStream.read(fileBytes);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return fileBytes;
    }
    public File convertByteInFile(byte[] fileBytes, String path)
    {
        File f = new File(path);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(fileBytes);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return f;
    }
}
