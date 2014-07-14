package org.applab.digitizingdata;

import android.app.Application;
import android.content.Context;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;




/**
 * Created by John Mark on 7/11/2014.
 */
public class LedgerLinkApplication extends Application
{
    ArrayList<Member> allMembers;
    private MemberRepo memberRepo;

    public LedgerLinkApplication() {
      super();

    }


}
