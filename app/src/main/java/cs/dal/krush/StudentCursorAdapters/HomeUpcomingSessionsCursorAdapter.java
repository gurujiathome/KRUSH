package cs.dal.krush.StudentCursorAdapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cs.dal.krush.R;

/**
 * This is the adapter class for the customized rows in the Upcoming Sessions
 * list view on the student home page.
 *
 * Source:
 * [4] C. (n.d.). Codepath/android_guides. Retrieved March 12, 2017,
 * from https://github.com/codepath/android_guides/wiki/Populating-a-ListView-with-a-CursorAdapter
 */
public class HomeUpcomingSessionsCursorAdapter extends CursorAdapter {

    //store the context set in the constructor
    private Context mContext;

    /**
     * Constructor that initializes the custom cursor adapter.
     *
     * @param context
     * @param cursor
     */
    public HomeUpcomingSessionsCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
        this.mContext = context;
    }

    /**
     * Overrides the newView method used to inflate a new view and return it.
     *
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.student_home_listentry, parent, false);
    }

    /**
     * Overrides the bindView method used to bind all data to a given view.
     *
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor){
        //fetch UI components:
        TextView header = (TextView)view.findViewById(R.id.firstLine);
        TextView subHeader = (TextView)view.findViewById(R.id.secondLine);
        ImageView image = (ImageView)view.findViewById(R.id.icon);

        //Get the tutor's profile image:
        String imageFileName = cursor.getString(cursor.getColumnIndexOrThrow("profile_pic"));
        if (imageFileName != null){
            int resourceId = mContext.getResources().getIdentifier(
                    imageFileName, "drawable", mContext.getPackageName());
            image.setImageResource(resourceId);
        }

        //Set the row's header text:
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        header.setText(title);

        //Set the row's sub-header text:
        String tutorFirstName = cursor.getString(cursor.getColumnIndexOrThrow("f_name"));
        String tutorLastName = cursor.getString(cursor.getColumnIndexOrThrow("l_name"));
        String tutorRating = cursor.getString(cursor.getColumnIndexOrThrow("rating"));
        String tutorRate = cursor.getString(cursor.getColumnIndexOrThrow("rate"));
        String sessionLocation = cursor.getString(cursor.getColumnIndexOrThrow("location"));
        String text2content = tutorFirstName + " " + tutorLastName + ", location: " + sessionLocation + ", Rating: " + tutorRating + ", Rate: " + tutorRate + "$";
        subHeader.setText(text2content);
    }
}
