# 📘 API FULL DOCUMENTATION — PART 2
# Publikasi, Penelitian, Pengabdian, Buku Ajar, HKI, Sertifikat

> **Base URL**: `http://localhost:8080/api/v1`
> **Auth Header**: `Authorization: Bearer <token_jwt>`

---

## 📝 H. PUBLIKASI

### `GET /api/v1/publikasi`
- **Akses**: Public

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    {
      "id": "uuid-publikasi",
      "dosen_id": "uuid-dosen",
      "judul": "Implementasi IoT pada Smart Grid",
      "nama_jurnal_konferensi": "Jurnal Teknologi Informasi Vol. 12",
      "deskripsi": "Penelitian tentang implementasi IoT...",
      "link_tautan": "https://doi.org/xxxxx",
      "tahun": 2024,
      "created_at": "2024-04-01T00:00:00+00:00",
      "media": []
    }
  ]
}
``` 

---

### `GET /api/v1/publikasi/{id}`
- **Akses**: Authenticated (Dosen/Admin)

**✅ Response 200:** (single object, format sama)

**❌ Response 404:**
```json
{ "success": false, "message": "Data tidak ditemukan" }
```

---

### `GET /api/v1/publikasi/my`
- **Akses**: Authenticated (Dosen/Admin)
- **Deskripsi**: Mengambil publikasi milik dosen yang login

**✅ Response 200:** (array format sama)

**❌ Response 401:**
```json
{ "success": false, "message": "Unauthorized" }
```

---

### `POST /api/v1/publikasi`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`

**Form Fields:**
| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `judul` | Text | ✅ | Judul publikasi |
| `nama_jurnal_konferensi` | Text | ❌ | Nama jurnal/konferensi |
| `deskripsi` | Text | ❌ | Deskripsi |
| `link_tautan` | Text | ❌ | Link/URL publikasi |
| `tahun` | Text (angka) | ❌ | **Validasi**: 1900-2100 |

**✅ Response 201 Created:**
```json
{
  "success": true,
  "message": "Berhasil ditambahkan",
  "data": {
    "id": "uuid-baru",
    "dosen_id": "uuid-dosen",
    "judul": "Implementasi IoT pada Smart Grid",
    "nama_jurnal_konferensi": "Jurnal Teknologi Informasi",
    "deskripsi": "...",
    "link_tautan": "https://doi.org/xxxxx",
    "tahun": 2024,
    "created_at": "2024-04-28T00:00:00+00:00",
    "media": []
  }
}
```

**❌ Response 400 — Judul Kosong:**
```json
{ "success": false, "message": "Judul wajib diisi" }
```

**❌ Response 400 — Tahun Invalid:**
```json
{ "success": false, "message": "Tahun tidak valid (1900-2100)" }
```

**❌ Response 403 — Bukan Dosen/Admin:**
```json
{ "success": false, "message": "Akses ditolak: Role 'user' tidak memiliki ijin." }
```

---

### `PUT /api/v1/publikasi/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- Semua field opsional (hanya kirim yang mau diubah)

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil diperbarui", "data": { ... } }
```

**❌ Response 404:**
```json
{ "success": false, "message": "Data tidak ditemukan" }
```

**❌ Response 403 — Dosen lain:**
```json
{ "success": false, "message": "FORBIDDEN: Anda tidak berhak mengubah data ini" }
```

---

### `DELETE /api/v1/publikasi/{id}`
- **Akses**: Dosen & Admin

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil dihapus" }
```

---

## 🔬 I. PENELITIAN

### `GET /api/v1/penelitian`
- **Akses**: Public

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    {
      "id": "uuid-penelitian",
      "dosen_id": "uuid-dosen",
      "judul": "Sistem Monitoring Energi Berbasis IoT",
      "tahun": 2024,
      "deskripsi": "Penelitian tentang monitoring...",
      "created_at": "2024-04-01T00:00:00+00:00",
      "media": [
        { "id": "uuid-media", "file_url": "https://xxx.supabase.co/..." }
      ],
      "anggota": [
        { "dosen_id": "uuid-dosen-1", "nama_dosen": "Dr. Budi", "peran": "Ketua" },
        { "dosen_id": "uuid-dosen-2", "nama_dosen": "Ir. Andi", "peran": "Anggota" }
      ]
    }
  ]
}
```

---

### `GET /api/v1/penelitian/{id}`
- **Akses**: Public

**✅ Response 200:** (single object)

**❌ Response 404:**
```json
{ "success": false, "message": "Not Found" }
```

---

### `GET /api/v1/penelitian/my`
- **Akses**: Authenticated (Dosen/Admin)

**✅ Response 200:** (array milik dosen yang login)

---

### `POST /api/v1/penelitian`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`

