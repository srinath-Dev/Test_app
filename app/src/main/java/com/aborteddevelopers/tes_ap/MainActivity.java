package com.aborteddevelopers.tes_ap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ImageView  image_profile;
    TextView save, tv_change;
    MaterialEditText fullname,  bio;
    FirebaseUser firebaseUser;
    Button nexti;
    private FirebaseAuth firebaseAuth;



    private Uri mImageUri;

    private StorageTask uploadTask;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image_profile = findViewById (R.id.image_profile);
        save = findViewById (R.id.save);
        tv_change = findViewById (R.id.tv_change);
        fullname = findViewById (R.id.fullname);
        bio = findViewById (R.id.bio);
        nexti = findViewById(R.id.next_butt);
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
        storageRef = FirebaseStorage.getInstance ().getReference ("uploads");
        Toast.makeText(MainActivity.this, "New visitor is created", Toast.LENGTH_LONG).show();

        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Users").child (firebaseUser.getUid ());
        reference.addValueEventListener (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue (User.class);




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        save.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                updateProfile(fullname.getText ().toString (),

                        bio.getText ().toString ());


            }


        });
        nexti.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                updateProfile(fullname.getText ().toString (),
                        bio.getText ().toString ());
                Toast.makeText(MainActivity.this, "Thank you for your visit", Toast.LENGTH_LONG).show();
                Logout();

            }
        });

        tv_change.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                CropImage.activity ()
                        .setAspectRatio (1,1).setCropShape (CropImageView.CropShape.OVAL)
                        .start (MainActivity.this);

            }
        });

        image_profile.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                CropImage.activity().setAspectRatio(1, 1);
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL);
                CropImage.activity().start(MainActivity.this);
            }
        });

        save.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {




            }


        });




    }

    private void updateProfile(String fullname, String bio) {

        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Users").child (firebaseUser.getUid ());

        HashMap<String, Object> hashMap = new HashMap<> ();
        hashMap.put ("fullname",fullname);

        hashMap.put ("bio",bio);

        reference.updateChildren (hashMap);
    }

    private String getFileExtension(Uri uri){

        ContentResolver contentResolver = getContentResolver ();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton ();
        return mimeTypeMap.getExtensionFromMimeType (contentResolver.getType (uri));

    }

    private void uploadImage(){

        final ProgressDialog pd = new ProgressDialog (this);
        pd.setMessage ("Uploading");
        pd.show ();


        if (mImageUri != null ){

            final StorageReference filereference = storageRef.child (System.currentTimeMillis ()+"."+ getFileExtension (mImageUri));

            uploadTask = filereference.putFile (mImageUri);
            uploadTask.continueWithTask (new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful ()){

                        throw task.getException ();
                    }


                    return filereference.getDownloadUrl ();
                }
            }).addOnCompleteListener (new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful ()){

                        Uri downloadUri = task.getResult ();
                        String myUrl = downloadUri.toString ();

                        DatabaseReference reference = FirebaseDatabase.getInstance ().getReference ("Users").child (firebaseUser.getUid ());
                        HashMap<String, Object> hashMap = new HashMap<> ();
                        hashMap.put ("imageurl",myUrl);

                        reference.updateChildren (hashMap);
                        pd.dismiss ();

                    }else {

                        Toast.makeText (MainActivity.this,"Failed",Toast.LENGTH_SHORT).show ();
                    }
                }
            }).addOnFailureListener (new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText (MainActivity.this, e.getMessage (), Toast.LENGTH_SHORT).show ();
                }
            });


        }else {

            Toast.makeText (this, "no image selected", Toast.LENGTH_SHORT).show ();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult (requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult (data);
            mImageUri = result.getUri ();

            uploadImage ();
        }else {

            Toast.makeText (this, "Something went Wrong", Toast.LENGTH_SHORT).show ();

        }



    }
    private void Logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
    }

}
