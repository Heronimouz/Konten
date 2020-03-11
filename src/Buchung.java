import java.math.BigDecimal;

public class Buchung {

    private final Konto sollKonto;
    private final Konto habenKonto;
    private final BigDecimal betrag;

    public Buchung(Konto sollKonto, Konto habenKonto, BigDecimal betrag) {
        this.sollKonto = sollKonto;
        this.habenKonto = habenKonto;
        this.betrag = betrag;

        this.sollKonto.addSollBuchung(this);
        this.habenKonto.addHabenBuchung(this);
    }

    public String erstelleBuchungssatz() {
        return sollKonto.getName() + " " + betrag + " an " + habenKonto.getName() + " " + betrag;
    }

    public Konto getSollKonto() {
        return sollKonto;
    }

    public Konto getHabenKonto() {
        return habenKonto;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }
}
