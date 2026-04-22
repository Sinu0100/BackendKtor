package infrastructure.di

import application.usecase.auth.LoginUseCase
import application.usecase.dosen.*
import application.usecase.statistik.ManageStatistikMahasiswaUseCase
import application.usecase.jadwal.ManageJadwalPerkuliahanUseCase
import application.usecase.fasilitas.ManageFasilitasUseCase
import application.usecase.tri_dharma.ManageTriDharmaUseCase

import application.usecase.penelitian.ManagePenelitianUseCase
import application.usecase.publikasi.ManagePublikasiUseCase
import application.usecase.pengabdian.ManagePengabdianUseCase
import application.usecase.hki.ManageHKIUseCase
import application.usecase.buku.ManageBukuAjarUseCase
import application.usecase.sertifikat.ManageSertifikatUseCase
import application.usecase.media.ManageMediaUseCase
import infrastructure.security.JwtService
import infrastructure.storage.StorageService

class UseCaseModule(
    private val repositoryModule: RepositoryModule,
    private val jwtService: JwtService,
    private val storageService: StorageService
) {
    val manageMediaUseCase by lazy {
        ManageMediaUseCase(repositoryModule.mediaRepository, storageService)
    }

    val loginUseCase by lazy { 
        LoginUseCase(repositoryModule.dosenRepository, jwtService) 
    }
    
    val manageDosenUseCase by lazy { 
        ManageDosenUseCase(repositoryModule.dosenRepository, manageMediaUseCase)
    }
    
    val getAllDosenUseCase by lazy { 
        GetAllDosenUseCase(repositoryModule.dosenRepository) 
    }

    val manageStatistikUseCase by lazy {
        ManageStatistikMahasiswaUseCase(repositoryModule.statistikRepository)
    }

    val manageJadwalUseCase by lazy {
        ManageJadwalPerkuliahanUseCase(repositoryModule.jadwalRepository, manageMediaUseCase)
    }

    val manageFasilitasUseCase by lazy {
        ManageFasilitasUseCase(repositoryModule.fasilitasRepository, manageMediaUseCase)
    }

    val manageTriDharmaUseCase by lazy {
        ManageTriDharmaUseCase(repositoryModule.triDharmaRepository, manageMediaUseCase)
    }

    val managePenelitianUseCase by lazy {
        ManagePenelitianUseCase(
            repositoryModule.penelitianRepository, 
            repositoryModule.dosenRepository,
            manageMediaUseCase
        )
    }

    val managePublikasiUseCase by lazy {
        ManagePublikasiUseCase(
            repositoryModule.publikasiRepository,
            repositoryModule.dosenRepository,
            manageMediaUseCase
        )
    }

    val managePengabdianUseCase by lazy {
        ManagePengabdianUseCase(
            repositoryModule.pengabdianRepository,
            repositoryModule.dosenRepository,
            manageMediaUseCase
        )
    }

    val manageHKIUseCase by lazy {
        ManageHKIUseCase(
            repositoryModule.hkiRepository,
            repositoryModule.dosenRepository,
            manageMediaUseCase
        )
    }

    val manageBukuAjarUseCase by lazy {
        ManageBukuAjarUseCase(
            repositoryModule.bukuAjarRepository, 
            repositoryModule.dosenRepository,
            manageMediaUseCase
        )
    }

    val manageSertifikatUseCase by lazy {
        ManageSertifikatUseCase(
            repositoryModule.sertifikatRepository, 
            repositoryModule.dosenRepository, 
            manageMediaUseCase
        )
    }
}
