package com.example.listaspesa;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.listaspesa.R;

public class MainActivity extends AppCompatActivity {

    EditText editProdotto;
    EditText editQuantita;
    Button buttonAggiungi;
    LinearLayout listaProdotti;

    boolean caricamento = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editProdotto = findViewById(R.id.editProdotto);
        editQuantita = findViewById(R.id.editQuantita);
        buttonAggiungi = findViewById(R.id.buttonAggiungi);
        listaProdotti = findViewById(R.id.listaProdotti);

        caricaProdotti();

        caricamento = false;

        buttonAggiungi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aggiungiProdotto();
            }
        });
    }

    public void aggiungiProdotto() {
        String nome = editProdotto.getText().toString().trim();
        String quantita = editQuantita.getText().toString().trim();

        if (nome.isEmpty() || quantita.isEmpty()) {
            Toast.makeText(this, "Inserisci prodotto e quantita'", Toast.LENGTH_SHORT).show();
            return;
        }

        int numero;

        try {
            numero = Integer.parseInt(quantita);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La quantità deve essere un numero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (numero <= 0) {
            Toast.makeText(this, "La quantità deve essere maggiore di zero", Toast.LENGTH_SHORT).show();
            return;
        }

        creaRiga(nome, numero, false, false);
        salvaProdotti();

        editProdotto.setText("");
        editQuantita.setText("");
        editProdotto.requestFocus();
    }

    public void creaRiga(String nome, int quantita, boolean acquistato, boolean mancante) {
        LinearLayout riga = new LinearLayout(this);
        riga.setOrientation(LinearLayout.HORIZONTAL);
        riga.setGravity(Gravity.CENTER_VERTICAL);
        riga.setPadding(4, 8, 4, 8);

        String[] dati = {nome, String.valueOf(quantita)};
        riga.setTag(dati);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(nome + " x" + quantita);
        checkBox.setTextSize(16);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        TextView testoMancante = new TextView(this);
        testoMancante.setText("MANCANTE");
        testoMancante.setTextSize(12);
        testoMancante.setVisibility(View.GONE);

        Button buttonMancante = new Button(this);

        if (mancante) {
            checkBox.setEnabled(false);
            checkBox.setChecked(false);
            checkBox.setPaintFlags(
                    checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );

            testoMancante.setVisibility(View.VISIBLE);
            buttonMancante.setText("RIPRISTINA");
        } else {
            checkBox.setChecked(acquistato);
            buttonMancante.setText("MANCANTE");
        }

        checkBox.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                salvaProdotti();
            }
        });

        buttonMancante.setOnClickListener(new View.OnClickListener() {
            boolean prodottoMancante = mancante;

            @Override
            public void onClick(View v) {
                if (prodottoMancante == false) {
                    prodottoMancante = true;

                    checkBox.setEnabled(false);
                    checkBox.setChecked(false);
                    checkBox.setPaintFlags(
                            checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    );

                    testoMancante.setVisibility(View.VISIBLE);
                    buttonMancante.setText("RIPRISTINA");
                } else {
                    prodottoMancante = false;

                    checkBox.setEnabled(true);
                    checkBox.setPaintFlags(
                            checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
                    );

                    testoMancante.setVisibility(View.GONE);
                    buttonMancante.setText("MANCANTE");
                }

                salvaProdotti();
            }
        });

        LinearLayout parteDestra = new LinearLayout(this);
        parteDestra.setOrientation(LinearLayout.VERTICAL);
        parteDestra.setGravity(Gravity.CENTER);
        parteDestra.addView(testoMancante);
        parteDestra.addView(buttonMancante);

        riga.addView(checkBox);
        riga.addView(parteDestra);

        listaProdotti.addView(riga);

        View separatore = new View(this);
        separatore.setBackgroundColor(0xFFDDDDDD);
        listaProdotti.addView(separatore, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
        ));
    }

    public void salvaProdotti() {
        if (caricamento) {
            return;
        }

        android.content.SharedPreferences preferenze =
                getSharedPreferences("ListaSpesa", MODE_PRIVATE);

        android.content.SharedPreferences.Editor editor = preferenze.edit();
        editor.clear();

        int numeroProdotti = 0;

        for (int i = 0; i < listaProdotti.getChildCount(); i++) {
            View elemento = listaProdotti.getChildAt(i);

            if (elemento instanceof LinearLayout) {
                LinearLayout riga = (LinearLayout) elemento;
                CheckBox checkBox = (CheckBox) riga.getChildAt(0);

                String[] dati = (String[]) riga.getTag();

                editor.putString("nome" + numeroProdotti, dati[0]);
                editor.putInt("quantita" + numeroProdotti, Integer.parseInt(dati[1]));

                if (checkBox.isEnabled() == false) {
                    editor.putInt("stato" + numeroProdotti, 2);
                } else if (checkBox.isChecked()) {
                    editor.putInt("stato" + numeroProdotti, 1);
                } else {
                    editor.putInt("stato" + numeroProdotti, 0);
                }

                numeroProdotti++;
            }
        }

        editor.putInt("numeroProdotti", numeroProdotti);
        editor.apply();
    }

    public void caricaProdotti() {
        android.content.SharedPreferences preferenze =
                getSharedPreferences("ListaSpesa", MODE_PRIVATE);

        int numeroProdotti = preferenze.getInt("numeroProdotti", 0);

        for (int i = 0; i < numeroProdotti; i++) {
            String nome = preferenze.getString("nome" + i, "");
            int quantita = preferenze.getInt("quantita" + i, 1);
            int stato = preferenze.getInt("stato" + i, 0);

            if (stato == 1) {
                creaRiga(nome, quantita, true, false);
            } else if (stato == 2) {
                creaRiga(nome, quantita, false, true);
            } else {
                creaRiga(nome, quantita, false, false);
            }
        }
    }
}