**Form Fields:**
| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `judul` | Text | ✅ | Judul penelitian |
| `tahun` | Text (angka) | ❌ | **Validasi**: 1900-2100 |
| `deskripsi` | Text | ❌ | Deskripsi penelitian |
| `anggota_dosen_id` | Text | ❌ | UUID dosen anggota. Bisa kirim berkali-kali atau pisah koma: `"uuid1,uuid2"` |
| `file` | File | ❌ | Foto/dokumen (bisa multiple) |

**✅ Response 201 Created:**
```json
{
  "success": true,
  "message": "Berhasil membuat penelitian dengan tim",
  "data": {
    "id": "uuid-baru",
    "dosen_id": "uuid-ketua",
    "judul": "...",
    "tahun": 2024,
    "deskripsi": "...",
    "created_at": "...",
    "media": [ { "id": "uuid-media", "file_url": "https://..." } ],
    "anggota": [
      { "dosen_id": "uuid-ketua", "nama_dosen": "Dr. Budi", "peran": "Ketua" },
      { "dosen_id": "uuid-anggota", "nama_dosen": "Ir. Andi", "peran": "Anggota" }
    ]
  }
}
```

---

### `PUT /api/v1/penelitian/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- Field: `judul`, `tahun`, `deskripsi`, `file` (semua opsional)

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil", "data": { ... } }
```

---

### `DELETE /api/v1/penelitian/{id}`
- **Akses**: Dosen & Admin

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil" }
```

---

## 🤝 J. PENGABDIAN (Kepada Masyarakat)

### `GET /api/v1/pengabdian`
- **Akses**: Public

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    {
      "id": "uuid",
      "dosen_id": "uuid-dosen",
      "judul_pengabdian": "Pelatihan Kelistrikan untuk Masyarakat Desa",
      "deskripsi": "Kegiatan pengabdian...",
      "tahun": 2024,
      "created_at": "2024-04-01T00:00:00+00:00",
      "media": [
        { "id": "uuid-media", "file_url": "https://..." }
      ]
    }
  ]
}
```

---

### `GET /api/v1/pengabdian/{id}` | `GET /api/v1/pengabdian/my`
- `/{id}`: Public | `/my`: Dosen only

---

### `POST /api/v1/pengabdian`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`

**Form Fields:**
| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `judul_pengabdian` | Text | ✅ | Judul pengabdian |
| `deskripsi` | Text | ❌ | Deskripsi |
| `tahun` | Text (angka) | ❌ | **Validasi**: 1900-2100 |
| `file` | File | ❌ | Foto/dokumen (bisa multiple) |

**✅ Response 201:**
```json
{ "success": true, "message": "Berhasil", "data": { ... } }
```

**❌ Response 400:**
```json
{ "success": false, "message": "Judul pengabdian wajib diisi" }
```

---

### `PUT /api/v1/pengabdian/{id}` | `DELETE /api/v1/pengabdian/{id}`
- **Akses**: Dosen & Admin
- PUT: `multipart/form-data`, semua field opsional

---

## 📚 K. BUKU AJAR

### `GET /api/v1/buku-ajar`
- **Akses**: Public

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    {
      "id": "uuid",
      "dosen_id": "uuid-dosen",
      "judul": "Dasar-Dasar Teknik Listrik",
      "tahun": 2024,
      "deskripsi": "Buku ajar tentang...",
      "peran_penulis": "Penulis Ketua",
      "created_at": "2024-04-01T00:00:00+00:00",
      "media": []
    }
  ]
}
```

---

### `GET /api/v1/buku-ajar/{id}` | `GET /api/v1/buku-ajar/my`
- `/{id}`: Public | `/my`: Dosen only

---

### `POST /api/v1/buku-ajar`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`

