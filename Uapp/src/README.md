# ModernLibraryApp (Java Swing)

Aplikasi perpustakaan sederhana berbasis **Java Swing** yang memenuhi spesifikasi:
- Minimal 4 halaman: Dashboard, List Buku, Input Buku, History
- CRUD Buku (Create/Read/Update/Delete)
- Pinjam & Kembalikan buku
- Tenggat 7 hari + denda Rp 2.000/hari terlambat
- Persistensi data (File Handling) ke `.txt` (tidak hilang saat aplikasi ditutup)
- Sorting & Searching pada tabel

## Requirements
- Java 17+ disarankan
- IntelliJ IDEA

## Cara Menjalankan
1. Buka project di IntelliJ
2. Pastikan **Working directory**:
    - Run → Edit Configurations… → Working directory = `$PROJECT_DIR$`
3. Jalankan class:
    - `GUI.ModernLibraryApp`

## Lokasi File Data
Data tersimpan di folder project:
- `data/books.txt`
- `data/loans.txt`

## Format File
### books.txt
`id|title|author|year|stockTotal|stockAvail`

### loans.txt
`trxId|bookId|bookTitle|borrower|borrowDate|dueDate|returnDate|status|fine`

## Fitur Utama
- **Dashboard**
    - Total buku (eksemplar)
    - Jumlah transaksi yang sedang dipinjam
- **List Buku**
    - Tabel data
    - Search & Sort
    - Tambah/Edit/Hapus
    - Pinjam buku
- **Input Buku**
    - Form tambah/edit buku
    - Validasi input angka & kosong
- **History**
    - List transaksi pinjam/kembali
    - Filter BORROWED/RETURNED
    - Kembalikan + denda otomatis

