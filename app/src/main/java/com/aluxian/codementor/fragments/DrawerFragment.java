package com.aluxian.codementor.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.adapters.ChatroomsAdapter;
import com.aluxian.codementor.lib.DividerItemDecoration;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.utils.SimpleObservable;

public class DrawerFragment extends Fragment implements SimpleObservable<DrawerFragment.Listener>,
        SwipeRefreshLayout.OnRefreshListener, ChatroomsAdapter.Callbacks {

    private static final String TAG = DrawerFragment.class.getSimpleName();

    /** Remember the position of the selected item. */
    private static final String STATE_SELECTED_POSITION = "selected_drawer_position";

    /** A pointer to the current callbacks instance (the Activity). */
    private Listener mListener;

    /** Helper component that ties the action bar to the navigation drawer. */
    private ActionBarDrawerToggle drawerToggle;

    private DrawerLayout drawerLayout;
    private ChatroomsAdapter chatroomsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View fragmentContainerView;
    private App app;

    private int currentSelectedPosition = -1;
    private boolean fromSavedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
//            selectItem(currentSelectedPosition);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drawer, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(inflater.getContext());
        recyclerView.setLayoutManager(layoutManager);

        chatroomsAdapter = new ChatroomsAdapter(app.getOkHttpClient(), app.getUserManager(), this);
        chatroomsAdapter.setHasStableIds(true);
        recyclerView.setAdapter(chatroomsAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.sunset_orange));
        swipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        }, 250);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);

        this.drawerLayout = drawerLayout;
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        drawerToggle = new ActionBarDrawerToggle(getActivity(), this.drawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        if (!fromSavedInstanceState) {
            this.drawerLayout.openDrawer(fragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state
        this.drawerLayout.post(drawerToggle::syncState);
        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    public void toggleDrawer() {
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(fragmentContainerView)) {
                drawerLayout.closeDrawer(fragmentContainerView);
            } else {
                drawerLayout.openDrawer(fragmentContainerView);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Forward the new configuration the drawer toggle component
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void removeListener() {
        mListener = null;
    }

    @Override
    public void onRefresh() {
        chatroomsAdapter.startRefresh();
    }

    @Override
    public void onRefreshFinished() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onChatroomClick(int position, Chatroom chatroom) {
        currentSelectedPosition = position;

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }

        if (mListener != null && chatroomsAdapter.getItemCount() > position) {
            mListener.onChatroomSelected(chatroom);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, e.getMessage(), e);
        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public interface Listener {

        /**
         * Called when a chatroom from the drawer is selected.
         */
        void onChatroomSelected(Chatroom chatroom);

    }

}
