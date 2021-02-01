package com.sandeept.doge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BreedRecyclerAdapter extends RecyclerView.Adapter<BreedRecyclerAdapter.BreedViewHolder> {

    private ArrayList<String> breedNames;

    public static class BreedViewHolder extends RecyclerView.ViewHolder{

        private TextView breedName;

        public BreedViewHolder(View view){

            super(view);

            breedName = view.findViewById(R.id.breed_name);
        }

        TextView getTextView(){

            return breedName;
        }
    }

    BreedRecyclerAdapter(ArrayList<String> breedNames){

        this.breedNames = breedNames;
    }

    @Override
    @NonNull
    public BreedViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.breed_recycler_item, viewGroup, false);

        return new BreedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BreedViewHolder breedViewHolder, int position){

        breedViewHolder.getTextView().setText(breedNames.get(position));
    }

    @Override
    public int getItemCount(){

        return breedNames.size();
    }

    void updateList(ArrayList<String> list){

        breedNames = list;
        notifyDataSetChanged();
    }
}
