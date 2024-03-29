package com.bdac.zhcyc.minititok.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdac.zhcyc.minititok.Network.beans.Feed;
import com.bdac.zhcyc.minititok.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.FeedViewHolder>{

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_FOOTER = 2;


    private List<Feed> feeds = new ArrayList<>();
    private FeedListItemClickListener feedListItemClickListener;
    private FeedListRefreshedListener feedListRefreshedListener;
    private View mHeaderView;
    private View mFooterView;
    private ImageView imageView;


    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public void setFeedListItemClickListener(FeedListItemClickListener feedListItemClickListener) {
        this.feedListItemClickListener = feedListItemClickListener;
    }

    public void setFeedListRefreshedListener(FeedListRefreshedListener feedListRefreshedListener) {
        this.feedListRefreshedListener = feedListRefreshedListener;
    }

    public void refresh (List<Feed> feeds) {
        if (feeds != null) {
            this.feeds = feeds;
        }
        for (int i = 0; i < feeds.size(); i++){
            notifyItemChanged(i + 1);
        }
        feedListRefreshedListener.onFeedListItemRefreshed();
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(feeds.size() + 1);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getFooterView () {
        return mFooterView;
    }

    @Override
    public int getItemViewType(int position) {
//        if(mHeaderView == null) return TYPE_NORMAL;
//        if(position == 0) return TYPE_HEADER;
//        return TYPE_NORMAL;

        int dataSize = feeds.size();
        if (mHeaderView == null) {
            if (position >= 0 && position < dataSize) {
                return TYPE_NORMAL;
            } else {
                if (mFooterView != null) {
                    return TYPE_FOOTER;
                }
            }
        } else {
            if (position == 0) {
                return TYPE_HEADER;
            } else if (position > 0 && position <= dataSize) {
                return TYPE_NORMAL;
            } else {
                if (mFooterView != null) {
                    return TYPE_FOOTER;
                }
            }
        }
        return TYPE_NORMAL;

    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER) return new FeedViewHolder(mHeaderView);
        if (mFooterView != null && viewType == TYPE_FOOTER) return new FeedViewHolder(mFooterView);
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.item_feeds_feed;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        FeedViewHolder viewHolder = new FeedViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        if(getItemViewType(position) == TYPE_HEADER) return;
        if (getItemViewType(position) == TYPE_FOOTER) return;
        final int realPosition = mHeaderView == null ? position : position - 1;;
        Feed feed = feeds.get(realPosition);
        //TODO 更改bind操作
        holder.textView.setText(feed.getUser_name());
        String imageUrl = feed.getImage_url();
        String videoUrl = feed.getVideo_url();

        ImageView imageView = new ImageView(holder.getHolderView().getContext());
        Glide.with(imageView.getContext())
                .setDefaultRequestOptions(new RequestOptions().centerCrop().error(R.drawable.pic_nothing).placeholder(R.drawable.pic_nothing))
                .load(imageUrl).into(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        holder.videoPlayer.setUp(videoUrl, true, "");
        holder.videoPlayer.setThumbImageView(imageView);

        setAnimation(holder.cardView);

    }

    @Override
    public int getItemCount() {
        int count =  mHeaderView == null ? feeds.size() : feeds.size() + 1;
        count = mFooterView == null ? count : count + 1;
        return count;
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //TODO 添加所有控件
        private TextView textView;
        private FeedsListVideoPlayer videoPlayer;
        private View holderView;
        private ImageView imageView;

        private CardView cardView;
        protected  void givealike() {


            imageView.setOnClickListener(new View.OnClickListener(){


                @Override
                public void onClick(View view) {
                    //  if(imageView.getDrawable().getCurrent().getConstantState().equals(getResources().getDrawable(R.drawable.white).getConstantState())){
                    imageView.setImageDrawable(imageView.getResources().getDrawable(R.drawable.red));
                    //   }
                    //else{
                    //   imageView.setImageResource(R.drawable.white);
                    //  }
                }
            });

        }

        public View getHolderView() {
            return holderView;
        }

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            holderView = itemView;
            if(itemView == mHeaderView) return;
            if (itemView == mFooterView) return;
            cardView = itemView.findViewById(R.id.feeds_card_view);
            textView = itemView.findViewById(R.id.textView_name);
            videoPlayer = itemView.findViewById(R.id.item_video_player);
//            videoPlayer.getTitleTextView().setVisibility(View.GONE);
//            videoPlayer.getBackButton().setVisibility(View.GONE);
            videoPlayer.setThumbPlay(true);
            videoPlayer.setLooping(true);
            //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
            itemView.setOnClickListener(this);

        }

        public int getRealPosition(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return mHeaderView == null ? position : position - 1;
        }



        @Override
        public void onClick(View view) {

            int clickedPosition = getRealPosition(this);
            if (feedListItemClickListener != null) {
                feedListItemClickListener.onFeedListItemClicked(clickedPosition, feeds.get(clickedPosition));
            }
        }
    }


    private void setAnimation(View viewToAnimate) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R
                    .anim.item_bottom_in);
            viewToAnimate.startAnimation(animation);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull FeedViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        try {
            holder.cardView.clearAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface FeedListItemClickListener {
        void onFeedListItemClicked(int clickedItemIndex, Feed feed);

    }

    public interface FeedListRefreshedListener {
        void onFeedListItemRefreshed ();
    }
}
