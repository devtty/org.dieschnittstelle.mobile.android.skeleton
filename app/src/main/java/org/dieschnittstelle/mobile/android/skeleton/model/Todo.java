package org.dieschnittstelle.mobile.android.skeleton.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

// TODO get model from web app (sample web api)

@Entity
public class Todo implements Serializable{

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    private String description;

    private long expiry;

    private boolean done;

    private boolean favourite;

    private ArrayList<Long> contacts;

    private String location;

    public long getId(){
	return id;
    }

    public void setId(long id){
	this.id = id;
    }

    public String getName(){
	return name;
    }

    public void setName(String name){
	this.name = name;
    }

    public String getDescription(){
	return description;
    }

    public void setDescription(String description){
	this.description = description;
    }

    public long getExpiry(){
	return expiry;
    }

    public void setExpiry(long expiry){
	this.expiry = expiry;
    }

    public boolean isDone(){
	return done;
    }

    public void setDone(boolean done){
	this.done = done;
    }

    public boolean isFavourite(){
	return favourite;
    }

    public void setFavourite(boolean favourite){
	this.favourite = favourite;
    }

    public ArrayList<Long> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Long> contacts) {
        this.contacts = contacts;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
/*
    @Ignore public boolean isExpired(){
        if(this.expiry > System.currentTimeMillis()){
            return true;
        }

        return false;
    }
*/
    public Todo(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return id == todo.id;
    }

    @Override
    public int hashCode(){return Objects.hash(id);}

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", expiry=" + expiry +
                ", done=" + done +
                ", favourite=" + favourite +
                ", super.toString()=" + super.toString() +
                '}';
    }
}
