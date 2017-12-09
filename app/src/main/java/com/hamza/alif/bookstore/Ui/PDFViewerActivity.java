package com.hamza.alif.bookstore.Ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.hamza.alif.bookstore.R;
import com.hamza.alif.bookstore.Util.ActivityLancher;
import com.hamza.alif.bookstore.Util.Constants;
import com.hamza.alif.bookstore.data.Book;

public class PDFViewerActivity extends AppCompatActivity {
    //View From Library to display PDF file
    PDFView pdfView;
    //Loading Bar
    ProgressBar progress;

    Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);

        pdfView = (PDFView) findViewById(R.id.pdfView);
        progress = (ProgressBar) findViewById(R.id.progress);

        //Get the Book Object
        book = (Book) getIntent().getSerializableExtra(ActivityLancher.BOOK_KEY);


        //There is a book and now i will download this book pdf from
        //Firebase Storage
        if (book != null) {

            File bookStoreFolder = new File(getFilesDir(), "BookStore");
            bookStoreFolder.mkdir();

            //Get all files inside the book store folder
            File[] files = bookStoreFolder.listFiles();

            //if Files array equals to null or it's length is Zero
            if (files == null || files.length == 0) {
                //Download the file by book id
                downloadBookPDF(book.getId());
            } else {
                boolean isFound = false;
                //Loop over the files array
                for (File file : files) {
                    //Check if the file name matches title
                    if (file.getName().equals(book.getId() + ".pdf")) {
                        //Display the File from local storage so no need to
                        //download the file again
                        displayPDFFile(file);
                        progress.setVisibility(View.GONE);
                        isFound = true;
                        break;
                    }

                }

                //The file not found in folder so download the file now
                if (!isFound) {
                    downloadBookPDF(book.getId());
                }

            }

            //Set the activity title with the book title
            setTitle(book.getTitle());
        }
    }

    private void downloadBookPDF(String bookId) {
        //Go to book store folder
        File bookStoreFolder = new File(getFilesDir(), "BookStore");
        //create if not exists
        bookStoreFolder.mkdir();

        //Create file with book id
        final File localFile = new File(bookStoreFolder, bookId + ".pdf");

        //Go To Selected Book PDF File and download the pdf to the local file
        FirebaseStorage.getInstance()
                .getReference(Constants.BOOK_PDF_FOLDER).child(bookId + ".pdf").getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        //hide progress bar
                        progress.setVisibility(View.GONE);
                        //View downloaded file
                        displayPDFFile(localFile);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progress.setVisibility(View.GONE);
                //If file not exists
                Toast.makeText(PDFViewerActivity.this, "Error In Download PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPDFFile(File file) {
        pdfView.fromFile(file)
                //To Show Page Number On Scroll
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

}