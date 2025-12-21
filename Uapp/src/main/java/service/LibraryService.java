package service;

import data.LibraryStore;
import model.Book;
import model.Loan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LibraryService {

    public static final int LOAN_DAYS = 7;
    public static final long FINE_PER_DAY = 2000;

    private final LibraryStore store;

    public LibraryService(LibraryStore store) {
        this.store = store;
    }

    public LibraryStore getStore() { return store; }

    // ---------- persistence ----------
    public void load() throws Exception { store.load(); }
    public void save() throws Exception { store.save(); }

    // ---------- stats ----------
    public int totalCopies() {
        int sum = 0;
        for (Book b : store.getBooks()) sum += b.getStockTotal();
        return sum;
    }

    public long borrowedCount() {
        return store.getLoans().stream().filter(l -> Loan.BORROWED.equals(l.getStatus())).count();
    }

    // ---------- CRUD ----------
    public Book addBook(String title, String author, int year, int total) {
        validateNonEmpty(title, "Judul");
        validateNonEmpty(author, "Penulis");
        if (total <= 0) throw new RuntimeException("Total Buku harus > 0.");

        String id = store.nextBookId();
        Book b = new Book(id, sanitize(title), sanitize(author), year, total, total);
        store.getBooks().add(b);
        return b;
    }

    public void updateBook(String id, String title, String author, int year, int total) {
        Book b = requireBook(id);
        validateNonEmpty(title, "Judul");
        validateNonEmpty(author, "Penulis");
        if (total <= 0) throw new RuntimeException("Total Buku harus > 0.");

        int borrowed = b.getStockTotal() - b.getStockAvail();
        if (total < borrowed) throw new RuntimeException("Total Buku tidak boleh < jumlah dipinjam (" + borrowed + ").");

        b.setTitle(sanitize(title));
        b.setAuthor(sanitize(author));
        b.setYear(year);
        b.setStockTotal(total);
        b.setStockAvail(total - borrowed);
    }

    public void deleteBook(String id) {
        // tidak boleh hapus jika sedang dipinjam
        for (Loan l : store.getLoans()) {
            if (l.getBookId().equalsIgnoreCase(id) && Loan.BORROWED.equals(l.getStatus())) {
                throw new RuntimeException("Tidak bisa hapus: buku masih dipinjam.");
            }
        }
        store.getBooks().removeIf(b -> b.getId().equalsIgnoreCase(id));
    }

    // ---------- borrow/return ----------
    public Loan borrowBook(String bookId, String borrower) {
        Book b = requireBook(bookId);
        validateNonEmpty(borrower, "Nama Peminjam");

        if (b.getStockAvail() <= 0) throw new RuntimeException("Tersedia 0. Tidak bisa dipinjam.");
        b.setStockAvail(b.getStockAvail() - 1);

        String trxId = store.nextLoanId();
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(LOAN_DAYS);

        Loan loan = new Loan(trxId, b.getId(), b.getTitle(), sanitize(borrower),
                borrowDate, dueDate, null, Loan.BORROWED, 0);

        store.getLoans().add(loan);
        return loan;
    }

    public Loan returnBook(String trxId) {
        Loan l = requireLoan(trxId);
        if (Loan.RETURNED.equals(l.getStatus())) throw new RuntimeException("Transaksi sudah RETURNED.");

        Book b = requireBook(l.getBookId());
        if (b.getStockAvail() >= b.getStockTotal()) throw new RuntimeException("Stok tersedia sudah penuh (data tidak konsisten).");

        LocalDate now = LocalDate.now();
        b.setStockAvail(b.getStockAvail() + 1);

        l.setStatus(Loan.RETURNED);
        l.setReturnDate(now);

        long fine = calculateFine(l.getDueDate(), now);
        l.setFine(fine);
        return l;
    }

    public long calculateFine(LocalDate dueDate, LocalDate actualReturnDate) {
        if (actualReturnDate.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, actualReturnDate);
            return daysLate * FINE_PER_DAY;
        }
        return 0;
    }

    public long currentFineIfLate(Loan loan, LocalDate today) {
        if (Loan.BORROWED.equals(loan.getStatus())) {
            return calculateFine(loan.getDueDate(), today);
        }
        return loan.getFine();
    }

    // ---------- sorting ----------
    public List<Book> getBooksSorted(String mode) {
        List<Book> copy = new ArrayList<>(store.getBooks());

        if ("Judul (A-Z)".equals(mode)) {
            copy.sort(Comparator.comparing(b -> b.getTitle().toLowerCase()));
        } else if ("Tahun (Terbaru)".equals(mode)) {
            copy.sort((a, b) -> Integer.compare(b.getYear(), a.getYear()));
        } else if ("Tersedia (Banyak)".equals(mode)) {
            copy.sort((a, b) -> Integer.compare(b.getStockAvail(), a.getStockAvail()));
        }
        return copy;
    }

    public List<Loan> getLoans() {
        return store.getLoans();
    }

    // ---------- helpers ----------
    private Book requireBook(String id) {
        Book b = store.findBook(id);
        if (b == null) throw new RuntimeException("Buku tidak ditemukan: " + id);
        return b;
    }

    private Loan requireLoan(String trxId) {
        Loan l = store.findLoan(trxId);
        if (l == null) throw new RuntimeException("Transaksi tidak ditemukan: " + trxId);
        return l;
    }

    private void validateNonEmpty(String val, String field) {
        if (val == null || val.trim().isEmpty()) throw new RuntimeException(field + " tidak boleh kosong.");
    }

    private String sanitize(String s) {
        return s.trim().replace("|", "/");
    }
}
