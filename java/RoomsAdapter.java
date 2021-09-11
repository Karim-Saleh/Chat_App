package summer.project.whatsappFinal;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import summer.project.whatsappFinal.Listener.OnRecyclerViewItemClickListener;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {
    Activity activity ;
    ArrayList<Room> rooms;
    String userPhoneNumber;
    OnRecyclerViewItemClickListener listener;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener)
    {
        this.listener = listener;
    }

    public RoomsAdapter(Activity activity , ArrayList<Room> rooms, String userPhoneNumber) {
        this.activity=activity;
        this.rooms = rooms;
        this.userPhoneNumber = userPhoneNumber;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rooms_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.textView_contactName.setText(room.getContactName());
        holder.textView_time.setText(room.getLastMessage().getTime());
        if(room.getContactName().isEmpty())
        {
            holder.textView_contactName.setText(room.getContactNumber());
        }
        if(userPhoneNumber.equals(room.getLastMessage().getSender()))
        {
            if(room.getLastMessage().getSeen())
            {
                holder.imageView_ticks.setImageResource(R.drawable.seen_ticks);
                holder.imageView_ticks.setVisibility(View.VISIBLE);
            }
            else if(room.getLastMessage().getDelivered())
            {
                holder.imageView_ticks.setImageResource(R.drawable.delivered_ticks);
                holder.imageView_ticks.setVisibility(View.VISIBLE);
            }
            else if(room.getLastMessage().getSent())
            {
                holder.imageView_ticks.setImageResource(R.drawable.sent_tick);
                holder.imageView_ticks.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.imageView_ticks.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            holder.imageView_ticks.setVisibility(View.INVISIBLE);
        }
        holder.textView_lastMessage.setText(room.getLastMessage().getMsg());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView_contactName, textView_lastMessage, textView_time;
        ImageView imageView_ticks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_contactName = itemView.findViewById(R.id.textView_contactName);
            textView_lastMessage = itemView.findViewById(R.id.textView_lastMessage);
            textView_time = itemView.findViewById(R.id.textView_lastMessageTime);
            imageView_ticks = itemView.findViewById(R.id.imageView_roomTicks);
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                    {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
