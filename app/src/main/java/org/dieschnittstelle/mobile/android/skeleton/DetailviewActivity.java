package org.dieschnittstelle.mobile.android.skeleton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityDetailviewBinding;

import org.dieschnittstelle.mobile.android.skeleton.model.ITodoCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.Todo;
import org.dieschnittstelle.mobile.android.skeleton.util.MADAsyncOperationRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;

public class DetailviewActivity extends AppCompatActivity implements DetailviewViewmodel{

    private static final String LOGGER = "DetailViewActivity";
    private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm", Locale.GERMANY);

    public static final String ARG_ITEM_ID = "itemId";

    public static final int STATUS_CREATED = 42;
    public static final int STATUS_UPDATED = 43;
    public static final int STATUS_DELETED = 44;

    String errorStatus = null;

    /* and there was light. And God saw the light, that it was good;
     *
     * Konstante repräsentiert das Ende des ersten Tages
     * Falls für die Faelligkeit nur eine Uhrzeit (aber kein Tag) ausgewaehlt wurde, liegt expiry
     * zwischen 0 und diesem Wert. Durch den kleinen Hack verliert man zwar den ersten Tag als
     * Faelligkeitsdatum, aber das kann man verschmerzen ;-) und erspart sich das Aufteilen von
     * expiry in zwei Variablen (date/time)
     *
     * Nachtrag: 06/19/22 - Implementation fuer die Aktualisierung hat sich geaendert und diese
     * Variable könnte auch 0 sein, ich lass das aber mal als Osterei drin.
     *
     * */
    private static final int  LET_THERE_BE_LIGHT = 82799;

    private Todo todo;
    private ActivityDetailviewBinding binding;

    private MADAsyncOperationRunner operationRunner;
    private ITodoCRUDOperations crudOperations;

    private ActivityResultLauncher<Intent> selectContactLauncher;

