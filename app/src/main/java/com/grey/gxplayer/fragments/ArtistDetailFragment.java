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
import com.grey.gxplayer.adapters.ArtistSongAdapter;
import com.grey.gxplayer.dataloader.ArtistLoader;
import com.grey.gxplayer.dataloader.ArtistSongLoader;
import com.grey.gxplayer.models.Artists;
import com.grey.gxplayer.models.Song;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.grey.gxplayer.adapters.SongAdapter.getImage;

public class ArtistDetailFragment extends Fragment {
    private CollapsingToolbarLayout cpToolbarLayout;
    private long artistId;

    private List<Song> artistSongList = new ArrayList<>();
    private Artists artists;
    private ImageView artimg, bigartist;
    private TextView artistname, albumti;
    private RecyclerView recycler1;
    private ArtistSongAdapter adapter;

    public static ArtistDetailFragment newInstance(long artist_id) {

        Bundle args = new Bundle();
        args.putLong("_ID", artist_id);
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        artistId = getArguments().getLong("_ID");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        bigartist = rootView.findViewById(R.id.artistimg);
        artimg = rootView.findViewById(R.id.bigartist);
        artistname = rootView.findViewById(R.id.artistname);
        albumti = rootView.findViewById(R.id.artistDateIls);
        cpToolbarLayout = rootView.findViewById(R.id.artistcollapsing);
        recycler1 = rootView.findViewById(R.id.recycler1);
        recycler1.setLayoutManager(new LinearLayoutManager(getActivity()));
        artists = new ArtistLoader().getArtist(getActivity(), artistId);
        setDetails();
        setUpArtistList();
        return rootView;
    }

    private void setUpArtistList() {
        artistSongList = ArtistSongLoader.getAllArtistSongs(getActivity(), artistId);
        adapter = new ArtistSongAdapter(getActivity(), artistSongList);
        recycler1.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recycler1.setAdapter(adapter);
    }

    private void setDetails() {
        cpToolbarLayout.setTitle(artists.artName);
        artistname.setText(artists.artName);
        albumti.setText("Songs: "+artists.songCount);
        ImageLoader.getInstance().displayImage(getImage(artists.id).toString(), bigartist,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.ic_album_black_24dp)
                        .resetViewBeforeLoading(true).build());
        ImageLoader.getInstance().displayImage(getImage(artists.id).toString(), artimg,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.mipmap.ic_audio)
                        .resetViewBeforeLoading(true).build());

    }
}
