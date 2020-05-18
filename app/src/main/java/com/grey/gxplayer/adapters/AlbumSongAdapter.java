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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import static com.grey.gxplayer.adapters.SongAdapter.getImage;
import static com.grey.gxplayer.music.PlayerServices.playAll;

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.ASVM>{

    private Activity context;
    private List<Song> albumSongList;
    private long[] mIds;

    public AlbumSongAdapter(Activity context, List<Song> albumSongList) {
        this.context = context;
        this.albumSongList = albumSongList;
        mIds = getIds();
    }

    private long[] getIds() {
        long[] res = new long[getItemCount()];
        for (int i=0; i<getItemCount(); i++ ){
            res[i] = albumSongList.get(i).id;
        }
        return res;
    }

    @NonNull
    @Override
    public ASVM onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ASVM(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ASVM holder, int position) {
        Song song = albumSongList.get(position);
        if(song != null){

            holder.atv.setText(song.title);
            holder.dtv.setText(song.artistName);
            int trackN = song.trackNumber;
            if(trackN == 0){
                holder.ntv.setText("1");
            }else holder.ntv.setText(String.valueOf(trackN));
        }
    }

    @Override
    public int getItemCount() {
        return albumSongList != null?albumSongList.size():0;
    }

    public class ASVM extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView atv, ntv, dtv;

        public ASVM(@NonNull View view) {
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
                        playAll(mIds, getAdapterPosition(), albumSongList.get(getAdapterPosition()).id, MyUtil.IdType.ALBUM);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }, 100);
        }
    }
}
