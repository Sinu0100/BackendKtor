package infrastructure.di

import domain.repository.*
import infrastructure.repository.*

class RepositoryModule {
    val mediaRepository: MediaRepository by lazy { 
        MediaRepositoryImpl() 
    }
    
    val dosenRepository: DosenRepository by lazy { 
        DosenRepositoryImpl() 
    }
    
    val statistikRepository: StatistikMahasiswaRepository by lazy { 
        StatistikMahasiswaRepositoryImpl() 
    }
    
    val jadwalRepository: JadwalPerkuliahanRepository by lazy {
        JadwalPerkuliahanRepositoryImpl()
    }
    
    val fasilitasRepository: FasilitasRepository by lazy { 
        FasilitasRepositoryImpl(mediaRepository)
    }

    val triDharmaRepository: TriDharmaRepository by lazy {
        TriDharmaRepositoryImpl(mediaRepository)
    }

    val penelitianRepository: PenelitianRepository by lazy {
        PenelitianRepositoryImpl(mediaRepository)
    }

    val publikasiRepository: PublikasiRepository by lazy {
        PublikasiRepositoryImpl()
    }

    val pengabdianRepository: PengabdianRepository by lazy {
        PengabdianRepositoryImpl(mediaRepository)
    }

    val hkiRepository: HKIRepository by lazy {
        HKIRepositoryImpl()
    }

    val bukuAjarRepository: BukuAjarRepository by lazy {
        BukuAjarRepositoryImpl()
    }

    val sertifikatRepository: SertifikatRepository by lazy {
        SertifikatRepositoryImpl()
    }
}
