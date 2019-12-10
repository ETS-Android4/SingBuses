package com.itachi1706.busarrivalsg.util

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Kenneth on 10/12/2019.
 * for com.itachi1706.busarrivalsg.util in SingBuses
 */
class SwipeMoveFavouriteCallback(var context2: Context, var callback2: ISwipeCallback) : SwipeFavouriteCallback(context2, callback2, ItemTouchHelper.UP or ItemTouchHelper.DOWN) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        // move item in `fromPos` to `toPos` in adapter.
        val fromPos = viewHolder.adapterPosition
        val toPos = target.adapterPosition
        return callback2.moveFavourite(fromPos, toPos)
    }
}