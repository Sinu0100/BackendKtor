# API Documentation - Sistem Informasi Prodi D3 Teknik Listrik

Dokumen ini berisi spesifikasi *deep-dive* API untuk Backend Sistem Informasi Program Studi D3 Teknik Listrik, berbasis Ktor dan PostgreSQL (Supabase). Semua *endpoints* telah dipetakan dengan tepat mengikuti implementasi kode (termasuk penggunaan `multipart/form-data` untuk mendukung *upload* file maupun gambar yang sebelumnya belum tertulis).

## 1. Base URL & Autentikasi
*   **Base URL**: Semua *endpoint* diasumsikan memiliki awalan (misalnya `/api/v1` jika diatur pada Routing, atau langsung pada *root* sesuai implementasi `route()`).
*   **Autentikasi**: Menggunakan JWT. Tambahkan *header* berikut untuk *endpoint* yang membutuhkan akses Dosen atau Admin:
    ```http
    Authorization: Bearer <token_jwt>
    ```

## 2. Standar Respons (ApiResponse)
Semua respons mematuhi struktur standar berikut:

### Respons Sukses (200 OK / 201 Created)
```json
{
  "success": true,
  "message": "Pesan sukses atau keterangan",
  "data": { ... } // Berupa Object, Array, atau kosong
}
```

### Respons Gagal (400, 401, 403, 404, 500)
```json
{
  "success": false,
  "message": "Terjadi kesalahan atau detail error dari validasi",
  "data": null,
  "errors": null 
}
```
*Catatan: Parameter `errors` (array detail field) akan terisi jika terdapat validasi *RequestValidation* dari Ktor, namun kebanyakan *error* dikembalikan langsung sebagai `message` berkat validasi internal Controller.*

---

## 3. Daftar Endpoints

> **PENTING: Content-Type**
> Perhatikan dengan baik kolom **Content-Type**! Sebagian besar *endpoint* yang mengelola data Master dan memiliki kemampuan unggah media menggunakan `multipart/form-data`, bukan `application/json`!

### A. Autentikasi

