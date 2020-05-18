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
import com.grey.gxplayer.adapters.ArtistAdapter;
import com.grey.gxplayer.adapters.GridSpacingItemDecoration;
import com.grey.gxplayer.dataloader.AlbumLoader;
import com.grey.gxplayer.dataloader.ArtistLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArtistAdapter artistAdapter;
    int spanCount = 2;
    int spacing = 20;
    boolean includeEdge = false;

    public ArtistsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        recyclerView = view.findViewById(R.id.arr);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));

        new LoadData().execute("");

        return view;
    }

    public class LoadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            if (getActivity() != null) {
                artistAdapter = new ArtistAdapter(getActivity(), new ArtistLoader().artistsList(getActivity()));
            }
            return "Launched";
        }

        @Override
        protected void onPostExecute(String s) {
            recyclerView.setAdapter(artistAdapter);
            if(getActivity() != null ) {
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
