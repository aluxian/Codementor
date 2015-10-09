package com.aluxian.codementor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.App;
import com.aluxian.codementor.R;
import com.aluxian.codementor.fragments.ConversationFragment;
import com.aluxian.codementor.fragments.DrawerFragment;
import com.aluxian.codementor.models.Chatroom;

public class MainActivity extends AppCompatActivity implements DrawerFragment.Listener {

    private DrawerFragment drawerFragment;
    private TextView emptyStateMsgView;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (App) getApplication();
        emptyStateMsgView = (TextView) findViewById(R.id.empty_state_msg);

        if (!app.getUserManager().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Fragment navDrawerFragment = getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        drawerFragment = (DrawerFragment) navDrawerFragment;
        drawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        drawerFragment.setListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (drawerFragment != null) {
            drawerFragment.removeListener();
        }
    }

    @Override
    public void onChatroomSelected(Chatroom chatroom) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(chatroom.getOtherUser(app.getUserManager().getUsername()).getName());
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ConversationFragment.newInstance(chatroom))
                .commit();

        emptyStateMsgView.setVisibility(View.GONE);
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
                drawerFragment.toggleDrawer();
                return true;

            case R.id.action_log_out:
                app.getUserManager().setLoggedOut();
                app.getCookieStore().removeAll();

                startActivity(new Intent(this, LoginActivity.class));
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
