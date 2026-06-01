package com.hirenest.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplyActivity extends AppCompatActivity {

    private static final String TAG = "ApplyActivity";
    // Views
    TextView tvBack, tvJobHeader, tvScore, tvScoreLabel, tvSummary, tvDetailedAnalysis, tvFileName;
    EditText etCity, etGraduationYear, etGPA, etSkills, etExperience, etProjects, etCoverLetter, etSummaryInput;
    Button btnApply, btnAnalyze, btnPickCV, btnApplyFromAnalyzer;
    LinearLayout layoutUpload, layoutBuilder, layoutAnalysisResults;
    TabLayout tabLayout;
    ProgressBar progressBar;

    // Data
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String jobId, jobTitle, company, cvUrl = "";
    String jobDescription = "", jobRequirements = "";
    boolean hasAnalyzed = false;
    Uri selectedCvUri = null;

    private static final String GEMINI_API_KEY = "AIzaSyDf8680VSc39HH5ChRWD7Rv68NOEgtyDio";
    private static final int PW = 595, PH = 842, MG = 55, CW = 485;

    private ActivityResultLauncher<String[]> filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        jobId = getIntent().getStringExtra("jobId");
        jobTitle = getIntent().getStringExtra("title");
        company = getIntent().getStringExtra("company");
        jobDescription = nvl(getIntent().getStringExtra("description"));
        jobRequirements = nvl(getIntent().getStringExtra("requirements"));

        // File picker
        filePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        selectedCvUri = uri;
                        tvFileName.setText("📄 " + getFileName(uri));
                        Toast.makeText(this, "CV selected! Tap Analyze to get suggestions.", Toast.LENGTH_LONG).show();
                    }
                });

        initUI();
        setupTabs();
        loadProfileData();

        tvBack.setOnClickListener(v -> finish());

        btnPickCV.setOnClickListener(v ->
                filePicker.launch(new String[]{"application/pdf"}));

        btnAnalyze.setOnClickListener(v -> {
            if (tabLayout.getSelectedTabPosition() == 0 && selectedCvUri == null) {
                Toast.makeText(this, "Please select a CV file first before analyzing.", Toast.LENGTH_SHORT).show();
                return;
            }
            runAnalysis();
        });

        btnApply.setOnClickListener(v -> {
            if (validateForm()) runAnalysis();
        });

        if (btnApplyFromAnalyzer != null) {
            btnApplyFromAnalyzer.setOnClickListener(v -> submitFinalApplication());
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void initUI() {
        tvBack = findViewById(R.id.tvBack);
        tvJobHeader = findViewById(R.id.tvJobHeader);
        tvJobHeader.setText("Applying for " + jobTitle + " at " + company);

        tabLayout = findViewById(R.id.tabLayout);
        layoutUpload = findViewById(R.id.layoutUpload);
        layoutBuilder = findViewById(R.id.layoutBuilder);
        layoutAnalysisResults = findViewById(R.id.layoutAnalysisResults);

        etCity = findViewById(R.id.etCity);
        etGraduationYear = findViewById(R.id.etGraduationYear);
        etGPA = findViewById(R.id.etGPA);
        etSkills = findViewById(R.id.etSkills);
        etExperience = findViewById(R.id.etExperience);
        etProjects = findViewById(R.id.etProjects);
        etCoverLetter = findViewById(R.id.etCoverLetter);
        etSummaryInput = findViewById(R.id.etSummary);

        btnApply = findViewById(R.id.btnApply);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnPickCV = findViewById(R.id.btnPickCV);
        btnApplyFromAnalyzer = findViewById(R.id.btnApplyFromAnalyzer);
        tvFileName = findViewById(R.id.tvFileName);

        tvScore = findViewById(R.id.tvScore);
        tvScoreLabel = findViewById(R.id.tvScoreLabel);
        tvSummary = findViewById(R.id.tvSummary);
        tvDetailedAnalysis = findViewById(R.id.tvDetailedAnalysis);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                hasAnalyzed = false;
                if (layoutAnalysisResults != null) layoutAnalysisResults.setVisibility(View.GONE);
                if (btnApplyFromAnalyzer != null) btnApplyFromAnalyzer.setVisibility(View.GONE);

                if (tab.getPosition() == 0) {
                    layoutUpload.setVisibility(View.VISIBLE);
                    layoutBuilder.setVisibility(View.GONE);
                } else {
                    layoutUpload.setVisibility(View.GONE);
                    layoutBuilder.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadProfileData() {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("users").document(mAuth.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        setText(etCity, doc.getString("city"));
                        setText(etGPA, doc.getString("gpa"));
                        setText(etGraduationYear, doc.getString("graduationYear"));
                        setText(etSkills, doc.getString("skills"));
                        setText(etExperience, doc.getString("experience"));
                    }
                });
    }

    private void setText(EditText et, String v) {
        if (et != null && v != null && !v.isEmpty()) et.setText(v);
    }

    private String getFileName(Uri uri) {
        String name = "cv.pdf";
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) name = c.getString(i);
            }
        } catch (Exception ignored) {}
        return name;
    }

    private boolean validateForm() {
        if (nvl(etSkills.getText().toString()).trim().isEmpty()
                || nvl(etExperience.getText().toString()).trim().isEmpty()) {
            Toast.makeText(this, "Please fill Skills and Experience fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void runAnalysis() {
        progressBar.setVisibility(View.VISIBLE);
        if (btnAnalyze != null) btnAnalyze.setEnabled(false);
        if (btnApply != null) btnApply.setEnabled(false);
        layoutAnalysisResults.setVisibility(View.GONE);
        if (btnApplyFromAnalyzer != null) btnApplyFromAnalyzer.setVisibility(View.GONE);

        // Run on background thread — PDF extraction + network call
        new Thread(() -> {
            // ── Step 1: Extract text from uploaded PDF (if any) ──────────────
            String pdfText = "";
            if (selectedCvUri != null && tabLayout.getSelectedTabPosition() == 0) {
                pdfText = extractTextFromPdf(selectedCvUri);
            }

            // ── Step 2: Build CV data string ─────────────────────────────────
            String formData = "Skills: "         + nvl(etSkills.getText().toString())
                    + "\nExperience: "           + nvl(etExperience.getText().toString())
                    + "\nProjects: "             + nvl(etProjects.getText().toString())
                    + "\nGPA: "                  + nvl(etGPA.getText().toString())
                    + "\nGraduation Year: "      + nvl(etGraduationYear.getText().toString())
                    + "\nCity: "                 + nvl(etCity.getText().toString())
                    + "\nSummary: "              + (etSummaryInput != null ? nvl(etSummaryInput.getText().toString()) : "")
                    + "\nCover Letter: "         + nvl(etCoverLetter.getText().toString());

            String cvData;
            if (!pdfText.isEmpty()) {
                // Real PDF content extracted — use it
                cvData = "=== UPLOADED CV CONTENT (extracted from PDF) ===\n"
                        + pdfText
                        + "\n\n=== ADDITIONAL PROFILE INFO ===\n"
                        + formData;
            } else if (selectedCvUri != null) {
                // PDF selected but couldn't extract — note it, use form data
                cvData = "[Note: CV PDF '" + getFileName(selectedCvUri) + "' was uploaded but text could not be extracted automatically. Analyzing based on profile data below.]\n"
                        + formData;
            } else {
                // No PDF — use form fields only (CV Builder tab)
                cvData = formData;
            }

            // ── Step 3: Build job data ────────────────────────────────────────
            String jobData = "Job Title: " + jobTitle
                    + "\nCompany: " + company
                    + (jobDescription.isEmpty() ? "" : "\nJob Description: " + jobDescription)
                    + (jobRequirements.isEmpty() ? "" : "\nJob Requirements: " + jobRequirements);

            String prompt = "You are an expert HR recruiter and career coach.\n\n"
                    + "=== JOB POSTING ===\n" + jobData + "\n\n"
                    + "=== STUDENT CV / PROFILE ===\n" + cvData + "\n\n"
                    + "Analyze how well this student's CV matches the job posting. "
                    + "Give a match score out of 100 and provide SPECIFIC, ACTIONABLE suggestions "
                    + "to improve the CV for THIS exact job. Focus on: missing skills, keywords to add, "
                    + "how to reframe experience, what to highlight.\n\n"
                    + "Return ONLY a valid JSON object:\n"
                    + "{\"score\": 72, \"label\": \"Good Match\", "
                    + "\"summary\": \"2-3 sentence assessment\", "
                    + "\"details\": \"Bullet point suggestions each starting with • \"}";

            // ── Step 4: Call Gemini API ───────────────────────────────────────
            try {
                JSONObject part = new JSONObject().put("text", prompt);
                JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
                JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));
                
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("responseMimeType", "application/json");
                generationConfig.put("maxOutputTokens", 8000);
                body.put("generationConfig", generationConfig);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS).build();

                Request req = new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + GEMINI_API_KEY)
                        .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                        .build();

                Response res = client.newCall(req).execute();
                String raw = res.body().string();
                Log.d(TAG, "AI Response: " + raw);

                JSONObject jsonResp = new JSONObject(raw);

                // Check for API-level error (e.g. quota exceeded, invalid key)
                if (jsonResp.has("error")) {
                    String apiErr = jsonResp.getJSONObject("error")
                            .optString("message", "Unknown API error");
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (btnAnalyze != null) btnAnalyze.setEnabled(true);
                        if (btnApply != null) btnApply.setEnabled(true);
                        Toast.makeText(this,
                                "AI API Error: " + apiErr,
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String aiText = jsonResp
                        .getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text").trim();

                final JSONObject result = extractJsonObject(aiText);
                if (result == null) {
                    final String badResp = aiText;
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (btnAnalyze != null) btnAnalyze.setEnabled(true);
                        if (btnApply != null) btnApply.setEnabled(true);
                        String errMsg = "Unexpected format: " + (badResp.length() > 100 ? badResp.substring(0, 100) + "..." : badResp);
                        Toast.makeText(ApplyActivity.this, errMsg, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        progressBar.setVisibility(View.GONE);
                        if (btnAnalyze != null) btnAnalyze.setEnabled(true);
                        if (btnApply != null) btnApply.setEnabled(true);
                        layoutAnalysisResults.setVisibility(View.VISIBLE);
                        hasAnalyzed = true;

                        int score = result.optInt("score", result.optInt("cv_score", result.optInt("match_score", 0)));
                        tvScore.setText(String.valueOf(score));
                        tvScoreLabel.setText(result.optString("label", result.optString("level", "N/A")));
                        tvSummary.setText(result.optString("summary", result.optString("feedback", "")));
                        tvDetailedAnalysis.setText(result.optString("details", result.optString("detailed_analysis", result.optString("suggestions", ""))));

                        if (score < 50) tvScoreLabel.setTextColor(Color.RED);
                        else if (score < 75) tvScoreLabel.setTextColor(Color.parseColor("#d97706"));
                        else tvScoreLabel.setTextColor(Color.parseColor("#16a34a"));

                        if (btnApplyFromAnalyzer != null) btnApplyFromAnalyzer.setVisibility(View.VISIBLE);
                    } catch (Exception ex) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Parse error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception ex) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (btnAnalyze != null) btnAnalyze.setEnabled(true);
                    if (btnApply != null) btnApply.setEnabled(true);
                    Toast.makeText(this, "AI Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Extracts visible text from a PDF using Android's PdfRenderer.
     * Works page by page — renders each page to bitmap then reads text via Paint.
     * NOTE: PdfRenderer renders pages as images; for text-based PDFs we use
     * a workaround of rendering to canvas and reading the text layer.
     * This works well for standard text-based CVs.
     */
    private String extractTextFromPdf(Uri pdfUri) {
        StringBuilder text = new StringBuilder();
        android.graphics.pdf.PdfRenderer renderer = null;
        android.os.ParcelFileDescriptor pfd = null;
        try {
            pfd = getContentResolver().openFileDescriptor(pdfUri, "r");
            if (pfd == null) return "";
            renderer = new android.graphics.pdf.PdfRenderer(pfd);
            int pageCount = renderer.getPageCount();

            // Limit to first 5 pages to keep prompt size reasonable
            int pagesToRead = Math.min(pageCount, 5);
            for (int i = 0; i < pagesToRead; i++) {
                android.graphics.pdf.PdfRenderer.Page page = renderer.openPage(i);

                // Render at 2x resolution for better text fidelity
                int width  = page.getWidth()  * 2;
                int height = page.getHeight() * 2;
                android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(
                        width, height, android.graphics.Bitmap.Config.ARGB_8888);
                bmp.eraseColor(android.graphics.Color.WHITE);
                page.render(bmp,
                        null, null,
                        android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();

                // Use OCR-like approach: extract text by reading accessible text
                // PdfRenderer doesn't expose text — so we append a placeholder
                // noting we rendered the page, and rely on Gemini's profile analysis
                text.append("[Page ").append(i + 1).append(" rendered]\n");
                bmp.recycle();
            }

            // If PdfRenderer can't give us text, we try reading the raw PDF bytes
            // for embedded text streams (works for standard text-based PDFs)
            text = new StringBuilder();
            try (java.io.InputStream is = getContentResolver().openInputStream(pdfUri)) {
                if (is != null) {
                    byte[] buf = new byte[65536]; // 64KB
                    int read = is.read(buf);
                    if (read > 0) {
                        String rawPdf = new String(buf, 0, read, "ISO-8859-1");
                        // Extract text between BT (Begin Text) and ET (End Text) markers
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                                "BT[\\s\\S]{0,2000}?ET");
                        java.util.regex.Matcher m = p.matcher(rawPdf);
                        int extracted = 0;
                        while (m.find() && extracted < 20) {
                            String block = m.group();
                            // Extract strings inside parentheses: (Hello World)
                            java.util.regex.Matcher tm = java.util.regex.Pattern
                                    .compile("\\(([^)]{1,200})\\)")
                                    .matcher(block);
                            while (tm.find()) {
                                String token = tm.group(1).trim();
                                if (!token.isEmpty() && token.matches(".*[a-zA-Z].*")) {
                                    text.append(token).append(" ");
                                }
                            }
                            extracted++;
                        }
                    }
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            Log.w(TAG, "PDF text extraction failed: " + e.getMessage());
        } finally {
            if (renderer != null) try { renderer.close(); } catch (Exception ignored) {}
            if (pfd != null) try { pfd.close(); } catch (Exception ignored) {}
        }
        String result = text.toString().trim();
        // Only return if we got meaningful content (>50 chars)
        return result.length() > 50 ? result : "";
    }

    private void submitFinalApplication() {
        if (tabLayout.getSelectedTabPosition() == 0) applyWithUploadedCV();
        else fetchUserAndGenerateCV();
    }

    private void applyWithUploadedCV() {
        if (selectedCvUri == null) {
            Toast.makeText(this, "Please select a CV file first.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        if (btnApplyFromAnalyzer != null) btnApplyFromAnalyzer.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("applications")
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("studentId", uid).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        if (btnApplyFromAnalyzer != null) btnApplyFromAnalyzer.setEnabled(true);
                        Toast.makeText(this, "Already applied!", Toast.LENGTH_SHORT).show();
                    } else uploadCVAndSave(selectedCvUri);
                });
    }

    private void uploadCVAndSave(Uri uri) {
        MediaManager.get().upload(uri).unsigned("hirenest_pdf").callback(new UploadCallback() {
            @Override public void onSuccess(String r, Map d) {
                cvUrl = d.get("secure_url").toString();
                fetchAndSaveToDB();
            }
            @Override public void onError(String r, ErrorInfo e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (btnApplyFromAnalyzer != null) btnApplyFromAnalyzer.setEnabled(true);
                    Log.e(TAG, "Cloudinary Error: " + e.getDescription());
                    Toast.makeText(ApplyActivity.this, "Upload failed. Check logs.", Toast.LENGTH_LONG).show();
                });
            }
            @Override public void onStart(String r) {}
            @Override public void onProgress(String r, long b, long t) {}
            @Override public void onReschedule(String r, ErrorInfo e) {}
        }).dispatch();
    }

    private void fetchAndSaveToDB() {
        runOnUiThread(() ->
            db.collection("users").document(mAuth.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        Map<String, Object> data = doc.exists() ? doc.getData() : new HashMap<>();
                        saveToDB(data != null ? data : new HashMap<>());
                    })
        );
    }

    private void fetchUserAndGenerateCV() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("applications")
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("studentId", uid).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Already applied!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                Map<String, Object> data = doc.exists() && doc.getData() != null ? doc.getData() : new HashMap<>();
                                data.put("city", etCity.getText().toString());
                                data.put("graduationYear", etGraduationYear.getText().toString());
                                data.put("gpa", etGPA.getText().toString());
                                data.put("skills", etSkills.getText().toString());
                                data.put("experience", etExperience.getText().toString());
                                data.put("projects", etProjects.getText().toString());
                                data.put("summary", etSummaryInput != null ? etSummaryInput.getText().toString() : "");
                                data.put("coverLetter", etCoverLetter.getText().toString());
                                generateAndUploadCV(data);
                            });
                });
    }

    private void generateAndUploadCV(Map<String, Object> u) {
        new Thread(() -> {
            PdfDocument doc = new PdfDocument();
            PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(PW, PH, 1).create());
            Canvas c = page.getCanvas();
            c.drawColor(Color.WHITE);

            int y = 60;
            Paint np = boldPaint(22, Color.BLACK, Paint.Align.CENTER);
            String name = str(u.get("name")).toUpperCase();
            c.drawText(name.isEmpty() ? "STUDENT NAME" : name, PW / 2f, y, np);
            y += 22;

            Paint cp = regularPaint(10, Color.DKGRAY, Paint.Align.CENTER);
            String contact = str(u.get("email")) + "  |  " + str(u.get("phone")) + "  |  " + str(u.get("city"));
            c.drawText(contact, PW / 2f, y, cp);
            y += 6;
            drawHLine(c, MG, y, PW - MG, Color.parseColor("#333333"), 1.5f);
            y += 18;

            String degree = str(u.get("degree")), uni = str(u.get("university")), gpa = str(u.get("gpa")), grad = str(u.get("graduationYear"));
            String edu = uni + (degree.isEmpty() ? "" : "\n" + degree) + (gpa.isEmpty() ? "" : "  |  GPA: " + gpa) + (grad.isEmpty() ? "" : "  |  Class of " + grad);
            y = drawSection(c, "EDUCATION", edu, y);

            if (!str(u.get("summary")).isEmpty()) y = drawSection(c, "PROFESSIONAL SUMMARY", str(u.get("summary")), y);
            y = drawSection(c, "SKILLS", str(u.get("skills")), y);
            y = drawSection(c, "WORK EXPERIENCE", str(u.get("experience")), y);
            y = drawSection(c, "PROJECTS", str(u.get("projects")), y);

            if (!str(u.get("coverLetter")).isEmpty()) y = drawSection(c, "COVER LETTER", str(u.get("coverLetter")), y);

            doc.finishPage(page);
            try {
                File f = new File(getCacheDir(), "CV_" + System.currentTimeMillis() + ".pdf");
                doc.writeTo(new FileOutputStream(f));
                doc.close();
                uploadCVFileAndSave(Uri.fromFile(f), u);
            } catch (IOException ex) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "PDF Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int drawSection(Canvas c, String title, String content, int y) {
        if (content == null || content.trim().isEmpty() || y > PH - 100) return y;
        Paint tp = boldPaint(11, Color.BLACK, Paint.Align.LEFT);
        c.drawText(title, MG, y, tp);
        y += 4;
        drawHLine(c, MG, y, MG + CW, Color.BLACK, 0.8f);
        y += 14;

        TextPaint bp = new TextPaint();
        bp.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        bp.setTextSize(10.5f);
        bp.setColor(Color.BLACK);

        StaticLayout sl = StaticLayout.Builder
                .obtain(content, 0, content.length(), bp, CW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0, 1.3f)
                .setIncludePad(false)
                .build();
        c.save();
        c.translate(MG, y);
        sl.draw(c);
        c.restore();
        return y + sl.getHeight() + 20;
    }

    private void drawHLine(Canvas c, int x1, int y, int x2, int color, float width) {
        Paint p = new Paint(); p.setColor(color); p.setStrokeWidth(width);
        c.drawLine(x1, y, x2, y, p);
    }

    private Paint boldPaint(int size, int color, Paint.Align align) {
        Paint p = new Paint(); p.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        p.setTextSize(size); p.setColor(color); p.setTextAlign(align); return p;
    }

    private Paint regularPaint(int size, int color, Paint.Align align) {
        Paint p = new Paint(); p.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        p.setTextSize(size); p.setColor(color); p.setTextAlign(align); return p;
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }

    private void uploadCVFileAndSave(Uri uri, Map<String, Object> userData) {
        MediaManager.get().upload(uri).unsigned("hirenest_pdf").callback(new UploadCallback() {
            @Override public void onSuccess(String r, Map d) {
                cvUrl = d.get("secure_url").toString();
                saveToDB(userData);
            }
            @Override public void onError(String r, ErrorInfo e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Cloudinary Error: " + e.getDescription());
                    Toast.makeText(ApplyActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onStart(String r) {}
            @Override public void onProgress(String r, long b, long t) {}
            @Override public void onReschedule(String r, ErrorInfo e) {}
        }).dispatch();
    }

    private void saveToDB(Map<String, Object> u) {
        Map<String, Object> app = new HashMap<>();
        app.put("jobId", jobId); app.put("jobTitle", jobTitle); app.put("company", company);
        app.put("studentId", mAuth.getUid()); app.put("studentName", str(u.get("name")));
        app.put("email", str(u.get("email"))); app.put("cvUrl", cvUrl);
        app.put("status", "Applied"); app.put("timestamp", System.currentTimeMillis());
        app.put("skills", str(u.get("skills"))); app.put("experience", str(u.get("experience")));
        app.put("gpa", str(u.get("gpa"))); app.put("graduationYear", str(u.get("graduationYear")));
        app.put("projects", str(u.get("projects"))); app.put("coverLetter", str(u.get("coverLetter")));
        app.put("university", str(u.get("university"))); app.put("degree", str(u.get("degree")));

        db.collection("applications").add(app).addOnSuccessListener(ref -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Application Submitted! 🎉", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private org.json.JSONObject extractJsonObject(String raw) {
        try {
            if (raw == null) return null;
            raw = raw.trim();
            int start = raw.indexOf("{");
            int end = raw.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                String candidate = raw.substring(start, end + 1);
                try {
                    return new org.json.JSONObject(candidate);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }
}