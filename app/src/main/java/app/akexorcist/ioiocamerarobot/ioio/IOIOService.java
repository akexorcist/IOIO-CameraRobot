package app.akexorcist.ioiocamerarobot.ioio;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import app.akexorcist.ioiocamerarobot.constant.Command;

public class IOIOService extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "IOIOService";
    private static final int PORT = 21111;

    private boolean isTaskRunning = true;
    private ServerSocket serverSocket;
    private Socket socket;
    private String password;

    private DataInputStream dataInputStream;
    private InputStream inputStream;
    private Handler handler;

    public IOIOService(Handler handler, String password) {
        this.handler = handler;
        this.password = password;
    }

    protected Void doInBackground(Void... params) {
        Runnable run = new Runnable() {
            public void run() {

                try {
                    byte[] message = new byte[10];
                    DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                    DatagramSocket datagramSocket = new DatagramSocket(null);
                    datagramSocket.setReuseAddress(true);
                    datagramSocket.setBroadcast(true);
                    datagramSocket.bind(new InetSocketAddress(PORT));

                    while (isTaskRunning) {
                        try {
                            datagramSocket.setSoTimeout(3000);
                            datagramSocket.receive(datagramPacket);
                            String text = new String(message, 0, datagramPacket.getLength());
                            String command = text.substring(0, 2);
                            if (command.equalsIgnoreCase(Command.FORWARD)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_UP, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.FORWARD_RIGHT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_UPRIGHT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.FORWARD_LEFT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_UPLEFT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.BACKWARD)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_DOWN, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.BACKWARD_RIGHT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_DOWNRIGHT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.BACKWARD_LEFT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_DOWNLEFT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.RIGHT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_RIGHT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.LEFT)) {
                                int speed = Integer.parseInt(text.substring(2, text.length()));
                                handler.obtainMessage(Command.MESSAGE_LEFT, speed - 50).sendToTarget();
                            } else if (command.equalsIgnoreCase(Command.STOP)) {
                                handler.obtainMessage(Command.MESSAGE_STOP).sendToTarget();
                            }
                        } catch (SocketTimeoutException e) {
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    datagramSocket.close();
                    Log.e(TAG, "Kill Task");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(run).start();

        try {
            serverSocket = new ServerSocket(21111);
            serverSocket.setSoTimeout(2000);
            Log.i(TAG, "Waiting for connect");
            while (socket == null && isTaskRunning) {
                try {
                    socket = serverSocket.accept();
                    socket.setSoTimeout(2000);
                } catch (InterruptedIOException e) {
                    Log.i(TAG, "Waiting for connect");
                } catch (SocketException e) {
                   e.printStackTrace();
                }
            }

            if (isTaskRunning) {
                inputStream = socket.getInputStream();
                dataInputStream = new DataInputStream(inputStream);
                int size = dataInputStream.readInt();
                byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
                if ((new String(buffer)).equalsIgnoreCase(password)) {
                    handler.obtainMessage(Command.MESSAGE_PASS, socket).sendToTarget();
                } else {
                    handler.obtainMessage(Command.MESSAGE_WRONG, socket).sendToTarget();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isTaskRunning) {
            try {
                int size = dataInputStream.readInt();
                byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
                String data = new String(buffer);

                if (data.equalsIgnoreCase(Command.SNAP)) {
                    handler.obtainMessage(Command.MESSAGE_SNAP).sendToTarget();
                } else if (data.equalsIgnoreCase(Command.LED_ON) || data.equalsIgnoreCase(Command.LED_OFF)) {
                    handler.obtainMessage(Command.MESSAGE_FLASH, data).sendToTarget();
                } else if (data.equalsIgnoreCase(Command.FOCUS)) {
                    handler.obtainMessage(Command.MESSAGE_FOCUS).sendToTarget();
                }
            } catch (EOFException e) {
                e.printStackTrace();
                handler.obtainMessage(Command.MESSAGE_CLOSE).sendToTarget();
                break;
            } catch (IOException e) {
            }

            if (!socket.isConnected()) {
                handler.obtainMessage(Command.MESSAGE_DISCONNECTED).sendToTarget();
            }
        }
        try {
            serverSocket.close();
            socket.close();
            inputStream.close();
            dataInputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Service was killed");
        return null;
    }

    public void killTask() {
        isTaskRunning = false;
    }
}
