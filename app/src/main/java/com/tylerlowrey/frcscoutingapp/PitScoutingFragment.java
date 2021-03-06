package com.tylerlowrey.frcscoutingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tylerlowrey.frcscoutingapp.views.CheckboxInputView;
import com.tylerlowrey.frcscoutingapp.views.FormInputView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;


/**
 * Provides logic and the Fragment implementation for the Pit Scouting Form
 * Adds event listeners to the buttons of the Pit Scouting Fragment
 */
public class PitScoutingFragment extends Fragment
{

    public static final String TAG = "PIT_SCOUTING_FRAGMENT";
    private LinearLayout formContainer;
    private String pictureName;
    private JSONObject scoutingDataJSON;
    public static final int CAMERA_REQUEST = 1888;
    public static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView imageHolder;
    public PitScoutingFragment()
    {
        // Required empty public constructor
    }

    /**
     * Returns a new instance of the PitScoutingFragment class
     * @return PitScoutingFragment - A new instance of the PitScoutingFragment class
     */
    public static PitScoutingFragment newInstance()
    {
        PitScoutingFragment fragment = new PitScoutingFragment();
        return fragment;
    }

    /**
     * Inflates the fragment and makes sure the AppBar is shown to the user
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        return inflater.inflate(R.layout.fragment_pit_scouting, container, false);
    }

    /**
     * This function sets up all of the button click event handler functions and stores
     * references to view objects that are used in other functions in order to grab data
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        formContainer = view.findViewById(R.id.pit_form_root_linear_layout);

        FormGenerator.generatePitScoutingForm((AppCompatActivity) getActivity(), formContainer);

        getLayoutInflater().inflate(R.layout.take_picture_button, formContainer);

        Button takePictureBtn = view.findViewById(R.id.take_picture_button);
        takePictureBtn.setOnClickListener(onTakePictureClick);

        Button submitFormBtn = view.findViewById(R.id.pit_scouting_submit_button);
        submitFormBtn.setOnClickListener(onSubmitForm);
        //teamNumberEditText = view.findViewById(R.id.pit_form_team_number);
    }

    /**
     * Requests Camera permissions if they have not already been given. Otherwise, displays
     * a Camera view so that the user can take a picture
     */
    private View.OnClickListener onTakePictureClick = view -> {
        if (Objects.requireNonNull(getActivity()).checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else
        {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    };

    // This function presents the user with a permission request to use the camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getContext(), "Permission For Camera Use Has Been Granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(getContext(), "Permission For Camera Use Has Been Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    // this function updates the previewed photo on the screen and proceeds to save the picture file in the SD card
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            imageHolder = new ImageView(getContext());
            Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            imageHolder.setImageBitmap(photo);

            // This updates the image uri to later be saved
            Uri picURI = data.getData();
            imageHolder.setImageURI(picURI);

            savePicture();
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    // This function serves to saves the imageview into the SD card under as 'user.jpg' with 'user' being the name of the user
    private void savePicture() {

        BitmapDrawable drawable = (BitmapDrawable) imageHolder.getDrawable();
        Bitmap bitmap = drawable.getBitmap();


        FileOutputStream outStream ;

        // Write to External Storage
        try
        {
            File fileStorageRootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File appRootStorageDir = new File(fileStorageRootDir + "/FRC Scouting App");

            if(!appRootStorageDir.exists())
                appRootStorageDir.mkdirs();

            File appLocalStorageDir = new File(appRootStorageDir, "local");

            if(!appLocalStorageDir.exists())
                appLocalStorageDir.mkdirs();

            SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            String username = sharedPrefs.getString(getString(R.string.shared_prefs_current_user),
                    MainActivity.DEFAULT_USERNAME);

            String fileName = String.format("%s_%s.jpg", username, Calendar.getInstance().getTime());
            pictureName = fileName;
            File outFile = new File(appLocalStorageDir, fileName);

            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

        }
        catch (IOException e)
        {
            MainActivity.makeToast(getContext(), "ERROR: Unable to Save Picture", Toast.LENGTH_LONG);
        }

    }

    /**
     * Iterates through form data and saves the data onto external storage
     *
     * @pre All form data should be valid input
     * @post A new file with the format USERNAME_TEAMNUMBER_DATETIME.csv will be stored on the external
     *       storage
     */
    private View.OnClickListener onSubmitForm = (View view) -> {

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long timeFromEpoch = calendar.getTimeInMillis();

        SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String username = sharedPrefs.getString(getString(R.string.shared_prefs_current_user),
                MainActivity.DEFAULT_USERNAME);

        if(pictureName == null)
            pictureName = "";

        JsonObject formData = new JsonObject();

        for(int i = 0; i < formContainer.getChildCount(); ++i)
        {
            if(formContainer.getChildAt(i) instanceof FormInputView)
            {
                LinearLayout formElement = (LinearLayout) formContainer.getChildAt(i);
                addFormElementDataToJsonObject(formElement, formData);
            }
        }

        ScoutingDatabase.getInstance(getContext()).saveScoutingData(username, timeFromEpoch,
                                                                    pictureName, formData.toString());

        MainActivity.makeToast(getContext(), "Form Saved", Toast.LENGTH_LONG);

    };

    /**
     * Takes in a view and returns the data that is contained within the view
     *
     * @param view - a valid View object.
     * @return String - If view is an EditText or RadioGroup View, Returns the data contained within the view
     */
    private String getStringDataFromView(View view)
    {
        if(view instanceof EditText)
        {
            return ((EditText)view).getText().toString();
        }
        else if (view instanceof RadioGroup)
        {
            RadioGroup radioGroup = (RadioGroup) view;
            RadioButton selectedButton = getActivity().findViewById(radioGroup.getCheckedRadioButtonId());
            return selectedButton.getText().toString();
        }
        return "";
    }

    private void addFormElementDataToJsonObject(View view, JsonObject jsonObject) throws NumberFormatException
    {
        FormInputView formInputView = (FormInputView) view;

        View element = formInputView.getChildAt(1);

        String elementType = (String) element.getTag(R.id.input_type);
        String fieldName = formInputView.getFieldName();
        String title = formInputView.getTitle();
        String inputType = formInputView.getInputType();

        try
        {
            switch (elementType)
            {
                case "text":
                {

                    String inputValue = formInputView.getInputValue();

                    if(inputValue.equals(""))
                    {
                        jsonObject.addProperty(fieldName, (String) null);
                        break;
                    }

                    if (inputType.equals("integer"))
                    {
                        int intValue = Integer.parseInt(inputValue);
                        jsonObject.addProperty(fieldName, intValue);
                    }
                    else if (inputType.equals("float"))
                    {
                        float floatValue = Float.parseFloat(inputValue);
                        jsonObject.addProperty(fieldName, floatValue);
                    }
                    else
                    {
                        jsonObject.addProperty(fieldName, formInputView.getInputValue());
                    }

                    break;
                }
                case "textarea":
                    jsonObject.addProperty(fieldName, formInputView.getInputValue());
                    break;
                case "radio":
                {

                    String inputValue = formInputView.getInputValue();

                    if(inputValue == null)
                    {
                        jsonObject.addProperty(fieldName, (String) null);
                        break;
                    }

                    if (inputType.equals("integer"))
                    {
                        int intValue = Integer.parseInt(inputValue);
                        jsonObject.addProperty(fieldName, intValue);
                    }
                    else if (inputType.equals("float"))
                    {
                        float floatValue = Float.parseFloat(inputValue);
                        jsonObject.addProperty(fieldName, floatValue);
                    }
                    else
                    {
                        jsonObject.addProperty(fieldName, formInputView.getInputValue());
                    }

                    break;
                }
                case "checkbox":
                    CheckboxInputView checkboxInputView = (CheckboxInputView) formInputView;

                    Gson gson = new GsonBuilder().create();

                    List<String> checkedValues = checkboxInputView.getCheckedItems();
                    JsonArray jsonArray;

                    if (inputType.equals("integer"))
                    {
                        List<Integer> intCheckedValues = new ArrayList<>();

                        for (String checkedValue : checkedValues)
                            Integer.parseInt(checkedValue);

                        JsonElement jsonElement = gson.toJsonTree(intCheckedValues);

                        jsonArray = jsonElement.getAsJsonArray();
                    }
                    else if (inputType.equals("float"))
                    {
                        List<Float> intCheckedValues = new ArrayList<>();

                        for (String checkedValue : checkedValues)
                            Float.parseFloat(checkedValue);

                        JsonElement jsonElement = gson.toJsonTree(intCheckedValues);

                        jsonArray = jsonElement.getAsJsonArray();
                    }
                    else
                    {
                        JsonElement jsonElement = gson.toJsonTree(checkedValues);
                        jsonArray = jsonElement.getAsJsonArray();
                    }


                    jsonObject.add(fieldName, jsonArray);
                    break;
                case "dropdown":
                {

                    String inputValue = formInputView.getInputValue();

                    if(inputValue == null)
                    {
                        jsonObject.addProperty(fieldName, (String) null);
                        break;
                    }

                    if (inputType.equals("integer"))
                    {
                        int intValue = Integer.parseInt(inputValue);
                        jsonObject.addProperty(fieldName, intValue);
                    }
                    else if (inputType.equals("float"))
                    {
                        float floatValue = Float.parseFloat(inputValue);
                        jsonObject.addProperty(fieldName, floatValue);
                    }
                    else
                    {
                        jsonObject.addProperty(fieldName, formInputView.getInputValue());
                    }

                    break;
                }
            }
        }
        catch (NumberFormatException e)
        {
            MainActivity.displayErrorDialog((AppCompatActivity) getActivity(), "Invalid input given for field: " + title);
        }
    }




}
