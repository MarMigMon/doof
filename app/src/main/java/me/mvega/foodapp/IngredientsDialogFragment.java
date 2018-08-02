package me.mvega.foodapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class IngredientsDialogFragment extends DialogFragment {

    private static final String KEY_INGREDIENTS = "ingredients";
    @BindView(R.id.tvIngredients) TextView tvIngredients;

    public static IngredientsDialogFragment newInstance(String ingredients) {
        Bundle args = new Bundle();
        args.putString(KEY_INGREDIENTS, ingredients);
        IngredientsDialogFragment fragment = new IngredientsDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ingredients_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        tvIngredients.setText(getArguments().getString(KEY_INGREDIENTS));
    }
}
