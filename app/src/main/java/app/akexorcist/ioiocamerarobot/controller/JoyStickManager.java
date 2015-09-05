package app.akexorcist.ioiocamerarobot.controller;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import app.akexorcist.ioiocamerarobot.R;
import app.akexorcist.ioiocamerarobot.utils.JoyStickView;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class JoyStickManager implements View.OnTouchListener {
    private static final int JOYSTICK_COOLDOWN = 200;
    private JoyStickEventListener listener;
    private JoyStickView joystick;

    private long time = System.currentTimeMillis();

    @SuppressWarnings("deprecation")
    public JoyStickManager(Context context, ViewGroup layoutJoyStick, int screenHeight) {
        setupJoyStick(context, layoutJoyStick, screenHeight);
        layoutJoyStick.setOnTouchListener(this);
    }

    public void setJoyStickEventListener(JoyStickEventListener listener) {
        this.listener = listener;
    }

    private void setupJoyStick(Context context, ViewGroup layoutJoyStick, int screenHeight) {
        joystick = new JoyStickView(context, layoutJoyStick, R.drawable.image_button);
        joystick.setStickSize(screenHeight / 7, screenHeight / 7);
        joystick.setLayoutSize(screenHeight / 2, screenHeight / 2);
        joystick.setLayoutAlpha(100);
        joystick.setStickAlpha(255);
        joystick.setOffset((int) ((screenHeight / 9) * 0.6));
        joystick.setMinimumDistance((int) ((screenHeight / 9) * 0.6));
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        joystick.drawStick(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getJoyStickDirection();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - time > JOYSTICK_COOLDOWN) {
                getJoyStickDirection();
                time = currentTimeMillis;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null)
                listener.onJoyStickNone();
        }
        return true;
    }

    public void getJoyStickDirection() {
        int direction = joystick.get8Direction();
        int speed = (int) (joystick.getDistance() / 1.875) + 20;
        speed = (speed > 100) ? 100 : speed;
        speed = (speed < 0) ? 0 : speed;

        if (listener != null) {
            if (direction == JoyStickView.STICK_UP) {
                listener.onJoyStickUp(speed);
            } else if (direction == JoyStickView.STICK_UPRIGHT) {
                listener.onJoyStickUpRight(speed);
            } else if (direction == JoyStickView.STICK_RIGHT) {
                listener.onJoyStickRight(speed);
            } else if (direction == JoyStickView.STICK_DOWNRIGHT) {
                listener.onJoyStickDownRight(speed);
            } else if (direction == JoyStickView.STICK_DOWN) {
                listener.onJoyStickDown(speed);
            } else if (direction == JoyStickView.STICK_DOWNLEFT) {
                listener.onJoyStickDownLeft(speed);
            } else if (direction == JoyStickView.STICK_LEFT) {
                listener.onJoyStickLeft(speed);
            } else if (direction == JoyStickView.STICK_UPLEFT) {
                listener.onJoyStickUpLeft(speed);
            } else if (direction == JoyStickView.STICK_NONE) {
                listener.onJoyStickNone();
            }
        }
    }

    public interface JoyStickEventListener {
        public void onJoyStickUp(int speed);
        public void onJoyStickUpRight(int speed);
        public void onJoyStickUpLeft(int speed);
        public void onJoyStickDown(int speed);
        public void onJoyStickDownRight(int speed);
        public void onJoyStickDownLeft(int speed);
        public void onJoyStickRight(int speed);
        public void onJoyStickLeft(int speed);
        public void onJoyStickNone();
    }
}
