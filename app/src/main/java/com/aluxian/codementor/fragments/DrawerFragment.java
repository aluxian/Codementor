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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.adapters.ChatroomsAdapter;
import com.aluxian.codementor.lib.DividerItemDecoration;
import com.aluxian.codementor.models.Chatroom;
import com.aluxian.codementor.utils.SimpleObservable;

public class DrawerFragment extends Fragment
        implements SimpleObservable<DrawerFragment.Listener>, SwipeRefreshLayout.OnRefreshListener {

    /** Remember the position of the selected item. */
    private static final String STATE_SELECTED_POSITION = "selected_drawer_position";

    /** A pointer to the current callbacks instance (the Activity). */
    private Listener mListener;

    /** Helper component that ties the action bar to the navigation drawer. */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ChatroomsAdapter mChatroomsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = -1;
    private boolean mFromSavedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
//            selectItem(mCurrentSelectedPosition);
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

        App app = (App) getActivity().getApplication();
        mChatroomsAdapter = new ChatroomsAdapter(app.getOkHttpClient(), app.getUserManager(), this::selectItem);
        recyclerView.setAdapter(mChatroomsAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.sunset_orange));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.postDelayed(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
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
        mFragmentContainerView = getActivity().findViewById(fragmentId);

        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        if (!mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state
        mDrawerLayout.post(mDrawerToggle::syncState);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position, Chatroom chatroom) {
        mCurrentSelectedPosition = position;

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        if (mListener != null && mChatroomsAdapter.getItemCount() > position) {
            mListener.onChatroomSelected(chatroom);
        }
    }

    public void toggleDrawer() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(mFragmentContainerView)) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            } else {
                mDrawerLayout.openDrawer(mFragmentContainerView);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Forward the new configuration the drawer toggle component
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        mChatroomsAdapter.refresh(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    public interface Listener {

        /**
         * Called when a chatroom from the drawer is selected.
         */
        void onChatroomSelected(Chatroom chatroom);

    }

}
