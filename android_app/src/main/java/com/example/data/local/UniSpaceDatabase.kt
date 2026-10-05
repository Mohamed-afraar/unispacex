package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        BusinessListingEntity::class,
        ProductEntity::class,
        ServiceEntity::class,
        CollaborationEntity::class,
        FeedPostEntity::class,
        UserSessionEntity::class,
        VerificationRequestEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class UniSpaceDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun businessListingDao(): BusinessListingDao
    abstract fun productDao(): ProductDao
    abstract fun serviceDao(): ServiceDao
    abstract fun collaborationDao(): CollaborationDao
    abstract fun feedPostDao(): FeedPostDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun verificationRequestDao(): VerificationRequestDao

    companion object {
        const val DATABASE_NAME = "unispace_cosmos.db"

        @Volatile
        private var INSTANCE: UniSpaceDatabase? = null

        fun getInstance(context: Context): UniSpaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UniSpaceDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
