package adapters;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;

import com.example.koobookandroidapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import controllers.BookController;
import dataaccess.setup.AppDatabase;
import entities.Book;


//Acquired the knowledge to create this adapter class from https://www.youtube.com/watch?v=CTBiwKlO5IU&feature=youtu.be&t=2160
public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    List<Book> books;
    Book book;
    AppDatabase db;
    Context context;
    BookController bookController;
    FragmentManager fragmentManager;
    String isbn;


    public SearchResultsAdapter(List<Book> books, Context context) {
        this.books = books;
        this.context = context;
        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "production").allowMainThreadQueries().build();
    }

    @NonNull
    @Override
    public SearchResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_result_book, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultsAdapter.ViewHolder holder, final int position) {
        book = books.get(position);
        holder.textview_book_title.setText(book.getTitle());

        //Check if the book has a valid thumbnail url, if it does not then override the thumbnail url string to be that of the default thumbnail url
        String bookThumbnailUrl = book.getThumbnailUrl();
        if(bookThumbnailUrl.matches("")){
            bookThumbnailUrl = "https://i.gyazo.com/a1b02a68b87056cb4469a6bcb6785932.png";
        }
        //Use picasso to download the image using the url and load it into the image view
        Picasso.with(context).load(bookThumbnailUrl).resize(200,400).centerInside().into(holder.imageview_book_thumbnail, new Callback() {
            @Override
            public void onSuccess() {
                Log.i("Picasso", "onSuccess: TRUE");
            }

            @Override
            public void onError() {
                Log.i("Picasso", "onError: TRUE");
            }
        });


        //I stored all the authors of the book into the subtitle attribute for the book such that it was easier to access it, please refer to line 368 in the book controller
        //The subtitle in this case would've been empty anyway so I just used it to access it in this class
        String author = book.getSubtitle();
        if(!(author.length()>3)){
            author = "Author(s) unavailable";
        }
        holder.textview_author.setText(author);


        //When one of the cardviews(rows) is clicked, the isbn is stored in the preference file and then checked to see if the book information has already been saved in the databas
        // if its not then it work with the desktop application to collect all the relevent book information and then retirieving and displaying into the Book review screen
        // "Book review" page will be loaded and will use the stored isbn to display the relevent book information
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBookDataIntoBookReviewPage(v, position);
            }
        });

        holder.button_book_row_view_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBookDataIntoBookReviewPage(v, position);
            }
        });
    }

    public void loadBookDataIntoBookReviewPage(View v, int position){
        fragmentManager = ((FragmentActivity)v.getContext()).getSupportFragmentManager();
        bookController = new BookController(v.getContext());
        isbn = books.get(position).getIsbnNumber();
        bookController.storeBookIsbn(v.getContext(), isbn);
        bookController.searchBook(isbn,"","");
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textview_book_title, textview_author;
        ImageView imageview_book_thumbnail;
        CardView cardView;
        Button button_book_row_view_more;

        public ViewHolder(View itemView) {
            super(itemView);
            textview_book_title = itemView.findViewById(R.id.textview_search_result_book_title);
            textview_author = itemView.findViewById(R.id.textview_search_result_book_authors);
            imageview_book_thumbnail = itemView.findViewById(R.id.imageview_search_result_book_thumbnail);
            cardView = itemView.findViewById(R.id.cardview_id);
            button_book_row_view_more = itemView.findViewById(R.id.button_book_row_view_more);
        }
    }
}
