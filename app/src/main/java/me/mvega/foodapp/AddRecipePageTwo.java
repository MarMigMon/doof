package me.mvega.foodapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    @BindView(R.id.cardIngredient1) CardView cardIngredient1;
    @BindView(R.id.ingredient1)
    EditText ingredient1;
    @BindView(R.id.btAddIngredient)
    Button btAddIngredient;

    @BindView(R.id.tvInstructions)
    TextView tvInstructions;
    @BindView(R.id.instructionsLayout)
    RelativeLayout instructionsLayout;
    @BindView(R.id.cardStep1)
    CardView cardStep1;
    @BindView(R.id.step1) EditText step1;

    @BindView(R.id.btAddStep)
    Button btAddStep;

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
    private static final String KEY_STEPS = "steps";
    private static final String KEY_INGREDIENTS = "ingredients";
    // True if a recipe is being edited
    private Boolean editing = false;
    private Recipe editedRecipe;
    private Context mContext;

    private PageTwoFragmentCommunication addRecipeListenerFragment;

    // implement interface
    public interface PageTwoFragmentCommunication {
        void back(Bundle bundle);

        void submit(Bundle bundle);

        void scrollDownTextField(boolean reverse, int distance);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // newInstance constructor for creating fragment with arguments
    public static AddRecipePageTwo newInstance(Bundle bundle) {
        AddRecipePageTwo fragmentSecond = new AddRecipePageTwo();
        fragmentSecond.setArguments(bundle);
        return fragmentSecond;
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_EDITING, editing);
        outState.putParcelable(KEY_RECIPE, editedRecipe);
        getArguments().putStringArrayList(KEY_INGREDIENTS, parseIngredients());
        getArguments().putStringArrayList(KEY_STEPS, parseInstructions());
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String> steps = getArguments().getStringArrayList(KEY_STEPS);
        if (steps != null) {
            addSteps(steps);
        }
        ArrayList<String> ingredients = getArguments().getStringArrayList(KEY_INGREDIENTS);
        if (ingredients != null) {
            addIngredients(ingredients);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        if (steps == null)
            steps = new ArrayList<>();
        if (ingredients == null)
            ingredients = new ArrayList<>();
        cardStep1.setId(stepCount);
        steps.add(step1);
        cardIngredient1.setId(ingredientCount);
        ingredients.add(ingredient1);

        if (savedInstanceState != null) {
            editing = savedInstanceState.getBoolean(KEY_EDITING, false);
            editedRecipe = savedInstanceState.getParcelable(KEY_RECIPE);
        } else {
            if (getArguments() != null) {
                editing = getArguments().getBoolean(KEY_EDITING);
                editedRecipe = getArguments().getParcelable(KEY_RECIPE);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                setButtons();
            }
        }).start();

        if (editing) {
            setupEdit(editedRecipe);
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
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
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
                addRecipeListenerFragment.scrollDownTextField(false, step1.getHeight());
            }
        });

        btAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddIngredient();
                addRecipeListenerFragment.scrollDownTextField(false, step1.getHeight());
            }
        });

    }

    private ArrayList<String> parseInstructions() {
        ArrayList<String> stepStrings = new ArrayList<>();
        String stepText;

        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                stepText = steps.get(i).getText().toString().trim();
                if (!stepText.equals("")) {
                    stepStrings.add(stepText);
                }
            }
        }

        return stepStrings;
    }

    private ArrayList<String> parseIngredients() {
        ArrayList<String> ingredientStrings = new ArrayList<>();
        String ingredientText;

        if (ingredients != null) {
            for (int i = 0; i < ingredients.size(); i++) {
                ingredientText = ingredients.get(i).getText().toString().trim();
                if (!ingredientText.equals("")) {
                    ingredientStrings.add(ingredientText);
                }
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
                Toast.makeText(mContext, "Accept permissions to enable adding recipes", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Pre-fills the fragment's instructions with the given list
     *
     * @param instructions steps for recipe
     */
    private void addSteps(List<String> instructions) {
        if (!instructions.isEmpty()) {
            step1.setText(instructions.get(0));
            for (String instruction : instructions.subList(1, instructions.size())) {
                onAddStep();
                steps.get(stepCount - 1).setText(instruction);
            }
        }
    }

    /**
     * Adds a new step (EditText) to the layout for the user to input text
     */
    private void onAddStep() {
        // Set layout params
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, stepCount);

        EditText step = new EditText(mContext);

        // Initialize a new CardView
        CardView card = formatStep(new CardView(mContext), params);
        stepCount += 1;
        card.setId(stepCount);

        // Set up new EditText view
        step.setHint("Step " + stepCount);
        step.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        step.setSingleLine(false);
        step.setLayoutParams(params);
        step.setBackgroundColor(getResources().getColor(R.color.white));
        step.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteDialog(view, steps, false);
                return true;
            }
        });

        // Add step
        steps.add(step);
        card.addView(step);
        instructionsLayout.addView(card);
    }

    private CardView formatStep(CardView card, RelativeLayout.LayoutParams params) {
        card.setLayoutParams(params);

        // Set cardView content padding
        card.setContentPadding(16, 16, 16, 16);

        // Set CardView elevation
        card.setCardElevation(2);

        //Setting Params for Cardview Margins
        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
        cardViewMarginParams.setMargins(0, 8, 0, 8);
        card.requestLayout();



        return card;
    }

    /**
     * Removes the selected step from the instructions layout
     */

    public void showDeleteDialog(final View step, final ArrayList<EditText> items, final Boolean ingredient) {
        // Create alert dialog
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        // Show error when userProfile is not the currentUser's profile
        // Add cancel option and message
        alertDialog.setCancelable(true);

        String itemText = ((EditText) step).getText().toString();
        alertDialog.setMessage(Html.fromHtml("Delete <b>" + truncate(itemText) + "</b> ?"));

        // Configure dialog button (OK)
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        CardView card = (CardView) step.getParent();
                        Boolean change = false;
                        for (int i = 0; i < items.size(); i++) {
                            EditText currentItem = (EditText) items.get(i);
                            CardView currentCard = (CardView) currentItem.getParent();
                            if (currentCard.getId() == card.getId()) {
                                change = true;
                            } else if (change) {
                                int currId = currentCard.getId();
                                currentCard.setId(currId - 1);
                                if (ingredient) {
                                    currentItem.setHint("Ingredient " + (currId - 1001));
                                } else {
                                    currentItem.setHint("Step " + (currId - 1));
                                }
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) currentCard.getLayoutParams();
                                params.addRule(RelativeLayout.BELOW, currId - 2);
                                currentCard.requestLayout();
                            }
                        }
                        items.remove(step);

                        if (ingredient) {
                            ingredientsLayout.removeView(card);
                            ingredientCount -= 1;
                        } else {
                            instructionsLayout.removeView(card);
                            stepCount -= 1;
                        }
                    }
                });

        // Configure dialog button (CANCEL)
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
             });

        // Display the dialog
        alertDialog.show();

        }

    private String truncate(String string) {
        if (string.length() > 20) {
            return string.substring(0, 20) + "...";
        } else {
            return string;
        }
    }


    /**
     * Pre-fills the fragment's ingredients with the given list
     *
     * @param components ingredients for recipe
     */
    private void addIngredients(List<String> components) {
        if (!components.isEmpty()) {
            ingredient1.setText(components.get(0));
            for (String ingredient : components.subList(1, components.size())) {
                onAddIngredient();
                ingredients.get(ingredientCount - 1001).setText(ingredient);
            }
        }
    }

    /**
     * Adds a new ingredient (EditText) to the layout for the user to input text
     */
    private void onAddIngredient() {
        EditText ingredient = new EditText(mContext);

        // Set layout params
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, ingredientCount);

        // Initialize a new CardView
        CardView card = formatStep(new CardView(mContext), params);
        ingredientCount += 1;
        card.setId(ingredientCount);

        // Set up new EditText view
        ingredient.setHint("Ingredient " + (ingredientCount - 1000));
        ingredient.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        ingredient.setSingleLine(false);
        ingredient.setBackgroundColor(getResources().getColor(R.color.white));
        ingredient.setLayoutParams(params);
        ingredient.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDeleteDialog(view, ingredients, true);
                return true;
            }
        });

        // Add ingredient
        ingredients.add(ingredient);
        card.addView(ingredient);
        ingredientsLayout.addView(card);
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
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        addSteps(recipe.getSteps());
        addIngredients(recipe.getIngredients());
    }
}
