package org.applab.ledgerlink;

import android.app.Dialog;
import android.content.Context;
import android.widget.EditText;

import org.applab.ledgerlink.domain.model.DataRecovery;
import org.applab.ledgerlink.domain.model.VslaInfo;

/**
 * Created by Ayvan
 * 17//01/2020
 */
public class VslaCodeDialog extends Dialog {
    protected DataRecovery dataRecovery;
    LedgerLinkApplication ledgerLinkApplication;
    private VslaInfo vslaInfo = null;

    public VslaCodeDialog(Context context) {
        super(context);
        Context mContext = context;
        this.setContentView(R.layout.dialog_vslacode);
        this.dataRecovery = new DataRecovery();
        ledgerLinkApplication = (LedgerLinkApplication) getContext();

        EditText txtVslaCode = (EditText)findViewById(R.id.enterVslaCodeAmount);
        VslaInfo vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();
        //String vslaName = vslaInfo.getVslaName();
        String vslaCode = vslaInfo.getVslaCode();

        //VslaInfoRepo vslaInfoRepo = new VslaInfoRepo();
        //VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
        txtVslaCode.setText(vslaCode);
        //dataRecovery.setVslaCode(txtVslaCode.getText().toString().trim());

    }
}