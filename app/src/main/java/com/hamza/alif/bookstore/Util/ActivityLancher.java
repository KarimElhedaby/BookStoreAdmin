package com.hamza.alif.bookstore.Util;

import android.content.Context;
import android.content.Intent;

import com.hamza.alif.bookstore.Ui.AddBookActivity;
import com.hamza.alif.bookstore.Ui.BooksActivity;
import com.hamza.alif.bookstore.Ui.EditBookActivity;
import com.hamza.alif.bookstore.Ui.EditPublisherActivity;
import com.hamza.alif.bookstore.Ui.LoginActivity;
import com.hamza.alif.bookstore.Ui.PDFViewerActivity;
import com.hamza.alif.bookstore.Ui.RegisterActivity;
import com.hamza.alif.bookstore.Ui.EditProfileActivity;
import com.hamza.alif.bookstore.data.Book;
import com.hamza.alif.bookstore.data.Publisher;

/**
 * Created by karim pc on 9/29/2017.
 */

public final class ActivityLancher {
    public static final String BOOK_KEY = "book";
    public static final String publisher_KEY = "publisher";


    public static void openLoginActivity(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));

    }

    public static void openBooksActivity(Context context) {
        context.startActivity(new Intent(context, BooksActivity.class));

    }
    public static void openRegisterActivity(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));

    }
    public static void openAddBookActivity(Context context){
        Intent i = new Intent(context, AddBookActivity.class);
        context.startActivity(i);
    }
    public static void openEditBookActivity(Context context, Book book){
        Intent i = new Intent(context, EditBookActivity.class);
        i.putExtra(BOOK_KEY, book);
        context.startActivity(i);
    }

    public static void openEditpublisherFragment(Context context, Publisher publisher){
        Intent i = new Intent(context, EditPublisherActivity.class);
        i.putExtra("publisher_KEY", publisher);
        context.startActivity(i);
    }

    public static void openEditPublisherActivity(Context context, Publisher publisher){
        Intent i = new Intent(context, EditProfileActivity.class);
        i.putExtra("publisher_KEY", publisher);
        context.startActivity(i);
    }
    public static void openPDFViewerActivity(Context context, Book book){
        Intent i = new Intent(context, PDFViewerActivity.class);
        i.putExtra(BOOK_KEY, book);
        context.startActivity(i);
    }


}
