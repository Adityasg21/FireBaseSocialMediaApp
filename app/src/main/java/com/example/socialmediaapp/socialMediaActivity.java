package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class socialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ImageView postImageView;
    private EditText edtDesc;
    private Button btnCreatePost;
    private ListView userListView;
    private Bitmap bitmap;
    private String imageIdentifier;
    private ArrayList<String> userNames;
    private ArrayAdapter adapter;
    private ArrayList<String> uids;


    private FirebaseAuth mAuth;
    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);
        mAuth = FirebaseAuth.getInstance();

        postImageView=findViewById(R.id.PostimageView);
        edtDesc=findViewById(R.id.edtDesc);
        btnCreatePost=findViewById(R.id.btnCreatePost);
        userListView=findViewById(R.id.users_listView);
        userListView.setOnItemClickListener(this);

        userNames=new ArrayList<>();
        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,userNames);
        uids=new ArrayList<>();


        userListView.setAdapter(adapter);


        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              uploadImageToServer();
            }
        });

        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logoutItem:
                logout();
                break;
            case R.id.ViewPost:
                Intent intent=new Intent(this,viewPostAcitvity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }
    public void logout(){
        mAuth.signOut();
        finish();
    }

    public void selectImage(){

        if(Build.VERSION.SDK_INT<23){
            Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1000);

        }else if(Build.VERSION.SDK_INT>=23) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1000 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1000 && resultCode==RESULT_OK && data!=null){
            Uri chosenImageData=data.getData();
            try {
                bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),chosenImageData);
                 postImageView.setImageBitmap(bitmap);
            } catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    private void uploadImageToServer(){

        if(bitmap!=null) {
            postImageView.setDrawingCacheEnabled(true);
            postImageView.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            imageIdentifier = UUID.randomUUID() + ".png";


            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(socialMediaActivity.this, exception.toString(), Toast.LENGTH_LONG).show();// Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Toast.makeText(socialMediaActivity.this, "uploading success", Toast.LENGTH_LONG).show(); // ...
                edtDesc.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            uids.add(snapshot.getKey());
                            String username=snapshot.child("username").getValue().toString();
                            userNames.add(username);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                imageDownloadLink=task.getResult().toString();
                            }

                        }
                    });

                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String,String> datamap=new HashMap<>();
        datamap.put("fromWhom",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        datamap.put("imageIdentifier",imageIdentifier);
        datamap.put("imageLink",imageDownloadLink);
        datamap.put("des",edtDesc.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("my_users").child(uids.get(position)).child("received_posts").push().setValue(datamap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Toast.makeText(socialMediaActivity.this,"Data sent",Toast.LENGTH_SHORT).show();
            }
        });



    }
}