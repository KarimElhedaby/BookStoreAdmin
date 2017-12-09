package com.hamza.alif.bookstore.Ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hamza.alif.bookstore.R;
import com.hamza.alif.bookstore.Util.ActivityLancher;
import com.hamza.alif.bookstore.Util.Constants;
import com.hamza.alif.bookstore.Util.DatePickerFragment;
import com.hamza.alif.bookstore.Util.Utilities;
import com.hamza.alif.bookstore.data.Book;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditBookActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    @BindView(R.id.book_dateB)
    Button bookDateB;
    @BindView(R.id.add_pdf_fileB)
    Button addPdfFileB;
     @BindView(R.id.savechangesB)
    Button savechangesB;
    @BindView(R.id.bookIV)
    ImageView bookIV;
    @BindView(R.id.book_titleET)
    EditText bookTitleET;
    @BindView(R.id.book_descET)
    EditText bookDescET;
    @BindView(R.id.book_priceET)
    EditText bookPriceET;
    private Uri pdfFileUri;
    private String mCurrentPhotoPath;
    private File imageFile;
    private Uri selectedImageUri;
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PICK_FILE_REQUEST_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            book = (Book) bundle.getSerializable(ActivityLancher.BOOK_KEY);
            fillBookData();
        } else {
            finish();
        }
    }

    private void fillBookData() {
        bookTitleET.setText(book.getTitle());
        bookDescET.setText(book.getDescription());
        bookPriceET.setText(String.valueOf(book.getPrice()));
        bookDateB.setText(book.getDate() == null ? "" : book.getDate());
        Glide.with(this).load(book.getImageUrl()).into(bookIV);
        addPdfFileB.setText(book.getPdfTitle() == null ? "" : book.getPdfTitle());
        addPdfFileB.setTextColor(Color.RED);
    }




    @OnClick(R.id.bookIV)
    public void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose)
                .setCancelable(true)
                .setItems(new String[]{getString(R.string.gallery), getString(R.string.camera)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Intent i = new Intent(Intent.ACTION_PICK);
                                    i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(i, GALLERY_REQUEST);
                                } else {
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                        if (photoFile != null) {
                                            Uri photoURI = FileProvider.getUriForFile(EditBookActivity.this,
                                                    "com.hamza.alif.bookstore.Fileprovider", photoFile);
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                                        }
                                    }
                                }
                            }
                        }).show();
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = bookIV.getWidth();
        int targetH = bookIV.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if (targetH != 0 && targetW != 0
                && photoW != 0 && photoH != 0) {
            // Determine how much to scale down the image
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        bookIV.setImageBitmap(bitmap);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Path", mCurrentPhotoPath);
        outState.putSerializable("File", imageFile);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString("Path");
        imageFile = (File) savedInstanceState.getSerializable("File");
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                readFileFromSelectedURI();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            setPic();
        } else if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            pdfFileUri = data.getData();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                getPdfFileTitle();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
            }
        }

    }

    private void getPdfFileTitle() {
        Cursor cursor = getContentResolver().query(pdfFileUri, new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME}, null, null, null);
        //default file selected
        String pdfFileName = "File Selected";
        if (cursor != null) {
            cursor.moveToFirst();
            pdfFileName = cursor.getString(0);
            cursor.close();
        } else {
            String[] split = pdfFileUri.toString().split("/");
            if (split.length > 0 && split[split.length - 1] != null) {
                pdfFileName = split[split.length - 1].replace("%20", " ");
            }
        }
        addPdfFileB.setText(pdfFileName);
        addPdfFileB.setTextColor(Color.RED);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFileFromSelectedURI();
            } else {
                Toast.makeText(this, R.string.cannot_read_image, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPdfFileTitle();
            } else {
                Toast.makeText(this, R.string.can_not_read_file, Toast.LENGTH_SHORT).show();
                pdfFileUri = null;
            }
        }
    }

    private void readFileFromSelectedURI() {
        Cursor cursor = getContentResolver().query(selectedImageUri,
                new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(0);
            cursor.close();
            imageFile = new File(imagePath);
            Bitmap image = BitmapFactory.decodeFile(imagePath);
            bookIV.setImageBitmap(image);
        }
    }

    private Book book;
    @OnClick(R.id.savechangesB)
     void updateBookData() {
        Utilities.showLoadingDialog(this, Color.WHITE);
        String title = bookTitleET.getText().toString();
        String description = bookDescET.getText().toString();
        double price = Double.parseDouble(bookPriceET.getText().toString());
        String date = bookDateB.getText().toString();

        book.setDate(date);
        book.setTitle(title);
        book.setDescription(description);
        book.setPrice(price);
        book.setPdfTitle(addPdfFileB.getText().toString());

        if (pdfFileUri != null)
            uploadBookPdfFile(pdfFileUri);
        else if(imageFile != null)
            uploadBookImage();
        else
            saveBookData();

    }




    private void uploadBookPdfFile(Uri pdfFileUri) {
        FirebaseStorage.getInstance()
                .getReference()
                .child(Constants.BOOK_PDF_FOLDER + book.getId() + ".pdf")
                .putFile(pdfFileUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            String pdfUrl = task.getResult().getDownloadUrl().toString();
                            book.setPdfUrl(pdfUrl);
                        }
                        uploadBookImage();
                    }
                });
    }

    private void uploadBookImage() {
        Uri photoURI = FileProvider.getUriForFile(EditBookActivity.this,
                "com.hamza.alif.bookstore.Fileprovider", imageFile);
        FirebaseStorage.getInstance()
                .getReference()
                .child(Constants.BOOK_IMAGES_FOLDER + book.getId() + ".jpg")
                .putFile(photoURI)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            String imageUrl = task.getResult().getDownloadUrl().toString();
                            book.setImageUrl(imageUrl);
                        }
                        saveBookData();
                    }
                });
    }

    private void saveBookData() {
        FirebaseDatabase.getInstance()
                .getReference(Constants.REF_BOOK)
                .child(book.getId())
                .setValue(book)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Utilities.dismissLoadingDialog();
                            finish();
                        } else {
                            Toast.makeText(EditBookActivity.this,
                                    R.string.error_in_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @OnClick(R.id.add_pdf_fileB)
    public void add_pdf() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("application/pdf");
        startActivityForResult(i, PICK_FILE_REQUEST_CODE);
    }

    @OnClick(R.id.book_dateB)
    public void addbook_Date() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        bookDateB.setText(dayOfMonth + " / " + (month + 1) + " / " + year);
    }

}
