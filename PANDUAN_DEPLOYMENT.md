# 🚀 Panduan Final Deployment Ktor ke Railway

Panduan ini dibuat super detail langkah demi langkah agar Backend Ktor Anda siap digunakan oleh Frontend (Vercel/Localhost) tanpa error.

> **Syarat Utama:** Pastikan Anda sudah menginstal Git di komputer Anda dan memiliki akun GitHub serta akun Railway.

---

## TAHAP 1: Upload Kode ke GitHub (Wajib Private)

Karena Railway mengambil kode sumber dari GitHub, kita harus menyimpan kode ini ke repositori GitHub Anda. Sangat disarankan menggunakan repositori **Private** agar kode Anda aman.

1. Buka **Terminal** atau **CMD** di dalam folder project Ktor Anda (`d:\2026ai\ktor-afel`).
2. Jalankan perintah ini satu per satu secara berurutan:
   ```bash
   git add .
   git commit -m "Final Deploy - Siap Production"
   git push origin main
   ```
   *(Catatan: Jika ini project baru dan belum ada di GitHub, buat repository baru dulu di github.com, lalu ikuti instruksi push dari GitHub).*

---

## TAHAP 2: Buat Project di Railway

Sekarang kita akan menarik kode dari GitHub ke server Railway.

1. Buka browser dan pergi ke **[railway.app](https://railway.app/)**.
2. **Login** menggunakan akun GitHub Anda.
3. Di halaman Dashboard Railway, klik tombol **"New Project"** (atau ikon **+** di pojok kanan atas).
4. Pilih opsi **"Deploy from GitHub repo"**.
5. Railway akan menampilkan daftar repository GitHub Anda. Cari dan pilih repository project Ktor Anda (contoh: `ktor-afel`).
6. Klik **"Deploy Now"**.
   
> **Note:** Setelah Anda klik Deploy, Railway akan langsung mencoba membangun (build) aplikasi Anda. **Proses ini awalnya akan GAGAL (Crash) karena kita belum memasukkan variabel rahasianya.** Ini normal. Lanjut ke Tahap 3!

---

## TAHAP 3: Masukkan Variabel Rahasia (Sangat Krusial)

Ini adalah langkah paling penting. Kita harus memberitahu Railway kunci rahasia Supabase dan JWT agar Backend Anda bisa menyala.

1. Di Dashboard Railway, klik project Ktor Anda yang sedang berjalan/gagal tadi.
2. Klik tab **"Variables"** di menu atas.
3. Klik tombol **"New Variable"** dan masukkan **6 pasang kunci** ini satu per satu:

| VARIABLE NAME | VALUE (Isi dengan ini) |
| :--- | :--- |
| `SUPABASE_URL` | `https://oprotjjjrsyxqumribxq.supabase.co` |
| `SUPABASE_KEY` | *(Paste Service Role Key Supabase Anda yang panjang)* |
| `JWT_SECRET` | `random_secret_testing` *(Atau ganti password acak lain)* |
| `JWT_ISSUER` | `https://oprotjjjrsyxqumribxq.supabase.co/auth/v1` |
| `JWT_AUDIENCE` | `authenticated` |
| `JWT_REALM` | `ktor-afel` |

4. Setelah semua 6 variabel dimasukkan, Railway akan otomatis melakukan **Re-deploy** (Membangun ulang server). 
5. Buka tab **"Deployments"**, tunggu sekitar 2-3 menit sampai statusnya berubah menjadi **Hijau (Active)**.

---

## TAHAP 4: Generate Domain Publik

Server Anda sudah menyala, tapi belum punya alamat URL Publik. Mari kita buatkan.

1. Di project Railway Anda, klik tab **"Settings"**.
2. Gulir ke bawah cari bagian **"Networking"** atau **"Public Networking"**.
3. Klik tombol **"Generate Domain"**.
4. Railway akan otomatis membuatkan link sakti untuk Anda (contoh: `https://ktor-afel-production.up.railway.app`).

---

## TAHAP 5: Serahkan ke Dosen / Frontend Developer

Langkah terakhir Anda sebagai Backend Developer selesai. Serahkan *Public URL* dari Tahap 4 ke developer Frontend.

**Pesan untuk Frontend Developer:**
* *"Bro, ini Base URL API Backend kita yang udah di-deploy: `https://[DOMAIN-RAILWAY-LU].up.railway.app`"*
* *"CORS udah di-setting **anyHost** dan **allowCredentials = true**. Lu bebas mau testing di localhost (NPM Run Dev) atau langsung di-deploy ke Vercel, dua-duanya dijamin nembus tanpa error CORS blocked."*
