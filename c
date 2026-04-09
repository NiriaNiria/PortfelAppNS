using System;
using System.ComponentModel;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Windows.Forms;

namespace WindowsFormsApp5
{
    public partial class Form1 : Form
    {
        private BindingList<Wydatek> listaWydatkow;
        private string sciezkaPliku = "wydatki.csv";

        public Form1()
        {
            InitializeComponent();

            listaWydatkow = new BindingList<Wydatek>();
            dgvWydatki.DataSource = listaWydatkow;

            if (cmbKategoria.Items.Count > 0)
                cmbKategoria.SelectedIndex = 0;

            cmbFiltr.Items.Add("Wszystkie");
            cmbFiltr.Items.Add("Jedzenie");
            cmbFiltr.Items.Add("Transport");
            cmbFiltr.Items.Add("Rozrywka");
            cmbFiltr.Items.Add("Inne");
            cmbFiltr.SelectedIndex = 0;
        }

        private void AktualizujSume()
        {
            decimal suma = listaWydatkow.Sum(w => w.Kwota);
            lblSuma.Text = $"Suma wydatków: {suma:C2}";
        }

        private void btnDodaj_Click(object sender, EventArgs e)
        {
            string tekstKwoty = txtKwota.Text.Replace('.', ',');

            if (decimal.TryParse(tekstKwoty, out decimal kwota))
            {
                if (string.IsNullOrWhiteSpace(txtNazwa.Text))
                {
                    MessageBox.Show("Podaj nazwę wydatku", "Brak danych");
                    return;
                }

                Wydatek nowyWydatek = new Wydatek(txtNazwa.Text, kwota, cmbKategoria.Text);
                listaWydatkow.Add(nowyWydatek);
                AktualizujSume();

                txtNazwa.Clear();
                txtKwota.Clear();
            }
            else
            {
                MessageBox.Show("Podano niepoprawną kwotę! Użyj odpowiedniego separatora dziesiętnego", "Błąd");
            }
        }

        private void btnUsun_Click(object sender, EventArgs e)
        {
            if (dgvWydatki.SelectedRows.Count > 0)
            {
                Wydatek wybranyWydatek = dgvWydatki.SelectedRows[0].DataBoundItem as Wydatek;

                if (wybranyWydatek != null)
                {
                    listaWydatkow.Remove(wybranyWydatek);
                    AktualizujSume();
                }
            }
        }

        private void btnZapisz_Click(object sender, EventArgs e)
        {
            try
            {
                using (StreamWriter sw = new StreamWriter(sciezkaPliku))
                {
                    foreach (Wydatek w in listaWydatkow)
                    {
                        sw.WriteLine($"{w.Nazwa};{w.Kwota};{w.Kategoria}");
                    }
                }

                MessageBox.Show("Dane zapisane pomyślnie", "Sukces");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Błąd podczas zapisu pliku: {ex.Message}", "Błąd");
            }
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            if (File.Exists(sciezkaPliku))
            {
                using (StreamReader sr = new StreamReader(sciezkaPliku))
                {
                    string linia;

                    while ((linia = sr.ReadLine()) != null)
                    {
                        string[] dane = linia.Split(';');

                        if (dane.Length == 3)
                        {
                            string nazwa = dane[0];

                            if (decimal.TryParse(dane[1], out decimal kwota))
                            {
                                string kategoria = dane[2];
                                listaWydatkow.Add(new Wydatek(nazwa, kwota, kategoria));
                            }
                        }
                    }
                }

                AktualizujSume();
            }
        }

        private void btnFiltruj_Click(object sender, EventArgs e)
        {
            string wybrana = cmbFiltr.Text;

            if (wybrana == "Wszystkie")
            {
                dgvWydatki.DataSource = listaWydatkow;
            }
            else
            {
                var filtrowane = new BindingList<Wydatek>(
                    listaWydatkow.Where(w => w.Kategoria == wybrana).ToList()
                );

                dgvWydatki.DataSource = filtrowane;
            }
        }

        private void cmbKategoria_SelectedIndexChanged(object sender, EventArgs e)
        {
        }

        private void txtNazwa_TextChanged(object sender, EventArgs e)
        {
        }

        private void lblSuma_Click(object sender, EventArgs e)
        {
        }

        private void txtKwota_TextChanged(object sender, EventArgs e)
        {
        }

        private void cmbFiltr_SelectedIndexChanged(object sender, EventArgs e)
        {
        }
    }
}