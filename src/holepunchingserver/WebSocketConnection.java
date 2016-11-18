package holepunchingserver;

/*
 * Copyright Â© 2015 Stofkat Computer Entertainment, all rights reserved
 *  Redistribution of this software is prohibited, unless stated otherwise
 *  in a written agreement with Stofkat.com.
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Dirk
 */
public class WebSocketConnection {
    
    
    HashMap<String,WebSocketClient> connections = new HashMap<>();
    
    public WebSocketConnection(){
        try{
            ServerSocket socket = new ServerSocket(22331);
            Socket client = socket.accept();
            
        }catch(Exception ex){ex.printStackTrace();}            
    }
    
    
    

}
