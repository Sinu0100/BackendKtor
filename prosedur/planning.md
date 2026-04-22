# Planning Backend Sistem Informasi Prodi

Dokumen ini adalah panduan arsitektur untuk backend Kotlin menggunakan Ktor dengan fokus pada:

* clean architecture
* scalable
* maintainable
* security yang bagus
* performa lancar
* coroutine
* dependency injection
* validasi input
* error message yang jelas
* rate limiting

Backend ini akan dipakai untuk sistem informasi program studi, dengan Supabase sebagai database PostgreSQL, authentication, dan storage.

---

## 1. Tujuan Utama

Sistem harus dibangun dengan prinsip:

* **Ktor sebagai pusat logika backend**
* **Supabase sebagai database, auth, dan storage**
* **business logic tidak ditaruh di frontend**
* **endpoint dibuat sendiri di Ktor**
* **setiap fitur dipisah per modul**
* **akses data diatur dengan role dan ownership**

---

## 2. Aktor Sistem

Ada 3 aktor utama:

### Admin

Bisa melakukan CRUD untuk:

* dosen
* statistik mahasiswa
* jadwal perkuliahan
* tri dharma
* fasilitas

### Dosen

Bisa melakukan CRUD untuk data milik sendiri:

* publikasi
* penelitian
* pengabdian
* buku ajar
* HKI
* sertifikat

### User / Publik

* hanya bisa membaca data publik
* tidak bisa melakukan perubahan data

---

## 3. Prinsip Arsitektur

Gunakan alur ini:

**Route → Controller → Use Case → Repository → Database / Storage**

### Penjelasan layer

* **presentation**: menerima request dan mengirim response
* **application**: menjalankan use case dan orchestration
* **domain**: model inti dan kontrak repository
* **data**: implementasi repository dan datasource
* **infrastructure**: database config, security, DI, config, plugin, helper teknis

### Aturan penting

* Route jangan berisi business logic
* Controller jangan akses database langsung
* Use case jangan tahu detail framework
* Repository hanya urus data access
* Validation dilakukan sebelum logic utama berjalan

---

## 4. Struktur Folder Final

Struktur folder yang wajib diikuti:

```text
├── domain/
│   ├── model/
│   │    ├── Admin.kt
│   │    ├── Dosen.kt
│   │    └── StatistikMahasiswa.kt
│   │
│   └── repository/
│        ├── AdminRepository.kt
│        ├── DosenRepository.kt
│        └── StatistikMahasiswaRepository.kt
│
├── application/
│   └── usecase/
│        ├── auth/
│        │    ├── LoginUseCase.kt
│        │    └── RegisterUseCase.kt
│        │
│        ├── dosen/
│        │    └── ManageDosenUseCase.kt
│        │
│        ├── statistik/
│        │    └── ManageStatistikMahasiswaUseCase.kt
│        │
│        ├── konten/
│        │    └── ManageKontenProdiUseCase.kt
│        │
│        └── media/
│             └── ManageMediaUseCase.kt
│
├── data/
│   ├── repository/
│   │    ├── AdminRepositoryImpl.kt
│   │    ├── DosenRepositoryImpl.kt
│   │    └── StatistikMahasiswaRepositoryImpl.kt
│   │
│   └── datasource/
│        └── DatabaseFactory.kt
│
├── presentation/
│   ├── controller/
│   │    ├── AuthController.kt
│   │    ├── DosenController.kt
│   │    ├── StatistikController.kt
│   │    └── KontenController.kt
│   │
│   ├── routes/
│   │    ├── AuthRoutes.kt
│   │    ├── DosenRoutes.kt
│   │    ├── StatistikRoutes.kt
│   │    └── KontenRoutes.kt
│   │
│   └── dto/
│        ├── request/
│        │    ├── LoginRequest.kt
│        │    ├── DosenRequest.kt
│        │    └── StatistikMahasiswaRequest.kt
│        │
│        └── response/
│             ├── LoginResponse.kt
│             ├── DosenResponse.kt
│             └── StatistikMahasiswaResponse.kt
│
├── infrastructure/
│   ├── database/
│   │    ├── tables/
│   │    │    ├── AdminTable.kt
│   │    │    ├── DosenTable.kt
│   │    │    └── StatistikMahasiswaTable.kt
│   │    │
│   │    └── DatabaseConfig.kt
│   │
│   ├── security/
│   │    ├── JwtService.kt
│   │    └── PasswordHasher.kt
│   │
│   ├── config/
│   │    └── ApplicationConfig.kt
│   │
│   └── di/
│        ├── RepositoryModule.kt
│        ├── UseCaseModule.kt
│        ├── ControllerModule.kt
│        └── AppComponent.kt
│
└── plugins/
     ├── Routing.kt
     ├── Serialization.kt
     ├── Security.kt
     └── Monitoring.kt
```

