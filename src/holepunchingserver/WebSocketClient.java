/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holepunchingserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Dirk
 */
public class WebSocketClient {

    private WebSocketConnection server;
    private String guid;
    private BufferedReader reader;
    private String webSocketHandShake;
    private Socket socket;

    private DataOutputStream out;
    private BufferedReader in;
    public WebSocketClient(Socket socket, WebSocketConnection server, String guid) {

        this.socket = socket;
        while (socket.isConnected()) {
            try {
                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 out = new DataOutputStream(socket.getOutputStream());
                String line;

                String responseData = "";
                while ((line = in.readLine()) != null) {
                    responseData += line;
                }
                
            if (responseData.contains("Sec-WebSocket-Key")) {

            String headerParts[] = responseData.split(": ");
            String webSocketKey = headerParts[1];
            String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String combinedHandShakeKey = webSocketKey + magicString;
            byte sha1Response[] = DigestUtils.sha1(combinedHandShakeKey);
            webSocketHandShake = new sun.misc.BASE64Encoder().encode(sha1Response);
        }

            String response = "HTTP/1.1 101 Switching Protocols\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Sec-WebSocket-Accept: " + webSocketHandShake + "\r\n\r\n";

            out.write(response.getBytes());
            
            out.flush();

            } catch (Exception ex) {
                Logger.getLogger(WebSocketClient.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
    
     private void sendByteBuffer(byte[] bytesRaw) {

        try {

            byte[] bytesFormat = new byte[20];
            bytesFormat[0] = (byte) 130;

            int indexStartRawData = -1;

            if (bytesRaw.length <= 125) {
                bytesFormat[1] = (byte) bytesRaw.length;
                indexStartRawData = 2;
            } else if (bytesRaw.length >= 126 && bytesRaw.length <= 65535) {
                bytesFormat[1] = 126;
                bytesFormat[2] = (byte) ((bytesRaw.length >> 8) & 255);
                bytesFormat[3] = (byte) ((bytesRaw.length) & 255);

                indexStartRawData = 4;

            } else {
                bytesFormat[1] = 127;
                bytesFormat[2] = (byte) ((bytesRaw.length >> 56) & 255);
                bytesFormat[3] = (byte) ((bytesRaw.length >> 48) & 255);
                bytesFormat[4] = (byte) ((bytesRaw.length >> 40) & 255);
                bytesFormat[5] = (byte) ((bytesRaw.length >> 32) & 255);
                bytesFormat[6] = (byte) ((bytesRaw.length >> 24) & 255);
                bytesFormat[7] = (byte) ((bytesRaw.length >> 16) & 255);
                bytesFormat[8] = (byte) ((bytesRaw.length >> 8) & 255);
                bytesFormat[9] = (byte) ((bytesRaw.length) & 255);
                indexStartRawData = 10;
            }

            byte[] compiledPost = new byte[bytesRaw.length + indexStartRawData];
            for (int i = 0; i < indexStartRawData; i++) {
                compiledPost[i] = bytesFormat[i];

            }
            int index = 0;
            for (int i = indexStartRawData; i < compiledPost.length; i++) {
                compiledPost[i] = bytesRaw[index];
                index++;
            }
            //bytesFormatted.put(bytesRaw, indexStartRawData)
            out.write(compiledPost);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
     
    private void commandInputListener() {
        try {
            byte[] b = new byte[512];
            while (true) {
                int len = socket.getInputStream().read(b);
                if (len != -1) {

                    byte rLength = 0;
                    int rMaskIndex = 2;
                    int rDataStart = 0;
                    //b[0] is always text in my case so no need to check;
                    byte data = b[1];
                    byte op = (byte) 127;
                    rLength = (byte) (data & op);

                    if (rLength == (byte) 126) {
                        rMaskIndex = 4;
                    }
                    if (rLength == (byte) 127) {
                        rMaskIndex = 10;
                    }

                    byte[] masks = new byte[4];

                    int j = 0;
                    int i = 0;
                    for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                        masks[j] = b[i];
                        j++;
                    }

                    rDataStart = rMaskIndex + 4;

                    int messLen = len - rDataStart;

                    byte[] message = new byte[messLen];

                    for (i = rDataStart, j = 0; i < len; i++, j++) {
                        message[j] = (byte) (b[i] ^ masks[j % 4]);
                    }

                    System.out.println(new String(message));
                    //parseMessage(new String(b));

                    b = new byte[512];

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }
    
}

    
   
