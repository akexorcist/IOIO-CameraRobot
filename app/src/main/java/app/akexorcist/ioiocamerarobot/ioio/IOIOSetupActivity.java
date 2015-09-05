package app.akexorcist.ioiocamerarobot.ioio;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.akexorcist.ioiocamerarobot.R;
import app.akexorcist.ioiocamerarobot.constant.ExtraKey;

public class IOIOSetupActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {
    private TextView tvImageQuality;
    private EditText etPassword;
    private SeekBar sbImageQuality;
    private Button btnOk;
    private Button btnPreviewSizeChooser;
    private ArrayList<String> previewSizeList;

    private int selectedSizePosition;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_ioio_setup);

        SharedPreferences settings = getSharedPreferences(ExtraKey.SETUP_PREFERENCE, Context.MODE_PRIVATE);
        selectedSizePosition = settings.getInt(ExtraKey.PREVIEW_SIZE, 0);
        String password = settings.getString(ExtraKey.OWN_PASSWORD, "");
        int quality = settings.getInt(ExtraKey.QUALITY, 100);

        initCameraPreviewSize();

        etPassword = (EditText) findViewById(R.id.et_password);
        etPassword.setText(password);

        btnPreviewSizeChooser = (Button) findViewById(R.id.btn_preview_size_chooser);
        updateSeletedPreviewSize();
        btnPreviewSizeChooser.setOnClickListener(this);

        tvImageQuality = (TextView) findViewById(R.id.tv_image_quality);
        updateTextViewQuality(quality);

        sbImageQuality = (SeekBar) findViewById(R.id.sb_image_quality);
        sbImageQuality.setProgress(quality);
        sbImageQuality.setOnSeekBarChangeListener(this);

        btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_preview_size_chooser) {
            createPreviewSizeChooserDialog();
        } else if (id == R.id.btn_ok) {
            confirmSetup();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        saveImageQuality(progress);
        updateTextViewQuality(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    public void updateSeletedPreviewSize() {
        String strSize = previewSizeList.get(selectedSizePosition);
        btnPreviewSizeChooser.setText(strSize);
    }

    public void updateTextViewQuality(int quality) {
        tvImageQuality.setText(getString(R.string.image_quality, quality));
    }

    public void savePassword(String password) {
        getPreferenceEditor().putString(ExtraKey.OWN_PASSWORD, password).apply();
    }

    public void saveImageQuality(int quality) {
        getPreferenceEditor().putInt(ExtraKey.QUALITY, quality).apply();
    }

    public void saveImagePreviewSize(int size) {
        getPreferenceEditor().putInt(ExtraKey.PREVIEW_SIZE, size).apply();
    }

    public SharedPreferences.Editor getPreferenceEditor() {
        SharedPreferences settings = getSharedPreferences(ExtraKey.SETUP_PREFERENCE, Context.MODE_PRIVATE);
        return settings.edit();
    }

    public void goToIOIOController() {
        Intent intent = new Intent(this, IOIOControllerActivity.class);
        intent.putExtra(ExtraKey.OWN_PASSWORD, etPassword.getText().toString());
        intent.putExtra(ExtraKey.PREVIEW_SIZE, selectedSizePosition);
        intent.putExtra(ExtraKey.QUALITY, sbImageQuality.getProgress());
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    public void initCameraPreviewSize() {
        Camera mCamera;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open();
        } else {
            mCamera = Camera.open(0);
        }
        Camera.Parameters params = mCamera.getParameters();
        initPreviewSizeList(params.getSupportedPreviewSizes());
        mCamera.release();
    }

    @SuppressWarnings("deprecation")
    public void initPreviewSizeList(List<Size> previewSize) {
        previewSizeList = new ArrayList<>();
        for (int i = 0; i < previewSize.size(); i++) {
            String str = previewSize.get(i).width + " x " + previewSize.get(i).height;
            previewSizeList.add(str);
        }
    }

    public void createPreviewSizeChooserDialog() {
        final Dialog dialogSize = new Dialog(this);
        dialogSize.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSize.setContentView(R.layout.dialog_camera_size);
        dialogSize.setCancelable(true);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.view_simple_textview, previewSizeList);
        ListView lvAvailablePreviewSize = (ListView) dialogSize.findViewById(R.id.lv_available_preview_size);
        lvAvailablePreviewSize.setAdapter(adapter);
        lvAvailablePreviewSize.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedSizePosition = position;
                saveImagePreviewSize(position);
                updateSeletedPreviewSize();
                dialogSize.cancel();
            }
        });
        dialogSize.show();
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void confirmSetup() {
        String strPassword = etPassword.getText().toString();
        if (strPassword.length() != 0) {
            savePassword(strPassword);
            goToIOIOController();
        } else {
            showToast(getString(R.string.password_is_required));
        }
    }
}
