package com.grey.gxplayer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grey.gxplayer.R;
import com.grey.gxplayer.adapters.AlbumAdapter;
import com.grey.gxplayer.adapters.GridSpacingItemDecoration;
import com.grey.gxplayer.adapters.SongAdapter;
import com.grey.gxplayer.dataloader.AlbumLoader;
import com.grey.gxplayer.dataloader.SongLoader;
import com.grey.gxplayer.models.Album;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    int spanCount = 2;
    int spacing = 20;
    boolean includeEdge = false;

    public AlbumsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_albums, container, false);

        recyclerView = view.findViewById(R.id.ar);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));

        new LoadData().execute("");

        return view;
    }

    public class LoadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            if(getActivity() != null){
                albumAdapter = new AlbumAdapter(getActivity(), new AlbumLoader().albumList(getActivity()));
            }
            return "Launched";
        }

        @Override
        protected void onPostExecute(String s) {
            recyclerView.setAdapter(albumAdapter);
            if(getActivity() != null ){
                recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge) {
                });
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
