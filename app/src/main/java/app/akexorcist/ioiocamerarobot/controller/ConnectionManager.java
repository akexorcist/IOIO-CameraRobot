package app.akexorcist.ioiocamerarobot.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import app.akexorcist.ioiocamerarobot.constant.Command;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class ConnectionManager {
    public static final int PORT = 21111;
    public static final int TIMEOUT = 5000;
    private Activity activity;
    private ConnectionListener connectionListener;
    private IOIOResponseListener responseListener;

    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;
    private InputStream inputStream;
    private DataInputStream dataInputStream;

    private Socket socket;
    private boolean isTaskRunning = false;

    private String ipAddress;
    private String password;

    public ConnectionManager(Activity activity, String ipAddress, String password) {
        this.activity = activity;
        this.ipAddress = ipAddress;
        this.password = password;
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void setResponseListener(IOIOResponseListener listener) {
        this.responseListener = listener;
    }

    public void start() {
        if (!isTaskRunning) {
            new Thread(readThread).start();
            isTaskRunning = true;
        }
    }

    Runnable readThread = new Runnable() {
        public void run() {
            try {
                socket = new Socket();
                socket.connect((new InetSocketAddress(InetAddress.getByName(ipAddress), PORT)), TIMEOUT);

                outputStream = socket.getOutputStream();
                dataOutputStream = new DataOutputStream(outputStream);

                inputStream = socket.getInputStream();
                dataInputStream = new DataInputStream(inputStream);
                sendCommand(password);

                while (isTaskRunning) {
                    try {
                        int size = dataInputStream.readInt();
                        final byte[] buffer = new byte[size];
                        dataInputStream.readFully(buffer);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (buffer.length > 0 && buffer.length < 20) {
                                    if (new String(buffer).equalsIgnoreCase(Command.SNAP)) {
                                        if (responseListener != null)
                                            responseListener.onPictureTaken();
                                    } else if (new String(buffer).equalsIgnoreCase(Command.WRONG_PASSWORD)) {
                                        if (connectionListener != null)
                                            connectionListener.onWrongPassword();
                                    } else if (new String(buffer).equalsIgnoreCase(Command.ACCEPT_CONNECTION)) {
                                        if (connectionListener != null)
                                            connectionListener.onIOIOConnected();
                                    } else if (new String(buffer).equalsIgnoreCase(Command.FLASH_UNAVAILABLE)) {
                                        if (responseListener != null)
                                            responseListener.onFlashUnavailable();
                                    }
                                } else if (buffer.length > 20) {
                                    if (responseListener != null) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                        responseListener.onCameraImageIncoming(bitmap);
                                    }
                                }
                            }
                        });
                    } catch (EOFException e) {
                        e.printStackTrace();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (connectionListener != null)
                                    connectionListener.onConnectionDown();
                            }
                        });
                        isTaskRunning = false;
                    } catch (NumberFormatException | IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException | UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionListener != null)
                            connectionListener.onConnectionFailed();
                    }
                });
            }
        }
    };

    public interface ConnectionListener {
        public void onConnectionDown();

        public void onConnectionFailed();

        public void onWrongPassword();

        public void onIOIOConnected();
    }

    public interface IOIOResponseListener {
        public void onPictureTaken();

        public void onFlashUnavailable();

        public void onCameraImageIncoming(Bitmap bitmap);
    }

    public void stop() {
        isTaskRunning = false;
        try {
            socket.close();
            outputStream.close();
            dataOutputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean sendCommand(String str) {
        try {
            dataOutputStream.writeInt(str.length());
            dataOutputStream.write(str.getBytes());
            outputStream.flush();
            return true;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendMovement(final String str) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] data = str.getBytes();
                    DatagramSocket datagramSocket = new DatagramSocket();
                    InetAddress inetAddress = InetAddress.getByName(ipAddress);
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, PORT);
                    datagramSocket.send(datagramPacket);
                    datagramSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
