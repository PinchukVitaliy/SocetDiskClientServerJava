import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {

    static  String PATH = "....здесь ваш путь к папке --> //Files";
    static ArrayList <String> arrayList;
    static HashMap<String, byte[]> n;
    static ObjectInputStream in;
    static ObjectOutputStream obs;
    static OutputStream os;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int portNumber = 56700;
        ServerSocket serverSocket = new ServerSocket(portNumber);
        int nPort = serverSocket.getLocalPort();
        System.out.println("Server adress: "
                + serverSocket.getInetAddress().getHostAddress());
        System.out.println("Local Port: " + nPort);

        Socket socket = serverSocket.accept();

        while (true) {

            in = new ObjectInputStream(socket.getInputStream());
            obs =new ObjectOutputStream(socket.getOutputStream());
            n = (HashMap<String, byte[]>)in.readObject();
            String[] arrOfStr = ((String)(n.keySet().toArray()[0])).split(";");

            if("Show".equals(n.keySet().toArray()[0])){
                os = socket.getOutputStream();
                os.write(ShowFiles(PATH).getBytes());
                os.flush();
            }
            else if("Delete".equals(n.keySet().toArray()[0])){
                String fileName = new String((byte[])n.values().toArray()[0]);
                File deleteFile = new File(PATH +"/"+fileName);
                boolean deleted = deleteFile.delete();
                if(deleted)
                    System.out.println("File "+fileName+" delete");
            }
            else if("Upload".equals(arrOfStr[0])){
                String allPath = PATH +"/"+ arrOfStr[1];
                convertByteInFile((byte[])n.values().toArray()[0], allPath);
            }
            else if("Dowload".equals(n.keySet().toArray()[0])){
                String fileName = new String((byte[])n.values().toArray()[0]);
                n = new HashMap<String, byte[]>();
                n.put("", convertFileInByte(new File(PATH+"/"+fileName)));
                obs.writeObject(n);
                obs.flush();
            }
        }
    }



    private static String ShowFiles(String path){
        String result=";";
        File dir = new File(path);
        if(dir.isDirectory())
        {
            for(File item : dir.listFiles()){
                result+=(item.getName()+";").trim();
            }
        }
        return result;
    }


    public static File convertByteInFile(byte[] fileBytes, String path)
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

    public static byte[] convertFileInByte(File file)
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
}


