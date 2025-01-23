package com.navix.mapboxvainilla;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Body;
import okhttp3.ResponseBody;

public interface ApiService {
    @POST("create-reservation")
    Call<ResponseBody> createReservation(@Body ReservationRequest request);
}