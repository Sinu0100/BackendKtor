# Gambaran Project untuk Agent

## Ringkasan

Proyek ini adalah **backend Kotlin menggunakan Ktor** untuk website informasi program studi. Backend berperan sebagai **lapisan logika bisnis utama**, sedangkan **Supabase** digunakan untuk **PostgreSQL (database), authentication, dan storage**.

Semua endpoint harus dibuat di Ktor (bukan menggunakan endpoint otomatis Supabase).

Target:

* scalable
* maintainable
* secure
* low latency
* production-ready

---

## Arsitektur Sistem

Alur utama:

Client → Ktor → Supabase (PostgreSQL + Storage + Auth)

Ktor bertanggung jawab atas:

* business logic
* validasi
* autentikasi
* otorisasi
* format response
* orchestrasi database

Supabase hanya sebagai:

* database
* auth provider
* file storage

---

## Aktor Sistem

Ada **3 aktor utama**:

### 1. Admin

Memiliki akses penuh untuk:

* CRUD dosen
* CRUD statistik mahasiswa
* CRUD jadwal perkuliahan
* CRUD tri dharma
* CRUD fasilitas

### 2. Dosen

Dapat mengelola data milik sendiri:

* CRUD publikasi
* CRUD penelitian
* CRUD pengabdian
* CRUD buku ajar
* CRUD HKI
* CRUD sertifikat

Catatan:

* hanya boleh mengubah data milik sendiri
* validasi kepemilikan wajib dilakukan di backend

### 3. User (Publik)

* hanya bisa membaca data (read-only)

---

## Domain Data

Entitas utama:

* profiles
* dosen
* keahlian
* dosen_keahlian
* statistik_mahasiswa
* jadwal_perkuliahan
* publikasi
* penelitian
* penelitian_anggota
* buku_ajar
* hki
* sertifikat
* pengabdian
* fasilitas
* tri_dharma
* media

---

## Teknologi

### Backend

* Kotlin
* Ktor
* Netty
* kotlinx.serialization

### Database

* PostgreSQL (Supabase)
* HikariCP (WAJIB untuk connection pool)

### Storage

* Supabase Storage (bucket: prodi-files)

---

## Plugin Ktor

### Wajib

* Routing
* ContentNegotiation
* StatusPages
* CallLogging
* DefaultHeaders
* CORS
* Authentication
* Authentication JWT
* Compression
* ConditionalHeaders
* CachingHeaders
* HSTS
* HTTPSRedirect
* Rate Limiting

---

## Keamanan

### JWT

* wajib verifikasi JWT dari Supabase
* validasi signature
* validasi issuer
* validasi audience

### RLS

* sudah aktif di Supabase
* backend tetap wajib validasi akses

### Prinsip

* jangan percaya input client
* gunakan environment variables
* jangan expose error internal

---

## Database

### Connection Pool


### Query

* gunakan pagination
* manfaatkan index
* hindari query berulang

---

## Storage

Bucket: prodi-files

Struktur:

* /dosen/foto/{user_id}
* /jadwal/
* /hki/{id}
* /sertifikat/{id}
* /media/{entity}/{id}

Aturan:

* validasi file
* simpan metadata di DB
* gunakan signed URL bila perlu

---

## Struktur Kode

```
src/
  routes/
  controllers/
  services/
  repositories/
  models/
  dto/
  auth/
  database/
  storage/
  utils/
```

---

## Pola Endpoint

Gunakan prefix:
/api/v1

Pisahkan:

* public endpoint
* admin endpoint
* dosen endpoint

---

## Format Response

Sukses:

```
{
  "success": true,
  "data": {}
}
```

Error:

```
{
  "success": false,
  "message": "error"
}
```

---

## Validasi

Wajib:

* field required
* tipe data
* relasi
* range nilai

---

## Performance

* gunakan compression
* gunakan caching
* gunakan pagination
* hindari payload besar

---

## Logging

* log request
* log error
* jangan log data sensitif

---

## Environment Variables

* DATABASE_URL
* DATABASE_USER
* DATABASE_PASSWORD
* SUPABASE_URL
* SUPABASE_KEY
* JWT_SECRET / JWKS

---

## Prinsip Utama

1. Ktor = pusat logic
2. Supabase = storage + DB
3. Security nomor 1
4. Clean architecture wajib
5. Hindari over-engineering

---

## Target Output Agent

Agent harus mampu:

* membuat endpoint sesuai role
* menjaga keamanan data
* mengikuti arsitektur
* membuat kode bersih dan scalable
* siap production
