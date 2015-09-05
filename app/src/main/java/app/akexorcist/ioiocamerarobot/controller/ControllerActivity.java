package app.akexorcist.ioiocamerarobot.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import app.akexorcist.ioiocamerarobot.R;
import app.akexorcist.ioiocamerarobot.constant.Command;
import app.akexorcist.ioiocamerarobot.constant.ExtraKey;

public class ControllerActivity extends Activity implements ConnectionManager.IOIOResponseListener, ConnectionManager.ConnectionListener, OnClickListener, OnCheckedChangeListener, JoyStickManager.JoyStickEventListener {
    private ImageView ivCameraImage;
    private CheckBox cbFlash;

    private ConnectionManager connectionManager;
    private JoyStickManager joyStickManager;

    private Button btnTakePhoto;
    private Button btnAutoFocus;
    private RelativeLayout layoutJoyStick;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_controller);

        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        String ipAddress = getIntent().getExtras().getString(ExtraKey.IP_ADDRESS);
        String password = getIntent().getExtras().getString(ExtraKey.TARGET_PASSWORD);

        ivCameraImage = (ImageView) findViewById(R.id.iv_camera_image);

        layoutJoyStick = (RelativeLayout) findViewById(R.id.layout_joystick);
        joyStickManager = new JoyStickManager(this, layoutJoyStick, screenHeight);
        joyStickManager.setJoyStickEventListener(this);

        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(this);

        btnAutoFocus = (Button) findViewById(R.id.btn_auto_focus);
        btnAutoFocus.setOnClickListener(this);

        cbFlash = (CheckBox) findViewById(R.id.cbFlash);
        cbFlash.setOnCheckedChangeListener(this);

        connectionManager = new ConnectionManager(this, ipAddress, password);
        connectionManager.start();
        connectionManager.setConnectionListener(this);
        connectionManager.setResponseListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.stop();
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_auto_focus) {
            requestAutoFocus();
        } else if (id == R.id.btn_take_photo) {
            requestTakePhoto();
        }
    }

    public void requestAutoFocus() {
        connectionManager.sendCommand(Command.FOCUS);
    }

    public void requestTakePhoto() {
        connectionManager.sendCommand(Command.SNAP);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            connectionManager.sendCommand(Command.LED_ON);
        } else {
            connectionManager.sendCommand(Command.LED_OFF);
        }
    }

    @Override
    public void onPictureTaken() {
        showToast(getString(R.string.photo_taken));
    }

    @Override
    public void onFlashUnavailable() {
        showToast(getString(R.string.unsupport_flash));
    }

    @Override
    public void onCameraImageIncoming(Bitmap bitmap) {
        ivCameraImage.setImageBitmap(bitmap);
    }

    @Override
    public void onConnectionDown() {
        showToast(getString(R.string.connection_down));
        finish();
    }

    @Override
    public void onConnectionFailed() {
        showToast(getString(R.string.connection_failed));
        finish();
    }

    @Override
    public void onWrongPassword() {
        showToast(getString(R.string.wrong_password));
        finish();
    }

    @Override
    public void onIOIOConnected() {
        showToast(getString(R.string.connection_accepted));
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onJoyStickUp(int speed) {
        connectionManager.sendMovement(Command.FORWARD + speed);
    }

    @Override
    public void onJoyStickUpRight(int speed) {
        connectionManager.sendMovement(Command.FORWARD_RIGHT + speed);
    }

    @Override
    public void onJoyStickUpLeft(int speed) {
        connectionManager.sendMovement(Command.FORWARD_LEFT + speed);
    }

    @Override
    public void onJoyStickDown(int speed) {
        connectionManager.sendMovement(Command.BACKWARD + speed);
    }

    @Override
    public void onJoyStickDownRight(int speed) {
        connectionManager.sendMovement(Command.BACKWARD_RIGHT + speed);
    }

    @Override
    public void onJoyStickDownLeft(int speed) {
        connectionManager.sendMovement(Command.BACKWARD_LEFT + speed);
    }

    @Override
    public void onJoyStickRight(int speed) {
        connectionManager.sendMovement(Command.RIGHT + speed);
    }

    @Override
    public void onJoyStickLeft(int speed) {
        connectionManager.sendMovement(Command.LEFT + speed);
    }

    @Override
    public void onJoyStickNone() {
        connectionManager.sendMovement(Command.STOP);
        connectionManager.sendMovement(Command.STOP);
    }
}
