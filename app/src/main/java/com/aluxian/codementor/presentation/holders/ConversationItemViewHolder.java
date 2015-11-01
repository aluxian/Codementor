package com.aluxian.codementor.presentation.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aluxian.codementor.data.models.ConversationItem;

public abstract class ConversationItemViewHolder extends RecyclerView.ViewHolder {

    private ConversationItem currentItem;

    public ConversationItemViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * Bind the given item to the view.
     *
     * @param item   The message to bind.
     * @param newest Whether it's the newest item in the conversation.
     */
    public void bindItem(ConversationItem item, boolean newest) {
        if (currentItem == null || !currentItem.contentEquals(item)) {
            onBind(item, newest);
        }

        currentItem = item;
    }

    protected abstract void onBind(ConversationItem item, boolean newest);

}
