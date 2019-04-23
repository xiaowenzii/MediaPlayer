package com.wellav.omp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wellav.omp.R;
import com.wellav.omp.channel.Channel;

import java.util.List;


public class ChannelAdapter extends BaseAdapter {

    private Context mContext;
    private int totalHeight;
    private List<Channel> contentList;
    private LayoutInflater mInflater;
    // 选中的位置
    private int selectedPosition;

    public ChannelAdapter(Context ct, int totalHeight, List<Channel> contentList) {
        this.mContext = ct;
        this.totalHeight = totalHeight;
        this.contentList = contentList;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return contentList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_channel, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.item_channel_name);
            viewHolder.num = convertView.findViewById(R.id.item_channel_num);
            viewHolder.channelLinearLayoutBg = convertView.findViewById(R.id.item_channel_linearLayout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        int size = contentList.size();
        viewHolder.num.setText(getNumber(size + "", (position + 1) + ""));
        viewHolder.name.setText(contentList.get(position).getName());
        if (selectedPosition == position) {
            viewHolder.channelLinearLayoutBg.setBackgroundResource(R.mipmap.live_play_channel_list_selected_item);
        } else {
            viewHolder.channelLinearLayoutBg.setBackgroundResource(0);
        }
        viewHolder.num.setTextColor(mContext.getResources().getColor(R.color.white));
        viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.white));
        ViewGroup.LayoutParams linearParams = viewHolder.channelLinearLayoutBg.getLayoutParams();
        linearParams.height = totalHeight / 9;
        return convertView;
    }

    private class ViewHolder {
        private LinearLayout channelLinearLayoutBg;
        private TextView num;
        private TextView name;
    }

    private String getNumber(String size, String num) {
        if (num.length() < size.length()) {
            for (int i = 0; i < size.length() - num.length(); i++) {
                num = "0" + num;
            }
        }
        return num;
    }

}
