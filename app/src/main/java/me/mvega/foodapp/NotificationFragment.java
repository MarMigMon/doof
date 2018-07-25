package me.mvega.foodapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import me.mvega.foodapp.model.Notification;


public class NotificationFragment extends Fragment {

    private SwipeRefreshLayout swipeContainerNotifications;
    private RecyclerView rvNotifications;
    ArrayList<Notification> notifications;
    private NotificationAdapter notificationAdapter;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_notification, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        swipeContainerNotifications = view.findViewById(R.id.swipeContainerNotifications);


        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notifications);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvNotifications.setLayoutManager(linearLayoutManager);
        rvNotifications.setAdapter(notificationAdapter);

//        notificationAdapter.setListener(new NotificationAdapter().AdapterCommunication() {
//            @Override
//            public void respond(Recipe recipe) {
//                listenerFragment.respond(recipe);
//            }
//
//            @Override
//            public void respond(Recipe recipe, ImageView image) {
//                listenerFragment.respond(recipe, image);
//            }
//        });

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvNotifications.addItemDecoration(itemDecoration);

        loadTopNotifications();
        setSwipeContainer();
    }

//    private void searchRecipes(String query) {
//        recipeQuery.getTop().withUser().newestFirst().containsQuery(query);
//        recipeQuery.findInBackground(new FindCallback<Recipe>() {
//            @Override
//            public void done(List<Recipe> newRecipes, ParseException e) {
//                resetAdapter(newRecipes, e);
//            }
//        });
//    }

    public void setSwipeContainer() {
        // Setup refresh listener which triggers new data loading
        swipeContainerNotifications.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadTopNotifications();
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

    private void loadTopNotifications() {
        final Notification.Query notificationQuery = new Notification.Query();
        notificationQuery.getTop().newestFirst();
        notificationQuery.include("activeUser.username")
                .include("activeUser.image")
                .include("recipe.user")
                .include("recipe.image");
        notificationQuery.findInBackground(new FindCallback<Notification>() {
            @Override
            public void done(List<Notification> newNotification, ParseException e) {
                if (e == null) {
                    notificationAdapter.clear();
                    notificationAdapter.addAll(newNotification);
                    swipeContainerNotifications.setRefreshing(false);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
