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