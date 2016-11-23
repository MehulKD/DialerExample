package com.dalbers.dialer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.dalbers.dialer.DataHandling.Contact;
import com.dalbers.dialer.DataHandling.Recent;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    private RecyclerView contactsList;
    private LinearLayoutManager layoutManager;
    private ContactsAdapter adapter;
    private ArrayList<Recent> recents;
    private ArrayList<Contact> contacts;
    private EditText searchBar;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recents = getArguments().getParcelableArrayList("recents");
            contacts = getArguments().getParcelableArrayList("contacts");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsList = (RecyclerView) view.findViewById(R.id.contacts_list);
        contactsList.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.getContext());
        contactsList.setLayoutManager(layoutManager);

        adapter = new ContactsAdapter(contacts,recents,contactsList);
        contactsList.setAdapter(adapter);

        searchBar = (EditText) view.findViewById(R.id.search_bar);
        //listen for changes in the textview
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                //update the contact list with only contacts containing the typed number string
                ArrayList<Contact> matchedContacts = new ArrayList<Contact>();
                for(Contact contact : contacts) {
                    //remove non-numbers from strings
                    String strippedNumber = contact.getNumber().replaceAll("[^\\d.]", "");
                    String strippedTyped = searchBar.getText().toString().replaceAll("[^\\d.]", "");
                    if(strippedNumber.contains(strippedTyped))
                        matchedContacts.add(contact);
                }
                //put new data in adapter and update
                adapter.setContacts(matchedContacts);
                adapter.notifyDataSetChanged();
            }
        });
        //on enter pressed
        searchBar.setOnEditorActionListener((new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                switch (actionId) {

                    case EditorInfo.IME_ACTION_DONE:
                        //hide the keyboard
                        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return true;

                    default:
                        return false;
                }
            }
        }));
        return view;
    }


}