**Form Fields:**
| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `judul` | Text | ✅ | Judul buku ajar |
| `tahun` | Text (angka) | ❌ | **Validasi**: 1900-2100 |
| `deskripsi` | Text | ❌ | Deskripsi |
| `peran_penulis` | Text | ❌ | Default: `"Anggota"`. Valid: `"Penulis Ketua"` atau `"Anggota"` |

**✅ Response 201:**
```json
{ "success": true, "message": "Berhasil", "data": { ... } }
```

**❌ Response 400:**
```json
{ "success": false, "message": "Judul wajib diisi" }
```

---

### `PUT /api/v1/buku-ajar/{id}` | `DELETE /api/v1/buku-ajar/{id}`
- **Akses**: Dosen & Admin
- PUT: `multipart/form-data`, semua opsional

---

## 🏆 L. HKI (Hak Kekayaan Intelektual)

### `GET /api/v1/hki`
- **Akses**: Public

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    {
      "id": "uuid",
      "dosen_id": "uuid-dosen",
      "judul_invensi": "Alat Penghemat Energi Listrik",
      "inventor": "Dr. Budi Santoso, Ir. Andi",
      "jenis_hki": "Paten Sederhana",
      "nomor_paten": "IDP000012345",
      "tahun": 2024,
      "file_url": "https://xxx.supabase.co/...",
      "created_at": "2024-04-01T00:00:00+00:00"
    }
  ]
}
```

---

### `GET /api/v1/hki/{id}` | `GET /api/v1/hki/my`
- `/{id}`: Authenticated | `/my`: Dosen/Admin

---

### `POST /api/v1/hki`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data` ⚠️ **WAJIB**

**Form Fields:**
| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `judul_invensi` | Text | ✅ | Judul invensi |
| `inventor` | Text | ❌ | Nama-nama inventor |
| `jenis_hki` | Text | ❌ | Enum: `Paten Sederhana`, `Paten Biasa`, `Hak Cipta`, `Merek`, `Desain Industri`, `Rahasia Dagang` |
| `nomor_paten` | Text | ❌ | Nomor paten/sertifikat |
| `tahun` | Text (angka) | ❌ | **Validasi**: 1900-2100 |
| `file` | File | ❌ | Dokumen sertifikat HKI |

**✅ Response 201:**
```json
{ "success": true, "message": "Berhasil", "data": { ... } }
```

**❌ Response 400 — Judul Kosong:**
```json
{ "success": false, "message": "Judul invensi wajib diisi" }
```

**❌ Response 400 — Content-Type Salah:**
```json
{ "success": false, "message": "Content-Type harus multipart/form-data" }
```

**❌ Response 400 — Multipart Error:**
```json
{ "success": false, "message": "Gagal membaca data form. Pastikan format multipart/form-data benar." }
```

---

### `PUT /api/v1/hki/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- Semua field opsional, bisa replace file

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil diperbarui", "data": { ... } }
```

**❌ Response 404:**
```json
{ "success": false, "message": "HKI tidak ditemukan" }
```

---

### `DELETE /api/v1/hki/{id}`
- **Akses**: Dosen & Admin

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil dihapus" }
```

---

## 📜 M. SERTIFIKAT

### `GET /api/v1/sertifikat`
- **Akses**: Public

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    {
      "id": "uuid",
      "dosen_id": "uuid-dosen",
      "judul_sertifikat": "Certified Network Engineer",
      "tahun": 2024,
      "file_url": "https://xxx.supabase.co/...",
      "created_at": "2024-04-01T00:00:00+00:00"
    }
  ]
}
```

---

### `GET /api/v1/sertifikat/{id}` | `GET /api/v1/sertifikat/my`
- `/{id}`: Public | `/my`: Dosen only

**❌ Response 404:**
```json
{ "success": false, "message": "Not Found" }
```

---

### `POST /api/v1/sertifikat`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`

**Form Fields:**
| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `judul_sertifikat` | Text | ✅ | Judul/nama sertifikat |
| `tahun` | Text (angka) | ❌ | **Validasi**: 1900-2100 |
| `file` | File | ❌ | Dokumen sertifikat |

