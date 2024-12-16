package com.vishnu.remindme.hilt

import android.content.Context
import androidx.room.Room
import com.vishnu.remindme.db.ReminderDAO
import com.vishnu.remindme.db.ReminderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideDatabase(context: Context): ReminderDatabase {
        return Room.databaseBuilder(
            context,
            ReminderDatabase::class.java,
            "reminder_items"
        ).build()
    }

    @Provides
    fun provideNoteDao(database: ReminderDatabase): ReminderDAO {
        return database.reminderDAO()
    }
}