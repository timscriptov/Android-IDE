package com.mcal.studio.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import com.mcal.studio.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter to display JavaScript logs
 */
public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    private String localWithoutIndex;

    /**
     * List to hold logs
     */
    private List<ConsoleMessage> jsLogs;

    /**
     * public Constructor
     *
     * @param logs list of logs
     */
    public LogsAdapter(String localWithoutIndex, List<ConsoleMessage> logs) {
        this.localWithoutIndex = localWithoutIndex;
        jsLogs = logs;
    }

    /**
     * When view holder is created
     *
     * @param parent   parent view
     * @param viewType type of view
     * @return LogsAdapter.ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new ViewHolder(rootView);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder   view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ConsoleMessage consoleMessage = jsLogs.get(position);
        String newId = consoleMessage.sourceId().replace(localWithoutIndex, "") + ":" + consoleMessage.lineNumber();
        String msg = consoleMessage.message();
        String level = consoleMessage.messageLevel().name().substring(0, 1);

        holder.level.setText(level);
        holder.level.setTextColor(getLogColor(consoleMessage.messageLevel()));
        holder.message.setText(msg);
        holder.details.setText(newId);
    }

    @ColorInt
    private int getLogColor(ConsoleMessage.MessageLevel messageLevel) {
        switch (messageLevel) {
            case LOG:
                return 0x87000000;
            case TIP:
                return 0xff7c4dff;
            case DEBUG:
                return 0xff00e676;
            case ERROR:
                return 0xffff5252;
            case WARNING:
                return 0xffffc400;
        }

        return Color.BLACK;
    }

    /**
     * Gets log count
     *
     * @return list size
     */
    @Override
    public int getItemCount() {
        return jsLogs.size();
    }

    /**
     * View holder class for logs
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.log_level)
        TextView level;
        @BindView(R.id.log_message)
        TextView message;
        @BindView(R.id.log_details)
        TextView details;

        /**
         * public Constructor
         *
         * @param v view to display log
         */
        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
