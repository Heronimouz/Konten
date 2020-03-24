import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Konto {
    private final String name;
    private Konto parentKonto;
    private List<Konto> childKontos = new ArrayList<>();
    private final KontoTyp typ;
    private final List<Buchung> sollBuchungen = new ArrayList<>();
    private final List<Buchung> habenBuchungen = new ArrayList<>();
    private boolean abgeschlossen = false;

    public Konto(String name, Konto parentKonto, KontoTyp typ) {
        this.name = name;
        this.parentKonto = parentKonto;
        if (parentKonto != null) {
            parentKonto.getChildKontos().add(this);
        }
        this.typ = typ;
    }

    public void buchen(Konto gegenKonto, BigDecimal betrag) {
        new Buchung(this, gegenKonto, betrag);
    }

    public void addSollBuchung(Buchung buchung) {
        sollBuchungen.add(buchung);
    }

    public void addHabenBuchung(Buchung buchung) {
        habenBuchungen.add(buchung);
    }

    public String getName() {
        return name;
    }

    public List<Buchung> getSollBuchungen() {
        return sollBuchungen;
    }

    public List<Buchung> getHabenBuchungen() {
        return habenBuchungen;
    }

    public List<Konto> getChildKontos() {
        return childKontos;
    }

    public Konto getParentKonto() {
        return parentKonto;
    }

    public void setParentKonto(Konto parentKonto) {
        this.parentKonto = parentKonto;
    }

    public void saldieren(Konto sbk) {
        if (abgeschlossen) {
            return;
        }

        for (Konto konto : childKontos) {
            konto.saldieren(this);
        }

        BigDecimal sollBetrag = sollBuchungen.stream().map(Buchung::getBetrag).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal habenBetrag = habenBuchungen.stream().map(Buchung::getBetrag).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        if (sollBetrag.compareTo(habenBetrag) > 0) {
            new Buchung(sbk, this, sollBetrag.subtract(habenBetrag));
        }
        else {
            new Buchung(this, sbk, habenBetrag.subtract(sollBetrag));
        }

        abgeschlossen = true;
    }

    public void setAbgeschlossen() {
        this.abgeschlossen = true;
    }

    public List<Buchung> getBuchungen() {
        ArrayList<Buchung> result = new ArrayList<>(getSollBuchungen());
        result.addAll(getHabenBuchungen());
        return result;
    }

    public String print() {
        StringBuilder sb = new StringBuilder();

        sb.append("                                                   ");
        sb.replace(1, 2, "S").replace(49, 50, "H");
        int halfLength = name.length() - (name.length() / 2);
        sb.replace(26-halfLength, 26+(name.length() / 2), name);
        sb.append("\n---------------------------------------------------");

        for (int i = 0; i < (Math.max(sollBuchungen.size(), habenBuchungen.size())); i++) {
            StringBuilder line = new StringBuilder("                                                   ");
            if (sollBuchungen.size() > i) {
                Buchung sollBuchung = sollBuchungen.get(i);
                String nameWithBetrag = sollBuchung.getHabenKonto().getName() + " " + sollBuchung.getBetrag();
                line.replace(1, nameWithBetrag.length(), nameWithBetrag);
            }
            line.replace(25,26,"|");
            if (habenBuchungen.size() > i) {
                Buchung habenBuchung = habenBuchungen.get(i);
                String nameWithBetrag = habenBuchung.getSollKonto().getName() + " " + habenBuchung.getBetrag();
                line.replace(50 - nameWithBetrag.length() , 50, nameWithBetrag);
            }
            sb.append("\n").append(line);
        }
        sb.append("\n---------------------------------------------------\n");
// TODO Beträge in eine Linie und Summe
//        sb.append(habenBuchungen.stream().map(Buchung::getBetrag).reduce(BigDecimal::add).orElse(BigDecimal.ZERO)).append("\n");

        return sb.toString();
    }
}