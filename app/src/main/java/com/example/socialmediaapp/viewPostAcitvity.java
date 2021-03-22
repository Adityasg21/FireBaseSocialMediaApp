package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class viewPostAcitvity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private ListView postListView;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private TextView txtDesc;
    private ImageView sentPost;
    private ArrayList<DataSnapshot> mDataSnapshots;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_acitvity);

        txtDesc=findViewById(R.id.textDesc);
        sentPost=findViewById(R.id.sentPost);
        mDataSnapshots=new ArrayList<>();


        firebaseAuth=FirebaseAuth.getInstance();
        postListView=findViewById(R.id.postListView);
        usernames=new ArrayList<>();
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,usernames);
        postListView.setAdapter(adapter);

        postListView.setOnItemClickListener(this);
        postListView.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                mDataSnapshots.add(snapshot);
                String  fromWhomUser=snapshot.child("fromWhom").getValue().toString();
                usernames.add(fromWhomUser);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int i=0;
                for(DataSnapshot datasnapshot:mDataSnapshots){
                    if(datasnapshot.getKey().equals(snapshot.getKey())){
                        mDataSnapshots.remove(i);
                        usernames.remove(i);
                    }
                    i++;
                }

                adapter.notifyDataSetChanged();
                sentPost.setImageResource(R.drawable.download);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DataSnapshot myDataSnapShot=mDataSnapshots.get(position);
        String downloadLink=myDataSnapShot.child("imageLink").getValue().toString();

        Picasso.get().load(downloadLink).into(sentPost);
        txtDesc.setText(myDataSnapShot.child("des").getValue().toString());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        new AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        FirebaseStorage.getInstance().getReference().child("my_images").child((String)mDataSnapshots.get(position).
                                child("imageIdentifier").getValue()).delete();

                        FirebaseDatabase.getInstance().getReference().child("my_users")
                                .child(firebaseAuth.getCurrentUser().getUid()).child("received_posts")
                                .child(mDataSnapshots.get(position).getKey()).removeValue();


                        // Continue with delete operation
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return false;
    }
}