package com.mcal.studio.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mcal.studio.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IntroFragment extends Fragment {

    @BindView(R.id.slide_layout)
    RelativeLayout slideLayout;
    @BindView(R.id.slide_image)
    ImageView slideImage;
    @BindView(R.id.slide_title)
    TextView slideTitle;
    @BindView(R.id.slide_desc)
    TextView slideDesc;
    private Unbinder unbinder;

    public IntroFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        if (arguments != null) {
            slideLayout.setBackgroundColor(arguments.getInt("bg"));
            slideImage.setImageResource(arguments.getInt("image"));
            slideTitle.setText(arguments.getString("title"));
            slideDesc.setText(arguments.getString("desc"));
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
