package summer.project.whatsappFinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import summer.project.whatsappFinal.Listener.OnRecyclerViewItemClickListener;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    ArrayList<Contact>arraylist;
    private OnRecyclerViewItemClickListener listener;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener)
    {
        this.listener = listener;
    }

    public ContactAdapter(ArrayList<Contact> arraylist) {
        this.arraylist = arraylist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = arraylist.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvNumber.setText(contact.getNumber());

    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName,tvNumber ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.textView_contactName);
            tvNumber=itemView.findViewById(R.id.tv_number);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
