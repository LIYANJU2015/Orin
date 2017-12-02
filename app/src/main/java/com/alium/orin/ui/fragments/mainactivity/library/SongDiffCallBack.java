package com.alium.orin.ui.fragments.mainactivity.library;

import android.support.v7.util.DiffUtil;

import com.alium.orin.model.Song;

import java.util.ArrayList;

/**
 * Created by liyanju on 2017/12/2.
 */

public class SongDiffCallBack extends DiffUtil.Callback {

    private ArrayList<Song> old_students, new_students;

    public SongDiffCallBack(ArrayList<Song> oldData, ArrayList<Song> newData) {
        this.old_students = oldData;
        this.new_students = newData;
    }

    @Override
    public int getOldListSize() {
        return old_students != null ? old_students.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return new_students != null ? new_students.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int i, int i1) {
        if (new_students != null && old_students != null
                && old_students.get(i) == new_students.get(i1)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int i, int i1) {
        if (new_students != null && old_students != null
                && old_students.get(i) == new_students.get(i1)) {
            return true;
        }
        return false;
    }
}
