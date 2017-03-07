package io.chino.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.chino.R;
import io.chino.models.Message;

public class MyMessageAdapter extends RecyclerView.Adapter<MyMessageAdapter.ViewHolder> {
    private List<Message> mDataset;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView dateTimeTextView;
        CircleImageView messengerImageView;

        ViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTimeTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }
    @Override
    public int getItemViewType(int position) {
        Message m = mDataset.get(position);
        if(m.getName().equals(Constants.user.getUsername())){
            return 1;
        } else {
            return 0;
        }
    }

    public void addItem(Message message){
        mDataset.add(message);
        notifyDataSetChanged();
    }

    public MyMessageAdapter(List<Message> myDataset) {
        mDataset = myDataset;
        Collections.reverse(myDataset);
    }

    @Override
    public MyMessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v;
        switch (viewType){
            case 0:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                break;
            case 1:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_my_message, parent, false);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Message m = mDataset.get(position);
        holder.dateTimeTextView.setText(m.getDate() + " " + m.getTime());
        holder.messageTextView.setText(m.getText());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
