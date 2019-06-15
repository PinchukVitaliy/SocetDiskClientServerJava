package sample;

import java.io.IOException;
import java.net.Socket;

public class Soket {
    Socket socket = null;

    Soket(String hostname, int port){
        try {
            socket = new Socket(hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  Socket inherit(String hostname, int port){
        if(socket == null){
            new Soket(hostname, port);
        }else{
            return this.socket;
        }
        return socket;
    }
}
