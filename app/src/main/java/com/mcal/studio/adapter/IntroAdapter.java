package com.mcal.studio.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mcal.studio.R;
import com.mcal.studio.fragment.IntroFragment;

public class IntroAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private int[] bgColors;
    private int[] images = {R.drawable.ic_intro_logo_n, R.drawable.ic_intro_editor, R.drawable.ic_intro_git, R.drawable.ic_intro_done};
    private String[] titles, desc;

    public IntroAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        bgColors = context.getResources().getIntArray(R.array.bg_screens);
        titles = context.getResources().getStringArray(R.array.slide_titles);
        desc = context.getResources().getStringArray(R.array.slide_desc);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putInt("bg", bgColors[position]);
        bundle.putInt("image", images[position]);
        bundle.putString("title", titles[position]);
        bundle.putString("desc", desc[position]);
        return Fragment.instantiate(context, IntroFragment.class.getName(), bundle);
    }

    @Override
    public int getCount() {
        return 4;
    }
}
