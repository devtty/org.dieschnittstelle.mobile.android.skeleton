package org.dieschnittstelle.mobile.android;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.dieschnittstelle.mobile.android.skeleton.model.RoomLocalTodoCRUDOperations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dieschnittstelle.mobile.android.skeleton.model.Todo;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SimpleTodoReadWriteTest {

    private RoomLocalTodoCRUDOperations.TodoDao todoDao;
    private RoomLocalTodoCRUDOperations.TodoDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
	    db = Room.inMemoryDatabaseBuilder(context, RoomLocalTodoCRUDOperations.TodoDatabase.class).build();
	    todoDao = db.getDao();
    }

    @After
    public void closeDb(){
	    db.close();
    }


    @Test
    public void writeTodoAndReadInList() {
        String testName = "Test Name";
	    Todo t1 = new Todo();
	    t1.setName(testName);
	    long id = todoDao.create(t1);

	    Todo t2 = todoDao.readById(id);

	    assertEquals(t2.getName(), testName);
    }    
}
