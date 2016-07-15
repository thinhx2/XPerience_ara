/*
* Copyright (C) 2012 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.internal.telephony;

import static com.android.internal.telephony.RILConstants.*;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Message;
import android.os.Parcel;

public class X2RIL extends RIL implements CommandsInterface {
	public X2RIL(Context context, int networkMode, int cdmaSubscription, Integer instanceId) {
		super(context, networkMode, cdmaSubscription, instanceId);
	}


   static final int RIL_REQUEST_SET_UICC_SUBSCRIPTION_X2 = 114;
   static final int RIL_REQUEST_SET_DATA_SUBSCRIPTION_X2 = 115;

    @Override
    public void setUiccSubscription(int slotId, int appIndex, int subId,
            int subStatus, Message result) {
        //Note: This RIL request is also valid for SIM and RUIM (ICC card)
        RILRequest rr = RILRequest.obtain(RIL_REQUEST_SET_UICC_SUBSCRIPTION_X2, result);

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest)
                + " slot: " + slotId + " appIndex: " + appIndex
                + " subId: " + subId + " subStatus: " + subStatus);

        rr.mParcel.writeInt(slotId);
        rr.mParcel.writeInt(appIndex);
        rr.mParcel.writeInt(subId);
        rr.mParcel.writeInt(subStatus);

        send(rr);
    }

//    @Override
//    public void setDataSubscription(Message result) {
//        RILRequest rr = RILRequest.obtain(RIL_REQUEST_SET_DATA_SUBSCRIPTION_X2, result);
//        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
//        send(rr);
//    }

    @Override
    protected void
    processUnsolicited (Parcel p) {
        Object ret;
        int dataPosition = p.dataPosition(); // save off position within the Parcel
        int response = p.readInt();

        switch(response) {
            case 1041: ret =  responseInts(p); break; // RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED

            default:
                // Rewind the Parcel
                p.setDataPosition(dataPosition);

                // Forward responses that we are not overriding to the super class
                super.processUnsolicited(p);
                return;
        }

        switch(response) {
            case 1041: { // RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED
                if (RILJ_LOGD) unsljLogRet(response, ret);
            
                if (mSubscriptionStatusRegistrants != null) {
                    mSubscriptionStatusRegistrants.notifyRegistrants(
                                        new AsyncResult (null, ret, null));
                }
                break;
	    }
        }
    }


}