#### `POST /auth/login`
- **Akses**: Public (Terkena Rate Limiting)
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  { "email": "user@example.com", "password": "password123" }
  ```
- **Response Data** (`LoginResponse`):
  ```json
  { "token": "eyJhb...", "email": "user@example.com", "role": "admin", "expiresAt": 1713837493 }
  ```

---

### B. Dosen

#### `GET /dosen` & `GET /dosen/{id}`
- **Akses**: Public
- **Response**: Array/Object `DosenResponse` yang memuat relasi Keahlian.

#### `GET /dosen/me`
- **Akses**: Authenticated (Admin/Dosen)
- **Response**: Profil diri berdasarkan token `DosenResponse`.

#### `POST /dosen`
- **Akses**: Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `nama` (Text, Wajib)
  - `nidn` (Text, Opsional)
  - `jabatan_fungsional` (Text, Opsional)
  - `pangkat_golongan` (Text, Opsional)
  - `email` (Text, Opsional)
  - `no_hp` (Text, Opsional)
  - `password` (Text, Wajib diisi untuk pembuatan akun)
  - `file` (File, Opsional) -> Foto Dosen

#### `PUT /dosen/{id}`
- **Akses**: Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**: Sama dengan POST, namun **tanpa** `password`. Hanya dikirim *field* yang ingin diubah.

#### `DELETE /dosen/{id}`
- **Akses**: Admin

---

### C. Keahlian

#### `GET /keahlian`
- **Akses**: Public
- **Response**: Array master keahlian.

#### `POST /admin/keahlian`
- **Akses**: Admin
- **Content-Type**: `application/json`
- **Request Body**: `{ "nama_keahlian": "IoT" }`

#### `POST /admin/keahlian/assign`
- **Akses**: Admin
- **Content-Type**: `application/json`
- **Deskripsi**: Menghubungkan keahlian dengan dosen tertentu secara paksa.

#### `GET /dosen/keahlian`
- **Akses**: Dosen
- **Deskripsi**: Daftar keahlian dari dosen yang login.

#### `POST /dosen/keahlian`
- **Akses**: Dosen
- **Content-Type**: `application/json`
- **Deskripsi**: Menambah keahlian untuk dosen sendiri.

#### `DELETE /dosen/keahlian/{id}`
- **Akses**: Dosen

---

### D. Publikasi

#### `GET /publikasi`, `GET /publikasi/{id}`, `GET /publikasi/my`
- **Akses**: Public (Kecuali `/my` khusus Dosen)
- **Response**: Menghasilkan `PublikasiResponse`.

#### `POST /publikasi` & `PUT /publikasi/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul` (Text, Wajib)
  - `nama_jurnal_konferensi` (Text, Opsional)
  - `deskripsi` (Text, Opsional)
  - `link_tautan` (Text, Opsional)
  - `tahun` (Text - Angka, Opsional) -> **Validasi**: Harus antara `1900 - 2100`. Jika tidak, akan mengembalikan 400 Bad Request.

#### `DELETE /publikasi/{id}`
- **Akses**: Dosen & Admin

---

### E. Penelitian

#### `GET /penelitian`, `GET /penelitian/{id}`, `GET /penelitian/my`
- **Akses**: Public (Kecuali `/my`)
- **Response**: Mengembalikan `PenelitianResponse` yang mencakup properti anggota peneliti.

#### `POST /penelitian` & `PUT /penelitian/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul` (Text, Wajib)
  - `tahun` (Text - Angka, Opsional) -> **Validasi**: `1900 - 2100`.
  - `deskripsi` (Text, Opsional)

#### `DELETE /penelitian/{id}`
- **Akses**: Dosen & Admin

---

### F. Pengabdian (Kepada Masyarakat)

#### `GET /pengabdian`, `GET /pengabdian/{id}`, `GET /pengabdian/my`
- **Akses**: Public (Kecuali `/my`)
- **Response**: Mengembalikan `PengabdianResponse`.

#### `POST /pengabdian` & `PUT /pengabdian/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul_pengabdian` (Text, Wajib)
  - `deskripsi` (Text, Opsional)
  - `tahun` (Text - Angka, Opsional) -> **Validasi**: `1900 - 2100`.

#### `DELETE /pengabdian/{id}`
- **Akses**: Dosen & Admin

---

### G. Buku Ajar

#### `GET /buku-ajar`, `GET /buku-ajar/{id}`, `GET /buku-ajar/my`
- **Akses**: Public (Kecuali `/my`)
- **Response**: Mengembalikan `BukuAjarResponse`.

#### `POST /buku-ajar` & `PUT /buku-ajar/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul` (Text, Wajib)
  - `tahun` (Text - Angka, Opsional) -> **Validasi**: `1900 - 2100`.
  - `deskripsi` (Text, Opsional)
  - `peran_penulis` (Text, Opsional, default: "Anggota")

#### `DELETE /buku-ajar/{id}`
- **Akses**: Dosen & Admin

---

### H. HKI (Hak Kekayaan Intelektual)

#### `GET /hki`, `GET /hki/{id}`, `GET /hki/my`
- **Akses**: Public (Kecuali `/my` Dosen/Admin)
- **Response**: Mengembalikan `HKIResponse`.

#### `POST /hki` & `PUT /hki/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul_invensi` (Text, Wajib)
  - `inventor` (Text, Opsional)
  - `jenis_hki` (Text, Opsional)
  - `nomor_paten` (Text, Opsional)
  - `tahun` (Text - Angka, Opsional) -> **Validasi**: `1900 - 2100`.
  - `file` (File, Opsional) -> Dokumen sertifikat paten.

#### `DELETE /hki/{id}`
- **Akses**: Dosen & Admin

---

### I. Sertifikat

#### `GET /sertifikat`, `GET /sertifikat/{id}`, `GET /sertifikat/my`
- **Akses**: Public (Kecuali `/my`)
- **Response**: Mengembalikan `SertifikatResponse`.

#### `POST /sertifikat` & `PUT /sertifikat/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul_sertifikat` (atau dikirim dengan key `nama_sertifikat`) (Text, Wajib)
  - `penerbit` (Text, Opsional)
  - `tahun` (Text - Angka, Opsional)
  - `file` (File, Opsional) -> Sertifikat dokumen.

#### `DELETE /sertifikat/{id}`
- **Akses**: Dosen & Admin

---

### J. Fasilitas

#### `GET /fasilitas` & `GET /fasilitas/{id}`
- **Akses**: Public
- **Response**: Mengembalikan `FasilitasResponse` (termasuk list `MediaResponse`).

#### `POST /fasilitas` & `PUT /fasilitas/{id}`
- **Akses**: Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul_fasilitas` atau `nama_fasilitas` (Text, Wajib)
  - `deskripsi` (Text, Opsional)
  - `file` (File, Opsional) -> Foto fasilitas. Bisa menampung unggahan *file bytes*.