---

## 5. Penerapan Struktur ke Proyek Ini

Struktur di atas bisa dipakai sebagai pola dasar, tetapi isi file harus disesuaikan dengan domain sistem informasi prodi.

### Mapping domain yang tepat

* `Admin.kt` → representasi admin atau profile admin
* `Dosen.kt` → entitas dosen
* `StatistikMahasiswa.kt` → entitas statistik mahasiswa

### Catatan

Struktur di atas adalah kerangka clean architecture. Nama file contoh harus mengikuti domain asli, bukan template produk atau sensor.

---

## 6. Domain dan Modul Fitur

Modul utama yang harus dibuat:

* auth
* profiles
* dosen
* keahlian
* statistik mahasiswa
* jadwal perkuliahan
* publikasi
* penelitian
* buku ajar
* HKI
* sertifikat
* pengabdian
* fasilitas
* tri dharma
* media

### Prioritas implementasi

1. auth dan profile
2. dosen dan role access
3. data publik read-only
4. CRUD dosen untuk data milik sendiri
5. CRUD admin untuk data master
6. upload dan manajemen storage
7. rate limiting, caching, observability

---

## 7. Clean Architecture Rules

### Domain

Berisi:

* entity/model
* repository interface
* aturan inti yang tidak bergantung ke framework

### Application

Berisi:

* use case
* alur bisnis
* orchestration antar repository
* aturan akses data secara logis

### Data

Berisi:

* repository implementation
* query ke database
* mapper
* datasource

### Presentation

Berisi:

* controller
* route
* request DTO
* response DTO

### Infrastructure

Berisi:

* database config
* security
* dependency injection
* plugin setup
* technical helper

---

## 8. Coroutines

Semua layer yang terlibat dalam request harus mendukung coroutine.

### Aturan coroutine

* endpoint menggunakan `suspend`
* use case menggunakan `suspend`
* repository menggunakan `suspend` bila akses I/O
* jangan blocking thread secara sembarangan
* gunakan dispatcher yang sesuai bila ada kerja berat

Tujuan utamanya:

* response lebih cepat
* thread lebih efisien
* server lebih stabil saat banyak request masuk

---

## 9. Dependency Injection

Pakai DI supaya kode modular dan gampang dites.

### Yang diinjeksi

* repository
* use case
* controller
* database factory / datasource
* jwt service
* password hasher
* storage service
* validator
* rate limiter

### Catatan

* boleh pakai Koin atau DI manual
* pilih yang paling cocok dengan ukuran project
* jangan hardcode dependency di dalam class

---

## 10. Validasi Input

Semua request harus divalidasi sebelum diproses.

### Yang harus dicek

* field wajib
* panjang string
* format email
* format UUID
* range tahun
* tipe file
* ukuran file
* ownership data untuk dosen

### Contoh error validasi

* `judul wajib diisi`
* `tahun harus antara 2000 dan 2100`
* `file harus berupa PDF`
* `akses ditolak, data bukan milik user ini`

---

## 11. Error Handling

Gunakan error response yang konsisten.

### Format sukses

```json
{
  "success": true,
  "message": "Data berhasil diambil",
  "data": {}
}
```

### Format error

```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": [
    {
      "field": "judul",
      "message": "Judul wajib diisi"
    }
  ]
}
```

### Kategori error

