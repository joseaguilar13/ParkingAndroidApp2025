// Generated by view binder compiler. Do not edit!
package com.navix.mapboxvainilla.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.navix.mapboxvainilla.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FormBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final TextView appName;

  @NonNull
  public final MaterialCardView bottomNavigation;

  @NonNull
  public final MaterialButton btnBooking;

  @NonNull
  public final MaterialButton btnLogin;

  @NonNull
  public final MaterialButton btnMessage;

  @NonNull
  public final MaterialButton btnProfile;

  @NonNull
  public final TextView createAccount;

  @NonNull
  public final EditText dateView;

  @NonNull
  public final EditText dateView2;

  @NonNull
  public final EditText dateView3;

  @NonNull
  public final LinearLayout editTexts;

  @NonNull
  public final ImageButton fbLogin;

  @NonNull
  public final ImageButton googleLogin;

  @NonNull
  public final TextView loginInText;

  @NonNull
  public final LinearLayout orText;

  @NonNull
  public final LinearLayout socialButtons;

  @NonNull
  public final TextView tvCostPerDayValue;

  @NonNull
  public final TextView tvDaysParkingValue;

  @NonNull
  public final TextView tvTotalCostValue;

  @NonNull
  public final TextView tvTotalToPayValue;

  @NonNull
  public final EditText userEmail;

  @NonNull
  public final EditText userPass;

  private FormBinding(@NonNull RelativeLayout rootView, @NonNull TextView appName,
      @NonNull MaterialCardView bottomNavigation, @NonNull MaterialButton btnBooking,
      @NonNull MaterialButton btnLogin, @NonNull MaterialButton btnMessage,
      @NonNull MaterialButton btnProfile, @NonNull TextView createAccount,
      @NonNull EditText dateView, @NonNull EditText dateView2, @NonNull EditText dateView3,
      @NonNull LinearLayout editTexts, @NonNull ImageButton fbLogin,
      @NonNull ImageButton googleLogin, @NonNull TextView loginInText, @NonNull LinearLayout orText,
      @NonNull LinearLayout socialButtons, @NonNull TextView tvCostPerDayValue,
      @NonNull TextView tvDaysParkingValue, @NonNull TextView tvTotalCostValue,
      @NonNull TextView tvTotalToPayValue, @NonNull EditText userEmail,
      @NonNull EditText userPass) {
    this.rootView = rootView;
    this.appName = appName;
    this.bottomNavigation = bottomNavigation;
    this.btnBooking = btnBooking;
    this.btnLogin = btnLogin;
    this.btnMessage = btnMessage;
    this.btnProfile = btnProfile;
    this.createAccount = createAccount;
    this.dateView = dateView;
    this.dateView2 = dateView2;
    this.dateView3 = dateView3;
    this.editTexts = editTexts;
    this.fbLogin = fbLogin;
    this.googleLogin = googleLogin;
    this.loginInText = loginInText;
    this.orText = orText;
    this.socialButtons = socialButtons;
    this.tvCostPerDayValue = tvCostPerDayValue;
    this.tvDaysParkingValue = tvDaysParkingValue;
    this.tvTotalCostValue = tvTotalCostValue;
    this.tvTotalToPayValue = tvTotalToPayValue;
    this.userEmail = userEmail;
    this.userPass = userPass;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FormBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FormBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
      boolean attachToParent) {
    View root = inflater.inflate(R.layout.form, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FormBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.app_name;
      TextView appName = ViewBindings.findChildViewById(rootView, id);
      if (appName == null) {
        break missingId;
      }

      id = R.id.bottom_navigation;
      MaterialCardView bottomNavigation = ViewBindings.findChildViewById(rootView, id);
      if (bottomNavigation == null) {
        break missingId;
      }

      id = R.id.btnBooking;
      MaterialButton btnBooking = ViewBindings.findChildViewById(rootView, id);
      if (btnBooking == null) {
        break missingId;
      }

      id = R.id.btn_login;
      MaterialButton btnLogin = ViewBindings.findChildViewById(rootView, id);
      if (btnLogin == null) {
        break missingId;
      }

      id = R.id.btnMessage;
      MaterialButton btnMessage = ViewBindings.findChildViewById(rootView, id);
      if (btnMessage == null) {
        break missingId;
      }

      id = R.id.btnProfile;
      MaterialButton btnProfile = ViewBindings.findChildViewById(rootView, id);
      if (btnProfile == null) {
        break missingId;
      }

      id = R.id.create_account;
      TextView createAccount = ViewBindings.findChildViewById(rootView, id);
      if (createAccount == null) {
        break missingId;
      }

      id = R.id.date_view;
      EditText dateView = ViewBindings.findChildViewById(rootView, id);
      if (dateView == null) {
        break missingId;
      }

      id = R.id.date_view2;
      EditText dateView2 = ViewBindings.findChildViewById(rootView, id);
      if (dateView2 == null) {
        break missingId;
      }

      id = R.id.date_view3;
      EditText dateView3 = ViewBindings.findChildViewById(rootView, id);
      if (dateView3 == null) {
        break missingId;
      }

      id = R.id.edit_texts;
      LinearLayout editTexts = ViewBindings.findChildViewById(rootView, id);
      if (editTexts == null) {
        break missingId;
      }

      id = R.id.fb_login;
      ImageButton fbLogin = ViewBindings.findChildViewById(rootView, id);
      if (fbLogin == null) {
        break missingId;
      }

      id = R.id.google_login;
      ImageButton googleLogin = ViewBindings.findChildViewById(rootView, id);
      if (googleLogin == null) {
        break missingId;
      }

      id = R.id.login_in_text;
      TextView loginInText = ViewBindings.findChildViewById(rootView, id);
      if (loginInText == null) {
        break missingId;
      }

      id = R.id.or_text;
      LinearLayout orText = ViewBindings.findChildViewById(rootView, id);
      if (orText == null) {
        break missingId;
      }

      id = R.id.social_buttons;
      LinearLayout socialButtons = ViewBindings.findChildViewById(rootView, id);
      if (socialButtons == null) {
        break missingId;
      }

      id = R.id.tv_cost_per_day_value;
      TextView tvCostPerDayValue = ViewBindings.findChildViewById(rootView, id);
      if (tvCostPerDayValue == null) {
        break missingId;
      }

      id = R.id.tv_days_parking_value;
      TextView tvDaysParkingValue = ViewBindings.findChildViewById(rootView, id);
      if (tvDaysParkingValue == null) {
        break missingId;
      }

      id = R.id.tv_total_cost_value;
      TextView tvTotalCostValue = ViewBindings.findChildViewById(rootView, id);
      if (tvTotalCostValue == null) {
        break missingId;
      }

      id = R.id.tv_total_to_pay_value;
      TextView tvTotalToPayValue = ViewBindings.findChildViewById(rootView, id);
      if (tvTotalToPayValue == null) {
        break missingId;
      }

      id = R.id.user_email;
      EditText userEmail = ViewBindings.findChildViewById(rootView, id);
      if (userEmail == null) {
        break missingId;
      }

      id = R.id.user_pass;
      EditText userPass = ViewBindings.findChildViewById(rootView, id);
      if (userPass == null) {
        break missingId;
      }

      return new FormBinding((RelativeLayout) rootView, appName, bottomNavigation, btnBooking,
          btnLogin, btnMessage, btnProfile, createAccount, dateView, dateView2, dateView3,
          editTexts, fbLogin, googleLogin, loginInText, orText, socialButtons, tvCostPerDayValue,
          tvDaysParkingValue, tvTotalCostValue, tvTotalToPayValue, userEmail, userPass);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