#### `DELETE /fasilitas/{id}`
- **Akses**: Admin

---

### K. Jadwal Perkuliahan

#### `GET /jadwal`
- **Akses**: Public
- **Response**: Mengembalikan `JadwalPerkuliahanResponse`.

#### `POST /jadwal` & `PUT /jadwal/{id}`
- **Akses**: Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `nama_jadwal` (Text, Wajib)
  - `tanggal_upload` (Date, Opsional)
  - `file` (File, Wajib pada POST jika mengunggah dokumen baru)

#### `DELETE /jadwal/{id}`
- **Akses**: Admin

---

### L. Statistik Mahasiswa

#### `GET /statistik`
- **Akses**: Public
- **Response**: Mengembalikan `StatistikMahasiswaResponse`.

#### `POST /statistik` & `PUT /statistik/{id}`
- **Akses**: Admin
- **Content-Type**: `application/json`
- **Request Body**:
  ```json
  {
    "tahun": 2024,
    "jumlah_pendaftar": 500,
    "jumlah_diterima": 200,
    "jumlah_lulusan": 190
  }
  ```

#### `DELETE /statistik/{id}`
- **Akses**: Admin

---

### M. Tri Dharma

#### `GET /tri-dharma` & `GET /tri-dharma/{id}`
- **Akses**: Public
- **Response**: Mengembalikan `TriDharmaResponse`.

#### `POST /tri-dharma` & `PUT /tri-dharma/{id}`
- **Akses**: Admin
- **Content-Type**: `multipart/form-data`
- **Form Fields**:
  - `judul` (Text, Wajib)
  - `deskripsi` (Text, Opsional)
  - `tanggal` (Date format string, misal `2024-05-12`, Opsional)
  - `file` (File, Opsional)

#### `DELETE /tri-dharma/{id}`
- **Akses**: Admin

---

## 4. Validasi Utama
Seluruh Controller telah dirancang untuk memeriksa kesalahan utama:
1.  **Field "Judul" Wajib Diisi**: Hampir semua input yang mengandung *judul/nama* memvalidasi input kosong dan melempar `Exception("Judul wajib diisi")` (Terkategori HTTP `400 Bad Request`).
2.  **Rentang Tahun**: Setiap input *tahun* memvalidasi range `1900..2100`. Jika *error*, mengembalikan pesan *Tahun tidak valid*.
3.  **Forbidden Access**: Mencoba mengubah data milik dosen lain atau mengakses API tanpa peran admin akan mengembalikan HTTP `403 Forbidden` (`FORBIDDEN: ...`).
4.  **Format Input**: API yang mengharuskan tipe `multipart/form-data` akan *error* jika menerima tipe lain (contoh: JSON).
