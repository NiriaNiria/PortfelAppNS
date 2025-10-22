package com.example.portfelappns;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    RadioGroup rgTyp;
    RadioButton rbDochod, rbWydatek;
    EditText etKwota, etOpis;
    Spinner spKategoria;
    LinearLayout boxKategoria;
    Button btnDodaj;
    TextView tvLista, tvSuma, tvRaport4, tvHistoria;


    String[] KATEGORIE = {"Jedzenie", "Mieszkanie/Media", "Transport", "Zdrowie"};
    double[] sumaKat = new double[KATEGORIE.length];


    ArrayList<Entry> wpisy = new ArrayList<>();
    double sumaDochod = 0.0;
    double sumaWydatek = 0.0;


    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());


    private static final String PREFS = "wallet_prefs";
    private static final String KEY_TEXT = "entries_text_v1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rgTyp = findViewById(R.id.rgTyp);
        rbDochod = findViewById(R.id.rbDochod);
        rbWydatek = findViewById(R.id.rbWydatek);
        etKwota = findViewById(R.id.etKwota);
        etOpis = findViewById(R.id.etOpis);
        spKategoria = findViewById(R.id.spKategoria);
        boxKategoria = findViewById(R.id.boxKategoria);
        btnDodaj = findViewById(R.id.btnDodaj);
        tvLista = findViewById(R.id.tvLista);
        tvSuma = findViewById(R.id.tvSuma);
        tvRaport4 = findViewById(R.id.tvRaport4);
        tvHistoria = findViewById(R.id.tvHistoria);


        ArrayAdapter<String> katAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, KATEGORIE);
        spKategoria.setAdapter(katAdapter);

        rgTyp.setOnCheckedChangeListener((group, checkedId) ->
                boxKategoria.setVisibility(checkedId == R.id.rbWydatek ? View.VISIBLE : View.GONE));
        boxKategoria.setVisibility(View.GONE);

        btnDodaj.setOnClickListener(v -> dodajWpis());


        loadAll();
        przeliczISwiezeWidoki();
    }

    private void dodajWpis() {
        String kwotaTxt = etKwota.getText().toString().trim();
        String opis = etOpis.getText().toString().trim();
        if (kwotaTxt.isEmpty()) {
            Toast.makeText(this, "Podaj kwotę", Toast.LENGTH_SHORT).show();
            return;
        }
        double kwota = Double.parseDouble(kwotaTxt.replace(",", "."));
        boolean toWydatek = rbWydatek.isChecked();

        Entry e = new Entry();
        e.isExpense = toWydatek;
        e.amount = kwota;
        e.timeMillis = System.currentTimeMillis();
        e.desc = sanitize(opis);
        e.category = toWydatek ? KATEGORIE[Math.max(0, spKategoria.getSelectedItemPosition())] : "";

        wpisy.add(e);
        saveAll();
        przeliczISwiezeWidoki();

        etKwota.setText("");
        etOpis.setText("");
    }

    private void przeliczISwiezeWidoki() {

        sumaDochod = 0.0;
        sumaWydatek = 0.0;
        for (int i = 0; i < sumaKat.length; i++) sumaKat[i] = 0.0;


        for (Entry e : wpisy) {
            if (e.isExpense) {
                sumaWydatek += e.amount;

                for (int i = 0; i < KATEGORIE.length; i++) {
                    if (KATEGORIE[i].equals(e.category)) {
                        sumaKat[i] += e.amount;
                        break;
                    }
                }
            } else {
                sumaDochod += e.amount;
            }
        }


        tvSuma.setText(String.format(Locale.getDefault(),
                "Suma dochodów: %.2f PLN • Suma wydatków: %.2f PLN",
                sumaDochod, sumaWydatek));


        StringBuilder r = new StringBuilder();
        for (int i = 0; i < KATEGORIE.length; i++) {
            r.append("• ").append(KATEGORIE[i]).append(": ")
                    .append(String.format(Locale.getDefault(), "%.2f PLN", sumaKat[i]))
                    .append("\n");
        }
        tvRaport4.setText(r.toString().trim());


        StringBuilder all = new StringBuilder();
        for (Entry e : wpisy) {
            all.append("• ").append(formatEntry(e)).append("\n");
        }
        tvLista.setText(all.toString().trim());


        StringBuilder hist = new StringBuilder();
        for (int i = wpisy.size() - 1; i >= 0; i--) {
            Entry e = wpisy.get(i);
            if (e.isExpense) {
                hist.append("• ").append(formatEntry(e)).append("\n");
            }
        }
        tvHistoria.setText(hist.toString().trim());
    }



    private void saveAll() {

        StringBuilder sb = new StringBuilder();
        for (Entry e : wpisy) {
            sb.append(e.isExpense ? "E" : "I").append('\t')
                    .append(e.amount).append('\t')
                    .append(e.category == null ? "" : e.category).append('\t')
                    .append(sanitize(e.desc)).append('\t')
                    .append(e.timeMillis).append('\n');
        }
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sp.edit().putString(KEY_TEXT, sb.toString()).apply();
    }

    private void loadAll() {
        wpisy.clear();
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String text = sp.getString(KEY_TEXT, "");
        if (text == null || text.isEmpty()) return;

        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\t");
            if (parts.length < 5) continue; // bezpieczeństwo
            Entry e = new Entry();
            e.isExpense = "E".equals(parts[0]);
            try { e.amount = Double.parseDouble(parts[1]); } catch (Exception ex) { e.amount = 0.0; }
            e.category = parts[2];
            e.desc = parts[3];
            try { e.timeMillis = Long.parseLong(parts[4]); } catch (Exception ex) { e.timeMillis = System.currentTimeMillis(); }
            wpisy.add(e);
        }
    }

    private String sanitize(String s) {
        if (s == null) return "";

        return s.replace("\n", " ").replace("\t", " ").trim();
    }


    private String formatEntry(Entry e) {
        String sign = e.isExpense ? "−" : "+";
        String extra = e.isExpense ? (" • " + (e.category == null || e.category.isEmpty() ? "Inne" : e.category)) : " • Dochód";
        String date = df.format(new Date(e.timeMillis));
        String opis = (e.desc == null || e.desc.isEmpty()) ? "" : (" — " + e.desc);
        return String.format(Locale.getDefault(), "%s %.2f PLN%s • %s%s",
                sign, e.amount, extra, date, opis);
    }


    static class Entry {
        boolean isExpense;
        double amount;
        String category;
        String desc;
        long timeMillis;
    }
}