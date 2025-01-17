package org.dieschnittstelle.mobile.android.skeleton.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Todo implements Serializable{

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    private String description;

    private long expiry;

    private boolean done;

    private boolean favourite;

    private ArrayList<String> contacts;

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

    public ArrayList<String> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<String> contacts) {
        this.contacts = contacts;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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
