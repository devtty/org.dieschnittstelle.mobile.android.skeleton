package org.dieschnittstelle.mobile.android.skeleton.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Todo.class}, version = 1)
@TypeConverters({ArrayConverter.class})
//TODO can be deleted
public abstract class AppDatabase extends RoomDatabase {

    public abstract TodoDao getTodoDao();
    
}
