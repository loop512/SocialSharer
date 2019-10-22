package com.example.socialsharer.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.socialsharer.R;
import java.util.ArrayList;

/**
 * This class extends the ArrayAdapter, allows handling custom dynamic list view.
 */
public class ContactAdapter extends ArrayAdapter<Contact> {
    Context context;

    public ContactAdapter(Context context, ArrayList<Contact> contactList) {
        super(context, R.layout.contact, contactList);
        this.context = context;
    }

    // View cache
    class ViewHolder{
        TextView name;
        ImageView image;
    }

    /**
     * Convert a single row's information to a view on the list,
     * Using default image if user haven't upload their own image.
     * @param position position on the list view
     * @param convertView converted view for each row on the list
     * @param parent the entire list view
     * @return converted view for this row, if exist just display it.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.contact, parent, false);
            holder = new ViewHolder();

            holder.name = convertView.findViewById(R.id.contact_name);
            holder.image = convertView.findViewById(R.id.contact_image);
            convertView.setTag(holder);
        }
        else{
            holder=(ViewHolder)convertView.getTag();
        }

        Contact contact=getItem(position);

        holder.name.setText(contact.getName());
        if(contact.getBitmap() == null){
            holder.image.setImageResource(contact.getImageID());
        } else {
            holder.image.setImageBitmap(contact.getBitmap());
        }
        return convertView;
    }
}