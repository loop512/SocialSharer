package com.example.socialsharer.data;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.socialsharer.R;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact> {
    ArrayList<Contact> contactList;
    Context context;

    public ContactAdapter(Context context, ArrayList<Contact> contactList) {
        super(context, R.layout.contact, contactList);
        contactList=contactList;
        this.context = context;
    }

    // View cache
    class ViewHolder{
        TextView name;
        ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.contact, parent, false);
            holder=new ViewHolder();

            holder.name=(TextView)convertView.findViewById(R.id.contact_name);
            holder.image=(ImageView)convertView.findViewById(R.id.contact_image);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder)convertView.getTag();
        }

        Contact contact=getItem(position);

        holder.name.setText(contact.getName());
        holder.image.setImageResource(contact.getImageID());

        return convertView;
    }
