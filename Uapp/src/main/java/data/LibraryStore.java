package data;

import model.Book;
import model.Loan;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class LibraryStore {

    private final Path booksFile;
    private final Path loansFile;

    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();

    public LibraryStore(Path booksFile, Path loansFile) {
        this.booksFile = booksFile;
        this.loansFile = loansFile;
    }

    public List<Book> getBooks() { return books; }
    public List<Loan> getLoans() { return loans; }

    public void load() throws Exception {
        ensureParent(booksFile);
        ensureParent(loansFile);

        books.clear();
        loans.clear();

        if (Files.exists(booksFile)) {
            for (String line : Files.readAllLines(booksFile, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank()) continue;
                // id|title|author|year|stockTotal|stockAvail
                String[] p = line.split("\\|", -1);
                if (p.length < 6) continue;

                books.add(new Book(
                        p[0], p[1], p[2],
                        Integer.parseInt(p[3]),
                        Integer.parseInt(p[4]),
                        Integer.parseInt(p[5])
                ));
            }
        }

        if (Files.exists(loansFile)) {
            for (String line : Files.readAllLines(loansFile, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank()) continue;

                // format baru:
                // trxId|bookId|bookTitle|borrower|borrowDate|dueDate|returnDate|status|fine
                // format lama (kompatibel):
                // trxId|bookId|bookTitle|borrower|borrowDate|returnDate|status

                String[] p = line.split("\\|", -1);

                if (p.length >= 9) {
                    LocalDate borrowDate = LocalDate.parse(p[4]);
                    LocalDate dueDate = LocalDate.parse(p[5]);
                    LocalDate returnDate = p[6].isBlank() ? null : LocalDate.parse(p[6]);
                    String status = p[7];
                    long fine = p[8].isBlank() ? 0 : Long.parseLong(p[8]);

                    loans.add(new Loan(p[0], p[1], p[2], p[3], borrowDate, dueDate, returnDate, status, fine));
                } else if (p.length >= 7) {
                    LocalDate borrowDate = LocalDate.parse(p[4]);
                    LocalDate dueDate = borrowDate.plusDays(7);
                    LocalDate returnDate = p[5].isBlank() ? null : LocalDate.parse(p[5]);
                    String status = p[6];
                    loans.add(new Loan(p[0], p[1], p[2], p[3], borrowDate, dueDate, returnDate, status, 0));
                }
            }
        }
    }

    public void save() throws Exception {
        ensureParent(booksFile);
        ensureParent(loansFile);

        List<String> bookLines = new ArrayList<>();
        for (Book b : books) {
            bookLines.add(String.join("|",
                    b.getId(),
                    safe(b.getTitle()),
                    safe(b.getAuthor()),
                    String.valueOf(b.getYear()),
                    String.valueOf(b.getStockTotal()),
                    String.valueOf(b.getStockAvail())
            ));
        }
        Files.write(booksFile, bookLines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        List<String> loanLines = new ArrayList<>();
        for (Loan l : loans) {
            loanLines.add(String.join("|",
                    l.getTrxId(),
                    l.getBookId(),
                    safe(l.getBookTitle()),
                    safe(l.getBorrower()),
                    l.getBorrowDate().toString(),
                    l.getDueDate().toString(),
                    l.getReturnDate() == null ? "" : l.getReturnDate().toString(),
                    l.getStatus(),
                    String.valueOf(l.getFine())
            ));
        }
        Files.write(loansFile, loanLines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public Book findBook(String id) {
        for (Book b : books) {
            if (b.getId().equalsIgnoreCase(id)) return b;
        }
        return null;
    }

    public Loan findLoan(String trxId) {
        for (Loan l : loans) {
            if (l.getTrxId().equalsIgnoreCase(trxId)) return l;
        }
        return null;
    }

    public String nextBookId() {
        int max = 0;
        for (Book b : books) {
            if (b.getId().startsWith("B")) {
                try { max = Math.max(max, Integer.parseInt(b.getId().substring(1))); }
                catch (Exception ignored) {}
            }
        }
        return String.format("B%04d", max + 1);
    }

    public String nextLoanId() {
        int max = 0;
        for (Loan l : loans) {
            if (l.getTrxId().startsWith("T")) {
                try { max = Math.max(max, Integer.parseInt(l.getTrxId().substring(1))); }
                catch (Exception ignored) {}
            }
        }
        return String.format("T%05d", max + 1);
    }

    private static void ensureParent(Path p) throws Exception {
        Path parent = p.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("|", "/").trim();
    }
}
