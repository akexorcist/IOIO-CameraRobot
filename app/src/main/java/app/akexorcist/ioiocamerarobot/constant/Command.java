package app.akexorcist.ioiocamerarobot.constant;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class Command {
    public static final String ACCEPT_CONNECTION = "ACCEPT";
    public static final String WRONG_PASSWORD = "WRONG";
    public static final String LED_ON = "LEDON";
    public static final String LED_OFF = "LEDOFF";
    public static final String FLASH_UNAVAILABLE = "NoFlash";
    public static final String SNAP = "SNAP";
    public static final String FOCUS = "FOCUS";

    // Unique command for IOIO Service
    public static final int MESSAGE_UPDATE = 0;
    public static final int MESSAGE_CLOSE = 1;
    public static final int MESSAGE_TOAST = 2;
    public static final int MESSAGE_PASS = 3;
    public static final int MESSAGE_WRONG = 4;
    public static final int MESSAGE_DISCONNECTED = 5;
    public static final int MESSAGE_FLASH = 6;
    public static final int MESSAGE_SNAP = 7;
    public static final int MESSAGE_FOCUS = 8;
    public static final int MESSAGE_STOP = 10;
    public static final int MESSAGE_UP = 11;
    public static final int MESSAGE_UPRIGHT = 12;
    public static final int MESSAGE_RIGHT = 13;
    public static final int MESSAGE_DOWNRIGHT = 14;
    public static final int MESSAGE_DOWN = 15;
    public static final int MESSAGE_DOWNLEFT = 16;
    public static final int MESSAGE_LEFT = 17;
    public static final int MESSAGE_UPLEFT = 18;

    public static final String FORWARD = "UU";
    public static final String FORWARD_RIGHT = "UR";
    public static final String FORWARD_LEFT = "UL";
    public static final String BACKWARD = "DD";
    public static final String BACKWARD_RIGHT = "DR";
    public static final String BACKWARD_LEFT = "DL";
    public static final String RIGHT = "RR";
    public static final String LEFT = "LL";
    public static final String STOP = "SS";


}
