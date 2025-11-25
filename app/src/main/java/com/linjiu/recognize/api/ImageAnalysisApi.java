package com.linjiu.recognize.api;


import com.linjiu.recognize.domain.image.ImageAnalysisRequest;
import com.linjiu.recognize.domain.image.ImageAnalysisResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ImageAnalysisApi {

    @POST("ai/tongyi/image")
    Call<ImageAnalysisResponse> analyzeImage(@Body ImageAnalysisRequest request);
}
