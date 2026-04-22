package infrastructure.di

import presentation.controller.*
import application.usecase.tri_dharma.ManageTriDharmaUseCase


class ControllerModule(private val useCaseModule: UseCaseModule) {
    val authController by lazy { AuthController(useCaseModule.loginUseCase) }
    val dosenController by lazy { DosenController(useCaseModule.manageDosenUseCase, useCaseModule.getAllDosenUseCase) }
    val statistikController by lazy { StatistikController(useCaseModule.manageStatistikUseCase) }
    val jadwalController by lazy { JadwalPerkuliahanController(useCaseModule.manageJadwalUseCase) }
    val fasilitasController by lazy { FasilitasController(useCaseModule.manageFasilitasUseCase) }
    val triDharmaController by lazy { TriDharmaController(useCaseModule.manageTriDharmaUseCase) }
    val penelitianController by lazy { PenelitianController(useCaseModule.managePenelitianUseCase) }
    val publikasiController by lazy { PublikasiController(useCaseModule.managePublikasiUseCase) }
    val pengabdianController by lazy { PengabdianController(useCaseModule.managePengabdianUseCase) }
    val hkiController by lazy { HKIController(useCaseModule.manageHKIUseCase) }
    val bukuAjarController by lazy { BukuAjarController(useCaseModule.manageBukuAjarUseCase) }
    val sertifikatController by lazy { SertifikatController(useCaseModule.manageSertifikatUseCase) }
}
