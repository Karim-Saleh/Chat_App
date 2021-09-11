package summer.project.whatsappFinal;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    ArrayList<Message> messages;
    String userPhoneNumber;
    private Context con;
    private int lastPosition = -1;
    private static int LEFT_BUBBLE = 0, RIGHT_BUBBLE = 1;

    public ChatAdapter (ArrayList<Message> messages, String userPhoneNumber, Context con)
    {
        this.messages = messages;
        this.userPhoneNumber = userPhoneNumber;
        this.con = con;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position)
    {
        return (messages.get(position).getSender()).equals(userPhoneNumber) ? RIGHT_BUBBLE :
                LEFT_BUBBLE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewLeft = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left,parent,false);
        View viewRight = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right,parent,false);
        if(viewType == LEFT_BUBBLE)
        {
            return new LeftViewHolder(viewLeft);
        }
        else
        {
            return new RightViewHolder(viewRight);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if(holder.getItemViewType() == LEFT_BUBBLE)
        {
            LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
            leftViewHolder.message.setText(message.getMsg());
            leftViewHolder.time.setText(message.getTime());
            if(!message.getSeen())
            {
                DatabaseReference reference_sentFrom = App.getReference_sentFrom();
                reference_sentFrom.child(message.getSender()).child(message.getId()).child("seen").setValue(true);
                message.setSeen(true);
            }
        }
        else
        {
            RightViewHolder rightViewHolder = (RightViewHolder) holder;
            rightViewHolder.message.setText(message.getMsg());
            rightViewHolder.time.setText(message.getTime());
            if(message.getSeen())
            {
                rightViewHolder.ticks.setImageResource(R.drawable.seen_ticks);
                rightViewHolder.ticks.setVisibility(View.VISIBLE);
            }
            else if(message.getDelivered())
            {
                rightViewHolder.ticks.setImageResource(R.drawable.delivered_ticks);
                rightViewHolder.ticks.setVisibility(View.VISIBLE);
            }
            else if(message.getSent())
            {
                rightViewHolder.ticks.setImageResource(R.drawable.sent_tick);
                rightViewHolder.ticks.setVisibility(View.VISIBLE);
            }
            else
            {
                rightViewHolder.ticks.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder
    {
        TextView message, time;
        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView)itemView.findViewById(R.id.textView_leftMessage);
            time = (TextView)itemView.findViewById(R.id.textView_timeLeft);
        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder
    {
        TextView message, time;
        ImageView ticks;
        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView)itemView.findViewById(R.id.textView_rightMessage);
            time = (TextView)itemView.findViewById(R.id.textView_timeRight);
            ticks = (ImageView) itemView.findViewById(R.id.imageView_ticks);
        }
    }

    private void setAnimationLeft(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            ScaleAnimation anim = new ScaleAnimation(0f, 1.0f, 1.0f, 1.0f, Animation.REVERSE, -2.0f, Animation.RELATIVE_TO_SELF, 0f);
            anim.setDuration(300);
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }

    private void setAnimationRight(View viewToAnimate, int position) {
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(2f, 1.0f, 1.0f, 1.0f, Animation.REVERSE, -2.0f, Animation.RELATIVE_TO_SELF, 0f);
            anim.setDuration(300);
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }
}
