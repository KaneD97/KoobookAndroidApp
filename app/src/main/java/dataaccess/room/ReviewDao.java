package dataaccess.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import entities.Review;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert
    void insertReview(Review... review);

    @Query("SELECT * FROM Review WHERE book_bookId= :bookId")
    List<Review> getReviews(int bookId);

    @Delete
    void deleteReview(Review review);
}
