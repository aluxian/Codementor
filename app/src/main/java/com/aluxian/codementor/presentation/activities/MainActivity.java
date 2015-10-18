package com.aluxian.codementor.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.CoreServices;
import com.aluxian.codementor.R;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.lib.PersistentCookieStore;
import com.aluxian.codementor.presentation.fragments.ChatroomsFragment;
import com.aluxian.codementor.presentation.fragments.ConversationFragment;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;
import com.aluxian.codementor.utils.UserManager;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements ChatroomSelectedListener {

    @Bind(R.id.empty_state_msg) TextView emptyStateMsgView;
    @Bind(R.id.navigation_drawer) View fragmentContainerView;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;

    private UserManager userManager;
    private PersistentCookieStore cookieStore;
    private ChatroomsFragment chatroomsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        CoreServices coreServices = getCoreServices();
        userManager = coreServices.getUserManager();
        cookieStore = coreServices.getCookieStore();

        if (!userManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Fragment navDrawerFragment = getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        chatroomsFragment = (ChatroomsFragment) navDrawerFragment;
        chatroomsFragment.init(fragmentContainerView, drawerLayout);
        chatroomsFragment.setChatroomSelectedListener(this);

        if (savedInstanceState == null) {
            emptyStateMsgView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChatroomSelected(Chatroom chatroom) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(chatroom.getOtherUser().getName());
            actionBar.setSubtitle(null);
        }

        emptyStateMsgView.setVisibility(View.GONE);
        replaceFragment(R.id.container, ConversationFragment.newInstance(chatroom));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.app_name_full);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                chatroomsFragment.toggleDrawer();
                return true;

            case R.id.action_log_out:
                userManager.setLoggedOut();
                cookieStore.removeAll();

                startActivity(new Intent(this, LoginActivity.class));
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (emptyStateMsgView.getVisibility() != View.VISIBLE && chatroomsFragment.isDrawerOpen()) {
            chatroomsFragment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

}
