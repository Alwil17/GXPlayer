package com.grey.gxplayer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.grey.gxplayer.R;

import static com.grey.gxplayer.music.PlayerServices.*;

public class ControllerFragment extends Fragment{


    private TextView artist, song, al, elaps, durat, songnom, artistnom;
    private SeekBar seekBar;
    private ImageView previous, next , play_pause, pp, songimage;
    public static View top_container;
    private View view;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        this.view = view;

        artist = view.findViewById(R.id.song_arti);
        song = view.findViewById(R.id.song_titre);
        al = view.findViewById(R.id.song_al);
        elaps = view.findViewById(R.id.song_ellapsed_time);
        durat = view.findViewById(R.id.song_duration);
        seekBar = view.findViewById(R.id.song_progress);
        previous = view.findViewById(R.id.skip_previous);
        next = view.findViewById(R.id.skip_next);
        play_pause = view.findViewById(R.id.play_pause);

        pp = view.findViewById(R.id.plystop);
        songimage = view.findViewById(R.id.songimage);
        songnom = view.findViewById(R.id.songnom);
        artistnom = view.findViewById(R.id.artistnom);

        top_container = view.findViewById(R.id.top_container);

        clickFuncs();

        return view;
    }

    private void clickFuncs(){

        pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isPlaying()){
                            try {
                                pause();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                play();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 100);
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isPlaying()){
                            try {
                                pause();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                play();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 100);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isPlaying()){
                            prev();
                        }else{
                            prev();
                        }
                    }
                }, 100);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isPlaying()){
                            try {
                                next();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                next();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 100);
            }
        });


    }
}
