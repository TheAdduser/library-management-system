package pl.wszib.lms.db;

import lombok.Getter;
import pl.wszib.lms.App;
import pl.wszib.lms.model.Book;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookRepository {
    @Getter
    private final ArrayList<Book> books = new ArrayList<>();
    Scanner scanner = new Scanner(System.in);
    public BookRepository(){
        this.books.add(new Book(1234,"TestLeasedBook", "Test Author",
                LocalDate.now(),
                LocalDate.now().plusWeeks(2),true,"Test User"));
        this.books.add(new Book(1234,"TestBook", "Test Author 2"));
    }


    public BookRepository(List<Book> books) {
        this.books.addAll(books);
    }

    public boolean testLeaseBook(long isbn){
        for(Book book : this.books){
            if(book.getIsbn() == isbn && !book.getLeaseStatus()){
                book.setLeaseStatus(true);
                return true;
            }
        }
        return false;
    }

    public boolean testAddBook(long isbn,String title, String author){
        Book newBook = new Book(isbn, title, author);
        return books.contains(newBook);
    }

    public boolean testReturnBook(long isbn){
        for(Book book : this.books){
            if(book.getIsbn() == isbn && book.getLeaseStatus()){
                book.setLeaseStatus(false);
                book.setUserName(null);
                book.setLeaseStartDate(null);
                book.setLeaseEndDate(null);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Book> getBooksFromDB() {
        ArrayList<Book> result = new ArrayList<>();
        try {
            String sql = "SELECT * FROM tbook";
            PreparedStatement preparedStatement = App.connection.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setIsbn(rs.getLong("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setLeaseStartDate(rs.getDate("leaseStartDate") != null ?
                        rs.getDate("leaseStartDate").toLocalDate() : null);
                book.setLeaseEndDate(rs.getDate("leaseEndDate") != null ?
                        rs.getDate("leaseEndDate").toLocalDate() : null);
                book.setLeaseStatus(rs.getBoolean("leaseStatus"));
                book.setUserName(rs.getString("usersName"));

                result.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean lease(long isbn){
        try{
            String sql = "SELECT * FROM tbook WHERE isbn = ?";
            PreparedStatement preparedStatement = App.connection.prepareStatement(sql);
            preparedStatement.setLong(1, isbn);

            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                boolean lease = rs.getBoolean("leaseStatus");
                if(!lease){
                    System.out.println("Enter your name: ");
                    String usersName = scanner.nextLine();
                    int id = rs.getInt("id");
                    String updateSql = "UPDATE tbook SET leaseStatus = ?, usersName = ?, leaseStartDate = ?, leaseEndDate = ? WHERE id = ?";
                    PreparedStatement updatePreparedStatement = App.connection.prepareStatement(updateSql);
                    updatePreparedStatement.setBoolean(1, true);
                    updatePreparedStatement.setString(2, usersName);
                    updatePreparedStatement.setDate(3, Date.valueOf(LocalDate.now()));
                    updatePreparedStatement.setDate(4, Date.valueOf(LocalDate.now().plusWeeks(2)));
                    updatePreparedStatement.setInt(5, id);

                    updatePreparedStatement.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean returnBook(long isbn){
        try{
            String sql = "SELECT * FROM tbook WHERE isbn = ?";
            PreparedStatement preparedStatement = App.connection.prepareStatement(sql);
            preparedStatement.setLong(1, isbn);

            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                boolean lease = rs.getBoolean("leaseStatus");
                if(lease){
                    int id = rs.getInt("id");
                    String updateSql = "UPDATE tbook SET leaseStatus = ?, usersName = ?, leaseStartDate = ?, leaseEndDate = ? WHERE id = ?";
                    PreparedStatement updatePreparedStatement = App.connection.prepareStatement(updateSql);
                    updatePreparedStatement.setBoolean(1, false);
                    updatePreparedStatement.setNull(2, Types.VARCHAR);
                    updatePreparedStatement.setNull(3, Types.DATE);
                    updatePreparedStatement.setNull(4, Types.DATE);
                    updatePreparedStatement.setInt(5, id);

                    updatePreparedStatement.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBook(){
        System.out.println("Enter ISBN: ");
        long isbn = scanner.nextLong();
        System.out.println("Enter title: ");
        String title = scanner.nextLine();
        System.out.println("Enter author: ");
        String author = scanner.nextLine();
        scanner.nextLine();
        try {
            String sql = "INSERT INTO tbook (isbn, title, author, leaseStartDate, leaseEndDate, leaseStatus, usersName) VALUES (?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = App.connection.prepareStatement(sql);
            preparedStatement.setLong(1, isbn);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, author);
            preparedStatement.setNull(4, Types.DATE);
            preparedStatement.setNull(5, Types.DATE);
            preparedStatement.setBoolean(6, false);
            preparedStatement.setNull(7, Types.VARCHAR);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void saveBook(Book book)  {
        try {
            String sql = "INSERT INTO tbook (isbn, title, author, leaseStartDate, leaseEndDate, leaseStatus, usersName) VALUES (?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = App.connection.prepareStatement(sql);

            preparedStatement.setLong(1, book.getIsbn());
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.setString(3, book.getAuthor());
            if (book.getLeaseStartDate() != null) {
                preparedStatement.setDate(4, java.sql.Date.valueOf(book.getLeaseStartDate()));
            } else {
                preparedStatement.setNull(4, java.sql.Types.DATE);
            }
            if (book.getLeaseEndDate() != null) {
                preparedStatement.setDate(5, java.sql.Date.valueOf(book.getLeaseEndDate()));
            } else {
                preparedStatement.setNull(5, java.sql.Types.DATE);
            }
            preparedStatement.setBoolean(6, book.getLeaseStatus());
            preparedStatement.setString(7, book.getUsersName());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}