package com.grey.gxplayer.adapters;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grey.gxplayer.R;
import com.grey.gxplayer.models.Song;
import com.grey.gxplayer.util.MyUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import static com.grey.gxplayer.music.PlayerServices.playAll;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.VM> {

    public static List<Song> songList;
    private long[] mIds;

    public SongAdapter(List<Song> songList) {
        this.songList = songList;
        mIds = getIds();
    }

    private long[] getIds() {
        long[] res = new long[getItemCount()];
        for (int i=0; i<getItemCount(); i++ ){
            res[i] = songList.get(i).id;
        }
        return res;
    }


    @NonNull
    @Override
    public VM onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new VM(LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VM holder, int position) {

        Song song = songList.get(position);
        if(song != null){
            holder.title.setText(song.title);
            holder.arts.setText(song.artistName);
            ImageLoader.getInstance().displayImage(getImage(song.albumId).toString(), holder.imageView,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.mipmap.ic_audio)
                    .resetViewBeforeLoading(true).build());
        }
    }

    public static Uri getImage(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),albumId);
    }


    @Override
    public int getItemCount() {
        return songList != null?songList.size():0;
    }

    public class VM extends RecyclerView.ViewHolder implements  View.OnClickListener{

        private ImageView imageView, img;
        private TextView title, arts;

        public VM(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.songimage);
            imageView= itemView.findViewById(R.id.songThumb);
            title = itemView.findViewById(R.id.songname);
            arts= itemView.findViewById(R.id.artistname);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    try {
                        playAll(mIds, getAdapterPosition(), songList.get(getAdapterPosition()).id, MyUtil.IdType.NA);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }, 100);

        }
    }
}
