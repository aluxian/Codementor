package com.aluxian.codementor.lib;

import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

/**
 * @source http://stackoverflow.com/a/28838834/1133344
 */
public abstract class TrackSelectionAdapter<VH extends TrackSelectionAdapter.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    // Start with first item selected
    private int focusedItem = 0;

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener((v, keyCode, event) -> {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

            // Return false if scrolled to the bounds and allow focus to move off the list
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    return tryMoveSelection(lm, 1);
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    return tryMoveSelection(lm, -1);
                }
            }

            return false;
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }

    protected boolean isSelected(int position) {
        return focusedItem == position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void handleSelectableClick() {
            // Redraw the old selection and the new
            notifyItemChanged(focusedItem);
            focusedItem = getLayoutPosition();
            notifyItemChanged(focusedItem);
        }

    }

}
