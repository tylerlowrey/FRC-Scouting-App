package com.tylerlowrey.frcscoutingapp.views;

import android.content.Context;
import android.content.res.Resources;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.tylerlowrey.frcscoutingapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckboxInputView extends FormInputView
{
    private Context context;
    private String fieldName;
    private String title;
    private Map<String, String> checkBoxesMap;
    private List<CheckBox> checkBoxes;
    private String inputType;

    public CheckboxInputView(Context context, String fieldName,
                             String title, Map<String,String> checkBoxesMap, String inputType)
    {
        super(context);

        this.context = context;
        this.fieldName = fieldName;
        this.title = title;
        this.checkBoxesMap = checkBoxesMap;
        this.inputType = inputType;
        this.checkBoxes = new ArrayList<>();

        init();
    }

    private void init()
    {
        Resources resources = context.getResources();

        this.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        int horizontalPixels = (int) convertDisplayPixelToPixel(25.0f);

        int verticalPixels = (int) convertDisplayPixelToPixel(15.0f);

        layoutParams.setMargins(horizontalPixels, verticalPixels, horizontalPixels, verticalPixels);
        this.setLayoutParams(layoutParams);


        //-- Create Title Box (TextView) --
        TextView inputBoxTitle = new TextView(context);
        inputBoxTitle.setText(title);
        inputBoxTitle.setTextColor(resources.getColor(R.color.form_input_title_text_color));
        inputBoxTitle.setTextSize(20);
        inputBoxTitle.setBackground(context.getDrawable(R.drawable.form_element_title_bg));
        inputBoxTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        int titlePadding = (int) convertDisplayPixelToPixel(5.0f);
        inputBoxTitle.setPadding(titlePadding, titlePadding, titlePadding, titlePadding);

        //-- Create LinearLayout for holding CheckBoxes --
        LinearLayout checkboxesHolder = new LinearLayout(context);
        checkboxesHolder.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        checkboxesHolder.setLayoutParams(layoutParams);
        checkboxesHolder.setBackground(context.getDrawable(R.drawable.form_element_body_bg));
        checkboxesHolder.setTag(R.id.input_type, "checkbox");

        for(Map.Entry<String, String> checkboxKeyVal : checkBoxesMap.entrySet())
        {
            CheckBox checkbox = new CheckBox(context);
            RadioGroup.LayoutParams radioButtonLayoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT);
            int marginVal = (int) convertDisplayPixelToPixel(4.0f);
            radioButtonLayoutParams.setMargins(marginVal, marginVal, marginVal, marginVal);

            checkbox.setLayoutParams(radioButtonLayoutParams);
            checkbox.setEnabled(true);
            checkbox.setClickable(true);
            checkbox.setText(checkboxKeyVal.getKey());
            checkbox.setTextColor(resources.getColor(R.color.form_input_body_text_color));
            checkbox.setButtonTintList(AppCompatResources.getColorStateList(context, R.color.form_radio_button_tint));
            checkbox.setTag(checkboxKeyVal.getValue());
            checkbox.setTextSize(24);

            checkBoxes.add(checkbox);
            checkboxesHolder.addView(checkbox);
        }

        this.addView(inputBoxTitle);
        this.addView(checkboxesHolder);

    }

    public List<String> getCheckedItems()
    {
        ArrayList<String> checkedValues = new ArrayList<>();
        for(CheckBox checkBox : checkBoxes)
        {
            if(checkBox.isChecked())
                checkedValues.add((String) checkBox.getTag());
        }

        return checkedValues;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getFieldName()
    {
        return fieldName;
    }

    @Override
    public String getInputValue()
    {
        throw new UnsupportedOperationException("CheckBox values should be retrieved by using the getCheckedItems");
    }

    @Override
    public String getInputType()
    {
        return inputType;
    }


}
