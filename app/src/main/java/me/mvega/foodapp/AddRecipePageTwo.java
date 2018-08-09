package me.mvega.foodapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class AddRecipePageTwo extends Fragment {

    @BindView(R.id.tvIngredients)
    TextView tvIngredients;
    @BindView(R.id.ingredientsLayout)
    RelativeLayout ingredientsLayout;
    @BindView(R.id.ingredient1)
    EditText ingredient1;
    @BindView(R.id.ingredientButtonLayout)
    LinearLayout ingredientButtonLayout;
    @BindView(R.id.btAddIngredient)
    Button btAddIngredient;
    @BindView(R.id.btRemoveIngredient)
    Button btRemoveIngredient;

    @BindView(R.id.tvInstructions)
    TextView tvInstructions;
    @BindView(R.id.instructionsLayout)
    RelativeLayout instructionsLayout;
    @BindView(R.id.step1)
    EditText step1;
    @BindView(R.id.stepButtonLayout)
    LinearLayout stepButtonLayout;
    @BindView(R.id.btAddStep)
    Button btAddStep;
    @BindView(R.id.btRemoveStep)
    Button btRemoveStep;

    @BindView(R.id.btBack)
    Button btBack;
    @BindView(R.id.btSubmit)
    Button btSubmit;

    @BindView(R.id.btAudio)
    Button btAudio;

    private ArrayList<EditText> steps;
    private int stepCount = 1;
    private ArrayList<EditText> ingredients;
    private int ingredientCount = 1001;

    /* Used to handle permission request */
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 399;

    private static final String KEY_RECIPE = "recipe";
    private static final String KEY_EDITING = "editing";
    private static final String KEY_EDIT_RECIPE = "used to retrieve bool from new instance";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_INGREDIENTS = "ingredients";
    // True if a recipe is being edited
    private Boolean editing = false;

    private PageTwoFragmentCommunication addRecipeListenerFragment;

    // implement interface
    public interface PageTwoFragmentCommunication {
        void back(Bundle bundle);

        void submit(Bundle bundle);

        void scrollDownTextField(boolean reverse, int distance);
    }

    // newInstance constructor for creating fragment with arguments
    public static AddRecipePageTwo newInstance(Bundle bundle) {
        AddRecipePageTwo fragmentSecond = new AddRecipePageTwo();
        fragmentSecond.setArguments(bundle);
        return fragmentSecond;
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateView(inflater, container, savedInstanceState);
        onAttachToParentFragment(getParentFragment());
        return inflater.inflate(R.layout.page_second_add_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            editing = savedInstanceState.getBoolean(KEY_EDITING, false);
        } else {
            if (getArguments() != null) {
                editing = getArguments().getBoolean(KEY_EDIT_RECIPE);
            }
        }

        // Create a new background thread
        HandlerThread handlerThread = new HandlerThread("Setup");
        handlerThread.start();
        Handler mHandler = new Handler(handlerThread.getLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                steps = new ArrayList<>();
                step1.setId(stepCount);
                steps.add(step1);
                ingredients = new ArrayList<>();
                ingredient1.setId(ingredientCount);
                ingredients.add(ingredient1);

                setButtons();
            }
        });
        handlerThread.quitSafely();
        if (editing) {
            setupEdit((Recipe) getParentFragment().getArguments().getParcelable(KEY_RECIPE));
        }
    }

    private void onAttachToParentFragment(Fragment childFragment) {
        try {
            addRecipeListenerFragment = (PageTwoFragmentCommunication) childFragment;

        } catch (ClassCastException e) {
            throw new ClassCastException(
                    childFragment.toString() + " must implement OnPlayerSelectionSetListener");
        }
    }

    private void setButtons() {
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(KEY_INGREDIENTS, parseIngredients());
                    bundle.putStringArrayList(KEY_STEPS, parseInstructions());
                    addRecipeListenerFragment.submit(bundle);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                addRecipeListenerFragment.back(bundle);
            }
        });

        btAddStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddStep();
                // Adds the "remove step" button so the user can remove the last added step
                btRemoveStep.setVisibility(View.VISIBLE);
                addRecipeListenerFragment.scrollDownTextField(false, step1.getHeight());
            }
        });

        btRemoveStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveStep();
                // Removes "remove step" button if there is only one step left
                if (steps.size() == 1) {
                    btRemoveStep.setVisibility(View.GONE);
                }
                addRecipeListenerFragment.scrollDownTextField(true, step1.getHeight());
            }
        });

        btAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddIngredient();
                // Adds the "remove ingredient" button so the user can remove the last added ingredient
                btRemoveIngredient.setVisibility(View.VISIBLE);
                addRecipeListenerFragment.scrollDownTextField(false, step1.getHeight());
            }
        });

        btRemoveIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveIngredient();
                // Removes "remove ingredient" button if there is only one ingredient left
                if (ingredients.size() == 1) {
                    btRemoveIngredient.setVisibility(View.GONE);
                }
                addRecipeListenerFragment.scrollDownTextField(true, step1.getHeight());
            }
        });
    }

    private ArrayList<String> parseInstructions() {
        ArrayList<String> stepStrings = new ArrayList<>();
        String stepText;

        for (int i = 0; i < steps.size(); i++) {
            stepText = steps.get(i).getText().toString().trim();
            if (!stepText.equals("")) {
                stepStrings.add(stepText);
            }
        }

        return stepStrings;
    }

    private ArrayList<String> parseIngredients() {
        ArrayList<String> ingredientStrings = new ArrayList<>();
        String ingredientText;

        for (int i = 0; i < ingredients.size(); i++) {
            ingredientText = ingredients.get(i).getText().toString().trim();
            if (!ingredientText.equals("")) {
                ingredientStrings.add(ingredientText);
            }
        }

        return ingredientStrings;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getContext(), "Accept permissions to enable adding recipes", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Pre-fills the fragment's instructions with the given list
     *
     * @param instructions steps for recipe
     */
    private void addSteps(List<String> instructions) {
        step1.setText(instructions.get(0));
        for (String instruction : instructions.subList(1, instructions.size())) {
            onAddStep();
            steps.get(stepCount - 1).setText(instruction);
        }
    }

    /**
     * Adds a new step (EditText) to the layout for the user to input text
     */
    private void onAddStep() {
        EditText step = new EditText(getContext());

        // Set layout params
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, stepCount);

        // Set up new EditText view
        stepCount += 1;
        step.setId(stepCount);
        step.setHint("Step " + stepCount);
        step.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        step.setLayoutParams(params);

        // Add step
        steps.add(step);
        instructionsLayout.addView(step);
    }

    /**
     * Removes the last added step (EditText) from the instructions layout
     */
    private void onRemoveStep() {
        EditText lastStep = steps.get(steps.size() - 1);
        steps.remove(lastStep);
        stepCount -= 1;
        instructionsLayout.removeView(lastStep);
    }

    /**
     * Pre-fills the fragment's ingredients with the given list
     *
     * @param components ingredients for recipe
     */
    private void addIngredients(List<String> components) {
        ingredient1.setText(components.get(0));
        for (String ingredient : components.subList(1, components.size())) {
            onAddIngredient();
            ingredients.get(ingredientCount - 1001).setText(ingredient);
        }
    }

    /**
     * Adds a new ingredient (EditText) to the layout for the user to input text
     */
    private void onAddIngredient() {
        EditText ingredient = new EditText(getContext());

        // Set layout params
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, ingredientCount);

        // Set up new EditText view
        ingredientCount += 1;
        ingredient.setId(ingredientCount);
        ingredient.setHint("Ingredient " + (ingredientCount - 1000));
        ingredient.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        ingredient.setLayoutParams(params);

        // Add ingredient
        ingredients.add(ingredient);
        ingredientsLayout.addView(ingredient);
    }

    /**
     * Removes the last added ingredient (EditText) from the ingredients layout
     */
    private void onRemoveIngredient() {
        EditText lastIngredient = ingredients.get(ingredients.size() - 1);
        ingredients.remove(lastIngredient);
        ingredientCount -= 1;
        ingredientsLayout.removeView(lastIngredient);
    }

    /**
     * Utilizes the Add Recipe Fragment to edit recipes
     *
     * @param recipe the recipe the user wants to edit
     */
    private void setupEdit(final Recipe recipe) {
        // Ensures that when a recipe is submitted
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getParentFragment().getArguments().putParcelable("recipe", recipe);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(KEY_STEPS, parseInstructions());
                    bundle.putStringArrayList(KEY_INGREDIENTS, parseIngredients());
                    addRecipeListenerFragment.submit(bundle);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        addSteps(recipe.getSteps());
        addIngredients(recipe.getIngredients());
    }
}
