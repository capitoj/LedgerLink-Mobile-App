package org.applab.ledgerlink;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import org.applab.ledgerlink.domain.model.DataRecovery;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.repo.VslaInfoRepo;

/**
 * Created by Ayvan
 * 17//01/2020
 */
public class VslaCodeDialog extends Dialog {
    protected DataRecovery dataRecovery;

    public VslaCodeDialog(Context context) {
        super(context);
        Context mContext = context;
        this.dataRecovery = new DataRecovery();


}
    @Override
 public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_vslacode);

        EditText txtVslaCode = (EditText)findViewById(R.id.enterVslaCodeAmount);

        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo();
        VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
        txtVslaCode.setText(vslaInfo.getVslaCode());
        //dataRecovery.setVslaCode(txtVslaCode.getText().toString().trim());


    }
}