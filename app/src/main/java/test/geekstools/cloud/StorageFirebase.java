package test.geekstools.cloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class StorageFirebase extends Activity {

    Button select;
    ImageView imageFile;
    TextView infoFile;

    FirebaseStorage storage;
    FirebaseAuth mAuth;

    String storageFirebase = "gs://boiling-inferno-1433.appspot.com/";
    InputStream imageStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_firebase);

        storage = FirebaseStorage.getInstance();

        select = (Button)findViewById(R.id.selectFile);
        imageFile = (ImageView)findViewById(R.id.imageFile);
        infoFile = (TextView)findViewById(R.id.fileInfo);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 7);

                Toast.makeText(getApplicationContext(), "Click on GeeksEmpire to Download", Toast.LENGTH_LONG).show();
            }
        });

        //Download
        imageFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String defaultDownload = "https://firebasestorage.googleapis.com/v0/b/boiling-inferno-1433.appspot.com/o/Dir%2Fgeosp.png?alt=media&token=d5ac2f2c-6fb5-493d-80aa-d9736dddccd7";
                SharedPreferences sharedPrefs = getSharedPreferences("Info", Context.MODE_PRIVATE);
                defaultDownload = sharedPrefs.getString("path", defaultDownload);  System.out.println("Path: " + defaultDownload);

                StorageReference httpsReference = storage.getReferenceFromUrl(defaultDownload);
                try {
                    downloadFromUri(httpsReference);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("x102x96x@gmail.com", "testtest")
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signInAnonymously();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        System.out.println("AUTH DONE!");
                        Toast.makeText(getApplicationContext(), "Auth Done >> You Are Good to Go", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case 7:
                if(resultCode == RESULT_OK){
                    try{
                        Uri selectedImage = imageReturnedIntent.getData();
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                    //    imageFile.setImageBitmap(selectedImageBitmap);

                        infoFile.setText(selectedImage.getPath());
                    }
                    catch (Exception e){
                        System.out.println(e);
                    }
                    finally{
                        try {
                            uploadFromUri(getContentResolver().openInputStream(imageReturnedIntent.getData()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    private void uploadFromUri(InputStream fileUri) {
        StorageReference mStorageRef = storage.getReferenceFromUrl(storageFirebase);
        StorageReference photoRef = mStorageRef.child("Dir").child("File");

        UploadTask uploadTask = photoRef.putStream(fileUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("DONE!");

                Uri dl = taskSnapshot.getMetadata().getDownloadUrl();

                SharedPreferences sharedpreferences = getSharedPreferences("Info", Context.MODE_PRIVATE);
                SharedPreferences.Editor dlLink = sharedpreferences.edit();
                dlLink.putString("path", String.valueOf(dl));
                dlLink.apply();

                infoFile.append("\n" + dl.getPath());
                System.out.println("Download >> " + dl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("!Damn!");
            }
        });
    }

    public void downloadFromUri(StorageReference storageRef) throws Exception {
        final File localFile = new File(Environment.getExternalStorageDirectory().getPath() + "/CloudAppTest_GeeksEmpire.jpg");

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                System.out.println("DONE!");

                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());  System.out.println("Downloaded >> " + localFile.getAbsolutePath());
                imageFile.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {}
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {}
        });
    }
}
