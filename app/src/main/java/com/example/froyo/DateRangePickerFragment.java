package com.example.froyo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DateRangePickerFragment extends DialogFragment {

    public static DatePickerDialog.OnDateSetListener onDateSetListener;
    public boolean isStartDate;

    public DateRangePickerFragment(DatePickerDialog.OnDateSetListener onDateSetListener, boolean isStartDate) {
        this.onDateSetListener = onDateSetListener;
        this.isStartDate = isStartDate;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(requireActivity(), onDateSetListener, year, month, day);
    }

    public boolean isStartDate() {
        return isStartDate;
    }
}

