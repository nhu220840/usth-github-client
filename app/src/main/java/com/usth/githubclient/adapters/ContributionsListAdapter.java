package com.usth.githubclient.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.usth.githubclient.R;
import com.usth.githubclient.domain.model.ContributionDataEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ContributionsListAdapter extends BaseAdapter {

    private final Context context;
    private final List<ContributionDataEntry> contributions;
    private final int daysInMonth;
    private final int firstDayOfMonth;

    public ContributionsListAdapter(Context context, List<ContributionDataEntry> contributions) {
        this.context = context;
        this.contributions = contributions != null ? contributions : new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        this.daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        this.firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sunday, 1=Monday...
    }

    @Override
    public int getCount() {
        return daysInMonth + firstDayOfMonth;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contribution_grid_item, parent, false);
        }

        TextView dayText = convertView.findViewById(R.id.day_text);

        if (position < firstDayOfMonth) {
            dayText.setText("");
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            int day = position - firstDayOfMonth + 1;
            dayText.setText(String.valueOf(day));

            int contributionCount = 0;
            for (ContributionDataEntry entry : contributions) {
                if (entry.getDate().get(Calendar.DAY_OF_MONTH) == day) {
                    contributionCount = entry.getCount();
                    break;
                }
            }

            int color = getColorForContributions(contributionCount);
            convertView.setBackgroundColor(color);
        }

        return convertView;
    }

    private int getColorForContributions(int count) {
        if (count == 0) {
            return Color.parseColor("#EBEDF0"); // Light Grey
        } else if (count >= 1 && count <= 3) {
            return Color.parseColor("#9BE9A8"); // Light Green
        } else if (count >= 4 && count <= 6) {
            return Color.parseColor("#40C463"); // Medium Green
        } else if (count >= 7 && count <= 9) {
            return Color.parseColor("#30A14E"); // Dark Green
        } else {
            return Color.parseColor("#216E39"); // Darkest Green
        }
    }
}