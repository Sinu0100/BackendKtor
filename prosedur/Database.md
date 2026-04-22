-- ============================================================================
-- SCHEMA v4 — Sistem Informasi Prodi D4 Teknik Informatika
-- TANPA Supabase Auth
-- Login pakai tabel public.users sendiri
-- ============================================================================
-- WARNING:
-- Skrip ini menghapus tabel schema lama yang dipakai di project ini.
-- Jalankan setelah backup kalau database sudah ada data penting.
-- ============================================================================

-- ============================================================================
-- EXTENSION
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- CLEANUP LAMA
-- ============================================================================
DROP TABLE IF EXISTS public.media CASCADE;
DROP TABLE IF EXISTS public.penelitian_anggota CASCADE;
DROP TABLE IF EXISTS public.penelitian CASCADE;
DROP TABLE IF EXISTS public.publikasi CASCADE;
DROP TABLE IF EXISTS public.buku_ajar CASCADE;
DROP TABLE IF EXISTS public.hki CASCADE;
DROP TABLE IF EXISTS public.sertifikat CASCADE;
DROP TABLE IF EXISTS public.pengabdian CASCADE;
DROP TABLE IF EXISTS public.fasilitas CASCADE;
DROP TABLE IF EXISTS public.tri_dharma CASCADE;
DROP TABLE IF EXISTS public.dosen_keahlian CASCADE;
DROP TABLE IF EXISTS public.keahlian CASCADE;
DROP TABLE IF EXISTS public.jadwal_perkuliahan CASCADE;
DROP TABLE IF EXISTS public.statistik_mahasiswa CASCADE;
DROP TABLE IF EXISTS public.dosen CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;

DROP FUNCTION IF EXISTS public.fn_set_updated_at() CASCADE;

