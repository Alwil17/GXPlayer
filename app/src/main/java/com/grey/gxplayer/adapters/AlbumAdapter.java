package com.grey.gxplayer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.grey.gxplayer.R;
import com.grey.gxplayer.fragments.AlbumDetailsFragment;
import com.grey.gxplayer.models.Album;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import static com.grey.gxplayer.adapters.SongAdapter.getImage;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>{

    private Activity context;
    private List<Album> albumList;

    public AlbumAdapter(Activity context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_grid,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        if(album != null){
            holder.alN.setText(album.albumName);
            holder.artN.setText(album.artistName);
            ImageLoader.getInstance().displayImage(getImage(album.id).toString(), holder.alimg,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.mipmap.ic_album)
                            .resetViewBeforeLoading(true).build());
        }
    }

    @Override
    public int getItemCount() {
        return albumList != null?albumList.size():0;
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView alimg;
        private TextView alN, artN;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            alimg = itemView.findViewById(R.id.albumimg);
            alN = itemView.findViewById(R.id.albumn);
            artN = itemView.findViewById(R.id.atn);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            long albumId = albumList.get(getAdapterPosition()).id;

            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment;
            transaction.setCustomAnimations(R.anim.layout_fab_in, R.anim.layout_fab_out, R.anim.layout_fab_in, R.anim.layout_fab_out);
            fragment = AlbumDetailsFragment.newInstance(albumId);
            transaction.hide(((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(R.id.main_container));
            transaction.add(R.id.main_container, fragment);
            transaction.addToBackStack(null).commit();
        }
    }

}
