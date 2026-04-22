package infrastructure.database.tables

object MediaTable {
    const val TABLE_NAME = "media"
    const val ID = "id"
    const val ENTITY_TYPE = "entity_type" // 'fasilitas' atau 'tri_dharma'
    const val ENTITY_ID = "entity_id"     // UUID atau ID entitas terkait
    const val FILE_URL = "file_url"
    const val CREATED_AT = "created_at"
}
