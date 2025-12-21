package GUI;

public interface AppActions {
    void showDashboard();
    void showBooks();
    void showHistory();
    void openAddBookForm();
    void openEditBookForm(String bookId);

    void refreshAll();
    void setStatus(String msg, boolean error);
}
