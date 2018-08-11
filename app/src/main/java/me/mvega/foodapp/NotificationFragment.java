package me.mvega.foodapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Notification;

import static me.mvega.foodapp.MainActivity.currentUser;


public class NotificationFragment extends Fragment {

    private Context context;
    @BindView(R.id.swipeContainerNotifications)
    SwipeRefreshLayout swipeContainerNotifications;
    @BindView(R.id.rvNotifications)
    RecyclerView rvNotifications;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.ivUserImage)
    ImageView ivUserImage;
    @BindView(R.id.tvNoNotifications)
    TextView tvNoNotifications;

    private NotificationAdapter notificationAdapter;
    private NotificationRecipeFragmentCommunication notificationRecipeListenerFragment;
    private NotificationUserFragmentCommunication notificationUserListenerFragment;

    // implement recipe listener interface
    public interface NotificationRecipeFragmentCommunication {
        void respond(ParseObject notificationRecipe);
    }

    // implement user listener interface
    public interface NotificationUserFragmentCommunication {
        void respond(ParseUser notificationUser);
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_notification, parent, false);
    }


    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof NotificationRecipeFragmentCommunication) {
            notificationRecipeListenerFragment = (NotificationRecipeFragmentCommunication) context;
        }
        if (context instanceof NotificationUserFragmentCommunication) {
            notificationUserListenerFragment = (NotificationUserFragmentCommunication) context;
        }
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        ivUserImage.setVisibility(View.INVISIBLE);
        tvNoNotifications.setVisibility(View.INVISIBLE);
        pbLoading.setVisibility(ProgressBar.VISIBLE);

        ArrayList<Notification> notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notifications);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvNotifications.setLayoutManager(linearLayoutManager);
        rvNotifications.setAdapter(notificationAdapter);

        notificationAdapter.setNotificationRecipeListener(new NotificationAdapter.NotificationAdapterRecipeCommunication() {
            @Override
            public void respond(ParseObject notificationRecipe) {
                notificationRecipeListenerFragment.respond(notificationRecipe);
            }
        });

        notificationAdapter.setNotificationUserListener(new NotificationAdapter.NotificationAdapterUserCommunication() {
            @Override
            public void respond(ParseUser notificationUser) {
                notificationUserListenerFragment.respond(notificationUser);
            }
        });
//

        loadYourNotifications();
        setSwipeContainer();
    }


    private void setSwipeContainer() {
        // Setup refresh listener which triggers new data loading
        swipeContainerNotifications.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(NotificationFragment.this).attach(NotificationFragment.this).commit();
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.

//                notificationAdapter.addAll(newNotification);
                loadYourNotifications();
            }
        });
        // Configure the refreshing colors
        swipeContainerNotifications.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public static NotificationFragment newInstance() {
        NotificationFragment fragmentNotification = new NotificationFragment();
        fragmentNotification.setArguments(new Bundle());
        return fragmentNotification;
    }

    public void loadYourNotifications() {
        final Notification.Query notificationQuery = new Notification.Query();
        notificationQuery.recipeUser(currentUser).whereNotEqualTo("activeUser", currentUser);
        notificationQuery.getTop().newestFirst();
        notificationQuery.include("activeUser.username")
                .include("activeUser.image")
                .include("recipe.user")
                .include("recipe.image")
                .include("recipe.views");
        notificationQuery.findInBackground(new FindCallback<Notification>() {
            @Override
            public void done(final List<Notification> newNotification, ParseException e) {
                if (e == null) {
                    if (newNotification.size() != 0) {
                        ArrayList<Notification> notifications = new ArrayList<>();
                        notificationAdapter = new NotificationAdapter(notifications);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                        rvNotifications.setLayoutManager(linearLayoutManager);
                        rvNotifications.setAdapter(notificationAdapter);

                        notificationAdapter.setNotificationRecipeListener(new NotificationAdapter.NotificationAdapterRecipeCommunication() {
                            @Override
                            public void respond(ParseObject notificationRecipe) {
                                notificationRecipeListenerFragment.respond(notificationRecipe);
                            }
                        });

                        notificationAdapter.setNotificationUserListener(new NotificationAdapter.NotificationAdapterUserCommunication() {
                            @Override
                            public void respond(ParseUser notificationUser) {
                                notificationUserListenerFragment.respond(notificationUser);
                            }
                        });

                        notificationAdapter.clear();
                        notificationAdapter.addAll(newNotification);
                        swipeContainerNotifications.setRefreshing(false);
                        pbLoading.setVisibility(ProgressBar.INVISIBLE);

                    } else {
                        pbLoading.setVisibility(ProgressBar.INVISIBLE);
                        rvNotifications.setVisibility(View.INVISIBLE);

                        ivUserImage.setVisibility(View.VISIBLE);
                        ParseFile profileImage = currentUser.getParseFile("image");
                        if (profileImage != null) {
                            String imageUrl = profileImage.getUrl();
                            Glide.with(context).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivUserImage);
                        } else
                            Glide.with(context).load(R.drawable.image_placeholder).apply(RequestOptions.circleCropTransform()).into(ivUserImage);

                        tvNoNotifications.setVisibility(View.VISIBLE);

                        swipeContainerNotifications.setRefreshing(false);
                        pbLoading.setVisibility(ProgressBar.INVISIBLE);
                    }

                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}