**✅ Response 201:**
```json
{ "success": true, "message": "Berhasil", "data": { ... } }
```

**❌ Response 400:**
```json
{ "success": false, "message": "Judul sertifikat wajib diisi" }
```

---

### `PUT /api/v1/sertifikat/{id}`
- **Akses**: Dosen & Admin
- **Content-Type**: `multipart/form-data`
- Field: `judul_sertifikat`, `tahun`, `file` (semua opsional)

**❌ Response 404:**
```json
{ "success": false, "message": "Data tidak ditemukan" }
```

---

### `DELETE /api/v1/sertifikat/{id}`
- **Akses**: Dosen & Admin

**✅ Response 200:**
```json
{ "success": true, "message": "Berhasil dihapus" }
```

---

## 🖼️ N. MEDIA (Manajemen Gambar/File)

> **Catatan Penting**: Endpoint ini untuk mengelola file/gambar yang disimpan di tabel `media` (polymorphic). Entity yang menggunakan tabel ini: **Penelitian, Pengabdian, Fasilitas, Tri Dharma, HKI, Sertifikat**.

### `GET /api/v1/media/entity/{entityType}/{entityId}`
- **Akses**: Public
- **Path Params**:
  - `entityType`: `penelitian` | `pengabdian` | `fasilitas` | `tri_dharma` | `hki` | `sertifikat`
  - `entityId`: UUID entity

**✅ Response 200 OK:**
```json
{
  "success": true,
  "message": "Berhasil",
  "data": [
    { "id": "uuid-media-1", "file_url": "https://xxx.supabase.co/storage/v1/object/public/prodi-files/penelitian/uuid.jpg" },
    { "id": "uuid-media-2", "file_url": "https://xxx.supabase.co/storage/v1/object/public/prodi-files/penelitian/uuid2.jpg" }
  ]
}
```

---

### `DELETE /api/v1/media/{mediaId}`
- **Akses**: Admin & Dosen (Authenticated)
- **Header**: `Authorization: Bearer <token>`
- **Path Param**: `mediaId` (UUID media dari tabel `media`)
- **Deskripsi**: Hapus satu file/gambar spesifik. File dihapus dari **Supabase Storage** DAN record dihapus dari **database**.

**Aturan Ownership:**

| Entity Type | Admin | Dosen (pemilik) | Dosen (bukan pemilik) |
|---|---|---|---|
| `penelitian` | ✅ Bisa | ✅ Bisa | ❌ 403 |
| `pengabdian` | ✅ Bisa | ✅ Bisa | ❌ 403 |
| `hki` | ✅ Bisa | ✅ Bisa | ❌ 403 |
| `sertifikat` | ✅ Bisa | ✅ Bisa | ❌ 403 |
| `fasilitas` | ✅ Bisa | ❌ 403 (admin only) | ❌ 403 |
| `tri_dharma` | ✅ Bisa | ❌ 403 (admin only) | ❌ 403 |

**✅ Response 200 OK:**
```json
{ "success": true, "message": "Media berhasil dihapus" }
```

**❌ Response 404 — Media Tidak Ditemukan:**
```json
{ "success": false, "message": "Media tidak ditemukan" }
```

**❌ Response 403 — Bukan Pemilik:**
```json
{ "success": false, "message": "FORBIDDEN: Anda tidak berhak menghapus media ini" }
```

**❌ Response 403 — Dosen Akses Admin-Only Entity:**
```json
{ "success": false, "message": "FORBIDDEN: Hanya Admin yang dapat menghapus media fasilitas" }
```

---

## 🔄 PERILAKU FILE SAAT PUT (UPDATE)

> **Penting untuk Frontend Developer**: Berikut cara kerja file replacement saat update di setiap entity.

### Entity dengan Multiple File (tabel `media`):
| Entity | PUT behavior |
|---|---|
| **Penelitian** | File baru **DITAMBAHKAN** ke list media. Untuk **hapus** gambar spesifik → gunakan `DELETE /media/{mediaId}` |
| **Pengabdian** | Sama seperti Penelitian |
| **Fasilitas** | Sama seperti Penelitian |
| **Tri Dharma** | Sama seperti Penelitian |

