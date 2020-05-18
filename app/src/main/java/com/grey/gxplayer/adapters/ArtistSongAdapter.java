package com.grey.gxplayer.adapters;

import android.app.Activity;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grey.gxplayer.R;
import com.grey.gxplayer.models.Song;
import com.grey.gxplayer.util.MyUtil;

import java.util.List;

import static com.grey.gxplayer.music.PlayerServices.playAll;

public class ArtistSongAdapter extends RecyclerView.Adapter<ArtistSongAdapter.AVH> {
    private Activity context;
    private List<Song> artistSongList;
    private long[] mIds;

    public ArtistSongAdapter(Activity context, List<Song> artistSongList) {
        this.context = context;
        this.artistSongList = artistSongList;
        mIds = getIds();
    }
    private long[] getIds() {
        long[] res = new long[getItemCount()];
        for (int i=0; i<getItemCount(); i++ ){
            res[i] = artistSongList.get(i).id;
        }
        return res;
    }

    @NonNull
    @Override
    public AVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AVH holder, int position) {
        Song song = artistSongList.get(position);
        if (song != null) {

            holder.atv.setText(song.title);
            holder.dtv.setText(song.artistName);
            int trackN = song.trackNumber;
            if (trackN == 0) {
                holder.ntv.setText("1");
            } else holder.ntv.setText(String.valueOf(trackN));
        }
    }

    @Override
    public int getItemCount() {
        return artistSongList != null ? artistSongList.size() : 0;
    }

    public class AVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView atv, ntv, dtv;

        public AVH(@NonNull View view) {
            super(view);

            atv = view.findViewById(R.id.songTitle);
            ntv = view.findViewById(R.id.number);
            dtv = view.findViewById(R.id.detail);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    try {
                        playAll(mIds, getAdapterPosition(), artistSongList.get(getAdapterPosition()).id, MyUtil.IdType.ARTIST);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 100);
        }
    }
}
