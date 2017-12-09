package com.hamza.alif.bookstore.Ui;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hamza.alif.bookstore.R;
import com.hamza.alif.bookstore.Util.ActivityLancher;
import com.hamza.alif.bookstore.Util.Constants;
import com.hamza.alif.bookstore.Util.Utilities;
import com.hamza.alif.bookstore.adapter.BookAdapter;
import com.hamza.alif.bookstore.data.Book;
import com.hamza.alif.bookstore.data.Publisher;

import java.util.ArrayList;

public class BooksActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , BookAdapter.OnBookClickListener,
        View.OnClickListener  {
    FloatingActionButton fab;
    private DrawerLayout drawer;
    private FirebaseAuth auth;
    private Publisher publisher;
    private ImageView profilePicIV;
    private TextView publisherNameTV, publisherEmailTV;
    private FirebaseUser user;
    private RecyclerView recyclerMyBooks;
    private BookAdapter adapter;
    private ArrayList<Book> books;

    private MenuItem searchMenuItem;

    private LinearLayout linearProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        books = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityLancher.openAddBookActivity(BooksActivity.this);
                searchMenuItem.collapseActionView();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        linearProfile= (LinearLayout) navigationView.getHeaderView(0);
        profilePicIV = linearProfile.findViewById(R.id.iv_profile);
        publisherNameTV = linearProfile.findViewById(R.id.publisher_nameTV);
        publisherEmailTV = linearProfile.findViewById(R.id.publisher_mailTV);


        recyclerMyBooks = (RecyclerView) findViewById(R.id.recycler_my_books);
        adapter = new BookAdapter(this, books);
        recyclerMyBooks.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerMyBooks.setAdapter(adapter);

        linearProfile.setOnClickListener(this);

        showPublisherInfoAndBooks();

    }
    private void showPublisherInfoAndBooks() {
        if (Utilities.isNetworkAvailable(this)) {
//            if( adapter.getItemCount()!=0)
         Utilities.showLoadingDialog(this, Color.WHITE);
            getPublisherInfo();
            getBooks();
        }
        else {
            Snackbar.make(recyclerMyBooks, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPublisherInfoAndBooks();
                        }
                    })
                    .setActionTextColor(Color.WHITE)
                    .show();
        }
    }



    private void getBooks() {
        FirebaseDatabase
                .getInstance()
                .getReference(Constants.REF_BOOK)
                .orderByChild(Constants.PUBLISHER_ID)
                .equalTo(user.getUid())
                .addChildEventListener(new ChildEventListener() {// listen to child
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevKey) {
                        Utilities.dismissLoadingDialog();
                        Book book = dataSnapshot.getValue(Book.class);
                        if (book != null) {
                            Log.d("ListenerBooks", book.getTitle());

                            adapter.addBook(book);
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d("ListenerBooks", "Changed");
                        Book book = dataSnapshot.getValue(Book.class);
                        if (book != null) {
                            Log.d("ListenerBooks", book.getTitle());
                            for (int i = 0; i < books.size(); i++) {
                                if (books.get(i).getId().equals(book.getId())) {
                                    adapter.setBook(i, book);
                                    return;
                                }
                            }
                        }

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d("ListenerBooks", "Removed");
                        Book book = dataSnapshot.getValue(Book.class);
                        adapter.removeBook(book);
                    }

                    @Override

                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d("ListenerBooks", "Moved");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("ListenerBooks", "Error");
                    }
                });
    }


    private void getPublisherInfo() {
        if (user != null) {
            FirebaseDatabase
                    .getInstance()
                    .getReference(Constants.REF_PUBLISHER)
                    .child(user.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            publisher = dataSnapshot.getValue(Publisher.class);
                            if (publisher != null) {
                                publisherEmailTV.setText(publisher.getEmail());
                                publisherNameTV.setText(publisher.getName());
                                Glide.with(BooksActivity.this)
                                        .load(publisher.getImageUrl())
                                        .into(profilePicIV);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.books, menu);
        // permision from system
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        // select the item
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // info about application  //package
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(BooksActivity.this, "Done", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
               adapter.filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
                 editdataPublisher();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {

            logout();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        auth.signOut();
                        ActivityLancher.openLoginActivity(BooksActivity.this);
                        finish();
                    }
                }).show();

    }

    private void editdataPublisher() {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.edit, EditPublisherActivity.with(publisher))
                .addToBackStack(null)
                .commit();


    }

    @Override
    public void onBookClick(Book book) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.edit, BookDetailsFragment.with(book))
                .addToBackStack(null)
                .commit();

    }
    @Override
    public void editBook(Book book) {
        ActivityLancher.openEditBookActivity(this, book);
        searchMenuItem.collapseActionView();
    }


    @Override
    public void onClick(View view) {

        ActivityLancher.openEditPublisherActivity(this, publisher);
    }
}
