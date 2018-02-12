package com.example.almaz.messenger;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.almaz.messenger.HammingCoding.Hamming;
import com.example.almaz.messenger.ShannonCoding.ShannonCodes;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.lang.Math.toIntExact;

public class Client extends Thread{

    private String serverMessage;
    public String serverIP;
    public int serverPort;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private DataOutputStream dout;
    private DataInputStream dis;
    private static Socket socket;


    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public Client(OnMessageReceived listener, String serverIP, int serverPort) throws IOException {
        mMessageListener = listener;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        //here you must put your computer's IP address.
        InetAddress serverAddr = InetAddress.getByName(serverIP);
        //create a socket to make the connection with the server
        socket = new Socket(serverAddr, serverPort);
        //send the message to the server
        dout = new DataOutputStream(socket.getOutputStream());
        //receive the message which the server sends back
        dis = new DataInputStream(socket.getInputStream());
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message) throws IOException {
        if (dout != null) {
            dout.writeBoolean(false);
            for(int i = 0; i < message.length(); i++)
                dout.writeChar(message.charAt(i));
            dout.writeChar('\n');
            dout.flush();
        }
    }

    /**
     * Sends attached file entered by client to the server
     * @param file attached file by client
     */
    public void sendFile(File file) throws IOException {
        if (dout != null) {
            dout.writeBoolean(true);
            for(int i = 0; i < file.getName().length(); i++)
                dout.writeChar(file.getName().charAt(i));
            dout.writeChar('\n');
            File[] files = ShannonCodes.compressShannonCodes(file);

            Hamming hamming = new Hamming();
            hamming.encode(files[1].getPath(), files[1].getPath() + "1");
            File compressedFile = new File(files[1].getPath() + "1");
            files[1].delete();
            // send compressed file
            int length = (int) compressedFile.length();
            dout.writeInt(length); // send size of file
            int size = (int) compressedFile.length();
            byte[] buff = new byte[size];
            try {
                FileInputStream fileInputStream = new FileInputStream(compressedFile);
                BufferedInputStream buf = new BufferedInputStream(fileInputStream);
                buf.read(buff, 0, buff.length);
                fileInputStream.close();
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            dout.write(buff, 0, length);

            Hamming hamming1 = new Hamming();
            hamming1.encode(files[0].getPath(), files[0].getPath() + "1");
            File dictionary = new File(files[0].getPath() + "1");
            files[0].delete();
            // send dictionary
            length = (int) dictionary.length();
            dout.writeInt(length); // send size of file
            size = (int) dictionary.length();
            buff = new byte[size];
            try {
                FileInputStream fileInputStream = new FileInputStream(dictionary);
                BufferedInputStream buf = new BufferedInputStream(fileInputStream);
                buf.read(buff, 0, buff.length);
                fileInputStream.close();
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            dout.write(buff, 0, length);
            dout.flush();

            Log.e("TCP Client", "C: Send files");

            // delete temporary files
            compressedFile.delete();
            dictionary.delete();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {

            Log.e("TCP Client", "C: Connecting...");

            try {

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    if(dis.available() == 0){
                        sleep(5);
                        continue;
                    }

                    boolean isFile = dis.readBoolean();

                    if (mMessageListener != null) {

                        StringBuilder stringBuilder = new StringBuilder();
                        char c;
                        do{
                            c = dis.readChar();
                            if(c != '\n')
                                stringBuilder.append(c);
                        } while(c != '\n');

                        serverMessage = stringBuilder.toString();

                        if(isFile){
                            String nameOfFile = serverMessage;
                            String path = Environment.getExternalStoragePublicDirectory
                                    (Environment.DIRECTORY_DOWNLOADS) + "/" + nameOfFile;
                            serverMessage = path;

                            // save compressed file in device
                            File compressedFile = new File(path + ".SC1");
                            FileOutputStream fileOutputStream1 = new FileOutputStream(compressedFile);
                            int fileSize1 = dis.readInt();
                            byte[] buffer1 = new byte[fileSize1];
                            dis.readFully(buffer1, 0, fileSize1);
                            fileOutputStream1.write(buffer1, 0, fileSize1);
                            fileOutputStream1.flush();
                            fileOutputStream1.close();
                            Noise.generateNoise(compressedFile, 1/(compressedFile.length()*8));
                            File decodedFile = new File(path + ".SC");
                            Hamming hamming = new Hamming();
                            hamming.decode(compressedFile.getPath(), decodedFile.getPath());

                            // save dictionary in device
                            File dictionary = new File(path + ".SCDict1");
                            FileOutputStream fileOutputStream2 = new FileOutputStream(dictionary);
                            int fileSize2 = dis.readInt();
                            byte[] buffer2 = new byte[fileSize2];
                            dis.readFully(buffer2, 0, fileSize2);
                            fileOutputStream2.write(buffer2, 0, fileSize2);
                            fileOutputStream2.flush();
                            fileOutputStream2.close();
                            Noise.generateNoise(dictionary, 1/(dictionary.length()*8));
                            File decodedDictionary = new File(path + ".SCDict");
                            Hamming hamming1 = new Hamming();
                            hamming1.decode(dictionary.getPath(), decodedDictionary.getPath());

                            // decompress file
                            ShannonCodes.decompressShannonCodes(decodedFile, decodedDictionary);

                            // delete temporary files
                            compressedFile.delete();
                            dictionary.delete();
                            decodedDictionary.delete();
                            decodedFile.delete();
                        }
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    
                    }
                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                dout.close();
                dis.close();
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}