### Entity dengan Single File (kolom `file_url`):
| Entity | PUT behavior |
|---|---|
| **HKI** | Upload file baru → **file lama DIHAPUS** dari Storage + DB → file baru menggantikan ✅ |
| **Sertifikat** | Sama — file lama **DIHAPUS**, diganti file baru ✅ |
| **Jadwal** | Sama — file lama **DIHAPUS**, diganti file baru ✅ |
| **Dosen (foto)** | Sama — foto lama **DIHAPUS**, diganti foto baru ✅ |

### Entity tanpa File:
| Entity | Keterangan |
|---|---|
| **Publikasi** | Tidak ada file upload |
| **Buku Ajar** | Tidak ada file upload |
| **Statistik** | Tidak ada file upload (pure angka) |

---

## 📋 RINGKASAN ENDPOINT

| # | Method | Endpoint | Akses | Content-Type |
|---|---|---|---|---|
| 1 | POST | `/auth/login` | Public (Rate Limited) | `application/json` |
| 2 | GET | `/dosen` | Public | - |
| 3 | GET | `/dosen/{id}` | Public | - |
| 4 | GET | `/dosen/me` | Auth (Admin/Dosen) | - |
| 5 | POST | `/dosen` | Admin | `multipart/form-data` |
| 6 | PUT | `/dosen/{id}` | Admin | `multipart/form-data` |
| 7 | DELETE | `/dosen/{id}` | Admin | - |
| 8 | GET | `/keahlian` | Public | - |
| 9 | POST | `/admin/keahlian` | Admin | `application/json` |
| 10 | POST | `/admin/keahlian/assign` | Admin | `application/json` |
| 11 | GET | `/dosen/keahlian` | Auth (Dosen) | - |
| 12 | POST | `/dosen/keahlian` | Auth (Dosen) | `application/json` |
| 13 | DELETE | `/dosen/keahlian/{id}` | Auth (Dosen) | - |
| 14 | GET | `/statistik` | Public | - |
| 15 | POST | `/statistik` | Admin | `application/json` |
| 16 | PUT | `/statistik/{id}` | Admin | `application/json` |
| 17 | DELETE | `/statistik/{id}` | Admin | - |
| 18 | GET | `/jadwal` | Public | - |
| 19 | POST | `/jadwal` | Admin | `multipart/form-data` |
| 20 | PUT | `/jadwal/{id}` | Admin | `multipart/form-data` |
| 21 | DELETE | `/jadwal/{id}` | Admin | - |
| 22 | GET | `/fasilitas` | Public | - |
| 23 | GET | `/fasilitas/{id}` | Public | - |
| 24 | POST | `/fasilitas` | Admin | `multipart/form-data` |
| 25 | PUT | `/fasilitas/{id}` | Admin | `multipart/form-data` |
| 26 | DELETE | `/fasilitas/{id}` | Admin | - |
| 27 | GET | `/tri-dharma` | Public | - |
| 28 | GET | `/tri-dharma/{id}` | Public | - |
| 29 | POST | `/tri-dharma` | Admin | `multipart/form-data` |
| 30 | PUT | `/tri-dharma/{id}` | Admin | `multipart/form-data` |
| 31 | DELETE | `/tri-dharma/{id}` | Admin | - |
| 32 | GET | `/publikasi` | Public | - |
| 33 | GET | `/publikasi/{id}` | Auth | - |
| 34 | GET | `/publikasi/my` | Auth (Dosen/Admin) | - |
| 35 | POST | `/publikasi` | Dosen/Admin | `multipart/form-data` |
| 36 | PUT | `/publikasi/{id}` | Dosen/Admin | `multipart/form-data` |
| 37 | DELETE | `/publikasi/{id}` | Dosen/Admin | - |
| 38 | GET | `/penelitian` | Public | - |
| 39 | GET | `/penelitian/{id}` | Public | - |
| 40 | GET | `/penelitian/my` | Auth (Dosen/Admin) | - |
| 41 | POST | `/penelitian` | Dosen/Admin | `multipart/form-data` |
| 42 | PUT | `/penelitian/{id}` | Dosen/Admin | `multipart/form-data` |
| 43 | DELETE | `/penelitian/{id}` | Dosen/Admin | - |
| 44 | GET | `/pengabdian` | Public | - |
| 45 | GET | `/pengabdian/{id}` | Public | - |
| 46 | GET | `/pengabdian/my` | Auth (Dosen) | - |
| 47 | POST | `/pengabdian` | Dosen/Admin | `multipart/form-data` |
| 48 | PUT | `/pengabdian/{id}` | Dosen/Admin | `multipart/form-data` |
| 49 | DELETE | `/pengabdian/{id}` | Dosen/Admin | - |
| 50 | GET | `/buku-ajar` | Public | - |
| 51 | GET | `/buku-ajar/{id}` | Public | - |
| 52 | GET | `/buku-ajar/my` | Auth (Dosen) | - |
| 53 | POST | `/buku-ajar` | Dosen/Admin | `multipart/form-data` |
| 54 | PUT | `/buku-ajar/{id}` | Dosen/Admin | `multipart/form-data` |
| 55 | DELETE | `/buku-ajar/{id}` | Dosen/Admin | - |
| 56 | GET | `/hki` | Public | - |
| 57 | GET | `/hki/{id}` | Auth | - |
| 58 | GET | `/hki/my` | Auth (Dosen/Admin) | - |
| 59 | POST | `/hki` | Dosen/Admin | `multipart/form-data` |
| 60 | PUT | `/hki/{id}` | Dosen/Admin | `multipart/form-data` |
| 61 | DELETE | `/hki/{id}` | Dosen/Admin | - |
| 62 | GET | `/sertifikat` | Public | - |
| 63 | GET | `/sertifikat/{id}` | Public | - |
| 64 | GET | `/sertifikat/my` | Auth (Dosen) | - |
| 65 | POST | `/sertifikat` | Dosen/Admin | `multipart/form-data` |
| 66 | PUT | `/sertifikat/{id}` | Dosen/Admin | `multipart/form-data` |
| 67 | DELETE | `/sertifikat/{id}` | Dosen/Admin | - |
| 68 | GET | `/media/entity/{entityType}/{entityId}` | Public | - |
| 69 | DELETE | `/media/{mediaId}` | Admin/Dosen | - |

