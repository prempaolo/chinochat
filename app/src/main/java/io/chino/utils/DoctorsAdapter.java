package io.chino.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.chino.R;

public class DoctorsAdapter extends BaseAdapter{

    private List<DoctorItem> articles=null;
    private Context context=null;

    public DoctorsAdapter(Context context,List<DoctorItem> articles)
    {
        this.articles=articles;
        this.context=context;
    }

    @Override
    public int getCount()
    {
        return articles.size();
    }

    @Override
    public Object getItem(int position)
    {
        return articles.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View v, ViewGroup vg)
    {
        if (v==null)
        {
            v= LayoutInflater.from(context).inflate(R.layout.doctor_item_layout, null);
        }
        DoctorItem di=(DoctorItem) getItem(position);
        TextView txt =(TextView) v.findViewById(R.id.doctor_item_email);
        txt.setText(di.getEmail());
        txt=(TextView) v.findViewById(R.id.doctor_item_last_name);
        txt.setText(di.getLastName());
        txt=(TextView) v.findViewById(R.id.doctor_item_name);
        txt.setText(di.getName());
        return v;
    }

}