* 400: input salah
* 401: belum login
* 403: akses ditolak
* 404: data tidak ditemukan
* 409: data bentrok
* 500: server error

Gunakan `StatusPages` untuk menangani error secara terpusat.

---

## 12. Security

### JWT

* verifikasi token Supabase di backend
* cek signature
* cek issuer
* cek audience bila ada

### Role access

* admin untuk CRUD data master
* dosen hanya untuk data miliknya
* user hanya read-only

### Proteksi tambahan

* HTTPSRedirect
* HSTS
* CORS dibatasi
* rate limit
* secret di environment variable
* jangan log data sensitif

---

## 13. Rate Limiting

Rate limiting wajib dipakai untuk endpoint penting.

### Contoh target

* login: ketat
* upload file: ketat
* CRUD admin: sedang
* public read endpoint: longgar tapi tetap terkontrol

### Tujuan

* mencegah spam
* mencegah abuse
* menjaga performa server

---

## 14. Database dan Supabase

### Aturan utama

* Supabase dipakai sebagai PostgreSQL utama
* Ktor menjadi tempat business logic
* query dibuat di repository
* gunakan HikariCP untuk connection pool

### Prinsip performa

* gunakan indeks yang sudah ada
* pagination untuk list data
* jangan query berulang tanpa perlu
* gunakan transaction hanya saat dibutuhkan

---

## 15. Storage

### Bucket

* `prodi-files`

### Contoh struktur file

* `/dosen/foto/{user_id}`
* `/jadwal/{filename}`
* `/hki/{id}`
* `/sertifikat/{id}`
* `/media/{entity}/{id}`

### Aturan

* file harus divalidasi
* metadata disimpan di database
* gunakan signed URL bila cocok
* jangan simpan file besar di DB

---

## 16. Logging dan Monitoring

Wajib ada:

* request log
* error log
* audit log untuk aksi admin
* latency log

### Jangan log

* password
* token
* service key
* data rahasia

Opsional yang bagus:

* health check
* metrics endpoint
* monitoring response time

---

## 17. Format Response yang Konsisten

Semua endpoint harus punya format response seragam.

### Sukses

```json
{
  "success": true,
  "message": "Berhasil",
  "data": []
}
```

### Gagal

```json
{
  "success": false,
  "message": "Akses ditolak"
}
```

### Validasi gagal

```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": []
}
```

---

## 18. File Tambahan yang Disarankan

Walaupun struktur utama sudah jelas, tetap perlu file pendukung seperti:

* `common/exception/`
* `common/response/`
* `common/validation/`
* `common/utils/`
* `common/mapper/`

### Catatan tentang utils

`utils` bukan khusus frontend. Di backend juga boleh dipakai untuk helper kecil yang stateless, misalnya:

* format tanggal
* cek string
* helper file
* helper response ringan

Tapi logic bisnis besar jangan masuk utils.

---

## 19. Implementasi Role per Modul

### Admin boleh CRUD:

* dosen
* statistik mahasiswa
* jadwal perkuliahan
* tri dharma
* fasilitas

### Dosen boleh CRUD:

* publikasi
* penelitian
* pengabdian
* buku ajar
* HKI
* sertifikat

### User publik:

* hanya membaca data publik

Semua akses harus dicek di backend walaupun Supabase RLS tetap aktif.

---

## 20. Tujuan Akhir Agent

Agent harus mampu menghasilkan:

* struktur project yang bersih
* endpoint yang teratur per modul
* DI yang rapi
* coroutine yang benar
* validasi input yang jelas
* error message yang konsisten
* rate limiting
* keamanan yang kuat
* performa yang stabil
* kode yang mudah dirawat

---

## 21. Ringkasan Final

Backend ini harus dibangun dengan pola:

* **Ktor = logic dan endpoint**
* **Supabase = database, auth, storage**
* **Clean architecture = pemisahan layer**
* **Coroutines = efisiensi async**
* **DI = maintainability**
* **Validation + error handling = pengalaman API yang rapi**
* **Security + rate limit = perlindungan sistem**
* **Repository + use case = scalable codebase**
