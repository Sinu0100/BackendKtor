package infrastructure.database.tables

object PenelitianTable {
    const val TABLE_NAME = "penelitian"
    const val ID = "id"
    const val DOSEN_ID = "dosen_id"
    const val JUDUL = "judul"
    const val TAHUN = "tahun"
    const val DESKRIPSI = "deskripsi"
    const val CREATED_AT = "created_at"
    const val UPDATED_AT = "updated_at"

    object Anggota {
        const val TABLE_NAME = "penelitian_anggota"
        const val ID = "id"
        const val PENELITIAN_ID = "penelitian_id"
        const val DOSEN_ID = "dosen_id"
        const val PERAN = "peran"
    }
}
