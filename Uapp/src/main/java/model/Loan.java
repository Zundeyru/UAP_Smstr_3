package model;

import java.time.LocalDate;

public class Loan {
    public static final String BORROWED = "BORROWED";
    public static final String RETURNED = "RETURNED";

    private String trxId;
    private String bookId;
    private String bookTitle;
    private String borrower;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // nullable
    private String status;        // BORROWED / RETURNED
    private long fine;            // final fine (set on return)

    public Loan(String trxId, String bookId, String bookTitle, String borrower,
                LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, String status, long fine) {
        this.trxId = trxId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrower = borrower;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fine = fine;
    }

    public String getTrxId() { return trxId; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getBorrower() { return borrower; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public String getStatus() { return status; }
    public long getFine() { return fine; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; } // untuk testing
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setStatus(String status) { this.status = status; }
    public void setFine(long fine) { this.fine = fine; }
}
