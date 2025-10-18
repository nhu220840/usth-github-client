package com.usth.githubclient.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.usth.githubclient.R;
import com.usth.githubclient.domain.model.ContributionDataEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ContributionsListAdapter extends BaseAdapter {

    private final Context context;
    private final List<ContributionDataEntry> contributions;
    private final Map<Integer, ContributionDataEntry> contributionsByDay;
    private final int daysInMonth;
    private final int firstDayOfMonthOffset;
    private final int totalCells;
    private final int today;
    private final boolean isDarkMode;


    public ContributionsListAdapter(Context context, List<ContributionDataEntry> contributions) {
        this.context = context;
        this.contributions = contributions != null ? contributions : new ArrayList<>();
        this.contributionsByDay = new HashMap<>();

        for (ContributionDataEntry entry : this.contributions) {
            int day = entry.getDate().get(Calendar.DAY_OF_MONTH);
            this.contributionsByDay.put(day, entry);
        }

        Calendar calendar = Calendar.getInstance();
        this.today = calendar.get(Calendar.DAY_OF_MONTH);
        this.daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        this.firstDayOfMonthOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int computedCells = daysInMonth + firstDayOfMonthOffset;
        int remainder = computedCells % 7;
        this.totalCells = remainder == 0 ? computedCells : computedCells + (7 - remainder);

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        this.isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public int getCount() {
        return totalCells;
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
        View itemView;
        ViewHolder holder;

        if (convertView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.contribution_grid_item, parent, false);
            holder = new ViewHolder();
            holder.container = (FrameLayout) itemView;
            holder.squareView = itemView.findViewById(R.id.contribution_square);
            holder.dayText = itemView.findViewById(R.id.day_text);
            itemView.setTag(holder);
        } else {
            itemView = convertView;
            holder = (ViewHolder) itemView.getTag();
        }

        if (position < firstDayOfMonthOffset || position >= firstDayOfMonthOffset + daysInMonth) {
            holder.squareView.setBackgroundColor(Color.TRANSPARENT);
            holder.dayText.setVisibility(View.INVISIBLE);
            holder.container.setForeground(null);
            itemView.setOnClickListener(null);
            itemView.setClickable(false);
        } else {
            holder.dayText.setVisibility(View.VISIBLE);
            final int dayOfMonth = position - firstDayOfMonthOffset + 1;
            holder.dayText.setText(String.valueOf(dayOfMonth));

            ContributionDataEntry entryForDay = contributionsByDay.get(dayOfMonth);
            int contributionCount = entryForDay != null ? entryForDay.getCount() : 0;

            int color = getColorForContributions(contributionCount);
            holder.squareView.setBackgroundColor(color);

            if (isDarkMode) {
                if (contributionCount == 0) {
                    holder.dayText.setTextColor(Color.parseColor("#C9D1D9"));
                } else {
                    holder.dayText.setTextColor(Color.WHITE);
                }
            } else {
                if (contributionCount == 0) {
                    holder.dayText.setTextColor(Color.parseColor("#57606A"));
                } else if (contributionCount <= 2) {
                    holder.dayText.setTextColor(Color.parseColor("#0A3069"));
                } else {
                    holder.dayText.setTextColor(Color.WHITE);
                }            }

            if (dayOfMonth == today) {
                holder.container.setForeground(ContextCompat.getDrawable(context, R.drawable.current_day_border));
            } else {
                holder.container.setForeground(null);
            }

            final int finalContributionCount = contributionCount;
            final ContributionDataEntry finalEntryForDay = entryForDay;
            itemView.setClickable(true);
            itemView.setOnClickListener(v -> {
                String message;
                if (finalContributionCount > 0 && finalEntryForDay != null) {
                    Calendar date = finalEntryForDay.getDate();
                    message = String.format(Locale.getDefault(), "%d contributions on %s",
                            finalContributionCount,
                            date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + date.get(Calendar.DAY_OF_MONTH)
                    );
                } else {
                    message = "No contributions on day " + dayOfMonth;
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
        }
        return itemView;
    }

    private int getColorForContributions(int count) {
        if (isDarkMode) {
            if (count == 0) {
                return Color.parseColor("#161B22");
            } else if (count >= 1 && count <= 2) {
                return Color.parseColor("#0E4429");
            } else if (count >= 3 && count <= 5) {
                return Color.parseColor("#006D32");
            } else if (count >= 6 && count <= 8) {
                return Color.parseColor("#26A641");
            } else {
                return Color.parseColor("#39D353");
            }
        } else {
            if (count == 0) {
                return Color.parseColor("#D8DEE4");
            } else if (count >= 1 && count <= 2) {
                return Color.parseColor("#9CC7FF");
            } else if (count >= 3 && count <= 5) {
                return Color.parseColor("#58A6FF");
            } else if (count >= 6 && count <= 8) {
                return Color.parseColor("#1F6FEB");
            } else {
                return Color.parseColor("#0A3069");
            }
        }
    }

    static class ViewHolder {
        FrameLayout container;
        View squareView;
        TextView dayText;
    }
}