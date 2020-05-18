package com.grey.gxplayer.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.grey.gxplayer.R;
import com.grey.gxplayer.adapters.AlbumSongAdapter;
import com.grey.gxplayer.dataloader.AlbumLoader;
import com.grey.gxplayer.dataloader.AlbumSongLoader;
import com.grey.gxplayer.models.Album;
import com.grey.gxplayer.models.Song;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.grey.gxplayer.adapters.SongAdapter.getImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumDetailsFragment extends Fragment {

    CollapsingToolbarLayout collapsingToolbarLayout;
    private long album_id;

    private List<Song> songList = new ArrayList<>();
    private Album album;
    private ImageView imageView;
    private ImageView imageView1;
    private TextView arname, alti;
    private RecyclerView recyclerView;
    AlbumSongAdapter adapter;

    public static AlbumDetailsFragment newInstance(long id) {

        Bundle args = new Bundle();
        args.putLong("_ID", id);
        AlbumDetailsFragment fragment = new AlbumDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        album_id = getArguments().getLong("_ID");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_details, container, false);

        imageView1 = rootView.findViewById(R.id.aaimg);
        imageView = rootView.findViewById(R.id.bigart);
        arname = rootView.findViewById(R.id.atrname);
        alti = rootView.findViewById(R.id.albumDateIls);
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsing);
        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        album = new AlbumLoader().getAlbum(getActivity(), album_id);
            setDetails();
            setUpAlbumList();
        return rootView;
    }

    private void setUpAlbumList() {
        songList = AlbumSongLoader.getAllAlbumSongs(getActivity(), album_id);
        adapter = new AlbumSongAdapter(getActivity(), songList);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    private void setDetails() {
        collapsingToolbarLayout.setTitle(album.albumName);
        arname.setText(album.albumName);
        alti.setText(album.artistName+" "+album.year+" Songs: "+album.numSong);
        ImageLoader.getInstance().displayImage(getImage(album.id).toString(), imageView,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.ic_album_black_24dp)
                        .resetViewBeforeLoading(true).build());
        ImageLoader.getInstance().displayImage(getImage(album.id).toString(), imageView1,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.mipmap.ic_audio)
                        .resetViewBeforeLoading(true).build());

    }
}
