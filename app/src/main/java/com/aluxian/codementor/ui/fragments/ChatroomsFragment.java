package com.aluxian.codementor.ui.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
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
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.adapters.ChatroomsAdapter;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;
import com.aluxian.codementor.presentation.presenters.ChatroomsPresenter;
import com.aluxian.codementor.presentation.views.ChatroomsView;
import com.aluxian.codementor.utils.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;

public class ChatroomsFragment extends BaseFragment<ChatroomsPresenter>
        implements ChatroomsView, ChatroomSelectedListener {

    @Bind(R.id.recycler) RecyclerView recyclerView;
    @Bind(R.id.swipe_container) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.tv_empty_state) TextView emptyState;

    private ChatroomSelectedListener chatroomSelectedListener;
    private ChatroomsAdapter chatroomsAdapter;
    private boolean drawerShouldBeOpen;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private View drawerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            drawerShouldBeOpen = true;
        }

        setPresenter(new ChatroomsPresenter(this, getCoreServices()));
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.post(() -> {
            if (swipeRefreshLayout != null) {
                getPresenter().viewReady();
            }
        });
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
    public void setAdapter(ChatroomsAdapter adapter) {
        chatroomsAdapter = adapter;
    }

    @Override
    public void onChatroomSelected(Chatroom chatroom) {
        if (chatroomSelectedListener != null) {
            chatroomSelectedListener.onChatroomSelected(chatroom);
        }
    }

    @Override
    public void openDrawer() {
        if (drawerLayout != null && drawerView != null) {
            drawerLayout.openDrawer(drawerView);
        }
    }

    @Override
    public void closeDrawer() {
        if (drawerLayout != null && drawerView != null) {
            drawerLayout.closeDrawer(drawerView);
        }
    }

    @Override
    public void toggleDrawer() {
        if (drawerLayout != null && drawerView != null) {
            if (drawerLayout.isDrawerOpen(drawerView)) {
                drawerLayout.closeDrawer(drawerView);
            } else {
                drawerLayout.openDrawer(drawerView);
            }
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(refreshing);
                }
            });
        }
    }

    @Override
    public void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void scrollToTop() {
        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);
    }

    /**
     * @return Whether the drawer is currently open.
     */
    public boolean isDrawerOpen() {
        return drawerLayout != null
                && drawerView != null
                && drawerLayout.isDrawerOpen(drawerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param drawerView   The container view of the drawer fragment.
     */
    public void init(DrawerLayout drawerLayout, View drawerView) {
        this.drawerLayout = drawerLayout;
        this.drawerView = drawerView;

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        if (drawerShouldBeOpen) {
            openDrawer();
            drawerShouldBeOpen = false;
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
    public void setChatroomSelectedListener(ChatroomSelectedListener chatroomSelectedListener) {
        this.chatroomSelectedListener = chatroomSelectedListener;
    }

}
