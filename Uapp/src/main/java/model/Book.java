package model;

public class Book {
    private String id;
    private String title;
    private String author;
    private int year;
    private int stockTotal;
    private int stockAvail;

    public Book(String id, String title, String author, int year, int stockTotal, int stockAvail) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.stockTotal = stockTotal;
        this.stockAvail = stockAvail;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public int getStockTotal() { return stockTotal; }
    public int getStockAvail() { return stockAvail; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setYear(int year) { this.year = year; }
    public void setStockTotal(int stockTotal) { this.stockTotal = stockTotal; }
    public void setStockAvail(int stockAvail) { this.stockAvail = stockAvail; }
}
