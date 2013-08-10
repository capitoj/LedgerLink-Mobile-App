package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.MembersCustomArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Moses on 6/24/13.
 */
public class BeginCycleMembersFrag extends Fragment {

    ArrayList<Member> members = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View fragView =  inflater.inflate(R.layout.frag_begin_cycle_members, container, false);

        //Pass the View and Inflator to assist in iteraction with xml layout files and widgets
        populateMembersList(fragView, inflater);

        return fragView;
    }

    //Populate Members List
    private void populateMembersList(View fragView, LayoutInflater inflater) {
        //Load the Main Menu
        members = new ArrayList<Member>();

        //Use the CustomArrayAdapter to load the items into the listview
        ListView lvwMembers = (ListView) fragView.findViewById(android.R.id.list);

        //Manually set the EmptyTextView
        TextView txtEmptyText = (TextView) fragView.findViewById(android.R.id.empty);
        lvwMembers.setEmptyView(txtEmptyText);

        //Now get the data via the adapter
        MembersCustomArrayAdapter adapter = new MembersCustomArrayAdapter(fragView.getContext(), members);

        //Assign Adapter to ListView
        lvwMembers.setAdapter(adapter);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Launching new Activity on selecting single List Item
                Member selectedMember = (Member) members.get(position);
                Intent viewMember = new Intent(view.getContext(), MemberDetailsViewActivity.class);
                startActivity(viewMember);

                //Toast.makeText(view.getContext(), selectedMember.toString() + " is " + ((selectedMember.isActive()) ? "Active" : "Not Active"), Toast.LENGTH_LONG).show();
            }
        });
    }
}