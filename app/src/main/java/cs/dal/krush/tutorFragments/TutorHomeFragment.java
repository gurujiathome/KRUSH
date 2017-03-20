package cs.dal.krush.tutorFragments;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import cs.dal.krush.R;
import cs.dal.krush.TutorCursorAdapters.SessionCursorAdapter;
import cs.dal.krush.models.DBHelper;
import cs.dal.krush.studentFragments.StudentTutorDetailsFragment;

/**
 * Sets up the Tutor Home fragment. This fragment belongs to the TutorMainActivity class
 * and is accessed through the tutor's bottom navigation bar.
 *
 *  Source:
 * [5] List View. (n.d.). Retrieved March 12, 2017,
 * from https://developer.android.com/guide/topics/ui/layout/listview.html
 */
public class TutorHomeFragment extends Fragment {

    static int USER_ID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tutor_home, container, false);
        USER_ID = getArguments().getInt("USER_ID");

        //get Context:
        Context C = getActivity().getApplicationContext();

        //init DB connection:
        DBHelper mydb = new DBHelper(C);

        Cursor tutor = mydb.tutor.getData(USER_ID);
        tutor.moveToFirst();

        //fetch UI elements:
        ListView upcomingSessionsListView = (ListView)view.findViewById(R.id.upcomingSessionsListView);
        RatingBar tutorRating = (RatingBar) view.findViewById(R.id.rating);
        TextView pageTitle = (TextView)view.findViewById(R.id.homeTitleLabel);
        TextView sessionsLabel = (TextView)view.findViewById(R.id.upcomingSessionsLabel);
        TextView ratingTitle = (TextView) view.findViewById(R.id.tutorRating);
        FloatingActionButton tutorHelpButton = (FloatingActionButton)view.findViewById(R.id.tutorHelpButton);


        String currentTutorRating = tutor.getString(tutor.getColumnIndex("rating"));
        if (currentTutorRating != null)
            tutorRating.setRating(Float.parseFloat(currentTutorRating));

        //fetch custom app font:
        final Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(),"fonts/FredokaOne-Regular.ttf");

        //set font style:
        pageTitle.setTypeface(typeFace);
        sessionsLabel.setTypeface(typeFace);
        ratingTitle.setTypeface(typeFace);

        //get all tutoring sessions by the tutor:
        final Cursor cursorSessionsResponse = mydb.tutoringSession.getDataByTutorIdForCursorAdapter(USER_ID);

        //set sessions listview adapter:
        SessionCursorAdapter sessionsAdapter = new SessionCursorAdapter(C, cursorSessionsResponse);
        upcomingSessionsListView.setAdapter(sessionsAdapter);

        // TODO: 2017-03-18 We need to write the instructions once the actual functionality is implemented to accurately write to be
        final String text = "Lorem ipsum dolor sit amet, pri magna delicata an. An " +
                "imperdiet, vitae nemore duo eu. Sed ne etiam inermis, aperiam convenire " +
                "appellantur ad ius, quo elit consequat vulputate eu. Eu cum choro " +
                "constituto, at per justo nostrum abhorreant. Ridens lobortis vix an." +
                " Impetus salutatus pro ea, ex recteque neglegentur signiferumque vim. " +
                "Vim ex scaevola scriptorem, usu te quando nonumes delectus.";


        //display tutor help dialog
        tutorHelpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Create custom dialog object
                final Dialog dialog = new Dialog(getContext());
                // Include dialog.xml file
                dialog.setContentView(R.layout.tutor_help);
                // Set dialog title
                dialog.setTitle("Custom Dialog");
                dialog.show();

                //fetch UI components
                TextView tutorHelpHeader = (TextView) dialog.findViewById(R.id.tutorHelpHeader);
                TextView tutorHelpIntro = (TextView) dialog.findViewById(R.id.tutorHelpIntro);
                TextView homeTutorHelpLabel = (TextView) dialog.findViewById(R.id.homeTutorHelpLabel);
                TextView homeTutorHelpText = (TextView) dialog.findViewById(R.id.homeTutorHelpText);
                TextView bookingTutorHelpLabel = (TextView) dialog.findViewById(R.id.bookingTutorHelpLabel);
                TextView bookingTutorHelpText = (TextView) dialog.findViewById(R.id.bookingTutorHelpText);
                TextView seesionsTutorHelpLabel = (TextView) dialog.findViewById(R.id.seesionsTutorHelpLabel);
                TextView sessionsTutorHelpText = (TextView) dialog.findViewById(R.id.sessionsTutorHelpText);
                TextView profileTutorHelpLabel = (TextView) dialog.findViewById(R.id.profileTutorHelpLabel);
                TextView profileTutorHelpText = (TextView) dialog.findViewById(R.id.profileTutorHelpText);


                //set logo font style
                tutorHelpHeader.setTypeface(typeFace);
                homeTutorHelpLabel.setTypeface(typeFace);
                bookingTutorHelpLabel.setTypeface(typeFace);
                seesionsTutorHelpLabel.setTypeface(typeFace);
                profileTutorHelpLabel.setTypeface(typeFace);

                //set text in dialogue
                tutorHelpIntro.setText(text);
                homeTutorHelpText.setText(text);
                bookingTutorHelpText.setText(text);
                sessionsTutorHelpText.setText(text);
                profileTutorHelpText.setText(text);

                //close dialogue button
                Button closeButton = (Button) dialog.findViewById(R.id.declineTutorButton);
                // if decline button is clicked, close the custom dialog
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close dialog
                        dialog.dismiss();
                    }
                });
            }
        });

        // Click listener for tutors list
        upcomingSessionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Get tutor id
                cursorSessionsResponse.moveToPosition(position);
                int SESSION_ID = cursorSessionsResponse.getInt(cursorSessionsResponse.getColumnIndex("id"));

                // Add USER_ID and TUTOR_ID to session details fragment for displaying
                Bundle bundle = new Bundle();
                bundle.putInt("USER_ID", USER_ID);
                bundle.putInt("SESSION_ID", SESSION_ID);

                // Swap into new fragment
                TutorSessionsDetailsFragment session = new TutorSessionsDetailsFragment();
                session.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.tutor_fragment_container, session);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        return view;
    }
}
