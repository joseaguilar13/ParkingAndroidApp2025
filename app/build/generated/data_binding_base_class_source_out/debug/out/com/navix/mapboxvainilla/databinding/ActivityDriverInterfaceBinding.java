// Generated by view binder compiler. Do not edit!
package com.navix.mapboxvainilla.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.navix.mapboxvainilla.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityDriverInterfaceBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnBooking;

  @NonNull
  public final Button btnMessage;

  @NonNull
  public final Button btnProfile;

  @NonNull
  public final EditText edtAccountId;

  @NonNull
  public final EditText edtDriverEmail;

  @NonNull
  public final EditText edtDriverName;

  @NonNull
  public final EditText edtLicenseNumber;

  @NonNull
  public final EditText edtPhoneNumber;

  @NonNull
  public final EditText edtPlateNumber;

  @NonNull
  public final EditText edtVehicleType;

  @NonNull
  public final Spinner spinnerAccountType;

  @NonNull
  public final Spinner spinnerPaymentMethod;

  private ActivityDriverInterfaceBinding(@NonNull LinearLayout rootView, @NonNull Button btnBooking,
      @NonNull Button btnMessage, @NonNull Button btnProfile, @NonNull EditText edtAccountId,
      @NonNull EditText edtDriverEmail, @NonNull EditText edtDriverName,
      @NonNull EditText edtLicenseNumber, @NonNull EditText edtPhoneNumber,
      @NonNull EditText edtPlateNumber, @NonNull EditText edtVehicleType,
      @NonNull Spinner spinnerAccountType, @NonNull Spinner spinnerPaymentMethod) {
    this.rootView = rootView;
    this.btnBooking = btnBooking;
    this.btnMessage = btnMessage;
    this.btnProfile = btnProfile;
    this.edtAccountId = edtAccountId;
    this.edtDriverEmail = edtDriverEmail;
    this.edtDriverName = edtDriverName;
    this.edtLicenseNumber = edtLicenseNumber;
    this.edtPhoneNumber = edtPhoneNumber;
    this.edtPlateNumber = edtPlateNumber;
    this.edtVehicleType = edtVehicleType;
    this.spinnerAccountType = spinnerAccountType;
    this.spinnerPaymentMethod = spinnerPaymentMethod;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityDriverInterfaceBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityDriverInterfaceBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_driver_interface, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityDriverInterfaceBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnBooking;
      Button btnBooking = ViewBindings.findChildViewById(rootView, id);
      if (btnBooking == null) {
        break missingId;
      }

      id = R.id.btnMessage;
      Button btnMessage = ViewBindings.findChildViewById(rootView, id);
      if (btnMessage == null) {
        break missingId;
      }

      id = R.id.btnProfile;
      Button btnProfile = ViewBindings.findChildViewById(rootView, id);
      if (btnProfile == null) {
        break missingId;
      }

      id = R.id.edtAccountId;
      EditText edtAccountId = ViewBindings.findChildViewById(rootView, id);
      if (edtAccountId == null) {
        break missingId;
      }

      id = R.id.edtDriverEmail;
      EditText edtDriverEmail = ViewBindings.findChildViewById(rootView, id);
      if (edtDriverEmail == null) {
        break missingId;
      }

      id = R.id.edtDriverName;
      EditText edtDriverName = ViewBindings.findChildViewById(rootView, id);
      if (edtDriverName == null) {
        break missingId;
      }

      id = R.id.edtLicenseNumber;
      EditText edtLicenseNumber = ViewBindings.findChildViewById(rootView, id);
      if (edtLicenseNumber == null) {
        break missingId;
      }

      id = R.id.edtPhoneNumber;
      EditText edtPhoneNumber = ViewBindings.findChildViewById(rootView, id);
      if (edtPhoneNumber == null) {
        break missingId;
      }

      id = R.id.edtPlateNumber;
      EditText edtPlateNumber = ViewBindings.findChildViewById(rootView, id);
      if (edtPlateNumber == null) {
        break missingId;
      }

      id = R.id.edtVehicleType;
      EditText edtVehicleType = ViewBindings.findChildViewById(rootView, id);
      if (edtVehicleType == null) {
        break missingId;
      }

      id = R.id.spinnerAccountType;
      Spinner spinnerAccountType = ViewBindings.findChildViewById(rootView, id);
      if (spinnerAccountType == null) {
        break missingId;
      }

      id = R.id.spinnerPaymentMethod;
      Spinner spinnerPaymentMethod = ViewBindings.findChildViewById(rootView, id);
      if (spinnerPaymentMethod == null) {
        break missingId;
      }

      return new ActivityDriverInterfaceBinding((LinearLayout) rootView, btnBooking, btnMessage,
          btnProfile, edtAccountId, edtDriverEmail, edtDriverName, edtLicenseNumber, edtPhoneNumber,
          edtPlateNumber, edtVehicleType, spinnerAccountType, spinnerPaymentMethod);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
