package pk.kissanmadadgar.mobile.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pk.kissanmadadgar.mobile.core.security.SessionManager
import pk.kissanmadadgar.mobile.data.local.KissanDatabase
import pk.kissanmadadgar.mobile.data.local.*
import pk.kissanmadadgar.mobile.data.local.dao.BookingDao
import pk.kissanmadadgar.mobile.data.remote.api.AuthApiService
import pk.kissanmadadgar.mobile.data.repository.AuthRepositoryImpl
import pk.kissanmadadgar.mobile.data.repository.BookingRepositoryImpl
import pk.kissanmadadgar.mobile.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KissanDiModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideKissanDatabase(
        @ApplicationContext context: Context
    ): KissanDatabase {
        return Room.databaseBuilder(
            context,
            KissanDatabase::class.java,
            "kissan_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        sessionManager: SessionManager,
        @ApplicationContext context: Context
    ): AuthRepository {
        return AuthRepositoryImpl(authApiService, sessionManager, context)
    }

    @Provides
    @Singleton
    fun provideMachineryRepository(): MachineryRepository {
        return InMemoryMachineryRepository()
    }

    @Provides
    @Singleton
    fun provideBookingDao(database: KissanDatabase): BookingDao {
        return database.bookingDao()
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        bookingDao: BookingDao,
        sessionManager: SessionManager
    ): BookingRepository {
        return BookingRepositoryImpl(bookingDao, sessionManager)
    }
}