-- ============================================================================
-- HELPER FUNCTION
-- ============================================================================
CREATE OR REPLACE FUNCTION public.fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 1. USERS
-- Pengganti auth.users / profiles
-- Password disimpan sebagai password_hash (bcrypt hash), bukan plain text.
-- Role dipakai oleh backend untuk bedakan admin / dosen / user.
-- ============================================================================
CREATE TABLE public.users (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    password_hash   TEXT         NOT NULL,
    nama            VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'user'
                      CHECK (role IN ('admin', 'dosen', 'user')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_nama_nonempty CHECK (length(trim(nama)) > 0),
    CONSTRAINT chk_users_email_nonempty CHECK (length(trim(email)) > 0)
);

COMMENT ON TABLE public.users IS 'Tabel login internal tanpa Supabase Auth. Digunakan untuk autentikasi via backend.';
COMMENT ON COLUMN public.users.password_hash IS 'Simpan hash password, misalnya bcrypt hasil crypt()/backend.';
COMMENT ON COLUMN public.users.role IS 'admin | dosen | user';

CREATE UNIQUE INDEX uq_users_email_lower
    ON public.users (LOWER(email));

CREATE INDEX idx_users_role_active
    ON public.users (role)
    WHERE is_active = TRUE;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 2. DOSEN
-- Relasi 1-to-1 ke users kalau dosen punya akun login.
-- ============================================================================
CREATE TABLE public.dosen (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID         REFERENCES public.users(id) ON DELETE SET NULL,
    nama               VARCHAR(255) NOT NULL,
    nidn               VARCHAR(20),
    jabatan_fungsional VARCHAR(100),
    pangkat_golongan   VARCHAR(100),
    email              VARCHAR(255),
    no_hp              VARCHAR(20),
    foto_url           TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_dosen_nidn    UNIQUE (nidn),
    CONSTRAINT uq_dosen_user_id UNIQUE (user_id)
);

COMMENT ON TABLE public.dosen IS 'Profil lengkap dosen. Relasi 1-to-1 dengan users jika dosen punya akun login.';

CREATE INDEX idx_dosen_user_id_fk
    ON public.dosen (user_id);

CREATE INDEX idx_dosen_nama_search
    ON public.dosen (nama);

CREATE TRIGGER trg_dosen_updated_at
    BEFORE UPDATE ON public.dosen
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 3. KEAHLIAN
-- ============================================================================
CREATE TABLE public.keahlian (
    id            SERIAL       PRIMARY KEY,
    nama_keahlian  VARCHAR(100) NOT NULL,
    CONSTRAINT uq_keahlian_nama UNIQUE (nama_keahlian)
);

COMMENT ON TABLE public.keahlian IS 'Master data bidang keahlian dosen.';

-- Pivot: Dosen <-> Keahlian
CREATE TABLE public.dosen_keahlian (
    id           SERIAL PRIMARY KEY,
    dosen_id     UUID   NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    keahlian_id  INT    NOT NULL REFERENCES public.keahlian(id) ON DELETE CASCADE,
    CONSTRAINT uq_dosen_keahlian UNIQUE (dosen_id, keahlian_id)
);

COMMENT ON TABLE public.dosen_keahlian IS 'Pivot many-to-many: dosen ↔ keahlian.';

CREATE INDEX idx_dosen_keahlian_dosen_fk
    ON public.dosen_keahlian (dosen_id);

CREATE INDEX idx_dosen_keahlian_keahlian_fk
    ON public.dosen_keahlian (keahlian_id);

-- ============================================================================
-- 4. STATISTIK MAHASISWA
-- ============================================================================
CREATE TABLE public.statistik_mahasiswa (
    id               SERIAL      PRIMARY KEY,
    tahun            INT         NOT NULL,
    jumlah_pendaftar INT         NOT NULL DEFAULT 0,
    jumlah_diterima  INT         NOT NULL DEFAULT 0,
    jumlah_lulusan   INT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_statistik_tahun         UNIQUE (tahun),
    CONSTRAINT chk_statistik_tahun        CHECK (tahun BETWEEN 2000 AND 2100),
    CONSTRAINT chk_pendaftar_non_neg      CHECK (jumlah_pendaftar >= 0),
    CONSTRAINT chk_diterima_non_neg       CHECK (jumlah_diterima  >= 0),
    CONSTRAINT chk_lulusan_non_neg        CHECK (jumlah_lulusan   >= 0),
    CONSTRAINT chk_diterima_lte_pendaftar CHECK (jumlah_diterima  <= jumlah_pendaftar)
);

COMMENT ON TABLE public.statistik_mahasiswa IS 'Data agregat per tahun untuk grafik dashboard.';

CREATE INDEX idx_statistik_mahasiswa_tahun_sort
    ON public.statistik_mahasiswa (tahun ASC);

CREATE TRIGGER trg_statistik_updated_at
    BEFORE UPDATE ON public.statistik_mahasiswa
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 5. JADWAL PERKULIAHAN
-- ============================================================================
CREATE TABLE public.jadwal_perkuliahan (
    id             SERIAL       PRIMARY KEY,
    nama_jadwal    VARCHAR(255) NOT NULL,
    tanggal_upload  DATE         NOT NULL DEFAULT CURRENT_DATE,
    file_url       TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.jadwal_perkuliahan IS 'Dokumen jadwal perkuliahan.';
COMMENT ON COLUMN public.jadwal_perkuliahan.file_url IS 'Path file, misalnya dari Supabase Storage atau URL backend.';

CREATE INDEX idx_jadwal_tanggal_upload_sort
    ON public.jadwal_perkuliahan (tanggal_upload DESC);

CREATE TRIGGER trg_jadwal_updated_at
    BEFORE UPDATE ON public.jadwal_perkuliahan
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 6. PUBLIKASI
-- ============================================================================
CREATE TABLE public.publikasi (
    id                     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dosen_id               UUID         NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    judul                  VARCHAR(500) NOT NULL,
    nama_jurnal_konferensi VARCHAR(255),
    deskripsi              TEXT,
    link_tautan            TEXT,
    tahun                  INT          CHECK (tahun BETWEEN 2000 AND 2100),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.publikasi IS 'Riwayat publikasi jurnal dan konferensi per dosen.';

CREATE INDEX idx_publikasi_dosen_tahun_comp
    ON public.publikasi (dosen_id, tahun DESC);

CREATE INDEX idx_publikasi_tahun_filter
    ON public.publikasi (tahun);

CREATE TRIGGER trg_publikasi_updated_at
    BEFORE UPDATE ON public.publikasi
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 7. PENELITIAN
-- ============================================================================
CREATE TABLE public.penelitian (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dosen_id   UUID         NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    judul      VARCHAR(500) NOT NULL,
    tahun      INT          CHECK (tahun BETWEEN 2000 AND 2100),
    deskripsi  TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.penelitian IS 'Penelitian milik dosen. Ketua = dosen_id.';

CREATE INDEX idx_penelitian_dosen_tahun_comp
    ON public.penelitian (dosen_id, tahun DESC);

CREATE INDEX idx_penelitian_tahun_filter
    ON public.penelitian (tahun);

CREATE TRIGGER trg_penelitian_updated_at
    BEFORE UPDATE ON public.penelitian
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

CREATE TABLE public.penelitian_anggota (
    id            SERIAL      PRIMARY KEY,
    penelitian_id UUID        NOT NULL REFERENCES public.penelitian(id) ON DELETE CASCADE,
    dosen_id      UUID        NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    peran         VARCHAR(50) NOT NULL DEFAULT 'Anggota'
                      CHECK (peran IN ('Ketua', 'Anggota')),
    CONSTRAINT uq_penelitian_anggota UNIQUE (penelitian_id, dosen_id)
);

COMMENT ON TABLE public.penelitian_anggota IS 'Tim peneliti per penelitian.';

CREATE INDEX idx_penelitian_anggota_penelitian_fk
    ON public.penelitian_anggota (penelitian_id);

CREATE INDEX idx_penelitian_anggota_dosen_fk
    ON public.penelitian_anggota (dosen_id);

-- ============================================================================
-- 8. BUKU AJAR
-- ============================================================================
CREATE TABLE public.buku_ajar (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dosen_id      UUID         NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    judul         VARCHAR(500) NOT NULL,
    tahun         INT          CHECK (tahun BETWEEN 2000 AND 2100),
    deskripsi     TEXT,
    peran_penulis VARCHAR(50)  NOT NULL DEFAULT 'Anggota'
                      CHECK (peran_penulis IN ('Penulis Ketua', 'Anggota')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.buku_ajar IS 'Karya buku ajar/modul yang ditulis dosen.';

CREATE INDEX idx_buku_ajar_dosen_tahun_comp
    ON public.buku_ajar (dosen_id, tahun DESC);

CREATE TRIGGER trg_buku_ajar_updated_at
    BEFORE UPDATE ON public.buku_ajar
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 9. HKI
-- ============================================================================
CREATE TABLE public.hki (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dosen_id      UUID         NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    judul_invensi VARCHAR(500) NOT NULL,
    inventor      TEXT,
    jenis_hki     VARCHAR(100) CHECK (jenis_hki IN (
                      'Paten Sederhana','Paten Biasa','Hak Cipta',
                      'Merek','Desain Industri','Rahasia Dagang'
                  )),
    nomor_paten   VARCHAR(100),
    tahun         INT          CHECK (tahun BETWEEN 2000 AND 2100),
    file_url      TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.hki IS 'HKI milik dosen. 1 file per record.';
COMMENT ON COLUMN public.hki.file_url IS 'Path file, misalnya dari Supabase Storage atau URL backend.';

CREATE INDEX idx_hki_dosen_tahun_comp
    ON public.hki (dosen_id, tahun DESC);

CREATE TRIGGER trg_hki_updated_at
    BEFORE UPDATE ON public.hki
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 10. SERTIFIKAT PROFESIONAL
-- ============================================================================
CREATE TABLE public.sertifikat (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dosen_id         UUID         NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    judul_sertifikat VARCHAR(500) NOT NULL,
    tahun            INT          CHECK (tahun BETWEEN 2000 AND 2100),
    file_url         TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.sertifikat IS 'Sertifikasi profesional dosen. 1 file per record.';
COMMENT ON COLUMN public.sertifikat.file_url IS 'Path file, misalnya dari Supabase Storage atau URL backend.';

CREATE INDEX idx_sertifikat_dosen_tahun_comp
    ON public.sertifikat (dosen_id, tahun DESC);

CREATE TRIGGER trg_sertifikat_updated_at
    BEFORE UPDATE ON public.sertifikat
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 11. PENGABDIAN KEPADA MASYARAKAT
-- ============================================================================
CREATE TABLE public.pengabdian (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dosen_id         UUID         NOT NULL REFERENCES public.dosen(id) ON DELETE CASCADE,
    judul_pengabdian VARCHAR(500) NOT NULL,
    deskripsi        TEXT,
    tahun            INT          CHECK (tahun BETWEEN 2000 AND 2100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.pengabdian IS 'Kegiatan pengabdian masyarakat. Foto/media di tabel media.';

CREATE INDEX idx_pengabdian_dosen_tahun_comp
    ON public.pengabdian (dosen_id, tahun DESC);

CREATE TRIGGER trg_pengabdian_updated_at
    BEFORE UPDATE ON public.pengabdian
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 12. FASILITAS
-- ============================================================================
CREATE TABLE public.fasilitas (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    judul_fasilitas VARCHAR(255) NOT NULL,
    deskripsi       TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.fasilitas IS 'Fasilitas laboratorium dll. Foto/media di tabel media.';

CREATE TRIGGER trg_fasilitas_updated_at
    BEFORE UPDATE ON public.fasilitas
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 13. TRI DHARMA
-- ============================================================================
CREATE TABLE public.tri_dharma (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    judul      VARCHAR(500) NOT NULL,
    deskripsi  TEXT,
    tanggal    DATE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.tri_dharma IS 'Arsip kegiatan Tri Dharma. File/foto di tabel media.';

CREATE TRIGGER trg_tri_dharma_updated_at
    BEFORE UPDATE ON public.tri_dharma
    FOR EACH ROW
    EXECUTE FUNCTION public.fn_set_updated_at();

-- ============================================================================
-- 14. MEDIA GLOBAL
-- Polymorphic table, jadi tidak dipaksa FK ke satu tabel saja.
-- ============================================================================
CREATE TABLE public.media (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL
                    CHECK (entity_type IN ('penelitian','pengabdian','fasilitas','tri_dharma')),
    entity_id   UUID        NOT NULL,
    file_url    TEXT        NOT NULL,
    tipe_file   VARCHAR(20) NOT NULL DEFAULT 'image'
                    CHECK (tipe_file IN ('image','pdf','video')),
    urutan      INT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.media IS 'Tabel media global (polymorphic). Foto/file untuk penelitian, pengabdian, fasilitas, tri_dharma.';
COMMENT ON COLUMN public.media.entity_type IS 'Nama entitas: penelitian | pengabdian | fasilitas | tri_dharma';
COMMENT ON COLUMN public.media.file_url IS 'Path file, misalnya storage/{entity_type}/{entity_id}/{filename}';
COMMENT ON COLUMN public.media.urutan IS '0 = cover/thumbnail. Sort ASC.';

CREATE INDEX idx_media_entity_urutan_comp
    ON public.media (entity_type, entity_id, urutan ASC);

CREATE INDEX idx_media_entity_image_partial
    ON public.media (entity_type, entity_id, urutan ASC)
    WHERE tipe_file = 'image';

-- ============================================================================
-- SEED DATA
-- ============================================================================
INSERT INTO public.keahlian (nama_keahlian) VALUES
    ('IoT'), ('Robotika'), ('Kontroler'),
    ('Keamanan Jaringan'), ('Jaringan Komputer'),
    ('Web Programming'), ('Mobile Programming'),
    ('Basis Data'), ('Elektronika Analog'),
    ('Data Science'), ('Kecerdasan Buatan'),
    ('Sistem Tertanam'), ('Cloud Computing')
ON CONFLICT (nama_keahlian) DO NOTHING;

INSERT INTO public.statistik_mahasiswa (tahun, jumlah_pendaftar, jumlah_diterima, jumlah_lulusan) VALUES
    (2022, 356, 180, 162),
    (2023, 410, 178, 180),
    (2024, 445, 192, 195),
    (2025, 429, 198, 267)
ON CONFLICT (tahun) DO NOTHING;

-- ============================================================================
-- CONTOH INSERT USER ADMIN
-- Password pakai bcrypt hash.
-- Jalankan dari backend juga bisa, atau langsung di SQL Editor untuk testing.
-- ============================================================================
-- INSERT INTO public.users (email, password_hash, nama, role)
-- VALUES (
--     'admin@polimdo.ac.id',
--     crypt('admin123', gen_salt('bf')),
--     'Administrator',
--     'admin'
-- );