package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.BusinessListingDao
import com.example.data.local.CollaborationDao
import com.example.data.local.FeedPostDao
import com.example.data.local.ProductDao
import com.example.data.local.ServiceDao
import com.example.data.local.UniSpaceDatabase
import com.example.data.local.UserProfileDao
import com.example.data.local.UserSessionDao
import com.example.data.local.VerificationRequestDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UniSpaceDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            UniSpaceDatabase::class.java,
            UniSpaceDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserProfileDao(db: UniSpaceDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideBusinessListingDao(db: UniSpaceDatabase): BusinessListingDao = db.businessListingDao()

    @Provides
    fun provideProductDao(db: UniSpaceDatabase): ProductDao = db.productDao()

    @Provides
    fun provideServiceDao(db: UniSpaceDatabase): ServiceDao = db.serviceDao()

    @Provides
    fun provideCollaborationDao(db: UniSpaceDatabase): CollaborationDao = db.collaborationDao()

    @Provides
    fun provideFeedPostDao(db: UniSpaceDatabase): FeedPostDao = db.feedPostDao()

    @Provides
    fun provideUserSessionDao(db: UniSpaceDatabase): UserSessionDao = db.userSessionDao()

    @Provides
    fun provideVerificationRequestDao(db: UniSpaceDatabase): VerificationRequestDao = db.verificationRequestDao()

    @Provides
    @Singleton
    fun provideSyncUtility(db: UniSpaceDatabase): com.example.data.sync.RoomToFirestoreSyncUtility =
        com.example.data.sync.RoomToFirestoreSyncUtility(db)
}
