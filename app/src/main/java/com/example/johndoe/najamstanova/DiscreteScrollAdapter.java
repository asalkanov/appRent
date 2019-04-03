package com.example.johndoe.najamstanova;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DiscreteScrollAdapter extends RecyclerView.Adapter<DiscreteScrollAdapter.ViewHolder> {

    private ArrayList<String> data;



    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        CardView imageCard;
        ImageButton swapLeft, swapRight, glavnaSlika;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            swapLeft = itemView.findViewById(R.id.swapLeft);
            swapRight = itemView.findViewById(R.id.swapRight);
            glavnaSlika = itemView.findViewById(R.id.glavnaSlika);
        }
    }


    public DiscreteScrollAdapter(ArrayList<String> data) {

        this.data = data;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.discrete_scroll_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(data.get(holder.getAdapterPosition()))
                .into(holder.image);

        if (holder.getAdapterPosition() == 0) {
            Log.d("pozicija0", holder.getAdapterPosition() + "!");
            holder.glavnaSlika.setVisibility(View.GONE);
            holder.swapLeft.setVisibility(View.GONE);
        } else {
            holder.glavnaSlika.setVisibility(View.VISIBLE);
            holder.swapLeft.setVisibility(View.VISIBLE);
        }
        if (holder.getAdapterPosition() == data.size()-1) {
            holder.swapRight.setVisibility(View.GONE);
        } else {
            holder.swapRight.setVisibility(View.VISIBLE);
        }


        // postavlja sliku kao Glavnu (prvu)
        holder.glavnaSlika.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer trenutnaPozicija = holder.getAdapterPosition();
                String trenutniUri = data.get(trenutnaPozicija);
                Integer glavnaPozicija = 0;
                String glavniUri = data.get(glavnaPozicija);
                Log.d("pozicija1", trenutnaPozicija + "<>" + glavnaPozicija);
                data.set(trenutnaPozicija, glavniUri);
                data.set(glavnaPozicija, trenutniUri);
                DiscreteScrollAdapter discreteAdapter;
                discreteAdapter = new DiscreteScrollAdapter(data);
                discreteAdapter.notifyDataSetChanged();
                notifyDataSetChanged();
                RecyclerView discreteScroll = ((Activity)  holder.itemView.getContext()).findViewById(R.id.discreteScroll);
                discreteScroll.smoothScrollToPosition(glavnaPozicija);
            }
        });

        // premješta sliku lijevo
        holder.swapLeft.setOnClickListener(v -> {
            if (holder.getAdapterPosition() > 0) {
                Integer trenutnaPozicija = holder.getAdapterPosition();
                String trenutniUri = data.get(trenutnaPozicija);
                Integer prethodnaPozicija = holder.getAdapterPosition()-1;
                String prethodniUri = data.get(prethodnaPozicija);
                Log.d("pozicija1", trenutnaPozicija + "<>" + prethodnaPozicija);
                data.set(trenutnaPozicija, prethodniUri);
                data.set(prethodnaPozicija, trenutniUri);
                DiscreteScrollAdapter discreteAdapter;
                discreteAdapter = new DiscreteScrollAdapter(data);
                discreteAdapter.notifyDataSetChanged();
                notifyDataSetChanged();
                RecyclerView discreteScroll = ((Activity)  holder.itemView.getContext()).findViewById(R.id.discreteScroll);
                discreteScroll.smoothScrollToPosition(prethodnaPozicija);
            }
        });

        // premješta sliku desno
        holder.swapRight.setOnClickListener(v -> {
            if (holder.getAdapterPosition() < data.size() && holder.getAdapterPosition() >= 0) {
                Integer trenutnaPozicija = holder.getAdapterPosition();
                String trenutniUri = data.get(trenutnaPozicija);
                Integer sljedecaPozicija = holder.getAdapterPosition()+1;
                String sljedeciUri = data.get(sljedecaPozicija);
                Log.d("pozicija2", trenutnaPozicija + "<>" + sljedecaPozicija);
                data.set(trenutnaPozicija, sljedeciUri);
                data.set(sljedecaPozicija, trenutniUri);
                DiscreteScrollAdapter discreteAdapter;
                discreteAdapter = new DiscreteScrollAdapter(data);
                discreteAdapter.notifyDataSetChanged();
                notifyDataSetChanged();
                RecyclerView discreteScroll = ((Activity)  holder.itemView.getContext()).findViewById(R.id.discreteScroll);
                discreteScroll.smoothScrollToPosition(sljedecaPozicija);
            }
        });

    }

    public void setItems(ArrayList<String> data) {
        this.data = data;
    }

    // Lista Slika (data) se šalje u sljedeći Activity (odabir lokacije na Mapi) kako bi se sačuvao redoslijed slika iz DiscreteAdaptera
    public ArrayList<String> vratiListuSlikuIzAdaptera(){
        return data;
    }

    @Override
    public int getItemCount() {
        return (data == null) ? 0 : data.size();
    }


}