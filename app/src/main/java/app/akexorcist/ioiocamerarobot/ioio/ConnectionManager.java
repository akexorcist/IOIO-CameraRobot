package app.akexorcist.ioiocamerarobot.ioio;

import android.os.Handler;
import android.os.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import app.akexorcist.ioiocamerarobot.constant.Command;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class ConnectionManager {
    private ConnectionListener connectionListener;
    private ControllerCommandListener commandListener;
    private SendCommandListener sendListener;
    private OutputStream out;
    private DataOutputStream dos;
    private IOIOService ioio;
    private String password;


    public ConnectionManager(String password) {
        this.password = password;
    }

    public void setConnectionListener(ConnectionListener listener) {
        connectionListener = listener;
    }

    public void setCommandListener(ControllerCommandListener listener) {
        commandListener = listener;
    }

    public void setSendCommandListener(SendCommandListener sendListener) {
        this.sendListener = sendListener;
    }

    public void start() {
        ioio = new IOIOService(mHandler, password);
        ioio.execute();
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            onDataIncoming();
            int messageType = msg.what;
            if (messageType == Command.MESSAGE_PASS) {
                onControllerConnected((Socket) msg.obj);
            } else if (messageType == Command.MESSAGE_WRONG) {
                onControllerPasswordWrong((Socket) msg.obj);
            } else if (messageType == Command.MESSAGE_DISCONNECTED) {
                onControllerDisconnected();
            } else if (messageType == Command.MESSAGE_CLOSE) {
                onControllerClosed();
            } else if (messageType == Command.MESSAGE_FLASH) {
                onFlashCommand(msg.obj.toString());
            } else if (messageType == Command.MESSAGE_SNAP) {
                onRequestTakePicture();
            } else if (messageType == Command.MESSAGE_FOCUS) {
                onRequestAutoFocus();
            } else if (messageType == Command.MESSAGE_UP) {
                onMoveForwardCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_UPRIGHT) {
                onMoveForwardRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_UPLEFT) {
                onMoveForwardLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_DOWN) {
                onMoveBackwardCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_DOWNRIGHT) {
                onMoveBackwardRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_DOWNLEFT) {
                onMoveBackwardLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_RIGHT) {
                onMoveRightCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_LEFT) {
                onMoveLeftCommand((Integer) msg.obj);
            } else if (messageType == Command.MESSAGE_STOP) {
                onMoveStopCommand();
            }
        }
    };

    public void onDataIncoming() {
        if(connectionListener != null)
            connectionListener.onDataIncoming();
    }

    public void onControllerConnected(Socket socket) {
        try {
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            if(connectionListener != null)
                connectionListener.onControllerConnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onControllerPasswordWrong(Socket socket) {
        try {
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            restart();
            if(connectionListener != null)
                connectionListener.onWrongPassword();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onControllerDisconnected() {
        restart();
        if(connectionListener != null)
            connectionListener.onControllerDisconnected();
    }

    public void onControllerClosed() {
        restart();
        if(connectionListener != null)
            connectionListener.onControllerClosed();
    }

    public void onFlashCommand(String command) {
        if(commandListener != null)
            commandListener.onFlashCommand(command);
    }

    public void onRequestTakePicture() {
        if(commandListener != null)
            commandListener.onRequestTakePicture();
    }

    public void onRequestAutoFocus() {
        if(commandListener != null)
            commandListener.onRequestAutoFocus();
    }

    public void onMoveForwardCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveForwardCommand(speed);
    }

    public void onMoveForwardRightCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveForwardRightCommand(speed);
    }

    public void onMoveForwardLeftCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveForwardLeftCommand(speed);
    }

    public void onMoveBackwardCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveBackwardCommand(speed);
    }

    public void onMoveBackwardRightCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveBackwardRightCommand(speed);
    }

    public void onMoveBackwardLeftCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveBackwardLeftCommand(speed);
    }

    public void onMoveRightCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveRightCommand(speed);
    }

    public void onMoveLeftCommand(int speed) {
        if(commandListener != null)
            commandListener.onMoveLeftCommand(speed);
    }

    public void onMoveStopCommand() {
        if(commandListener != null)
            commandListener.onMoveStopCommand();
    }

    public void stop() {
        if (ioio != null)
            ioio.killTask();
    }

    public void restart() {
        stop();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                start();
            }
        }, 1000);
    }

    public void sendImageData(byte[] data) {
        try {
            dos.writeInt(data.length);
            dos.write(data);
            out.flush();
            if(sendListener != null)
                sendListener.onSendCommandSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            if(sendListener != null)
                sendListener.onSendCommandFailure();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String str) {
        try {
            dos.writeInt(str.length());
            dos.write(str.getBytes());
            out.flush();
            if(sendListener != null)
                sendListener.onSendCommandSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            if(sendListener != null)
                sendListener.onSendCommandFailure();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public interface ConnectionListener {
        public void onControllerConnected();
        public void onWrongPassword();
        public void onControllerDisconnected();
        public void onControllerClosed();
        public void onDataIncoming();
    }

    public interface ControllerCommandListener {
        public void onFlashCommand(String command);
        public void onRequestTakePicture();
        public void onRequestAutoFocus();
        public void onMoveForwardCommand(int speed);
        public void onMoveForwardRightCommand(int speed);
        public void onMoveForwardLeftCommand(int speed);
        public void onMoveBackwardCommand(int speed);
        public void onMoveBackwardRightCommand(int speed);
        public void onMoveBackwardLeftCommand(int speed);
        public void onMoveLeftCommand(int speed);
        public void onMoveRightCommand(int speed);
        public void onMoveStopCommand();
    }

    public interface SendCommandListener {
        public void onSendCommandSuccess();
        public void onSendCommandFailure();
    }
}
