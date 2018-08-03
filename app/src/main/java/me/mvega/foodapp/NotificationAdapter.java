package me.mvega.foodapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Notification;
import me.mvega.foodapp.model.Recipe;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private NotificationAdapterRecipeCommunication nrCommunication;
    private NotificationAdapterUserCommunication nuCommunication;
    private final List<Notification> notifications;
    private Context context;

    // notification recipe listener
    public interface NotificationAdapterRecipeCommunication {
        void respond (ParseObject notificationRecipe);
    }
    public void setNotificationRecipeListener(NotificationAdapterRecipeCommunication notificationRecipeListener) {
        this.nrCommunication = notificationRecipeListener;
    }

    // notification user listener
    public interface NotificationAdapterUserCommunication {
        void respond(ParseUser notificationUser);
    }
    public void setNotificationUserListener(NotificationAdapterUserCommunication notificationUserListener) {
        this.nuCommunication = notificationUserListener;
    }

    // pass in the Notifications array in the constructor
    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View notificationView = inflater.inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(notificationView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Notification notification = notifications.get(i);

        holder.tvActiveUser.setText(notification.getActiveUser().getUsername());
        holder.tvRelativeTime.setText(getRelativeTimeAgo(notification.getCreatedAt().toString()));

        ParseFile userPicture = notification.getActiveUser().getParseFile("image");
            if (userPicture != null) {
                String userPictureUrl = userPicture.getUrl();
                Glide.with(context).load(userPictureUrl).apply(RequestOptions.circleCropTransform()).into(holder.ivActiveUserImage);
            } else {
                holder.ivActiveUserImage.setImageResource(R.drawable.ic_profile);
            }

        ParseFile recipePicture = notification.getRecipe().getParseFile("image");
            if (recipePicture != null) {
                String recipePictureUrl = recipePicture.getUrl();
                Glide.with(context).load(recipePictureUrl).into(holder.ivRecipe);
            } else {
                holder.ivRecipe.setImageResource(R.drawable.image_placeholder);
            }

        if (notification.getFavorite()) {
            holder.tvNotificationMessage.setText(R.string.liked);
        }
        if (notification.getRate()) {
            holder.tvNotificationMessage.setText(R.string.rated);
        }
    }


    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.ivActiveUserImage) ImageView ivActiveUserImage;
        @BindView(R.id.ivRecipe) ImageView ivRecipe;
        @BindView(R.id.tvActiveUser) TextView tvActiveUser;
        @BindView(R.id.tvNotificationMessage) TextView tvNotificationMessage;
        @BindView(R.id.tvRelativeTime) TextView tvRelativeTime;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            // add this as the itemView's OnClickListener
            itemView.setOnClickListener(this);
            ivRecipe.setOnClickListener(this);
            tvActiveUser.setOnClickListener(this);
            ivActiveUserImage.setOnClickListener(this);
        }

        // when the user clicks on a row, show details for the selected recipe
        @Override
        public void onClick(View v) {
            if (v.getId() == ivRecipe.getId()) {
                int position = getAdapterPosition();
                // make sure the position is valid, i.e. actually exists in the view
                if (position != RecyclerView.NO_POSITION) {
                    // get the recipe at the position, this won't work if the class is static
                    final Notification notification = notifications.get(position);
                    Recipe notificationRecipe = (Recipe) notification.getRecipe();
                    // update view count when recipe is clicked
                    notificationRecipe.setViews(notificationRecipe.getViews() + 1);
                    notificationRecipe.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d("Recipe", "Saved");
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                    nrCommunication.respond(notificationRecipe);
                }
            }
            if (v.getId() == tvActiveUser.getId()) {
                int position = getAdapterPosition();
                // make sure the position is valid, i.e. actually exists in the view
                if (position != RecyclerView.NO_POSITION) {
                    // get the recipe at the position, this won't work if the class is static
                    final Notification notification = notifications.get(position);
                    ParseUser notificationUser = notification.getActiveUser();
                    nuCommunication.respond(notificationUser);
                }
            }
        }
    }

    private String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }

    // Clean all elements of the recycler
    public void clear() {
        notifications.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Notification> list) {
        notifications.addAll(list);
        notifyDataSetChanged();
    }


}
