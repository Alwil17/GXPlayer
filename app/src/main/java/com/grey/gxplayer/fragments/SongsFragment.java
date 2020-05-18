package com.grey.gxplayer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grey.gxplayer.R;
import com.grey.gxplayer.adapters.SongAdapter;
import com.grey.gxplayer.dataloader.SongLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment {

    private SongAdapter songAdapter;
    private RecyclerView recyclerView;
    public SongsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.srview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        new LoadData().execute("");
        return view;
    }

    public class LoadData extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            if(getActivity() != null){
                songAdapter = new SongAdapter(new SongLoader().getAllSongs(getActivity()));
            }
            return "Launched";
        }

        @Override
        protected void onPostExecute(String s) {
            if(getActivity() != null ){
                recyclerView.setAdapter(songAdapter);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
