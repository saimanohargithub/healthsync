package com.example.healthsync.frontend.data.remote;

import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeminiApiService {

    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiResponse> generateMealPlan(
        @Path("model") String model,
        @Query("key") String apiKey,
        @Body GeminiRequest request
    );

    class GeminiRequest {
        public List<Content> contents;

        public GeminiRequest(String prompt) {
            this.contents = Collections.singletonList(new Content(prompt));
        }

        public static class Content {
            public List<Part> parts;
            public Content(String text) {
                this.parts = Collections.singletonList(new Part(text));
            }
        }

        public static class Part {
            public String text;
            public Part(String text) {
                this.text = text;
            }
        }
    }

    class GeminiResponse {
        public List<Candidate> candidates;

        public static class Candidate {
            public Content content;
        }

        public static class Content {
            public List<Part> parts;
        }

        public static class Part {
            public String text;
        }

        public String getResponseText() {
            if (candidates != null && !candidates.isEmpty() && 
                candidates.get(0).content != null && 
                candidates.get(0).content.parts != null && 
                !candidates.get(0).content.parts.isEmpty()) {
                return candidates.get(0).content.parts.get(0).text;
            }
            return null;
        }
    }
}
