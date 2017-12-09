package com.hamza.alif.bookstore.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.hamza.alif.bookstore.R;
import com.hamza.alif.bookstore.Util.ActivityLancher;
import com.hamza.alif.bookstore.Util.Constants;
import com.hamza.alif.bookstore.data.Book;

import java.util.ArrayList;

/**
 * Created by karim pc on 11/8/2017.
 */


public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private ArrayList<Book> books;
    private ArrayList<Book> filterBooks;

    private Context context;
    private OnBookClickListener onBookActionListener;
    private static final int ITEM_DELETE = 1;
    private static final int ITEM_EDIT = 2;

    public BookAdapter(Context context, ArrayList<Book> books) {
        this.books = books;
        filterBooks = new ArrayList<>();
        filterBooks.addAll(books);
        this.context = context;
        if(context instanceof OnBookClickListener){
            onBookActionListener = (OnBookClickListener) context;
        }else{
            throw new RuntimeException("Context must implement OnBookClickListener");
        }

    }


    public void addBook(Book book) {
        books.add(book);
        filterBooks.add(book);
        notifyDataSetChanged();
    }

    public void removeBook(Book book) {
        books.remove(book);
        filterBooks.remove(book);
        notifyDataSetChanged();
    }

    public void setBook(int position, Book book) {
        books.set(position, book);
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_book, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.book = filterBooks.get(position);
        holder.tvTitle.setText(holder.book.getTitle());
        holder.tvPrice.setText(String.valueOf(holder.book.getPrice()));
        Glide.with(context).load(holder.book.getImageUrl()).into(holder.ivBook);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBookActionListener.onBookClick(holder.book);
            }
        });

        holder.ivOverFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(holder);
            }
        });

    }

    private void showPopupMenu(final ViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, holder.ivOverFlow);
        popupMenu.getMenu().add(1,ITEM_DELETE,1,"Delete");
        popupMenu.getMenu().add(1,ITEM_EDIT,2,"Edit");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == ITEM_DELETE){
                    showConfirmationDeleteDialog(holder.book.getId());
                    return true;
                }else if(item.getItemId() == ITEM_EDIT){
                    onBookActionListener.editBook(holder.book);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
    private void showConfirmationDeleteDialog(final String id) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Book")
                .setMessage("Are u sure ?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBookById(id);
                    }
                })
                .show();
    }

    private void deleteBookById(final String id) {
        FirebaseDatabase
                .getInstance()
                .getReference(Constants.REF_BOOK)
                .child(id)
                .removeValue();

        FirebaseStorage
                .getInstance()
                .getReference(Constants.BOOK_IMAGES_FOLDER)
                .child(id + ".jpg")
                .delete();

        FirebaseStorage
                .getInstance()
                .getReference(Constants.BOOK_PDF_FOLDER)
                .child(id + ".pdf")
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                } else {
                    if (task.getException() != null)
                        task.getException().printStackTrace();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return filterBooks.size();
    }



    public void filter(String text) {
        filterBooks.clear();
        if (text.isEmpty()) {
            filterBooks.addAll(books);
        } else {
            for (Book book : books) {
                if (book.getTitle().toLowerCase().trim().startsWith(text.toLowerCase().trim())) {
                    filterBooks.add(book);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvPrice, tvTitle;
        ImageView ivBook, ivOverFlow;
        Book book;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvPrice = view.findViewById(R.id.priceTV);
            tvTitle = view.findViewById(R.id.titleitemTV);
            ivOverFlow = view.findViewById(R.id.overflow_menuIV);
            ivBook = view.findViewById(R.id.bookitemIV);
        }
    }
    public interface OnBookClickListener{
        void onBookClick(Book book);
        void editBook(Book book);


    }
}