---

## 🔒 VALIDASI & ERROR GLOBAL

### Validasi Input
| Rule | Pesan Error | HTTP Code |
|---|---|---|
| Judul/nama wajib diisi | `"Judul wajib diisi"` / `"Judul invensi wajib diisi"` / dll | 400 |
| Tahun harus 1900-2100 | `"Tahun tidak valid (1900-2100)"` | 400 |
| Password wajib (create dosen) | `"Password wajib diisi"` | 400 |
| File jadwal wajib | `"File jadwal wajib diunggah"` | 400 |
| Content-Type salah (HKI) | `"Content-Type harus multipart/form-data"` | 400 |
| ID parameter missing | `"ID diperlukan"` / `"ID dosen diperlukan"` | 400 |

### Authorization Error
| Kondisi | Pesan Error | HTTP Code |
|---|---|---|
| Token tidak ada | `"Header Authorization (Bearer Token) tidak ditemukan"` | 401 |
| Token format salah | `"Format token salah. Gunakan 'Bearer <token>'"` | 401 |
| Token expired/invalid | `"Token tidak valid, expired, atau Secret server tidak cocok."` | 401 |
| Role tidak cocok | `"Akses ditolak: Role 'dosen' tidak memiliki ijin."` | 403 |
| Token null (role check) | `"Token tidak valid atau tidak terbaca. Pastikan header Authorization benar."` | 403 |
| Admin-only endpoint | `"FORBIDDEN: Hanya Admin yang dapat menambah jadwal"` (contoh) | 403 |
| Dosen akses data orang lain | `"FORBIDDEN: Anda tidak berhak mengubah data ini"` | 403 |
| Dosen hapus media bukan miliknya | `"FORBIDDEN: Anda tidak berhak menghapus media ini"` | 403 |
| Dosen hapus media admin-only | `"FORBIDDEN: Hanya Admin yang dapat menghapus media fasilitas"` | 403 |
| Media tidak ditemukan | `"Media tidak ditemukan"` | 404 |

### Server Error
| Kondisi | HTTP Code |
|---|---|
| Internal Server Error | 500 |
| Rate Limit Exceeded | 429 |
