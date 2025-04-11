package ai.movie.modzy.Activity.Booking;



import android.view.View;
import android.widget.AdapterView;

public abstract class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    @Override public void onNothingSelected(AdapterView<?> parent) {}
    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onItemSelected(parent.getItemAtPosition(position).toString());
    }
    public abstract void onItemSelected(String selected);
}

