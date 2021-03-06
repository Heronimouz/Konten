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

    public KontoTyp getTyp() {
        return typ;
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
                line.replace(1, 1 + sollBuchung.getHabenKonto().getName().length(), sollBuchung.getHabenKonto().getName());
                line.replace(24 - sollBuchung.getBetrag().toString().length(), 24, sollBuchung.getBetrag().toString());
            }
            line.replace(25,26,"|");
            if (habenBuchungen.size() > i) {
                Buchung habenBuchung = habenBuchungen.get(i);
                line.replace(27, 27 + habenBuchung.getSollKonto().getName().length(), habenBuchung.getSollKonto().getName());
                line.replace(50 - habenBuchung.getBetrag().toString().length() , 50, habenBuchung.getBetrag().toString());
            }
            sb.append("\n").append(line);
        }
        sb.append("\n---------------------------------------------------\n");
        StringBuilder lastLine = new StringBuilder(" Gesamt                  | Gesamt                  \n");

        String habenBetrag = getHabenGesamt().toString();
        String sollBetrag = getSollGesamt().toString();

        lastLine.replace(24 - sollBetrag.length(), 24, sollBetrag);
        lastLine.replace(50 - habenBetrag.length(), 50, habenBetrag);

        sb.append(lastLine);

        return sb.toString();
    }

    public BigDecimal getHabenGesamt() {
        return habenBuchungen
                .stream()
                .map(Buchung::getBetrag)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getSollGesamt() {
        return sollBuchungen
                .stream()
                .map(Buchung::getBetrag)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
}