    private ArrayAdapter<String> contactViewAdapter;
    private ArrayList<String> contactListItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this,R.layout.activity_detailview);

        this.selectContactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        onContactSelected(result.getData());
                    }
                }
        );

        this.crudOperations = ((TodoApplication) getApplication()).getCrudOperations();

        this.operationRunner = new MADAsyncOperationRunner(this, null);


        long todoId = getIntent().getLongExtra(ARG_ITEM_ID, -1);

        if(todoId != -1){
            operationRunner.run(
                    //operation
                    () -> this.crudOperations.readTodo(todoId),
                    //onOperationResult
                    todo -> {
                        this.todo = todo;
                        this.binding.setController(this);
                        if(todo.getContacts()!=null)
                            contactListItems.addAll(todo.getContacts());
                    });
        }

        Log.i(LOGGER, "showing detailview for todo: " + todo );

        if(todo == null){
            this.todo = new Todo();
        }

        this.binding.setController(this);

        ListView contactListView = findViewById(R.id.contactList);
        contactViewAdapter = initializeContactViewAdapter();
        contactListView.setAdapter(contactViewAdapter);

    }

    public Todo getTodo(){
        return this.todo;
    }

    public void onSaveItem(){
        Intent returnIntent = new Intent();

        int resultCode = todo.getId() > 0 ? STATUS_UPDATED : STATUS_CREATED;

        String datePartOfExpiry = ((TextInputEditText) binding.getRoot().findViewById(R.id.itemExpiry)).getText().toString();
        String timePartOfExpiry = ((TextInputEditText) binding.getRoot().findViewById(R.id.itemExpiryTime)).getText().toString();

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMANY);
        try {
            Date updateExpiry = formatter.parse(datePartOfExpiry + " " + timePartOfExpiry);
            todo.setExpiry(updateExpiry.getTime());
        }catch (ParseException e){
            Log.i(LOGGER, "ParseException");
        }

        //todo.setContacts(Arrays.toString(contactListItems.toArray()));
        todo.setContacts(contactListItems);

        operationRunner.run(() ->
               todo.getId() > 0 ? crudOperations.updateTodo(todo) : crudOperations.createTodo(todo),
               todo -> {
                     this.todo = todo;
                     returnIntent.putExtra(ARG_ITEM_ID, this.todo.getId());
                     setResult(resultCode, returnIntent);
                     finish();
               });
    }

    public void onDeleteItem(){
        Intent returnIntent = new Intent();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Wirklich Löschen")
                .setNeutralButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) ->
                    operationRunner.run(() -> crudOperations.deleteTodo(todo.getId()),
                            success -> {
                                if(success){
                                    setResult(STATUS_DELETED, returnIntent);
                                    finish();
                                }
                    })).show();
    }

    public void onExpirySelected(){
        final Calendar calendar = Calendar.getInstance();

        if(todo.getExpiry()>LET_THERE_BE_LIGHT)
            calendar.setTimeInMillis(todo.getExpiry());

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, month);
            newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //an der Stelle verzichten wir auf L10n
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
            String r = df.format(new Date(newDate.getTimeInMillis()));

            //todo.setExpiry(newDate.getTimeInMillis());
            ((TextInputEditText) binding.getRoot().findViewById(R.id.itemExpiry)).setText(r);
            binding.getRoot().findViewById(R.id.itemExpiryTime).setEnabled(true);
        }, calendar.get(Calendar.YEAR) , calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public void onExpiryTimeSelected(){
        final Calendar calendar = Calendar.getInstance();
        if(todo.getExpiry()>LET_THERE_BE_LIGHT)
            calendar.setTimeInMillis(todo.getExpiry());

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            Calendar newTime = Calendar.getInstance();
            newTime.setTimeInMillis(todo.getExpiry());
            newTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            newTime.set(Calendar.MINUTE, minute);
            String r = TIME_FORMATTER.format(newTime.getTime());
            ((TextInputEditText) binding.getRoot().findViewById(R.id.itemExpiryTime)).setText(r);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    @Override
    public boolean checkFieldInputCompleted(View v, int actionId, boolean hasFocus, boolean isCalledOnFocusChange){
        Log.i(LOGGER, "checkFieldInputCompleted" + v + ", ");
        if(isCalledOnFocusChange ? !hasFocus
                : (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NEXT)){

            if(todo.getName()!=null && todo.getName().length() < 5) {
                errorStatus = "Name to short!";
                this.binding.setController(this);
            }
        }
        return false;
    }

    public String getErrorStatus(){
        return errorStatus;
    }

    @Override
    public boolean onNameFieldInputChanged(){
        Log.i(LOGGER, "onNameFieldInput(): error status is currently: " + this.errorStatus);
        if(this.errorStatus != null) {
            this.errorStatus = null;
            this.binding.setController(this);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.detailview_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.selectContact){
            selectContact();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    public void selectContact(){
        Log.i(LOGGER, "selectContact");
        Intent selectContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        this.selectContactLauncher.launch(selectContactIntent);
    }

    public void onContactSelected(Intent resultData){
        Log.i(LOGGER, "onContactSelected(): " + resultData);

        showContactDetails(resultData.getData());

    }

    private Uri latestSelectedContactUri;
    private static final int REQUEST_PERMISSION_REQUEST_CODE = 42;


    //damit anwendung bei fehlenden Rechten nicht unterbrochen wird
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LOGGER, "onRequestPermissionResult");
        if(requestCode == REQUEST_PERMISSION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permission granted by user
                if(latestSelectedContactUri != null){
                    showContactDetails(latestSelectedContactUri);
                }else{
                    Toast.makeText(this, "Cannot continue granted. No contact selected", Toast.LENGTH_SHORT).show();
                }
            }else{
                // permissions not granted
                Toast.makeText(this, "Contacts cannot be accessed", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showContactDetails(Uri contactUri){
        Log.i(LOGGER, "showContactDetails");
        //Permission Request
        int hasReadContactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
        if(hasReadContactsPermission != PackageManager.PERMISSION_GRANTED){
            latestSelectedContactUri = contactUri;
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
            return;
        }

        // Get Contact
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if(cursor.moveToFirst()){
            Log.i(LOGGER, "moved to first element of query result");
            int contactNamePosition = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            String contactName = cursor.getString(contactNamePosition);
            Log.i(LOGGER, "got contact name: " + contactName);
            int internalIdPosition = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            long internalId = cursor.getLong(internalIdPosition);
            Log.i(LOGGER, "got internal id: " + internalId);
            //ui in letzter letzt semesteraufz
            contactListItems.add(String.valueOf(internalId));
            this.contactViewAdapter.notifyDataSetChanged();
            showContactDetailsForInternalId(internalId);
        }
    }

    public Contact showContactDetailsForInternalId(long internalId){
        Log.i(LOGGER, "showContactDetailForInternalId(): " + internalId);
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null,
                "_id=?",
                new String[]{String.valueOf(internalId)}, null);

        if(cursor.moveToFirst()){
            @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Log.i(LOGGER, "got displayName: " + displayName);
            //return displayName;

            String mobile = null;

            Cursor phoneNumberCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[]{String.valueOf(internalId)},
                    null);
            while (phoneNumberCursor.moveToNext()){
                @SuppressLint("Range") String number = phoneNumberCursor.getString(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                @SuppressLint("Range") int phoneNumberType = phoneNumberCursor.getInt(phoneNumberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                Log.i(LOGGER, "got number " + number + " of type " + (phoneNumberType==ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ? " mobile " : " not mobile"));
                if(phoneNumberType==ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    mobile = number;
            }

            String mailAddr = null;

            Cursor mailcursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                    new String[]{String.valueOf(internalId)},
                    null);
            while(mailcursor.moveToNext()){
                int mailAddrIndex = mailcursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                mailAddr = mailcursor.getString(mailAddrIndex);
                Log.i(LOGGER, "found mail address: " + mailAddr);
            }

            Log.i(LOGGER, "Contact: " + displayName + mailAddr + mobile);
            return new Contact(displayName, mailAddr, mobile);
        }else{
            Toast.makeText(this, "No Contacts found for internal id " + internalId + " ...", Toast.LENGTH_SHORT).show();
            return null;
        }
    }



    private ArrayAdapter<String> initializeContactViewAdapter(){
        Log.i(LOGGER, "initializeContactViewAdapter");
        return new ArrayAdapter<>(this, R.layout.contact_list, contactListItems){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                Log.i(LOGGER, "Adapterinit : " + contactListItems.toString());
                ViewGroup contactView = (ViewGroup) getLayoutInflater().inflate(R.layout.contact_list,null);

                TextView contactName = contactView.findViewById(R.id.contactName);
                ImageView removeContact = contactView.findViewById(R.id.removeButton);
                ImageView callContact = contactView.findViewById(R.id.callButton);
                ImageView mailContact = contactView.findViewById(R.id.mailButton);


                Long internalId = Long.parseLong(super.getItem(position));

                removeContact.setOnClickListener(v -> contactViewAdapter.remove(contactViewAdapter.getItem(position)));

                if(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    Contact detail = showContactDetailsForInternalId(internalId);
                    if(detail != null){
                        contactName.setText(detail.getName());
                        if(detail.getMobile()!=null){
                            callContact.setOnClickListener(v -> sendSMS(detail.getMobile()));
                        }else{
                            callContact.setEnabled(false);
                        }
                        if(detail.getEmail()!=null){
                            mailContact.setOnClickListener(v -> sendMail(detail.getEmail()));
                        }else{
                            mailContact.setEnabled(false);
                        }
                    }
                }
                return contactView;

            }
        };

    }

    public void sendSMS(String mobileNumber){
        Uri receiverPhoneUri = Uri.parse("smsto:" + mobileNumber);
        Intent sendSMSIntent = new Intent(Intent.ACTION_SENDTO, receiverPhoneUri);
        sendSMSIntent.putExtra("sms_body", this.todo.getName());
        startActivity(sendSMSIntent);
    }

    public void sendMail(String mailAddr){
        Uri mailUri = Uri.parse("mailto:" + mailAddr);
        Intent sendMailIntent = new Intent(Intent.ACTION_SENDTO, mailUri);
        sendMailIntent.putExtra(Intent.EXTRA_EMAIL, mailAddr);
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, this.todo.getName());
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, this.todo.getDescription());
        startActivity(sendMailIntent);
    }

    @BindingAdapter("expiryDate")
    public static void bindExpiryDate(@NonNull TextView textView, long time){
        if(time>0){
            DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
            textView.setText(df.format(new Date(time)));
        }

    }
    @BindingAdapter("expiryTime")
    public static void bindExpiryTime(@NonNull TextView textView, long time){
        if(time>0){
            DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
            textView.setText(df.format(new Date(time)));
        }

    }
}
