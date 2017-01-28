package io.chino.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.chino.R;
import io.chino.models.Message;
import io.chino.models.Request;

public class MyRequestAdapter extends RecyclerView.Adapter<MyRequestAdapter.ViewHolder> {

    private List<Request> mDataset;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView emailTextView;
        TextView roleTextView;
        Button accept;

        ViewHolder(View v) {
            super(v);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            emailTextView = (TextView) itemView.findViewById(R.id.emailTextView);
            roleTextView = (TextView) itemView.findViewById(R.id.roleTextView);
        }
    }

    public void addItem(Request request){
        mDataset.add(request);
        notifyDataSetChanged();
    }

    public MyRequestAdapter(List<Request> myDataset) {
        mDataset = myDataset;
        Collections.reverse(myDataset);
    }

    @Override
    public MyRequestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Request m = mDataset.get(position);
        holder.emailTextView.setText(m.getEmail());
        holder.nameTextView.setText(m.getName() + " " + m.getLastName());
        holder.roleTextView.setText(m.getRole());
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: gestire cosa fa quando clicca su aggiungi
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

