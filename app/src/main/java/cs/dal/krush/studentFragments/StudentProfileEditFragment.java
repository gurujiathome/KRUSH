package cs.dal.krush.studentFragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import cs.dal.krush.R;
import cs.dal.krush.models.DBHelper;

import static android.R.attr.name;
import static android.app.Activity.RESULT_OK;
import static cs.dal.krush.R.id.profile_name;
import static cs.dal.krush.R.id.school_selecter;

/**
 * Fragment for editing a profile. Sets initial fields from the database and saves new fields
 * when save button is pressed
 * Uses camera to take picture to set as profile picture
 */
public class StudentProfileEditFragment extends Fragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY = 2;
    static int USER_ID;
    private String imagePath = "";
    private String user_password;
    private ArrayList<String> schoolList;
    private DBHelper mydb;
    private Cursor cursor;
    Button saveProfile, changePicture;
    ImageView profile_picture_view;
    View myView;
    TextView profile_name_view;
    EditText email_view, curr_password_view, new_password_view, new_password_view_conf;
    Spinner school_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get USER_ID
        USER_ID = getArguments().getInt("USER_ID");
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.student_profile_edit, container, false);

        // Get Views
        profile_name_view = (TextView) myView.findViewById(R.id.profile_name_edit);
        profile_picture_view = (ImageView) myView.findViewById(R.id.profile_picture_edit);
        email_view = (EditText) myView.findViewById(R.id.profile_email_edit);
        school_view = (Spinner) myView.findViewById(R.id.profile_school_edit);
        curr_password_view = (EditText) myView.findViewById(R.id.current_password);
        new_password_view = (EditText) myView.findViewById(R.id.new_password);
        new_password_view_conf = (EditText) myView.findViewById(R.id.new_password_confirmation);

        //Database connection
        mydb = new DBHelper(getContext());
        cursor = mydb.student.getData(USER_ID);
        cursor.moveToFirst();

        //Get list of schools
        schoolList = new ArrayList<>();
        Cursor schoolCursor = mydb.school.getAll();

        if(schoolCursor.getCount() != 0) {
            schoolCursor.moveToFirst();
            do {
                // column id 1 is school name
                schoolList.add(schoolCursor.getString(1));
            }
            while(schoolCursor.moveToNext());
        }

        //Set list of schools
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, schoolList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        school_view.setAdapter(adapter);

        // Set value of school list to currently selected school
        int school_id = cursor.getInt(cursor.getColumnIndex("school_id"));
        Cursor sc = mydb.school.getData(school_id);
        sc.moveToFirst();
        String school = sc.getString(sc.getColumnIndex("name"));
        int spinnerPos = adapter.getPosition(school);
        school_view.setSelection(spinnerPos);

        // Get current values from database
        String name = cursor.getString(cursor.getColumnIndex("f_name")) + " " + cursor.getString(cursor.getColumnIndex("l_name"));
        String email = cursor.getString(cursor.getColumnIndex(("email")));
        user_password = cursor.getString(cursor.getColumnIndex(("password")));

        //Profile Picture
        String imagePath = cursor.getString(cursor.getColumnIndex("profile_pic"));
        if(imagePath != null && !imagePath.isEmpty()) {
            Bitmap profile_pic = BitmapFactory.decodeFile(imagePath);
            profile_picture_view.setImageBitmap(profile_pic);
        }

        // Set values to fields
        profile_name_view.setText(name);
        email_view.setText(email);

        //fetch custom app font
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(),"fonts/FredokaOne-Regular.ttf");

        //Set custom app font
        profile_name_view.setTypeface(typeFace);
        email_view.setTypeface(typeFace);
        saveProfile = (Button) myView.findViewById(R.id.save_profile_button);
        changePicture = (Button) myView.findViewById(R.id.change_picture_button);

        saveProfile.setOnClickListener(this);
        changePicture.setOnClickListener(this);

        return myView;
    }

    /**
     * Save Profile and Change Picture button listeners
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.save_profile_button:  //Save values in views and return to profile view
                try {
                    // Get data from fields
                    String new_email = email_view.getText().toString();
                    int new_school = schoolList.indexOf(school_view.getSelectedItem().toString()) + 1;

                    // Write new fields to table
                    ContentValues cv = new ContentValues();
                    cv.put("email", new_email);
                    cv.put("school_id", new_school);

                    // Check if profile picture was changed
                    if(!imagePath.equals(""))
                        cv.put("profile_pic", imagePath);

                    // Check if password was changed
                    String curr_password = curr_password_view.getText().toString();
                    if(curr_password.equals(user_password)) {
                        String new_password = new_password_view.getText().toString();
                        String new_password_conf = new_password_view_conf.getText().toString();

                        if(!new_password.isEmpty() && !new_password_conf.isEmpty()) {
                            if(new_password.equals(new_password_conf)) {
                                cv.put("password", new_password);
                            }
                        }
                    }

                    // Save new values to db
                    mydb.getWritableDatabase().update("students", cv,"id="+USER_ID, null);

                    //Close DB
                    cursor.close();
                    mydb.close();

                    // Add USER_ID to bundle to pass back to profile fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("USER_ID", USER_ID);

                    // Switch to profile fragment
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    StudentProfileFragment profile = new StudentProfileFragment();
                    profile.setArguments(bundle);
                    transaction.replace(R.id.student_fragment_container, profile);
                    transaction.commit();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case R.id.change_picture_button:
                changePicture();
                break;
        }
    }

    /**
     * Uses AlertDialog to prompt user on how they want to change profile picture
     * by camera or existing image in gallery
     *
     * Source:
     * Jasani, T. (2016). Android Take Photo from Camera and Gallery - Code Sample. “TheAppGuruz”. Retrieved 15 March 2017,
     * from http://www.theappguruz.com/blog/android-take-photo-camera-gallery-code-sample
     */
    public void changePicture() {
        final String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Profile Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (options[item]) {
                    case "Take Photo":
                        try {
                            openCamera();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "Choose from Gallery":
                        try {
                            openGallery();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * Creates a new Intent to open the camera
     * Creates a file to save the image to and includes that in the intent
     * @throws IOException
     *
     * Source:
     * Taking Photos Simply | Android Developers. (2017). Developer.android.com. Retrieved 15 March 2017,
     * from https://developer.android.com/training/camera/photobasics.html
     */
    private void openCamera() throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create file to save photo
            File imageFile = createImage();
            Uri uri = FileProvider.getUriForFile(getActivity(),"cs.dal.krush.fileprovider", imageFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Creates new Intent to open gallery and allow user to select existing image
     * @throws IOException
     */
    private void openGallery() throws IOException {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), REQUEST_GALLERY);
    }

    /**
     * Method to handle any activity intent results
     * Checks if requestCode is from camera intent or gallery intent and handles accordingly
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Set profile picture to image that was just captured
            ImageView profile_picture_view = (ImageView) myView.findViewById(R.id.profile_picture_edit);
            Bitmap profile_pic = BitmapFactory.decodeFile(imagePath);
            profile_picture_view.setImageBitmap(profile_pic);
        }

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            ImageView profile_picture_view = (ImageView) myView.findViewById(R.id.profile_picture_edit);
            try {
                Bitmap profile_pic = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
                profile_picture_view.setImageBitmap(profile_pic);

                //Save image path to db
                // TODO: 2017-03-15 move copy of gallery image to location and save in db

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a new file to save an image to and saves file path to imagePath for database
     * @return
     * @throws IOException
     *
     * Source:
     * Taking Photos Simply | Android Developers. (2017). Developer.android.com. Retrieved 15 March 2017,
     * from https://developer.android.com/training/camera/photobasics.html
     */
    protected File createImage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "profile_picture_" + timeStamp;
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);

        imagePath = image.getAbsolutePath();

        return image;
    }
}