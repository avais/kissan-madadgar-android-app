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
import pk.kissanmadadgar.mobile.data.mock.*
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
    fun provideAuthRepository(): AuthRepository {
        return MockAuthRepository()
    }

    @Provides
    @Singleton
    fun provideMachineryRepository(): MachineryRepository {
        return MockMachineryRepository()
    }

    @Provides
    @Singleton
    fun provideBookingRepository(): BookingRepository {
        return MockBookingRepository()
    }
}
