package com.hirenest.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicantsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvJobTitle, tvBack, tvApplicantCount;
    Button btnAiRank;
    FirebaseFirestore db;
    String jobId, jobTitle;
    List<Map<String, Object>> applicantsList = new ArrayList<>();
    ApplicantsAdapter adapter;

    // Gemini API Key
    private static final String GEMINI_API_KEY =
            "AIzaSyDf8680VSc39HH5ChRWD7Rv68NOEgtyDio";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applicants);

        db = FirebaseFirestore.getInstance();

        jobId = getIntent().getStringExtra("jobId");
        jobTitle = getIntent().getStringExtra("jobTitle");

        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvBack = findViewById(R.id.tvBack);
        tvApplicantCount = findViewById(R.id.tvApplicantCount);
        btnAiRank = findViewById(R.id.btnAiRank);
        recyclerView = findViewById(R.id.recyclerView);

        tvJobTitle.setText(jobTitle);
        tvBack.setOnClickListener(v -> finish());

        adapter = new ApplicantsAdapter(applicantsList, false);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadApplicants();

        btnAiRank.setOnClickListener(v -> {
            if (applicantsList.isEmpty()) {
                Toast.makeText(this,
                        "No applicants to rank yet!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            btnAiRank.setText("AI Ranking...");
            btnAiRank.setEnabled(false);
            rankWithGemini();
        });
    }

    private void loadApplicants() {
        db.collection("applications")
                .whereEqualTo("jobId", jobId)
                .get()
                .addOnSuccessListener(snap -> {
                    applicantsList.clear();
                    for (var doc : snap.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        data.put("applicationId", doc.getId());
                        applicantsList.add(data);
                    }
                    adapter.notifyDataSetChanged();
                    tvApplicantCount.setText(
                            applicantsList.size() + " Applicants");
                });
    }

    private void rankWithGemini() {
        new Thread(() -> {
            try {
                StringBuilder prompt = new StringBuilder();
                prompt.append("You are an expert HR recruiter. ");
                prompt.append("Rank these applicants for: ");
                prompt.append(jobTitle).append("\n\n");
                prompt.append("Return ONLY a JSON array containing candidate_id, name, score, and reason. Format like this:\n");
                prompt.append("[{\"candidate_id\":1,\"name\":\"John\",\"score\":95,");
                prompt.append("\"reason\":\"Strong match\"}]\n\n");
                prompt.append("Candidates:\n");

                for (int i = 0; i < applicantsList.size(); i++) {
                    Map<String, Object> app =
                            applicantsList.get(i);
                    prompt.append("\nCandidate ID: ")
                            .append(i + 1).append("\n");
                    prompt.append("Name: ")
                            .append(app.get("studentName"))
                            .append("\n");
                    prompt.append("University: ")
                            .append(app.get("university"))
                            .append("\n");
                    prompt.append("Degree: ")
                            .append(app.get("degree"))
                            .append("\n");
                    prompt.append("Cover Letter: ")
                            .append(app.get("coverLetter"))
                            .append("\n");
                    prompt.append("CV URL: ")
                            .append(app.get("cvUrl"))
                            .append("\n");
                    prompt.append("Please also consider the CV content from the URL above when ranking.\n");
                }

                // Build request
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt.toString());
                parts.put(part);
                content.put("parts", parts);
                contents.put(content);
                requestBody.put("contents", contents);

                android.util.Log.e("ApplicantsActivity", "PROMPT SENT:\n" + prompt.toString());

                // Add generation config
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.1);
                generationConfig.put("maxOutputTokens", 8000);
                generationConfig.put("responseMimeType", "application/json");
                requestBody.put("generationConfig",
                        generationConfig);

                okhttp3.OkHttpClient client =
                        new okhttp3.OkHttpClient.Builder()
                                .connectTimeout(60,
                                        java.util.concurrent.TimeUnit.SECONDS)
                                .readTimeout(60,
                                        java.util.concurrent.TimeUnit.SECONDS)
                                .build();

                okhttp3.RequestBody body =
                        okhttp3.RequestBody.create(
                                requestBody.toString(),
                                okhttp3.MediaType.parse(
                                        "application/json; charset=utf-8"));

                okhttp3.Request request =
                        new okhttp3.Request.Builder()
                                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + GEMINI_API_KEY)
                                .post(body)
                                .addHeader("Content-Type",
                                        "application/json")
                                .build();

                okhttp3.Response response =
                        client.newCall(request).execute();
                String responseStr =
                        response.body().string();

                android.util.Log.e("ApplicantsActivity", "RAW RESPONSE:\n" + responseStr);

                JSONObject jsonResponse =
                        new JSONObject(responseStr);

                // Check for API-level errors (e.g. invalid key, quota exceeded)
                if (jsonResponse.has("error")) {
                    String apiErr = jsonResponse.getJSONObject("error")
                            .optString("message", "Unknown API error");
                    runOnUiThread(() -> {
                        btnAiRank.setText("🤖 FIND TOP 10 WITH AI");
                        btnAiRank.setEnabled(true);
                        Toast.makeText(
                                ApplicantsActivity.this,
                                "AI API Error: " + apiErr,
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String aiTextRaw = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                        .trim();

                JSONArray ranked = extractJsonArray(aiTextRaw);
                if (ranked == null) {
                    final String badResp = aiTextRaw;
                    runOnUiThread(() -> {
                        btnAiRank.setText("🤖 FIND TOP 10 WITH AI");
                        btnAiRank.setEnabled(true);
                        String errMsg = "Unexpected format: " + (badResp.length() > 100 ? badResp.substring(0, 100) + "..." : badResp);
                        Toast.makeText(
                                ApplicantsActivity.this,
                                errMsg,
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                List<Map<String, Object>> rankedList =
                        new ArrayList<>();

                for (int i = 0;
                     i < Math.min(10, ranked.length()); i++) {
                    JSONObject r = ranked.getJSONObject(i);
                    int candidateId = r.optInt("candidate_id", r.optInt("candidateId", r.optInt("id", -1)));
                    String name = r.optString("name", r.optString("candidate_name", r.optString("student_name", r.optString("studentName", ""))));
                    int score = r.optInt("score", r.optInt("match_score", r.optInt("rating", r.optInt("suitability_score", 0))));
                    String reason = r.optString("reason", r.optString("comments", r.optString("explanation", r.optString("feedback", ""))));

                    // 1. Try matching by candidateId index (1-based index)
                    if (candidateId >= 1 && candidateId <= applicantsList.size()) {
                        Map<String, Object> app = applicantsList.get(candidateId - 1);
                        app.put("score", score);
                        app.put("reason", reason);
                        if (!rankedList.contains(app)) {
                            rankedList.add(app);
                        }
                    } else {
                        // 2. Fallback to name matching
                        String nameLower = name.toLowerCase().trim();
                        for (Map<String, Object> app : applicantsList) {
                            String studentName = app.get("studentName") != null ? app.get("studentName").toString().toLowerCase().trim() : "";
                            if (!nameLower.isEmpty() && !studentName.isEmpty() && (studentName.contains(nameLower) || nameLower.contains(studentName))) {
                                app.put("score", score);
                                app.put("reason", reason);
                                if (!rankedList.contains(app)) {
                                    rankedList.add(app);
                                }
                                break;
                            }
                        }
                    }
                }

                // If name matching failed use position
                if (rankedList.isEmpty()) {
                    for (int i = 0; i < Math.min(10,
                            applicantsList.size()); i++) {
                        try {
                            if (i < ranked.length()) {
                                applicantsList.get(i)
                                        .put("score",
                                                ranked.getJSONObject(i)
                                                        .getInt("score"));
                                applicantsList.get(i)
                                        .put("reason",
                                                ranked.getJSONObject(i)
                                                        .getString("reason"));
                            }
                            rankedList.add(
                                    applicantsList.get(i));
                        } catch (Exception ignored) {}
                    }
                }

                final List<Map<String, Object>> finalList =
                        rankedList;

                runOnUiThread(() -> {
                    applicantsList.clear();
                    applicantsList.addAll(finalList);
                    adapter = new ApplicantsAdapter(
                            applicantsList, true);
                    recyclerView.setAdapter(adapter);
                    tvApplicantCount.setText(
                            "🏆 Top " + finalList.size()
                                    + " Ranked by AI");
                    btnAiRank.setText("✅ AI Ranked!");
                    Toast.makeText(
                            ApplicantsActivity.this,
                            "AI ranked top candidates! 🎉",
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnAiRank.setText(
                            "🤖 FIND TOP 10 WITH AI");
                    btnAiRank.setEnabled(true);
                    Toast.makeText(
                            ApplicantsActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Extracts a JSONArray robustly from raw Gemini responses.
     * Handles direct arrays, markdown-fenced arrays, and objects containing arrays.
     */
    private org.json.JSONArray extractJsonArray(String raw) {
        try {
            if (raw == null) return null;
            raw = raw.trim();

            // 1. Try directly parsing as JSONArray
            int arrStart = raw.indexOf("[");
            int arrEnd = raw.lastIndexOf("]");
            if (arrStart != -1 && arrEnd != -1 && arrEnd > arrStart) {
                String candidate = raw.substring(arrStart, arrEnd + 1);
                try {
                    return new org.json.JSONArray(candidate);
                } catch (Exception ignored) {}
            }

            // 2. Try parsing as JSONObject and lookup nested array/objects
            int objStart = raw.indexOf("{");
            int objEnd = raw.lastIndexOf("}");
            if (objStart != -1 && objEnd != -1 && objEnd > objStart) {
                String candidate = raw.substring(objStart, objEnd + 1);
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(candidate);
                    
                    // Case A: Look for any top level JSONArray key
                    java.util.Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object val = obj.get(key);
                        if (val instanceof org.json.JSONArray) {
                            return (org.json.JSONArray) val;
                        }
                    }

                    // Case B: Look for nested objects containing candidate info (e.g. {"1": {...}, "2": {...}})
                    org.json.JSONArray backupArray = new org.json.JSONArray();
                    keys = obj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object val = obj.get(key);
                        if (val instanceof org.json.JSONObject) {
                            org.json.JSONObject innerObj = (org.json.JSONObject) val;
                            if (innerObj.has("name") || innerObj.has("studentName") || innerObj.has("student_name")) {
                                backupArray.put(innerObj);
                            }
                        }
                    }
                    if (backupArray.length() > 0) {
                        return backupArray;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }
}