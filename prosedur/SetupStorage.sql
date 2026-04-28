-- ============================================================================
-- SCRIPT SETUP BUCKET STORAGE (PRODI-FILES)
-- ============================================================================
-- Jalankan script ini di menu "SQL Editor" pada Supabase project baru kamu!
-- Script ini akan:
-- 1. Membuat bucket "prodi-files" (jika belum ada)
-- 2. Mengatur bucket menjadi PUBLIC (agar gambar bisa diakses langsung via URL)
-- 3. Membuat policy agar public bisa melihat (SELECT) file
-- 4. Membuat policy opsional untuk autentikasi (opsional karena service_role otomatis bypass RLS)
-- ============================================================================

-- 1. Buat bucket baru bernama 'prodi-files' dan set menjadi PUBLIC
INSERT INTO storage.buckets (id, name, public)
VALUES ('prodi-files', 'prodi-files', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- 2. Izinkan akses BACA (SELECT) ke semua orang (Public)
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING ( bucket_id = 'prodi-files' );

-- 3. Izinkan upload/insert untuk Authenticated users & Service Role
CREATE POLICY "Auth Upload"
ON storage.objects FOR INSERT
WITH CHECK ( bucket_id = 'prodi-files' );

-- 4. Izinkan update/delete untuk Authenticated users & Service Role
CREATE POLICY "Auth Update Delete"
ON storage.objects FOR UPDATE
USING ( bucket_id = 'prodi-files' );

CREATE POLICY "Auth Delete"
ON storage.objects FOR DELETE
USING ( bucket_id = 'prodi-files' );

-- Note: Service Role Key Ktor kamu secara bawaan (default) sudah mem-bypass semua RLS ini.
-- Jadi Ktor kamu DIJAMIN bisa Insert, Update, dan Delete ke bucket ini selama bucketnya ada.
