package com.aby.advancedlandmarkbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aby.advancedlandmarkbook.databinding.RecyclerRowBinding;
import com.aby.advancedlandmarkbook.model.Location;
import com.aby.advancedlandmarkbook.view.RecyclerViewInterface;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{
    ArrayList<Location> locationArrayList;
    private final RecyclerViewInterface recyclerViewInterface;

    public RecyclerViewAdapter(ArrayList<Location> locationArrayList, RecyclerViewInterface recyclerViewInterface) // constructor
    {
        this.locationArrayList = locationArrayList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) // binding recycler_row.xml
    {
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position)
    {
        holder.binding.recyclerViewTextView.setText(locationArrayList.get(position).name);
    }

    @Override
    public int getItemCount()
    {
        return locationArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private RecyclerRowBinding binding;

        public ViewHolder(RecyclerRowBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if (recyclerViewInterface != null)
                    {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) // position is valid
                        {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view)
                {
                    if (recyclerViewInterface != null)
                    {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) // position is valid
                        {
                            recyclerViewInterface.onItemLongClick(position);
                        }
                    }

                    return true;
                }
            });
        }
    }
}