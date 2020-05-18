package com.grey.gxplayer.adapters;

import android.content.Context;
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
import com.grey.gxplayer.fragments.ArtistDetailFragment;
import com.grey.gxplayer.models.Artists;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import static com.grey.gxplayer.adapters.SongAdapter.getImage;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ARV> {

    private Context context;
    private List<Artists> artistsList;

    public ArtistAdapter(Context context, List<Artists> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ARV holder, int position) {
        Artists artists = artistsList.get(position);
        if(artists != null){
            holder.artNaem.setText(artists.artName);
            ImageLoader.getInstance().displayImage(getImage(artists.id).toString(), holder.artimg,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.mipmap.ic_audio)
                            .resetViewBeforeLoading(true).build());
        }
    }

    @Override
    public int getItemCount() {
        return artistsList != null?artistsList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView artimg;
        private TextView artNaem;

        public ARV(@NonNull View itemView) {
            super(itemView);
            artimg = itemView.findViewById(R.id.arthumb);
            artNaem = itemView.findViewById(R.id.artname);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            long artistId = artistsList.get(getAdapterPosition()).id;

            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment;

            transaction.setCustomAnimations(R.anim.layout_fab_in, R.anim.layout_fab_out, R.anim.layout_fab_in, R.anim.layout_fab_out);

            fragment = ArtistDetailFragment.newInstance(artistId);

            transaction.hide(((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(R.id.main_container));

            transaction.add(R.id.main_container, fragment);
            transaction.addToBackStack(null).commit();
        }
    }


}
