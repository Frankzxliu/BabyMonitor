package com.frank.babymonitor;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frank.babymonitor.utils.TitleSelectionHelper;

import java.util.ArrayList;

/**
 * Home Page List
 */
public class BabyMonitorListFragment extends Fragment {

    private final String TAG = "BabyMonitorListFragment";

    private ArrayList<Title> titles;
    private String[] names;
    private TypedArray images;
    private OnTitleSelected selectedListener;

    public static BabyMonitorListFragment newInstance(){
        Log.d("Instance", "Called Instance");
        return new BabyMonitorListFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(TAG, "onAttach Called");

        final Resources resource = context.getResources();
        names = resource.getStringArray(R.array.names);

        images = getResources().obtainTypedArray(R.array.images);

        titles = new ArrayList<Title>();

        for(int i =0; i < names.length; i++){
            Log.d(TAG, names[i]);
            titles.add(new Title(images.getResourceId(i,-1), names[i]));
        }
        images.recycle();

        selectedListener = new TitleSelectionHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView Called");
        final View view = inflater.inflate(R.layout.main_page_list, container, false);

        final Activity activity = getActivity();
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(activity,1));
        recyclerView.setAdapter(new BabyMonitorListAdapter(activity));
        return view;
    }

    private class BabyMonitorListAdapter extends RecyclerView.Adapter<ViewHolder>{

        private LayoutInflater layoutInflater;

        public BabyMonitorListAdapter(Context context){layoutInflater = LayoutInflater.from(context);}

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.main_page_detail,parent,false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.setData(titles.get(position).getFunctionImage(), names[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedListener.OnTitleSelected(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        // Views
        private ImageView imageView;
        private TextView nameTextView;

        private ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.title_image);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
        }

        private void setData(int imagePosition, String name) {
            imageView.setImageResource(imagePosition);
            nameTextView.setText(name);
        }
    }

    /**
     * Help to select an title from the fragment
     */
    public interface OnTitleSelected {
        void OnTitleSelected(int position);
    }
}
