package com.aluxian.codementor.presentation.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.lib.DividerItemDecoration;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;
import com.aluxian.codementor.presentation.presenters.ChatroomsPresenter;
import com.aluxian.codementor.presentation.views.ChatroomsView;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

public class ChatroomsFragment extends BaseFragment<ChatroomsPresenter>
        implements ChatroomsView, ChatroomSelectedListener {

    @Bind(R.id.recycler) RecyclerView recyclerView;
    @Bind(R.id.swipe_container) SwipeRefreshLayout swipeRefreshLayout;

    private @Nullable ChatroomSelectedListener chatroomSelectedListener;
    private @Nullable DrawerLayout drawerLayout;
    private @Nullable ActionBarDrawerToggle drawerToggle;
    private @Nullable View fragmentContainerView;

    private boolean fromSavedInstanceState;
    private ChatroomsAdapter chatroomsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            fromSavedInstanceState = true;
        }

        chatroomsAdapter = new ChatroomsAdapter();
        chatroomsAdapter.setChatroomSelectedListener(this);
        chatroomsAdapter.setHasStableIds(true);

        setPresenter(new ChatroomsPresenter(this, getCoreServices(), chatroomsAdapter));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chatrooms, container, false);
        ButterKnife.bind(this, rootView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(chatroomsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.brand_accent));
        swipeRefreshLayout.setOnRefreshListener(getPresenter());

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onChatroomSelected(Chatroom chatroom) {
        closeDrawer();

        if (chatroomSelectedListener != null) {
            chatroomSelectedListener.onChatroomSelected(chatroom);
        }
    }

    @Override
    public void openDrawer() {
        if (drawerLayout != null && fragmentContainerView != null) {
            drawerLayout.openDrawer(fragmentContainerView);
        }
    }

    @Override
    public void closeDrawer() {
        if (drawerLayout != null && fragmentContainerView != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
    }

    @Override
    public void toggleDrawer() {
        if (drawerLayout != null && fragmentContainerView != null) {
            if (drawerLayout.isDrawerOpen(fragmentContainerView)) {
                drawerLayout.closeDrawer(fragmentContainerView);
            } else {
                drawerLayout.openDrawer(fragmentContainerView);
            }
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void showEmptyState(boolean show) {
        // TODO: toggle a view
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentContainerView The container view of the drawer fragment.
     * @param drawerLayout          The DrawerLayout containing this fragment's UI.
     */
    public void init(View fragmentContainerView, DrawerLayout drawerLayout) {
        this.fragmentContainerView = fragmentContainerView;
        this.drawerLayout = drawerLayout;

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        if (!fromSavedInstanceState) {
            openDrawer();
        }

        // Defer code dependent on the restoration of the previous instance state
        drawerLayout.post(drawerToggle::syncState);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    /**
     * Set a callback to be notified when a Chatroom is selected.
     *
     * @param chatroomSelectedListener The callback listener.
     */
    public void setChatroomSelectedListener(@Nullable ChatroomSelectedListener chatroomSelectedListener) {
        this.chatroomSelectedListener = chatroomSelectedListener;
    }

}
