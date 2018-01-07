package com.alium.orin.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.admodule.AdModule;
import com.alium.orin.adapter.base.MediaEntryViewHolder;
import com.alium.orin.glide.SongGlideRequest;
import com.alium.orin.glide.artistimage.ArtistImage;
import com.alium.orin.helper.MusicPlayerRemote;
import com.alium.orin.model.Album;
import com.alium.orin.model.Song;
import com.alium.orin.soundcloud.HomeSound;
import com.alium.orin.soundcloud.Track;
import com.alium.orin.util.ArtistSignatureUtil;
import com.alium.orin.util.MusicUtil;
import com.alium.orin.util.NavigationUtil;
import com.alium.orin.util.StatReportUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.alium.orin.R;
import com.alium.orin.helper.menu.SongMenuHelper;
import com.alium.orin.model.Artist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private static final int HEADER = 0;
    private static final int ALBUM = 1;
    private static final int ARTIST = 2;
    private static final int SONG = 3;
    private static final int SOUND_CLOUND = 4;

    private final AppCompatActivity activity;
    private List<Object> dataSet;

    public SearchAdapter(@NonNull AppCompatActivity activity, @NonNull List<Object> dataSet) {
        this.activity = activity;
        this.dataSet = dataSet;
    }

    public void swapDataSet(@NonNull List<Object> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) instanceof Album) return ALBUM;
        if (dataSet.get(position) instanceof Artist) return ARTIST;
        if (dataSet.get(position) instanceof Song) return SONG;
        if (dataSet.get(position) instanceof Track) return SOUND_CLOUND;
        return HEADER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER)
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.sub_header, parent, false), viewType);
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false), viewType);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ALBUM:
                final Album album = (Album) dataSet.get(position);
                holder.title.setText(album.getTitle());
                holder.text.setText(album.getArtistName());
                SongGlideRequest.Builder.from(Glide.with(activity), album.safeGetFirstSong())
                        .checkIgnoreMediaStore(activity).build()
                        .placeholder(R.drawable.default_album_art)
                        .into(holder.image);
                break;
            case ARTIST:
                final Artist artist = (Artist) dataSet.get(position);
                holder.title.setText(artist.getName());
                holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
                Glide.with(activity)
                        .load(new ArtistImage(artist.getName(), false))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.default_artist_image)
                        .animate(android.R.anim.fade_in)
                        .priority(Priority.LOW)
                        .signature(ArtistSignatureUtil.getInstance(activity).getArtistSignature(artist.getName()))
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(holder.image);
                break;
            case SONG:
                final Song song = (Song) dataSet.get(position);
                holder.title.setText(song.title);
                holder.text.setText(song.albumName);
                break;
            case SOUND_CLOUND:
                final Track track = (Track) dataSet.get(position);
                holder.title.setText(track.getTitle());
                Pair<String,String> temp = getTime(track.getDuration());
                holder.text.setText(temp.first + ":" + temp.second);
                holder.image.setVisibility(View.VISIBLE);
                Glide.with(activity)
                        .load(track.getArtworkURL())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.default_artist_image)
                        .animate(android.R.anim.fade_in)
                        .into(holder.image);
                break;
            default:
                holder.title.setText(dataSet.get(position).toString());
                break;
        }
    }

    public Pair<String, String> getTime(int millsec) {
        int min, sec;
        sec = millsec / 1000;
        min = sec / 60;
        sec = sec % 60;
        String minS, secS;
        minS = String.valueOf(min);
        secS = String.valueOf(sec);
        if (sec < 10) {
            secS = "0" + secS;
        }
        return Pair.create(minS, secS);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);
            itemView.setOnLongClickListener(null);

            if (itemViewType != HEADER) {
                itemView.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }
                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
            }

            if (menu != null) {
                if (itemViewType == SONG) {
                    menu.setVisibility(View.VISIBLE);
                    menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                        @Override
                        public Song getSong() {
                            return (Song) dataSet.get(getAdapterPosition());
                        }
                    });
                } else {
                    menu.setVisibility(View.GONE);
                }
            }

            switch (itemViewType) {
                case ALBUM:
                    setImageTransitionName(activity.getString(R.string.transition_album_art));
                    break;
                case ARTIST:
                    setImageTransitionName(activity.getString(R.string.transition_artist_image));
                    break;
                case SOUND_CLOUND:
                    break;
                default:
                    View container = itemView.findViewById(R.id.image_container);
                    if (container != null) {
                        container.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            Object item = dataSet.get(getAdapterPosition());
            switch (getItemViewType()) {
                case ALBUM:
                    NavigationUtil.goToAlbum(activity,
                            ((Album) item).getId(),
                            Pair.create(image,
                                    activity.getResources().getString(R.string.transition_album_art)
                            ));
                    break;
                case ARTIST:
                    NavigationUtil.goToArtist(activity,
                            ((Artist) item).getId(),
                            Pair.create(image,
                                    activity.getResources().getString(R.string.transition_artist_image)
                            ));
                    break;
                case SONG:
                    ArrayList<Song> playList = new ArrayList<>();
                    playList.add((Song) item);
                    MusicPlayerRemote.openQueue(playList, 0, true);
                    break;
                case SOUND_CLOUND:
                    Track track = (Track)item;
                    HomeSound.ContentsBeanX.ContentsBean contentsBean = new HomeSound.ContentsBeanX.ContentsBean();
                    contentsBean.song_download_url = track.getStreamURL();
                    contentsBean.album_images = track.getArtworkURL();
                    contentsBean.title = track.getTitle();
                    contentsBean.song_play_time = String.valueOf(track.getDuration());
                    playList = new ArrayList<>();
                    playList.add(contentsBean);
                    MusicPlayerRemote.openQueue(playList, 0, true);

                    try {
                        AdModule.getInstance().getAdMob().showInterstitialAd();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    StatReportUtils.trackCustomEvent("search_page", "listItem click");
                    break;
            }
        }
    }
}
