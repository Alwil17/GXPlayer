package com.grey.gxplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.grey.gxplayer.fragments.ControllerFragment;
import com.grey.gxplayer.fragments.MainFragment;
import com.grey.gxplayer.music.PlayerServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static com.grey.gxplayer.music.PlayerServices.mRemot;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final int CODE = 123;
    private static final String TAG = "MainActivity";
    private SlidingUpPanelLayout panelLayout;

    private PlayerServices.ServiceToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        token = PlayerServices.bindToService(this, this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE);
            return;
        } else {
            Initialize();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Initialize();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void Initialize() {

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));

        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.slide_layout);

        Fragment fragment = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.commit();

        setPanelSlideListener(panelLayout);
        new initQuickControls().execute("");

    }

    private void setPanelSlideListener(SlidingUpPanelLayout panelLayout) {
        panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                View playingCon = ControllerFragment.top_container;
                playingCon.setAlpha(1-slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }

            public void onPanelCollapsed(View panel){
                View playingCon = ControllerFragment.top_container;
                playingCon.setAlpha(1);
            }

            public void onPanelExpanded(View panel){
                View playingCon = ControllerFragment.top_container;
                playingCon.setAlpha(0);
            }

            public void onPanelAnchored(View panel){

            }


            public void onPanelHidden(View panel){

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(token != null){
            PlayerServices.unbindToService(token);
            token = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mRemot = MusicAIDL.Stub.asInterface(iBinder);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mRemot = null;
    }

    public class initQuickControls extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            ControllerFragment fragment1 = new ControllerFragment();
            FragmentManager fragmentManager1 = getSupportFragmentManager();
            fragmentManager1.beginTransaction().replace(R.id.controls_container, fragment1).commitAllowingStateLoss();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onPreExecute() {

        }
    }
}